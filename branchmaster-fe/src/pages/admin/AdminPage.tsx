// src/pages/admin/AdminBranchesPage.tsx
import { useEffect, useMemo, useState } from "react";
import {
  Alert,
  Box,
  CircularProgress,
  Container,
  Divider,
  List,
  ListItemButton,
  ListItemText,
  Paper,
  Stack,
  TextField,
  Typography,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  Switch,
  MenuItem,
  Accordion,
  AccordionSummary,
  AccordionDetails,
} from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { useNavigate } from "react-router-dom";
import type { BranchAdmin } from "../../types/branch";
import { useStaffAuth } from "../../staff/StaffAuthContext";
import {
  getBranchDetailsAdmin,
  getBranchListAdmin,
  updateBranchAdmin,
  createBranchAdmin,
} from "../../api/branch";
import AdminBranchResourcesPanel from "../../components/AdminBranchResourcePanel";
import AdminBranchOperatingHoursPanel from "../../components/AdminBranchOperatingHoursPanel";
import AdminBranchBookingsPanel from "../../components/AdminBranchBookingPanel";
import { SA_PROVINCES } from "../../utils/province";

export default function AdminPage() {
  const navigate = useNavigate();

  const [branches, setBranches] = useState<BranchAdmin[]>([]);
  const [listLoading, setListLoading] = useState(false);
  const [listError, setListError] = useState<string>("");
  const { logout } = useStaffAuth();

  const [query, setQuery] = useState("");
  const [selectedId, setSelectedId] = useState<number | null>(null);

  const [details, setDetails] = useState<BranchAdmin | null>(null);
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [detailsError, setDetailsError] = useState<string>("");

  const [editOpen, setEditOpen] = useState(false);
  const [editSaving, setEditSaving] = useState(false);
  const [editError, setEditError] = useState("");

  const [editName, setEditName] = useState("");
  const [editAddress, setEditAddress] = useState("");
  const [editSuburb, setEditSuburb] = useState("");
  const [editCity, setEditCity] = useState("");
  const [editProvince, setEditProvince] = useState<string>("");
  const [editPostalCode, setEditPostalCode] = useState("");
  const [editActive, setEditActive] = useState(true);
  const [editTimeslotLength, setEditTimeslotLength] = useState<number>(30);
  const [editLatitude, setEditLatitude] = useState<string>("");
  const [editLongitude, setEditLongitude] = useState<string>("");

  const [addOpen, setAddOpen] = useState(false);
  const [addSaving, setAddSaving] = useState(false);
  const [addError, setAddError] = useState("");

  const [addName, setAddName] = useState("");
  const [addAddress, setAddAddress] = useState("");
  const [addSuburb, setAddSuburb] = useState("");
  const [addCity, setAddCity] = useState("");
  const [addProvince, setAddProvince] = useState<string>(SA_PROVINCES[0]);
  const [addPostalCode, setAddPostalCode] = useState("");
  const [addActive, setAddActive] = useState(true);
  const [addTimeslotLength, setAddTimeslotLength] = useState<number>(30);
  const [addLatitude, setAddLatitude] = useState<string>("");
  const [addLongitude, setAddLongitude] = useState<string>("");

  useEffect(() => {
    let alive = true;

    async function loadList() {
      setListLoading(true);
      setListError("");

      try {
        const res = await getBranchListAdmin();
        if (!alive) return;
        setBranches(res);
      } catch (e: unknown) {
        if (!alive) return;
        setBranches([]);
        setSelectedId(null);
        const message = e instanceof Error ? e.message : "Failed to load branches";
        setListError(message ?? "Failed to load branches");
      } finally {
        if (alive) setListLoading(false);
      }
    }

    loadList();
    return () => {
      alive = false;
    };
  }, []);

  useEffect(() => {
    let alive = true;

    async function loadDetails(id: number) {
      setDetailsLoading(true);
      setDetailsError("");

      try {
        const res = await getBranchDetailsAdmin(id);
        if (!alive) return;
        setDetails(res);
      } catch (e: unknown) {
        if (!alive) return;
        setDetails(null);
        const message = e instanceof Error ? e.message : "Failed to load branch details.";

        setDetailsError(message ?? "Failed to load branch details");
      } finally {
        if (alive) setDetailsLoading(false);
      }
    }

    if (selectedId == null) {
      setDetails(null);
      setDetailsError("");
      setDetailsLoading(false);
      return;
    }

    loadDetails(selectedId);
    return () => {
      alive = false;
    };
  }, [selectedId]);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return branches;
    return branches.filter((b) =>
      String(b.name ?? "")
        .toLowerCase()
        .includes(q)
    );
  }, [branches, query]);

  const TIMESLOT_OPTIONS = useMemo(() => {
    const opts: number[] = [];
    for (let m = 15; m <= 180; m += 15) opts.push(m);
    return opts;
  }, []);

  const isValidNumber = (v: string) => v.trim() !== "" && !Number.isNaN(Number(v));

  const validateBranchForm = (v: {
    name: string;
    address: string;
    city: string;
    province: string;
    postalCode: string;
    latitude: string;
    longitude: string;
  }) => {
    if (!v.name.trim()) return "Name is required.";
    if (!v.address.trim()) return "Address is required.";
    if (!v.city.trim()) return "City is required.";
    if (!v.postalCode.trim()) return "Postal code is required.";

    if (!SA_PROVINCES.includes(v.province as (typeof SA_PROVINCES)[number])) {
      return "Province must be one of the South African provinces.";
    }

    if (!isValidNumber(v.latitude) || !isValidNumber(v.longitude)) {
      return "Latitude and Longitude are required and must be valid numbers.";
    }

    const lat = Number(v.latitude);
    const lng = Number(v.longitude);
    if (lat < -90 || lat > 90) return "Latitude must be between -90 and 90.";
    if (lng < -180 || lng > 180) return "Longitude must be between -180 and 180.";

    return "";
  };

  const handleLogout = () => {
    logout();
    navigate("/staff/login", { replace: true });
  };

  const openEditModal = () => {
    if (!details) return;

    setEditError("");

    setEditName(details.name ?? "");
    setEditAddress(details.address ?? "");
    setEditSuburb(details.suburb ?? "");
    setEditCity(details.city ?? "");
    setEditProvince(details.province ?? "");
    setEditPostalCode(details.postalCode ?? "");

    setEditActive(Boolean(details.active));
    setEditTimeslotLength(Number(details.timeslotLength ?? 30));

    setEditLatitude(details.latitude != null ? String(details.latitude) : "");
    setEditLongitude(details.longitude != null ? String(details.longitude) : "");

    setEditOpen(true);
  };

  const closeEditModal = () => {
    if (editSaving) return;
    setEditOpen(false);
  };

  const saveEditModal = async () => {
    if (!details) return;

    const msg = validateBranchForm({
      name: editName,
      address: editAddress,
      city: editCity,
      province: editProvince,
      postalCode: editPostalCode,
      latitude: editLatitude,
      longitude: editLongitude,
    });

    if (msg) {
      setEditError(msg);
      return;
    }

    setEditSaving(true);
    setEditError("");

    const lat = Number(editLatitude);
    const lng = Number(editLongitude);

    try {
      const updated = await updateBranchAdmin({
        id: details.branchId,
        name: editName.trim(),
        address: editAddress.trim(),
        suburb: editSuburb.trim() || null,
        city: editCity.trim(),
        province: editProvince.trim(),
        postalCode: editPostalCode.trim(),
        active: editActive,
        timeslotLength: editTimeslotLength,
        latitude: lat,
        longitude: lng,
      });

      setDetails(updated);

      setBranches((prev) =>
        prev.map((b) =>
          b.branchId === updated.branchId
            ? {
                ...b,
                name: updated.name,
                address: updated.friendlyAddress ?? updated.address,
              }
            : b
        )
      );

      setEditOpen(false);
    } catch (e: unknown) {
      const message = e instanceof Error ? e.message : "Failed to update branch details.";

      setEditError(message ?? "Failed to update branch");
    } finally {
      setEditSaving(false);
    }
  };

  const openAddModal = () => {
    setAddError("");

    setAddName("");
    setAddAddress("");
    setAddSuburb("");
    setAddCity("");
    setAddProvince(SA_PROVINCES[0]);
    setAddPostalCode("");
    setAddActive(true);
    setAddTimeslotLength(30);
    setAddLatitude("");
    setAddLongitude("");

    setAddOpen(true);
  };

  const closeAddModal = () => {
    if (addSaving) return;
    setAddOpen(false);
  };

  const saveAddModal = async () => {
    const msg = validateBranchForm({
      name: addName,
      address: addAddress,
      city: addCity,
      province: addProvince,
      postalCode: addPostalCode,
      latitude: addLatitude,
      longitude: addLongitude,
    });

    if (msg) {
      setAddError(msg);
      return;
    }

    setAddSaving(true);
    setAddError("");

    const lat = Number(addLatitude);
    const lng = Number(addLongitude);

    try {
      const created = await createBranchAdmin({
        name: addName.trim(),
        address: addAddress.trim(),
        suburb: addSuburb.trim() || null,
        city: addCity.trim(),
        province: addProvince.trim(),
        postalCode: addPostalCode.trim(),
        country: "South Africa",
        active: addActive,
        timeslotLength: addTimeslotLength,
        latitude: lat,
        longitude: lng,
      });

      setBranches((prev) => {
        const id = created.branchId;

        const without = prev.filter((b: BranchAdmin) => b.branchId !== id);

        return [
          {
            ...created,
            address: created.friendlyAddress,
          } as BranchAdmin,
          ...without,
        ];
      });
      setSelectedId(created.branchId);

      setAddOpen(false);
    } catch (e: unknown) {
      const message = e instanceof Error ? e.message : "Failed to create branch.";

      setAddError(message ?? "Failed to create branch");
    } finally {
      setAddSaving(false);
    }
  };

  return (
    <Container maxWidth="xl" sx={{ py: 3 }}>
      <Stack spacing={2}>
        <Stack direction="row" alignItems="baseline" justifyContent="space-between">
          <Box>
            <Typography variant="h4" sx={{ fontWeight: 800 }}>
              Admin Portal
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Select a branch on the left to load full details.
            </Typography>
          </Box>

          <Stack direction="row" spacing={1}>
            <Button variant="contained" onClick={openAddModal}>
              Add branch
            </Button>
            <Button variant="outlined" color="error" onClick={handleLogout}>
              Log out
            </Button>
          </Stack>
        </Stack>

        {listError && <Alert severity="error">{listError}</Alert>}

        <Paper sx={{ borderRadius: 3, overflow: "hidden" }}>
          <Stack direction={{ xs: "column", md: "row" }} sx={{ minHeight: 520 }}>
            <Box
              sx={{
                width: { xs: "100%", md: 360 },
                borderRight: { md: "1px solid" },
                borderColor: { md: "divider" },
              }}
            >
              <Box sx={{ p: 2 }}>
                <TextField
                  label="Search branches"
                  size="small"
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  fullWidth
                />

                <Typography
                  variant="caption"
                  color="text.secondary"
                  sx={{ display: "block", mt: 1 }}
                >
                  {filtered.length} of {branches.length} branches
                </Typography>
              </Box>

              <Divider />

              {listLoading ? (
                <Box sx={{ p: 2, display: "flex", alignItems: "center", gap: 1 }}>
                  <CircularProgress size={18} />
                  <Typography variant="body2" color="text.secondary">
                    Loading branches…
                  </Typography>
                </Box>
              ) : filtered.length === 0 ? (
                <Box sx={{ p: 2 }}>
                  <Typography variant="body2" color="text.secondary">
                    No branches found.
                  </Typography>
                </Box>
              ) : (
                <List disablePadding sx={{ maxHeight: { xs: 360, md: 520 }, overflow: "auto" }}>
                  {filtered.map((b) => (
                    <ListItemButton
                      key={b.branchId}
                      selected={b.branchId === selectedId}
                      onClick={() => setSelectedId(b.branchId)}
                      sx={{ py: 1.25 }}
                    >
                      <ListItemText
                        primary={<Typography sx={{ fontWeight: 750 }}>{b.name}</Typography>}
                        secondary={
                          <Typography variant="caption" color="text.secondary">
                            {b.friendlyAddress ? b.friendlyAddress : b.address }
                          </Typography>
                        }
                      />
                    </ListItemButton>
                  ))}
                </List>
              )}
            </Box>

            {/* RIGHT: Details */}
            <Box sx={{ flex: 1, p: { xs: 2, md: 3 } }}>
              {selectedId == null ? (
                <Typography variant="body2" color="text.secondary">
                  Select a branch to load details.
                </Typography>
              ) : detailsLoading ? (
                <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                  <CircularProgress size={18} />
                  <Typography variant="body2" color="text.secondary">
                    Loading details…
                  </Typography>
                </Box>
              ) : detailsError ? (
                <Alert severity="error">{detailsError}</Alert>
              ) : !details ? (
                <Typography variant="body2" color="text.secondary">
                  No details to show.
                </Typography>
              ) : (
                <Stack spacing={2}>
                  <Box>
                    <Typography variant="h5" sx={{ fontWeight: 800 }}>
                      {details.name}
                    </Typography>

                    <Stack direction="row" spacing={1} sx={{ mt: 1, flexWrap: "wrap" }}>
                      <Chip
                        size="small"
                        label={details.active ? "Open" : "Closed"}
                        color={details.active ? "success" : "default"}
                      />
                    </Stack>
                  </Box>

                  <Divider />

                  <Stack direction={{ xs: "column", sm: "row" }} spacing={1.5}>
                    <Button
                      variant="contained"
                      onClick={() => navigate(`/branches/${details.branchId}/calendar`)}
                    >
                      View calendar
                    </Button>

                    <Button variant="outlined" onClick={openEditModal}>
                      Edit branch
                    </Button>
                  </Stack>

                  <Paper variant="outlined" sx={{ borderRadius: 2, p: 2 }}>
                    <Typography sx={{ fontWeight: 700, mb: 1 }}>Branch details</Typography>

                    <Stack spacing={0.75}>
                      <Row label="Address" value={details.address ?? "-"} />
                      <Row label="City" value={details.city ?? "-"} />
                      <Row label="Suburb" value={details.suburb ?? "-"} />
                      <Row label="Province" value={details.province ?? "-"} />
                      <Row label="Postal code" value={details.postalCode ?? "-"} />
                      <Row label="Country" value={details.country ?? "-"} />
                      <Row label="Timeslot length" value={`${details.timeslotLength} minutes`} />
                      <Row
                        label="Coordinates"
                        value={
                          details.latitude != null && details.longitude != null
                            ? `${details.latitude}, ${details.longitude}`
                            : "-"
                        }
                      />
                    </Stack>
                  </Paper>

                  <Accordion disableGutters>
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                      <Typography sx={{ fontWeight: 750 }}>Operating hours</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <AdminBranchOperatingHoursPanel branchId={details.branchId} />
                    </AccordionDetails>
                  </Accordion>

                  <Accordion disableGutters>
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                      <Typography sx={{ fontWeight: 750 }}>Availabilities</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <AdminBranchResourcesPanel branchId={details.branchId} />
                    </AccordionDetails>
                  </Accordion>

                  <Accordion disableGutters>
                    <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                      <Typography sx={{ fontWeight: 750 }}>Bookings</Typography>
                    </AccordionSummary>
                    <AccordionDetails>
                      <AdminBranchBookingsPanel branchId={details.branchId} />
                    </AccordionDetails>
                  </Accordion>
                </Stack>
              )}
            </Box>
          </Stack>
        </Paper>
      </Stack>

      {/* Edit branch modal */}
      <Dialog open={editOpen} onClose={closeEditModal} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ textAlign: "center" }}>Edit branch</DialogTitle>

        <DialogContent sx={{ pt: 1 }}>
          <Stack spacing={2} sx={{ mt: 1 }}>
            {editError && <Alert severity="error">{editError}</Alert>}

            <TextField
              label="Name"
              value={editName}
              onChange={(e) => setEditName(e.target.value)}
              fullWidth
              required
            />
            <TextField
              label="Address"
              value={editAddress}
              onChange={(e) => setEditAddress(e.target.value)}
              fullWidth
              required
            />

            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <TextField
                label="Suburb"
                value={editSuburb}
                onChange={(e) => setEditSuburb(e.target.value)}
                fullWidth
              />
              <TextField
                label="City"
                value={editCity}
                onChange={(e) => setEditCity(e.target.value)}
                fullWidth
                required
              />
            </Stack>

            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <TextField
                select
                label="Province"
                value={editProvince}
                onChange={(e) => setEditProvince(e.target.value)}
                fullWidth
                required
              >
                {SA_PROVINCES.map((p) => (
                  <MenuItem key={p} value={p}>
                    {p}
                  </MenuItem>
                ))}
              </TextField>

              <TextField
                label="Postal code"
                value={editPostalCode}
                onChange={(e) => setEditPostalCode(e.target.value)}
                fullWidth
                required
              />
            </Stack>

            <TextField
              label="Country"
              value={details?.country ?? "South Africa"}
              fullWidth
              disabled
            />

            <FormControlLabel
              control={<Switch checked={editActive} onChange={(_, v) => setEditActive(v)} />}
              label={editActive ? "Open" : "Closed"}
            />

            <TextField
              select
              label="Timeslot length"
              value={editTimeslotLength}
              onChange={(e) => setEditTimeslotLength(Number(e.target.value))}
              fullWidth
            >
              {TIMESLOT_OPTIONS.map((m) => (
                <MenuItem key={m} value={m}>
                  {m} minutes
                </MenuItem>
              ))}
            </TextField>

            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <TextField
                label="Latitude"
                value={editLatitude}
                onChange={(e) => setEditLatitude(e.target.value)}
                placeholder="-33.9249"
                fullWidth
                required
              />
              <TextField
                label="Longitude"
                value={editLongitude}
                onChange={(e) => setEditLongitude(e.target.value)}
                placeholder="18.4241"
                fullWidth
                required
              />
            </Stack>
          </Stack>
        </DialogContent>

        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={closeEditModal} disabled={editSaving}>
            Cancel
          </Button>
          <Button variant="contained" onClick={saveEditModal} disabled={editSaving}>
            {editSaving ? "Saving…" : "Save"}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Add branch modal */}
      <Dialog open={addOpen} onClose={closeAddModal} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ textAlign: "center" }}>Add branch</DialogTitle>

        <DialogContent sx={{ pt: 1 }}>
          <Stack spacing={2} sx={{ mt: 1 }}>
            {addError && <Alert severity="error">{addError}</Alert>}

            <TextField
              label="Name"
              value={addName}
              onChange={(e) => setAddName(e.target.value)}
              fullWidth
              required
            />
            <TextField
              label="Address"
              value={addAddress}
              onChange={(e) => setAddAddress(e.target.value)}
              fullWidth
              required
            />

            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <TextField
                label="Suburb"
                value={addSuburb}
                onChange={(e) => setAddSuburb(e.target.value)}
                fullWidth
              />
              <TextField
                label="City"
                value={addCity}
                onChange={(e) => setAddCity(e.target.value)}
                fullWidth
                required
              />
            </Stack>

            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <TextField
                select
                label="Province"
                value={addProvince}
                onChange={(e) => setAddProvince(e.target.value)}
                fullWidth
                required
              >
                {SA_PROVINCES.map((p) => (
                  <MenuItem key={p} value={p}>
                    {p}
                  </MenuItem>
                ))}
              </TextField>

              <TextField
                label="Postal code"
                value={addPostalCode}
                onChange={(e) => setAddPostalCode(e.target.value)}
                fullWidth
                required
              />
            </Stack>

            <TextField label="Country" value="South Africa" fullWidth disabled />

            <FormControlLabel
              control={<Switch checked={addActive} onChange={(_, v) => setAddActive(v)} />}
              label={addActive ? "Open" : "Closed"}
            />

            <TextField
              select
              label="Timeslot length"
              value={addTimeslotLength}
              onChange={(e) => setAddTimeslotLength(Number(e.target.value))}
              fullWidth
            >
              {TIMESLOT_OPTIONS.map((m) => (
                <MenuItem key={m} value={m}>
                  {m} minutes
                </MenuItem>
              ))}
            </TextField>

            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <TextField
                label="Latitude"
                value={addLatitude}
                onChange={(e) => setAddLatitude(e.target.value)}
                placeholder="-33.9249"
                fullWidth
                required
              />
              <TextField
                label="Longitude"
                value={addLongitude}
                onChange={(e) => setAddLongitude(e.target.value)}
                placeholder="18.4241"
                fullWidth
                required
              />
            </Stack>
          </Stack>
        </DialogContent>

        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={closeAddModal} disabled={addSaving}>
            Cancel
          </Button>
          <Button variant="contained" onClick={saveAddModal} disabled={addSaving}>
            {addSaving ? "Saving…" : "Create"}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <Stack direction="row" spacing={2} alignItems="baseline">
      <Typography variant="body2" sx={{ width: 140, color: "text.secondary" }}>
        {label}
      </Typography>
      <Typography variant="body2" sx={{ fontWeight: 600 }}>
        {value}
      </Typography>
    </Stack>
  );
}
