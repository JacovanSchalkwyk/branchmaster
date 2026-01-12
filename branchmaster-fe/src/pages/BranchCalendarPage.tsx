// src/pages/BranchCalendarPage.tsx
import { useCallback, useEffect, useMemo, useState } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import { toMinutes, floorToHour, ceilToHour, trimTime } from "../utils/time";
import {
  Box,
  Button,
  CircularProgress,
  Container,
  Stack,
  Typography,
  IconButton,
  Paper,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Alert,
  Snackbar,
  Divider,
} from "@mui/material";
import LocationOnIcon from "@mui/icons-material/LocationOn";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
import EventIcon from "@mui/icons-material/Event";
import StoreIcon from "@mui/icons-material/Store";

import { format, startOfWeek, endOfWeek, addDays } from "date-fns";
import type { AvailabilityResponse, Timeslot } from "../types/availability";
import WeekCalendarGrid from "../components/WeekCalendarGrid";
import { getBranchAvailability } from "../api/availability";
import ChevronLeftIcon from "@mui/icons-material/ChevronLeft";
import ChevronRightIcon from "@mui/icons-material/ChevronRight";
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import { createAppointment } from "../api/appointments";
import type { BranchFull } from "../types/branch";
import { getActiveBranchListFull } from "../api/branch";
import BranchAutocomplete from "../components/BranchAutoComplete";

type BookingForm = {
  name: string;
  email: string;
  phone: string;
  reason: string;
};

export default function BranchCalendarPage() {
  const { branchId } = useParams();
  const navigate = useNavigate();
  const branchIdNum = Number(branchId);

  const [weekAnchorDate, setWeekAnchorDate] = useState<Date>(new Date());

  const weekStart = useMemo(
    () => startOfWeek(weekAnchorDate, { weekStartsOn: 1 }),
    [weekAnchorDate]
  );
  const weekEnd = useMemo(() => endOfWeek(weekAnchorDate, { weekStartsOn: 1 }), [weekAnchorDate]);

  const startDateStr = useMemo(() => format(weekStart, "yyyy-MM-dd"), [weekStart]);
  const endDateStr = useMemo(() => format(weekEnd, "yyyy-MM-dd"), [weekEnd]);

  const days = useMemo(
    () => Array.from({ length: 7 }, (_, i) => addDays(weekStart, i)),
    [weekStart]
  );

  const goPrevWeek = () => setWeekAnchorDate((d) => addDays(d, -7));
  const goNextWeek = () => setWeekAnchorDate((d) => addDays(d, 7));

  const SLOT_MINUTES = 15;

  const [data, setData] = useState<AvailabilityResponse>({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [bookingOpen, setBookingOpen] = useState(false);
  const [selected, setSelected] = useState<{ dateStr: string; slot: Timeslot } | null>(null);

  const [form, setForm] = useState<BookingForm>({
    name: "",
    email: "",
    phone: "",
    reason: "",
  });

  const [submitting, setSubmitting] = useState(false);
  const [bookingError, setBookingError] = useState<string>("");

  const [toast, setToast] = useState<{ open: boolean; message: string }>({
    open: false,
    message: "",
  });

  const location = useLocation();
  const navBranches = (location.state as { branches?: BranchFull[] } | null)?.branches;
  const [branches, setBranches] = useState<BranchFull[]>(() => navBranches ?? []);
  const [branchLoading, setBranchLoading] = useState(false);

  useEffect(() => {
    if (branches.length > 0) {
      setBranchLoading(false);
      return;
    }
    let alive = true;

    async function loadBranches() {
      setBranchLoading(true);

      try {
        const res = await getActiveBranchListFull();
        if (!alive) return;
        setBranches(res ?? []);
      } catch (e: unknown) {
        if (!alive) return;
        const message = e instanceof Error ? e.message : "Could not load active branch list";
        setError(message);
        setBranches([]);
      } finally {
        if (alive) setBranchLoading(false);
      }
    }

    loadBranches();
    return () => {
      alive = false;
    };
  }, [branches.length]);

  const selectedBranch = useMemo(
    () => branches.find((b) => b.branchId === branchIdNum) ?? null,
    [branches, branchIdNum]
  );

  const loadAvailability = useCallback(async () => {
    if (!branchIdNum || Number.isNaN(branchIdNum)) {
      setError("Invalid branchId");
      setData({});
      return;
    }

    setLoading(true);
    setError("");

    try {
      const res = await getBranchAvailability(branchIdNum, startDateStr, endDateStr);
      setData(res ?? {});
    } catch (e: unknown) {
      const message = e instanceof Error ? e.message : "Failed to load branch availability";
      setError(message ?? "Failed to load availability");
      setData({});
    } finally {
      setLoading(false);
    }
  }, [branchIdNum, startDateStr, endDateStr]);

  useEffect(() => {
    loadAvailability();
  }, [loadAvailability]);

  const { startHour, endHour, perDayMin, perDayMax } = useMemo(() => {
    const allDates = days.map((d) => format(d, "yyyy-MM-dd"));
    const allSlots = Object.values(data).flat();

    if (allSlots.length === 0) {
      return {
        startHour: 9,
        endHour: 17,
        perDayMin: new Map<string, number>(),
        perDayMax: new Map<string, number>(),
      };
    }

    const perDayMinTmp = new Map<string, number>();
    const perDayMaxTmp = new Map<string, number>();

    for (const dateStr of allDates) {
      const slots = data[dateStr] ?? [];
      if (slots.length === 0) continue;

      perDayMinTmp.set(dateStr, Math.min(...slots.map((s) => toMinutes(s.startTime))));
      perDayMaxTmp.set(dateStr, Math.max(...slots.map((s) => toMinutes(s.endTime))));
    }

    const minStartWeek = Math.min(...allSlots.map((s) => toMinutes(s.startTime)));
    const maxEndWeek = Math.max(...allSlots.map((s) => toMinutes(s.endTime)));

    const startMins = floorToHour(minStartWeek);
    const endMins = ceilToHour(maxEndWeek);

    return {
      startHour: Math.floor(startMins / 60),
      endHour: Math.ceil(endMins / 60),
      perDayMin: perDayMinTmp,
      perDayMax: perDayMaxTmp,
    };
  }, [data, days]);

  const onSelectSlot = (dateStr: string, slot: Timeslot) => {
    setSelected({ dateStr, slot });
    setBookingError("");
    setSubmitting(false);

    setForm({ name: "", email: "", phone: "", reason: "" });

    setBookingOpen(true);
  };

  const closeBooking = () => {
    if (submitting) return;
    setBookingOpen(false);
    setSelected(null);
    setBookingError("");
  };

  const submitBooking = async () => {
    if (!selected) return;

    if (!form.name.trim()) {
      setBookingError("Name is required.");
      return;
    }

    if (!form.email.trim()) {
      setBookingError("Email is required.");
      return;
    }

    if (!/^\S+@\S+\.\S+$/.test(form.email.trim())) {
      setBookingError("Invalid email address provided.");
      return;
    }

    setSubmitting(true);
    setBookingError("");

    try {
      const booking = await createAppointment({
        branchId: branchIdNum,
        appointmentDate: selected.dateStr,
        startTime: selected.slot.startTime,
        endTime: selected.slot.endTime,
        name: form.name.trim(),
        email: form.email.trim(),
        phoneNumber: form.phone.trim() || null,
        reason: form.reason.trim() || null,
      });

      setBookingOpen(false);
      setSelected(null);

      navigate(`/booking/confirmation`, {
        replace: true,
        state: {
          branchName: selectedBranch?.name,
          branchFriendlyAddress: selectedBranch?.friendlyAddress,
          booking: booking,
          latitude: selectedBranch?.latitude,
          longitude: selectedBranch?.longitude,
        },
      });
    } catch (e: unknown) {
      const message = e instanceof Error ? "Something went wrong, please refresh the page and try again." : "Booking failed.";

      setBookingError(message);
      loadAvailability()
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Container maxWidth="xl" sx={{ py: 3, position: "relative" }}>
      <Typography variant="h4" sx={{ mb: 2 }} textAlign="center">
        Book an Appointment
      </Typography>

      <Stack
        direction={{ xs: "column", md: "row" }}
        spacing={2}
        alignItems="left"
        justifyContent="left"
        sx={{ mb: 2 }}
      >
        <Box sx={{ width: { xs: "100%", md: 360 } }}>
          <BranchAutocomplete
            branches={branches}
            loading={branchLoading}
            value={selectedBranch}
            onChange={(branch) => {
              if (!branch) return;
              navigate(`/branches/${branch.branchId}/calendar`, { replace: true });
            }}
            label="Branch"
            placeholder="Search branch..."
            size="small"
          />
        </Box>

        <Stack direction="row" spacing={1} alignItems="center" sx={{ justifyContent: "center" }}>
          <Tooltip title="Previous week">
            <IconButton onClick={goPrevWeek} aria-label="previous week">
              <ChevronLeftIcon />
            </IconButton>
          </Tooltip>

          <DatePicker
            label="Week"
            value={weekAnchorDate}
            onChange={(newValue) => {
              if (newValue) setWeekAnchorDate(newValue);
            }}
            slotProps={{
              textField: {
                size: "small",
              },
            }}
          />

          <Tooltip title="Next week">
            <IconButton onClick={goNextWeek} aria-label="next week">
              <ChevronRightIcon />
            </IconButton>
          </Tooltip>
        </Stack>
      </Stack>

      {loading && (
        <Box
          sx={{
            position: "absolute",
            inset: 0,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            backgroundColor: "rgba(255,255,255,0.6)",
            zIndex: 10,
          }}
        >
          <CircularProgress />
        </Box>
      )}

      {error && (
        <Typography color="error" sx={{ mb: 2 }}>
          {error}
        </Typography>
      )}

      {!error && (
        <WeekCalendarGrid
          days={days}
          data={data}
          onSelect={onSelectSlot}
          slotMinutes={SLOT_MINUTES}
          startHour={startHour}
          endHour={endHour}
          perDayMin={perDayMin}
          perDayMax={perDayMax}
        />
      )}

      {/* Booking modal */}
      <Dialog open={bookingOpen} onClose={closeBooking} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ textAlign: "center" }}>Book appointment</DialogTitle>

        <DialogContent sx={{ pt: 1 }}>
          <Paper variant="outlined" sx={{ p: 2, mb: 2, borderRadius: 2 }}>
            <Stack spacing={1.25}>
              <Stack direction="row" spacing={1.25} alignItems="flex-start">
                <StoreIcon fontSize="small" />
                <Box>
                  <Typography sx={{ fontWeight: 700 }}>
                    {selectedBranch?.name ?? "Selected branch"}
                  </Typography>
                </Box>
              </Stack>

              <Divider />

              <Stack direction="row" spacing={1.25} alignItems="center">
                <EventIcon fontSize="small" />
                <Typography variant="body2">
                  {selected
                    ? format(new Date(selected.dateStr), "EEE, d MMM yyyy")
                    : "No date selected"}
                </Typography>
              </Stack>

              <Stack direction="row" spacing={1.25} alignItems="center">
                <AccessTimeIcon fontSize="small" />
                <Typography variant="body2">
                  {selected
                    ? `${trimTime(selected.slot.startTime)} - ${trimTime(selected.slot.endTime)}`
                    : "No time selected"}
                </Typography>
              </Stack>

              {selectedBranch?.friendlyAddress && (
                <Stack direction="row" spacing={1.25} alignItems="flex-start">
                  <LocationOnIcon fontSize="small" />
                  <Typography variant="body2" color="text.secondary">
                    {selectedBranch.friendlyAddress}
                  </Typography>
                </Stack>
              )}
            </Stack>
          </Paper>

          {bookingError && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {bookingError}
            </Alert>
          )}

          <Stack spacing={2}>
            <TextField
              label="Name"
              value={form.name}
              onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
              required
              autoFocus
            />

            <TextField
              label="Email"
              value={form.email}
              onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
              required
            />

            <TextField
              label="Phone"
              value={form.phone}
              onChange={(e) => setForm((f) => ({ ...f, phone: e.target.value }))}
            />

            <TextField
              label="Reason (optional)"
              value={form.reason}
              onChange={(e) => setForm((f) => ({ ...f, reason: e.target.value }))}
              multiline
              rows={3}
              placeholder="Optional note or reason for the appointment"
            />
          </Stack>
        </DialogContent>

        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={closeBooking} disabled={submitting}>
            Cancel
          </Button>
          <Button variant="contained" onClick={submitBooking} disabled={submitting}>
            {submitting ? "Booking..." : "Submit booking"}
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={toast.open}
        autoHideDuration={3000}
        onClose={() => setToast({ open: false, message: "" })}
        anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
      >
        <Alert severity="success" onClose={() => setToast({ open: false, message: "" })}>
          {toast.message}
        </Alert>
      </Snackbar>
    </Container>
  );
}
