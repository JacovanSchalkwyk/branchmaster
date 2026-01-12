import { useEffect, useMemo, useState } from "react";
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Paper,
  Stack,
  TextField,
  Typography,
  FormControlLabel,
  Switch,
} from "@mui/material";
import type {
  BranchOperatingHours,
  CreateBranchOperatingHoursRequest,
  UpdateBranchOperatingHoursRequest,
} from "../types/branchOperatingHours";
import {
  getBranchOperatingHours,
  updateBranchOperatingHours,
  createBranchOperatingHours,
} from "../api/branchOperatingHours";
import { isValidHHMM, toMinutes, trimHHMM } from "../utils/time";
import { DAYS } from "../utils/day";

type Props = {
  branchId: number;
};

export default function AdminBranchOperatingHoursPanel({ branchId }: Props) {
  const [rows, setRows] = useState<BranchOperatingHours[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [dialogOpen, setDialogOpen] = useState(false);
  const [saving, setSaving] = useState(false);

  // ✅ editing target can be "existing row" OR "missing day"
  const [editing, setEditing] = useState<BranchOperatingHours | null>(null);
  const [editingDay, setEditingDay] = useState<number>(0);

  const [isClosed, setIsClosed] = useState(false);
  const [openTime, setOpenTime] = useState("08:00");
  const [closeTime, setCloseTime] = useState("17:00");

  useEffect(() => {
    let alive = true;

    async function loadBranchOperatingHours() {
      setLoading(true);
      setError("");
      try {
        const res = await getBranchOperatingHours(branchId);
        if (!alive) return;
        setRows(res ?? []);
      } catch (e: unknown) {
        if (!alive) return;

        const message = e instanceof Error ? e.message : "Failed to load branch operating hours";

        setRows([]);
        setError(message ?? "Failed to load operating hours");
      } finally {
        if (alive) setLoading(false);
      }
    }

    loadBranchOperatingHours();
    return () => {
      alive = false;
    };
  }, [branchId]);

  const byDay = useMemo(() => {
    const map = new Map<number, BranchOperatingHours>();
    for (const r of rows) map.set(Number(r.dayOfWeek), r);
    return map;
  }, [rows]);

  const openEdit = (dayOfWeek: number) => {
    const existing = byDay.get(dayOfWeek) ?? null;

    setEditing(existing); // can be null
    setEditingDay(dayOfWeek);
    setError("");

    const existingOpen = trimHHMM(existing?.openingTime);
    const existingClose = trimHHMM(existing?.closingTime);
    const closed = !existing || existing.closed || !existingOpen || !existingClose;

    setIsClosed(closed);
    setOpenTime(existingOpen || "08:00");
    setCloseTime(existingClose || "17:00");

    setDialogOpen(true);
  };

  const closeDialog = () => {
    if (saving) return;
    setDialogOpen(false);
  };

  const validate = () => {
    if (isClosed) return "";

    if (!isValidHHMM(openTime)) return "Opening time must be HH:mm (e.g. 08:00).";
    if (!isValidHHMM(closeTime)) return "Closing time must be HH:mm (e.g. 17:00).";
    if (toMinutes(closeTime) <= toMinutes(openTime)) {
      return "Closing time must be after opening time.";
    }
    return "";
  };

  const save = async () => {
    const msg = validate();
    if (msg) {
      setError(msg);
      return;
    }

    setSaving(true);
    setError("");

    try {
      if (editing?.id != null) {
        const payload: UpdateBranchOperatingHoursRequest = {
          id: editing.id,
          dayOfWeek: editing.dayOfWeek,
          openingTime: openTime,
          closingTime: closeTime,
          closed: isClosed,
        };

        await updateBranchOperatingHours(payload);

        setRows((prev) =>
          prev.map((x) =>
            x.id === editing.id
              ? {
                  ...x,
                  openingTime: isClosed ? null : openTime,
                  closingTime: isClosed ? null : closeTime,
                  closed: isClosed,
                }
              : x
          )
        );
      } else {
        const created: BranchOperatingHours = await createBranchOperatingHours({
          branchId,
          dayOfWeek: editingDay,
          openingTime: openTime,
          closingTime: closeTime,
        } as CreateBranchOperatingHoursRequest);

        setRows((prev) => [created, ...prev]);
      }

      setDialogOpen(false);
    } catch (e: unknown) {
      const message = e instanceof Error ? e.message : "Failed to update branch operating hours";

      setError(message ?? "Failed to save operating hours");
    } finally {
      setSaving(false);
    }
  };

  const renderDayLabel = (dayOfWeek: number) => {
    const row = byDay.get(dayOfWeek);
    if (!row || row.closed) return "Closed";

    const o = trimHHMM(row.openingTime);
    const c = trimHHMM(row.closingTime);

    return o && c ? `${o} - ${c}` : "-";
  };

  return (
    <Paper variant="outlined" sx={{ borderRadius: 2, p: 2 }}>
      <Stack spacing={1.25}>
        {loading ? (
          <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
            <CircularProgress size={18} />
            <Typography variant="body2" color="text.secondary">
              Loading operating hours…
            </Typography>
          </Box>
        ) : (
          <Stack spacing={1}>
            {DAYS.map((d) => (
              <Paper key={d.value} variant="outlined" sx={{ p: 1.25, borderRadius: 2 }}>
                <Stack
                  direction="row"
                  alignItems="center"
                  justifyContent="space-between"
                  spacing={1}
                >
                  <Box>
                    <Typography sx={{ fontWeight: 650 }}>{d.label}</Typography>
                    <Typography variant="caption" color="text.secondary">
                      {renderDayLabel(d.value)}
                    </Typography>
                  </Box>

                  <Button size="small" variant="outlined" onClick={() => openEdit(d.value)}>
                    Edit
                  </Button>
                </Stack>
              </Paper>
            ))}
          </Stack>
        )}
      </Stack>

      <Dialog open={dialogOpen} onClose={closeDialog} maxWidth="xs" fullWidth>
        <DialogTitle sx={{ textAlign: "center" }}>
          Edit {DAYS.find((d) => d.value === editingDay)?.label ?? "day"}
        </DialogTitle>

        <DialogContent sx={{ pt: 1 }}>
          {error && <Alert severity="error">{error}</Alert>}

          <Stack spacing={2} sx={{ mt: 1 }}>
            <FormControlLabel
              control={<Switch checked={!isClosed} onChange={(_, v) => setIsClosed(!v)} />}
              label={isClosed ? "Closed" : "Open"}
            />

            <TextField
              label="Opening time (HH:mm)"
              value={openTime}
              onChange={(e) => setOpenTime(e.target.value)}
              placeholder="08:00"
              fullWidth
              required={!isClosed}
              disabled={isClosed}
            />
            <TextField
              label="Closing time (HH:mm)"
              value={closeTime}
              onChange={(e) => setCloseTime(e.target.value)}
              placeholder="17:00"
              fullWidth
              required={!isClosed}
              disabled={isClosed}
            />
          </Stack>
        </DialogContent>

        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={closeDialog} disabled={saving}>
            Cancel
          </Button>
          <Button variant="contained" onClick={save} disabled={saving}>
            {saving ? "Saving…" : "Save"}
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
}
