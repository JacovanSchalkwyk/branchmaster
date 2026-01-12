// src/components/admin/AdminBranchBookingsPanel.tsx
import { useEffect, useMemo, useState } from "react";
import { Alert, Box, CircularProgress, Divider, Paper, Stack, Typography } from "@mui/material";
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import { format } from "date-fns";
import type { Booking } from "../types/booking";
import { getBookingsForBranchDay } from "../api/booking";

type Props = { branchId: number };

function toIsoDate(d: Date) {
  return format(d, "yyyy-MM-dd");
}

function trimHHMM(t?: string | null) {
  return t ? t.slice(0, 5) : "";
}

function FieldRow({ label, value }: { label: string; value: string }) {
  return (
    <Stack direction="row" spacing={2} alignItems="baseline">
      <Typography variant="caption" sx={{ width: 110, color: "text.secondary" }}>
        {label}
      </Typography>
      <Typography variant="body2" sx={{ fontWeight: 650 }}>
        {value}
      </Typography>
    </Stack>
  );
}

export default function AdminBranchBookingsPanel({ branchId }: Props) {
  const [day, setDay] = useState<Date>(new Date());
  const [rows, setRows] = useState<Booking[]>([]);
  const [bookingsLoading, setBookingsLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let alive = true;

    async function loadBookings() {
      setBookingsLoading(true);
      setError("");
      try {
        const res = await getBookingsForBranchDay(branchId, toIsoDate(day));
        if (!alive) return;
        setRows(res ?? []);
      } catch (e: unknown) {
        if (!alive) return;

        const message = e instanceof Error ? e.message : "Failed to load bookings";

        setRows([]);
        setError(message);
      } finally {
        if (alive) setBookingsLoading(false);
      }
    }

    loadBookings();
    return () => {
      alive = false;
    };
  }, [branchId, day]);

  const sorted = useMemo(() => {
    const copy = [...rows];
    copy.sort((a, b) => (a.startTime ?? "").localeCompare(b.startTime ?? ""));
    return copy;
  }, [rows]);

  const selectedDateLabel = useMemo(() => {
    const serverDate = sorted[0]?.appointmentDate;
    return serverDate ?? toIsoDate(day);
  }, [sorted, day]);

  return (
    <Paper variant="outlined" sx={{ borderRadius: 2, p: 2 }}>
      <Stack spacing={1.25}>
        <Stack direction={{ xs: "column", sm: "row" }} spacing={1.5} alignItems={{ sm: "center" }}>
          <DatePicker
            label="Day"
            value={day}
            onChange={(v) => {
              if (v) setDay(v);
            }}
            slotProps={{
              textField: { size: "small", fullWidth: true, required: true },
            }}
          />
        </Stack>

        {error && <Alert severity="error">{error}</Alert>}
        <Divider />

        {bookingsLoading ? (
          <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
            <CircularProgress size={18} />
            <Typography variant="body2" color="text.secondary">
              Loading bookings…
            </Typography>
          </Box>
        ) : sorted.length === 0 ? (
          <Typography variant="body2" color="text.secondary">
            No bookings for {toIsoDate(day)}.
          </Typography>
        ) : (
          <Stack spacing={1.25}>
            <Typography variant="body2" color="text.secondary">
              {sorted.length} booking{sorted.length === 1 ? "" : "s"} • {selectedDateLabel}
            </Typography>

            {sorted.map((b) => (
              <Paper key={b.appointmentId} variant="outlined" sx={{ p: 1.25, borderRadius: 2 }}>
                <Stack spacing={0.5}>
                  <FieldRow label="Date" value={b.appointmentDate} />
                  <FieldRow
                    label="Time"
                    value={`${trimHHMM(b.startTime)} - ${trimHHMM(b.endTime)}`}
                  />
                  <FieldRow label="Email" value={(b.email ?? "-").toString()} />
                  <FieldRow label="Phone" value={(b.phoneNumber ?? "-").toString()} />
                  <FieldRow label="Reason" value={(b.reason ?? "-").toString()} />
                </Stack>
              </Paper>
            ))}
          </Stack>
        )}
      </Stack>
    </Paper>
  );
}
