// src/pages/BookingConfirmationPage.tsx
import { useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {
  Alert,
  Box,
  Button,
  Container,
  Divider,
  IconButton,
  Link,
  Paper,
  Stack,
  Tooltip,
  Typography,
} from "@mui/material";
import LocationOnIcon from "@mui/icons-material/LocationOn";
import StoreIcon from "@mui/icons-material/Store";
import EventIcon from "@mui/icons-material/Event";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
import EmailIcon from "@mui/icons-material/Email";
import PhoneIcon from "@mui/icons-material/Phone";
import NotesIcon from "@mui/icons-material/Notes";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";

import { format } from "date-fns";
import { trimTime } from "../utils/time";
import type { Booking } from "../types/booking";
import { cancelAppointment } from "../api/appointments";

type NavState = {
  branchName: string;
  branchFriendlyAddress: string;
  latitude: number;
  longitude: number;
  booking: Booking;
};

function formatDate(yyyyMmDd: string) {
  return format(new Date(yyyyMmDd), "EEE, d MMM yyyy");
}

export default function BookingConfirmationPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const state = location.state as NavState;

  const booking = state.booking;

  const lat = state.latitude;
  const lng = state.longitude;

  const mapsUrl = useMemo(() => {
    if (lat == null || lng == null) return "";
    return `https://www.google.com/maps?q=${encodeURIComponent(`${lat},${lng}`)}`;
  }, [lat, lng]);

  const [cancelled, setCancelled] = useState(false);

  const onCancel = async () => {
    if (!booking) return;

    await cancelAppointment(booking.appointmentId);

    setCancelled(true);
  };

  return (
    <Container maxWidth="sm" sx={{ py: 4 }}>
      <Paper sx={{ p: 2.5, borderRadius: 2 }}>
        <Stack spacing={2}>
          <Stack spacing={0.5} alignItems="center" sx={{ textAlign: "center" }}>
            <CheckCircleIcon fontSize="large" />
            <Typography variant="h5" sx={{ fontWeight: 800 }}>
              Booking confirmed
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Your appointment has been scheduled.
            </Typography>
          </Stack>

          {cancelled && <Alert severity="info">This booking has been cancelled.</Alert>}

          <Divider />

          <Stack direction="row" spacing={1.25} alignItems="flex-start">
            <StoreIcon fontSize="small" />
            <Box sx={{ flex: 1 }}>
              <Stack direction="row" alignItems="center" justifyContent="space-between" spacing={1}>
                <Box>
                  <Typography sx={{ fontWeight: 750 }}>
                    {state.branchName ?? "Selected branch"}
                  </Typography>

                  {state.branchFriendlyAddress && (
                    <Typography variant="caption" color="text.secondary">
                      {state.branchFriendlyAddress}
                    </Typography>
                  )}
                </Box>

                {mapsUrl && (
                  <Tooltip title="Open in Google Maps">
                    <IconButton
                      size="small"
                      component="a"
                      href={mapsUrl}
                      target="_blank"
                      rel="noreferrer"
                      aria-label="Open in Google Maps"
                    >
                      <LocationOnIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                )}
              </Stack>

              {mapsUrl && (
                <Box sx={{ mt: 0.5 }}>
                  <Link
                    href={mapsUrl}
                    target="_blank"
                    rel="noreferrer"
                    underline="hover"
                    variant="caption"
                  >
                    Open location in Google Maps
                  </Link>
                </Box>
              )}
            </Box>
          </Stack>

          <Stack direction="row" spacing={1.25} alignItems="center">
            <EventIcon fontSize="small" />
            <Typography variant="body2">{formatDate(booking.appointmentDate)}</Typography>
          </Stack>

          <Stack direction="row" spacing={1.25} alignItems="center">
            <AccessTimeIcon fontSize="small" />
            <Typography variant="body2">
              {trimTime(booking.startTime)} - {trimTime(booking.endTime)}
            </Typography>
          </Stack>

          <Stack direction="row" spacing={1.25} alignItems="center">
            <EmailIcon fontSize="small" />
            <Typography variant="body2">{booking.email ?? "—"}</Typography>
          </Stack>

          <Stack direction="row" spacing={1.25} alignItems="center">
            <PhoneIcon fontSize="small" />
            <Typography variant="body2">{booking.phoneNumber ?? "—"}</Typography>
          </Stack>

          <Stack direction="row" spacing={1.25} alignItems="flex-start">
            <NotesIcon fontSize="small" />
            <Box sx={{ flex: 1 }}>
              <Typography variant="body2" sx={{ whiteSpace: "pre-wrap" }}>
                {booking.reason?.trim() ? booking.reason : "—"}
              </Typography>
            </Box>
          </Stack>

          <Divider />

          <Stack direction={{ xs: "column", sm: "row" }} spacing={1.5}>
            <Button
              variant="contained"
              fullWidth
              onClick={() => navigate("/branches", { replace: true })}
            >
              Make another booking
            </Button>

            <Button
              variant="outlined"
              color="error"
              fullWidth
              disabled={cancelled}
              onClick={onCancel}
            >
              {cancelled ? "Cancelled" : "Cancel booking"}
            </Button>
          </Stack>

          <Typography variant="caption" color="text.secondary" sx={{ textAlign: "center" }}>
            Booking reference: #{booking.appointmentId}
          </Typography>
        </Stack>
      </Paper>
    </Container>
  );
}
