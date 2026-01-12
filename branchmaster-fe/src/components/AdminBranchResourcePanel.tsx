import { useEffect, useMemo, useState } from "react";
import {
  Alert,
  Autocomplete,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  Paper,
  Stack,
  Tab,
  Tabs,
  TextField,
  Typography,
  ToggleButton,
  ToggleButtonGroup,
} from "@mui/material";
import { DatePicker } from "@mui/x-date-pickers/DatePicker";

import type {
  AvailableResource,
  CreateResourceAvailabilityRequest,
  UpdateResourceAvailabilityRequest,
  UnavailableResource,
  CreateResourceUnavailabilityRequest,
  UpdateResourceUnavailabilityRequest,
} from "../types/resources";

import {
  createResourceAvailability,
  deleteResourceAvailability,
  getAvailableResourcesForBranch,
  updateResourceAvailability,
  getUnavailableResourcesForBranch,
  createResourceUnavailability,
  updateResourceUnavailability,
  deleteResourceUnavailability,
} from "../api/resources";
import { isValidHHMM, parseIsoDate, toIsoDate, toMinutes, trimHHMM } from "../utils/time";
import { DAYS } from "../utils/day";

type Props = {
  branchId: number;
};

type ResourceOption = {
  availabilityId: number;
  name: string;
};

type UnavailGroup = {
  availabilityId: number;
  name: string;
  byDate: Array<{
    date: string;
    items: UnavailableResource[];
  }>;
};

export default function AdminBranchResourcesPanel({ branchId }: Props) {
  const [tab, setTab] = useState<0 | 1>(0); // 0=availability, 1=unavailability

  const [availableResources, setAvailableResources] = useState<AvailableResource[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [day, setDay] = useState<number>(0);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<AvailableResource | null>(null);

  const [resourceName, setResourceName] = useState("");
  const [resourceDay, setResourceDay] = useState<number>(0);
  const [startTime, setStartTime] = useState("09:00");
  const [endTime, setEndTime] = useState("17:00");

  const [startDate, setStartDate] = useState<Date>(new Date());
  const [endDate, setEndDate] = useState<Date>(
    new Date(new Date().setFullYear(new Date().getFullYear() + 1))
  );

  const [saving, setSaving] = useState(false);

  const [unavail, setUnavail] = useState<UnavailableResource[]>([]);
  const [unavailLoading, setUnavailLoading] = useState(false);
  const [unavailError, setUnavailError] = useState("");

  const [unavailDialogOpen, setUnavailDialogOpen] = useState(false);
  const [unavailSaving, setUnavailSaving] = useState(false);
  const [unavailEditing, setUnavailEditing] = useState<UnavailableResource | null>(null);

  const [unavailDate, setUnavailDate] = useState<Date>(new Date());
  const [unavailStart, setUnavailStart] = useState("09:00");
  const [unavailEnd, setUnavailEnd] = useState("10:00");

  const [unavailReason, setUnavailReason] = useState<string>("");

  const [selectedResourceOpt, setSelectedResourceOpt] = useState<ResourceOption | null>(null);

  useEffect(() => {
    let alive = true;

    async function load() {
      setLoading(true);
      setError("");
      try {
        const res = await getAvailableResourcesForBranch(branchId);
        if (!alive) return;
        setAvailableResources(res ?? []);
      } catch (e: unknown) {
        if (!alive) return;
        setAvailableResources([]);

        const message =
          e instanceof Error ? e.message : "Failed to load available resources for branch";

        setError(message ?? "Failed to load resources");
      } finally {
        if (alive) setLoading(false);
      }
    }

    load();
    return () => {
      alive = false;
    };
  }, [branchId]);

  useEffect(() => {
    if (tab !== 1) return;

    let alive = true;

    async function loadUnavail() {
      setUnavailLoading(true);
      setUnavailError("");
      try {
        const res = await getUnavailableResourcesForBranch(branchId);
        if (!alive) return;
        setUnavail(res ?? []);
      } catch (e: unknown) {
        if (!alive) return;
        setUnavail([]);
        const message =
          e instanceof Error ? e.message : "Failed to load unavailable resources for branch";
        setUnavailError(message ?? "Failed to load unavailability");
      } finally {
        if (alive) setUnavailLoading(false);
      }
    }

    loadUnavail();
    return () => {
      alive = false;
    };
  }, [tab, branchId]);

  const dayItems = useMemo(() => {
    return availableResources.filter((r: AvailableResource) => r.dayOfWeek === day);
  }, [availableResources, day]);

  const groupedByName = useMemo(() => {
    const map = new Map<string, { displayName: string; items: AvailableResource[] }>();

    for (const r of dayItems) {
      const displayName = (r.name ?? "Unnamed resource").trim();
      const key = displayName.toLowerCase();

      const existing = map.get(key);
      if (!existing) map.set(key, { displayName, items: [r] });
      else existing.items.push(r);
    }

    const groups = Array.from(map.values()).sort((a, b) =>
      a.displayName.localeCompare(b.displayName)
    );

    for (const g of groups) {
      g.items.sort((a: AvailableResource, b: AvailableResource) =>
        (a.startTime ?? "").localeCompare(b.startTime ?? "")
      );
    }

    return groups;
  }, [dayItems]);

  const resourceOptions: ResourceOption[] = useMemo(() => {
    const map = new Map<string, ResourceOption>();

    for (const r of availableResources as AvailableResource[]) {
      const name = (r.name ?? "").trim();
      if (!name) continue;

      const key = name.toLowerCase();
      const id = Number(r.id);

      const existing = map.get(key);
      if (!existing) {
        map.set(key, { name, availabilityId: id });
      } else if (id < existing.availabilityId) {
        map.set(key, { name, availabilityId: id });
      }
    }

    return Array.from(map.values()).sort((a, b) => a.name.localeCompare(b.name));
  }, [availableResources]);

  const openAdd = () => {
    setEditing(null);
    setError("");

    setResourceName("");
    setResourceDay(day);
    setStartTime("09:00");
    setEndTime("17:00");

    setStartDate(new Date());
    setEndDate(new Date(new Date().setFullYear(new Date().getFullYear() + 1)));

    setDialogOpen(true);
  };

  const openEdit = (resource: AvailableResource) => {
    setEditing(resource);
    setError("");

    const resourceCopy: AvailableResource = resource;

    setResourceName(resource.name ?? "");
    setResourceDay(resourceCopy.dayOfWeek ?? day);
    setStartTime(trimHHMM(resource.startTime) || "09:00");
    setEndTime(trimHHMM(resource.endTime) || "17:00");

    setStartDate(parseIsoDate(resource.startDate) ?? new Date());
    setEndDate(
      parseIsoDate(resource.endDate) ??
        new Date(new Date().setFullYear(new Date().getFullYear() + 1))
    );

    setDialogOpen(true);
  };

  const closeDialog = () => {
    if (saving) return;
    setDialogOpen(false);
  };

  const validate = () => {
    if (!resourceName.trim()) return "Resource name is required.";
    if (resourceDay < 0 || resourceDay > 6) return "Day of week is invalid.";
    if (!isValidHHMM(startTime)) return "Start time must be HH:mm (e.g. 09:00).";
    if (!isValidHHMM(endTime)) return "End time must be HH:mm (e.g. 17:00).";
    if (toMinutes(endTime) <= toMinutes(startTime)) return "End time must be after start time.";
    if (endDate.getTime() < startDate.getTime()) return "End date cannot be before start date.";
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
      if (!editing) {
        const payload: CreateResourceAvailabilityRequest = {
          branchId,
          name: resourceName.trim(),
          dayOfWeek: resourceDay,
          startTime,
          endTime,
          startDate: toIsoDate(startDate),
          endDate: toIsoDate(endDate),
        };

        const created = await createResourceAvailability(payload);
        setAvailableResources((prev) => [created, ...prev]);
      } else {
        const payload: UpdateResourceAvailabilityRequest = {
          id: editing.id,
          name: resourceName.trim(),
          dayOfWeek: resourceDay,
          startTime,
          endTime,
          startDate: toIsoDate(startDate),
          endDate: toIsoDate(endDate),
        };

        await updateResourceAvailability(payload);

        setAvailableResources((prev) =>
          prev.map((x) =>
            x.id === editing.id
              ? {
                  ...x,
                  name: resourceName.trim(),
                  dayOfWeek: resourceDay,
                  startTime,
                  endTime,
                  startDate: toIsoDate(startDate),
                  endDate: toIsoDate(endDate),
                }
              : x
          )
        );
      }

      setDialogOpen(false);
    } catch (e: unknown) {
      const message = e instanceof Error ? e.message : "Failed to save resource availability.";

      setError(message ?? "Failed to save");
    } finally {
      setSaving(false);
    }
  };

  const remove = async (id: number) => {
    if (!window.confirm("Delete this availability window?")) return;

    setError("");
    try {
      await deleteResourceAvailability(id);
      setAvailableResources((prev) => prev.filter((x) => x.id !== id));
    } catch (e: unknown) {
      const message = e instanceof Error ? e.message : "Failed to delete resource availability.";

      setError(message ?? "Failed to delete");
    }
  };

  const formatRange = (r: AvailableResource) => {
    const s = r.startDate ? String(r.startDate) : null;
    const e = r.endDate ? String(r.endDate) : null;
    return `${s ?? "—"} - ${e ?? "—"}`;
  };

  const openAddUnavailability = () => {
    setUnavailEditing(null);
    setUnavailError("");

    const first = resourceOptions[0] ?? null;
    setSelectedResourceOpt(first);

    setUnavailDate(new Date());
    setUnavailStart("09:00");
    setUnavailEnd("10:00");
    setUnavailReason("");

    setUnavailDialogOpen(true);
  };

  const openEditUnavailability = (u: UnavailableResource) => {
    setUnavailEditing(u);
    setUnavailError("");

    setUnavailDate(parseIsoDate(u.date) ?? new Date());
    setUnavailStart(trimHHMM(u.startTime) || "09:00");
    setUnavailEnd(trimHHMM(u.endTime) || "10:00");

    setUnavailReason(String(u.reason ?? ""));

    const matchById =
      resourceOptions.find((o) => o.availabilityId === u.availableResourceId) ?? null;

    setSelectedResourceOpt(matchById);

    setUnavailDialogOpen(true);
  };

  const closeUnavailabilityDialog = () => {
    if (unavailSaving) return;
    setUnavailDialogOpen(false);
  };

  const validateUnavailability = () => {
    if (!selectedResourceOpt) return "Please select a resource name.";
    if (!isValidHHMM(unavailStart)) return "Start time must be HH:mm (e.g. 09:00).";
    if (!isValidHHMM(unavailEnd)) return "End time must be HH:mm (e.g. 10:00).";
    if (toMinutes(unavailEnd) <= toMinutes(unavailStart))
      return "End time must be after start time.";
    if (unavailReason.trim().length > 255) return "Reason must be 255 characters or less.";
    return "";
  };

  const saveUnavailability = async () => {
    const msg = validateUnavailability();
    if (msg) {
      setUnavailError(msg);
      return;
    }

    setUnavailSaving(true);
    setUnavailError("");

    try {
      if (!selectedResourceOpt) return;

      if (!unavailEditing) {
        const payload: CreateResourceUnavailabilityRequest = {
          branchId,
          date: toIsoDate(unavailDate),
          startTime: unavailStart,
          endTime: unavailEnd,
          availableResourceId: selectedResourceOpt.availabilityId,

          reason: unavailReason.trim() || null,
        } as CreateResourceUnavailabilityRequest;

        const created = await createResourceUnavailability(payload);
        setUnavail((prev) => [created, ...prev]);
      } else if (unavailEditing) {
        const payload: UpdateResourceUnavailabilityRequest = {
          id: unavailEditing.id,
          date: toIsoDate(unavailDate),
          startTime: unavailStart,
          endTime: unavailEnd,
          availableResourceId: selectedResourceOpt!.availabilityId,
          reason: unavailReason.trim() || null,
        } as UpdateResourceUnavailabilityRequest;

        await updateResourceUnavailability(payload);

        setUnavail((prev) =>
          prev.map((x) =>
            x.id === unavailEditing.id
              ? {
                  ...x,
                  date: toIsoDate(unavailDate),
                  startTime: unavailStart,
                  endTime: unavailEnd,
                  availableResourceId: selectedResourceOpt.availabilityId,
                  resourceAvailabilityId: selectedResourceOpt.availabilityId, // compat
                  reason: unavailReason.trim() || null,
                }
              : x
          )
        );
      }

      setUnavailDialogOpen(false);
    } catch (e: unknown) {
      const message = e instanceof Error ? e.message : "Failed to save resource unavailability.";

      setUnavailError(message ?? "Failed to save unavailability");
    } finally {
      setUnavailSaving(false);
    }
  };

  const removeUnavailability = async (id: number) => {
    if (!window.confirm("Delete this unavailability entry?")) return;

    setUnavailError("");
    try {
      await deleteResourceUnavailability(id);
      setUnavail((prev) => prev.filter((x) => x.id !== id));
    } catch (e: unknown) {
      const message = e instanceof Error ? e.message : "Failed to delete resource unavailability.";

      setUnavailError(message ?? "Failed to delete unavailability");
    }
  };

  const availabilityNameById = useMemo(() => {
    const m = new Map<number, string>();

    for (const r of availableResources) {
      const id = Number(r.id);
      const name = String(r.name ?? "").trim();
      if (!Number.isNaN(id) && name) m.set(id, name);
    }

    return m;
  }, [availableResources]);

  const unavailGrouped: UnavailGroup[] = useMemo(() => {
    const byAvailability = new Map<number, UnavailableResource[]>();

    for (const u of unavail) {
      const availabilityId = Number(u.availableResourceId);
      if (!availabilityId || Number.isNaN(availabilityId)) continue;

      const list = byAvailability.get(availabilityId);
      if (!list) byAvailability.set(availabilityId, [u]);
      else list.push(u);
    }

    const groups: UnavailGroup[] = Array.from(byAvailability.entries()).map(
      ([availabilityId, items]) => {
        const byDate = new Map<string, UnavailableResource[]>();

        for (const u of items) {
          const date = String(u.date ?? "");
          if (!date) continue;

          const list = byDate.get(date);
          if (!list) byDate.set(date, [u]);
          else list.push(u);
        }

        const dateGroups = Array.from(byDate.entries())
          .map(([date, list]) => {
            list.sort((a, b) => (a.startTime ?? "").localeCompare(b.startTime ?? ""));
            return { date, items: list };
          })
          .sort((a, b) => b.date.localeCompare(a.date));

        const resolvedName =
          availabilityNameById.get(availabilityId) ?? `Resource #${availabilityId}`;

        return { availabilityId, name: resolvedName, byDate: dateGroups };
      }
    );

    groups.sort((a, b) => a.name.localeCompare(b.name));
    return groups;
  }, [unavail, availabilityNameById]);

  return (
    <Paper variant="outlined" sx={{ borderRadius: 2, p: 2 }}>
      <Stack spacing={1.25}>
        <Stack direction="row" alignItems="center" justifyContent="space-between">
          <Typography sx={{ fontWeight: 700 }}>Resources & availability</Typography>

          {tab === 0 ? (
            <Button variant="contained" size="small" onClick={openAdd}>
              Add availability
            </Button>
          ) : (
            <Button
              variant="contained"
              size="small"
              onClick={openAddUnavailability}
              disabled={resourceOptions.length === 0}
            >
              Add unavailability
            </Button>
          )}
        </Stack>

        <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ minHeight: 38 }}>
          <Tab label="Availability" sx={{ minHeight: 38 }} />
          <Tab label="Unavailability" sx={{ minHeight: 38 }} />
        </Tabs>

        {tab === 0 && (
          <>
            <ToggleButtonGroup
              exclusive
              value={day}
              onChange={(_, v) => {
                if (v != null) setDay(v);
              }}
              size="small"
              sx={{ flexWrap: "wrap" }}
            >
              {DAYS.map((d) => (
                <ToggleButton key={d.value} value={d.value}>
                  {d.label}
                </ToggleButton>
              ))}
            </ToggleButtonGroup>

            {error && <Alert severity="error">{error}</Alert>}

            <Divider />

            {loading ? (
              <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                <CircularProgress size={18} />
                <Typography variant="body2" color="text.secondary">
                  Loading resources…
                </Typography>
              </Box>
            ) : groupedByName.length === 0 ? (
              <Typography variant="body2" color="text.secondary">
                No availabilities for this day.
              </Typography>
            ) : (
              <Stack spacing={1.25}>
                {groupedByName.map((group) => (
                  <Paper
                    key={group.displayName.toLowerCase()}
                    variant="outlined"
                    sx={{ p: 1.25, borderRadius: 2 }}
                  >
                    <Stack spacing={1}>
                      <Stack direction="row" alignItems="center" justifyContent="space-between">
                        <Typography sx={{ fontWeight: 750 }}>{group.displayName}</Typography>
                        <Typography variant="caption" color="text.secondary">
                          {group.items.length} slot{group.items.length === 1 ? "" : "s"}
                        </Typography>
                      </Stack>

                      <Stack spacing={0.75}>
                        {group.items.map((r) => (
                          <Paper
                            key={r.id}
                            variant="outlined"
                            sx={{ p: 1, borderRadius: 2, bgcolor: "background.paper" }}
                          >
                            <Stack
                              direction={{ xs: "column", sm: "row" }}
                              alignItems={{ sm: "center" }}
                              justifyContent="space-between"
                              spacing={1}
                            >
                              <Box>
                                <Typography variant="body2" sx={{ fontWeight: 650 }}>
                                  {r.startTime && r.endTime
                                    ? `${r.startTime.slice(0, 5)} - ${r.endTime.slice(0, 5)}`
                                    : "—"}
                                </Typography>

                                <Typography variant="caption" color="text.secondary">
                                  {formatRange(r)}
                                </Typography>
                              </Box>

                              <Stack direction="row" spacing={1} alignItems="center">
                                <Button size="small" variant="outlined" onClick={() => openEdit(r)}>
                                  Edit
                                </Button>
                                <Button size="small" color="error" onClick={() => remove(r.id)}>
                                  Delete
                                </Button>
                              </Stack>
                            </Stack>
                          </Paper>
                        ))}
                      </Stack>
                    </Stack>
                  </Paper>
                ))}
              </Stack>
            )}
          </>
        )}

        {tab === 1 && (
          <>
            {resourceOptions.length === 0 && (
              <Alert severity="warning">
                Add at least one availability first (resource names come from availability).
              </Alert>
            )}

            {unavailError && <Alert severity="error">{unavailError}</Alert>}

            <Divider />

            {unavailLoading ? (
              <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                <CircularProgress size={18} />
                <Typography variant="body2" color="text.secondary">
                  Loading unavailability…
                </Typography>
              </Box>
            ) : unavailGrouped.length === 0 ? (
              <Typography variant="body2" color="text.secondary">
                No unavailability entries yet.
              </Typography>
            ) : (
              <Stack spacing={1.25}>
                {unavailGrouped.map((g) => (
                  <Paper
                    key={g.availabilityId}
                    variant="outlined"
                    sx={{ p: 1.25, borderRadius: 2 }}
                  >
                    <Stack spacing={1}>
                      <Stack direction="row" alignItems="center" justifyContent="space-between">
                        <Typography sx={{ fontWeight: 750 }}>{g.name}</Typography>
                        <Typography variant="caption" color="text.secondary">
                          {g.byDate.reduce((acc, d) => acc + d.items.length, 0)} item
                          {g.byDate.reduce((acc, d) => acc + d.items.length, 0) === 1 ? "" : "s"}
                        </Typography>
                      </Stack>

                      <Stack spacing={1}>
                        {g.byDate.map((d) => (
                          <Box key={`${g.availabilityId}-${d.date}`}>
                            <Typography variant="caption" color="text.secondary">
                              {d.date}
                            </Typography>

                            <Stack spacing={0.75} sx={{ mt: 0.5 }}>
                              {d.items.map((u) => (
                                <Paper key={u.id} variant="outlined" sx={{ p: 1, borderRadius: 2 }}>
                                  <Stack
                                    direction={{ xs: "column", sm: "row" }}
                                    alignItems={{ sm: "center" }}
                                    justifyContent="space-between"
                                    spacing={1}
                                  >
                                    <Box>
                                      <Typography variant="body2" sx={{ fontWeight: 650 }}>
                                        {trimHHMM(u.startTime)} - {trimHHMM(u.endTime)}
                                      </Typography>

                                      {String(u.reason ?? "").trim() && (
                                        <Typography variant="caption" color="text.secondary">
                                          Reason: {String(u.reason).trim()}
                                        </Typography>
                                      )}
                                    </Box>

                                    <Stack direction="row" spacing={1}>
                                      <Button
                                        size="small"
                                        variant="outlined"
                                        onClick={() => openEditUnavailability(u)}
                                      >
                                        Edit
                                      </Button>
                                      <Button
                                        size="small"
                                        color="error"
                                        onClick={() => removeUnavailability(u.id)}
                                      >
                                        Delete
                                      </Button>
                                    </Stack>
                                  </Stack>
                                </Paper>
                              ))}
                            </Stack>
                          </Box>
                        ))}
                      </Stack>
                    </Stack>
                  </Paper>
                ))}
              </Stack>
            )}
          </>
        )}
      </Stack>

      {/* Add/Edit availability dialog */}
      <Dialog open={dialogOpen} onClose={closeDialog} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ textAlign: "center" }}>
          {editing ? "Edit availability" : "Add availability"}
        </DialogTitle>

        <DialogContent sx={{ pt: 1 }}>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField
              label="Resource name"
              value={resourceName}
              onChange={(e) => setResourceName(e.target.value)}
              autoFocus
              required
            />

            <ToggleButtonGroup
              exclusive
              value={resourceDay}
              onChange={(_, v) => {
                if (v != null) setResourceDay(v);
              }}
              size="small"
              sx={{ flexWrap: "wrap" }}
            >
              {DAYS.map((d) => (
                <ToggleButton key={d.value} value={d.value}>
                  {d.label}
                </ToggleButton>
              ))}
            </ToggleButtonGroup>

            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <DatePicker
                label="Start date"
                value={startDate}
                onChange={(v) => {
                  if (v) setStartDate(v);
                }}
                slotProps={{
                  textField: { size: "small", fullWidth: true, required: true },
                }}
              />

              <DatePicker
                label="End date"
                value={endDate}
                onChange={(v) => {
                  if (v) setEndDate(v);
                }}
                slotProps={{
                  textField: { size: "small", fullWidth: true, required: true },
                }}
              />
            </Stack>

            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <TextField
                label="Start time (HH:mm)"
                value={startTime}
                onChange={(e) => setStartTime(e.target.value)}
                placeholder="09:00"
                fullWidth
                slotProps={{
                  input: {
                    inputMode: "numeric",
                  },
                }}
              />

              <TextField
                label="End time (HH:mm)"
                value={endTime}
                onChange={(e) => setEndTime(e.target.value)}
                placeholder="17:00"
                slotProps={{
                  input: {
                    inputMode: "numeric",
                  },
                }}
                fullWidth
              />
            </Stack>
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

      {/* Add/Edit unavailability dialog */}
      <Dialog open={unavailDialogOpen} onClose={closeUnavailabilityDialog} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ textAlign: "center" }}>
          {unavailEditing ? "Edit unavailability" : "Add unavailability"}
        </DialogTitle>

        <DialogContent sx={{ pt: 1 }}>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <Autocomplete<ResourceOption, false, false, false>
              options={resourceOptions}
              value={selectedResourceOpt}
              onChange={(_, v) => setSelectedResourceOpt(v)}
              getOptionLabel={(o) => o.name}
              isOptionEqualToValue={(a, b) => a.availabilityId === b.availabilityId}
              renderInput={(params) => (
                <TextField {...params} label="Resource name" required size="small" />
              )}
            />

            <DatePicker
              label="Date"
              value={unavailDate}
              onChange={(v) => {
                if (v) setUnavailDate(v);
              }}
              slotProps={{
                textField: { size: "small", fullWidth: true, required: true },
              }}
            />

            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <TextField
                label="Start time (HH:mm)"
                value={unavailStart}
                onChange={(e) => setUnavailStart(e.target.value)}
                placeholder="09:00"
                fullWidth
              />
              <TextField
                label="End time (HH:mm)"
                value={unavailEnd}
                onChange={(e) => setUnavailEnd(e.target.value)}
                placeholder="10:00"
                fullWidth
              />
            </Stack>

            <TextField
              label="Reason (optional)"
              value={unavailReason}
              onChange={(e) => setUnavailReason(e.target.value)}
              placeholder="e.g. Lunch break, maintenance, on leave..."
              fullWidth
              multiline
              minRows={2}
              slotProps={{
                htmlInput: { maxLength: 255 },
              }}
              helperText={`${unavailReason.trim().length}/255`}
            />
          </Stack>
        </DialogContent>

        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={closeUnavailabilityDialog} disabled={unavailSaving}>
            Cancel
          </Button>
          <Button variant="contained" onClick={saveUnavailability} disabled={unavailSaving}>
            {unavailSaving ? "Saving…" : "Save"}
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
}
