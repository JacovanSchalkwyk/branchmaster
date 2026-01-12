import {
  Alert,
  Box,
  Button,
  Card,
  CardActionArea,
  CardContent,
  Chip,
  Container,
  Divider,
  Paper,
  Stack,
  Typography,
  Fade,
} from "@mui/material";
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getActiveBranchListFull } from "../api/branch";
import BranchAutocomplete from "../components/BranchAutoComplete";
import type { BranchFull } from "../types/branch";

export default function BranchSelectPage() {
  const navigate = useNavigate();

  const [branches, setBranches] = useState<BranchFull[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string>("");

  const [selectedBranch, setSelectedBranch] = useState<BranchFull | null>(null);

  useEffect(() => {
    let alive = true;

    async function loadBranches() {
      setLoading(true);

      try {
        const res = await getActiveBranchListFull();
        if (!alive) return;
        setBranches(res ?? []);
      } catch (e: unknown) {
        if (!alive) return;
        const message = e instanceof Error ? e.message : "Failed to load branches";
        setError(message ?? "Failed to load branches");
        setBranches([]);
        setSelectedBranch(null);
      } finally {
        if (alive) setLoading(false);
      }
    }

    loadBranches();
    return () => {
      alive = false;
    };
  }, []);

  const featured = useMemo(() => branches.slice(0, 3), [branches]);

  const continueDisabled = !selectedBranch;

  const goToBranchCalendar = (branchId: number) => {
    navigate(`/branches/${branchId}/calendar`, {
      state: { branches },
    });
  };

  return (
    <Box
      sx={{
        minHeight: "100vh",
        background:
          "radial-gradient(900px 380px at 50% -120px, rgba(55,65,81,0.16), transparent), linear-gradient(#F3F4F6, #F3F4F6)",
        py: { xs: 3, md: 6 },
      }}
    >
      <Container maxWidth="md">
        <Stack spacing={3}>
          <Stack spacing={2} alignItems="center" textAlign="center">
            <Typography
              variant="h2"
              sx={{
                fontWeight: 800,
                letterSpacing: -1,
                lineHeight: 1.05,
                fontSize: { xs: "2.1rem", md: "3rem" },
              }}
            >
              Welcome to{" "}
              <Box component="span" sx={{ color: "primary.main" }}>
                Branch Master
              </Box>
            </Typography>

            <Typography
              sx={{
                color: "text.secondary",
                maxWidth: 720,
                fontSize: { xs: "1rem", md: "1.05rem" },
              }}
            >
              Pick a branch, view live availability, and book your appointment in seconds. Use the
              search box to find a location by name, suburb, or city.
            </Typography>

            <Paper
              elevation={0}
              sx={{
                mt: 1,
                p: { xs: 1.5, md: 2 },
                borderRadius: 3,
                width: "100%",
                maxWidth: 900,
                bgcolor: "rgba(255,255,255,0.75)",
                border: "1px solid",
                borderColor: "divider",
                backdropFilter: "blur(8px)",
              }}
            >
              <Stack
                direction={{ xs: "column", md: "row" }}
                spacing={1.5}
                alignItems={{ md: "center" }}
                justifyContent="center"
              >
                <Stack direction="row" spacing={1} justifyContent="center" alignItems="center">
                  <Box
                    sx={{ width: 10, height: 10, borderRadius: "50%", bgcolor: "success.main" }}
                  />
                  <Typography variant="body2" sx={{ color: "text.secondary" }}>
                    Live availability
                  </Typography>
                </Stack>

                <Divider
                  flexItem
                  orientation="vertical"
                  sx={{ display: { xs: "none", md: "block" } }}
                />
                <Divider sx={{ display: { xs: "block", md: "none" } }} />

                <Stack direction="row" spacing={1} justifyContent="center" alignItems="center">
                  <Box
                    sx={{ width: 10, height: 10, borderRadius: "50%", bgcolor: "primary.main" }}
                  />
                  <Typography variant="body2" sx={{ color: "text.secondary" }}>
                    Search branches instantly
                  </Typography>
                </Stack>

                <Divider
                  flexItem
                  orientation="vertical"
                  sx={{ display: { xs: "none", md: "block" } }}
                />
                <Divider sx={{ display: { xs: "block", md: "none" } }} />

                <Stack direction="row" spacing={1} justifyContent="center" alignItems="center">
                  <Box
                    sx={{ width: 10, height: 10, borderRadius: "50%", bgcolor: "warning.main" }}
                  />
                  <Typography variant="body2" sx={{ color: "text.secondary" }}>
                    Book in a few clicks
                  </Typography>
                </Stack>
              </Stack>
            </Paper>
          </Stack>

          <Paper sx={{ p: { xs: 2, md: 3 }, borderRadius: 3 }}>
            <Stack spacing={2}>
              {error && <Alert severity="error">{error}</Alert>}

              <BranchAutocomplete
                branches={branches}
                loading={loading}
                value={selectedBranch}
                onChange={setSelectedBranch}
                size="medium"
              />

              {selectedBranch && (
                <Box sx={{ mt: 1 }}>
                  <Divider sx={{ mb: 2 }} />

                  <Stack
                    direction={{ xs: "column", md: "row" }}
                    spacing={2}
                    alignItems={{ md: "center" }}
                    justifyContent="space-between"
                  >
                    <Stack spacing={0.5}>
                      <Typography sx={{ fontWeight: 700 }}>{selectedBranch.name}</Typography>
                      <Typography variant="body2" sx={{ color: "text.secondary" }}>
                        {selectedBranch.friendlyAddress}
                      </Typography>
                    </Stack>

                    <Stack
                      direction="row"
                      spacing={1}
                      alignItems="center"
                      justifyContent={{ xs: "flex-start", md: "flex-end" }}
                    >
                      <Button
                        variant="contained"
                        disabled={continueDisabled}
                        onClick={() => goToBranchCalendar(selectedBranch.branchId)}
                      >
                        Continue
                      </Button>
                    </Stack>
                  </Stack>
                </Box>
              )}
            </Stack>
          </Paper>

          <Fade in={featured.length > 0} timeout={600}>
            <Box>
              {featured.length > 0 && (
                <Stack spacing={1.5}>
                  <Stack direction="row" alignItems="center" justifyContent="space-between">
                    <Typography sx={{ fontWeight: 700 }}>Popular branches</Typography>
                    <Typography variant="body2" sx={{ color: "text.secondary" }}>
                      Quick picks
                    </Typography>
                  </Stack>

                  <Stack direction={{ xs: "column", md: "row" }} spacing={2}>
                    {featured.map((b, index) => (
                      <Fade
                        key={b.branchId}
                        in
                        timeout={500 + index * 120} // subtle stagger
                      >
                        <Card sx={{ flex: 1, borderRadius: 3 }}>
                          <CardActionArea
                            onClick={() => goToBranchCalendar(b.branchId)}
                            sx={{ height: "100%" }}
                          >
                            <CardContent>
                              <Stack spacing={1}>
                                <Typography sx={{ fontWeight: 700 }}>{b.name}</Typography>
                                <Typography variant="body2" sx={{ color: "text.secondary" }}>
                                  {b.friendlyAddress}
                                </Typography>

                                <Stack direction="row" spacing={1} sx={{ pt: 1, flexWrap: "wrap" }}>
                                  {b.city && <Chip size="small" label={b.city} />}
                                  {b.suburb && <Chip size="small" label={b.suburb} />}
                                </Stack>

                                <Box sx={{ pt: 1 }}>
                                  <Typography
                                    variant="body2"
                                    sx={{
                                      fontWeight: 700,
                                      color: "primary.main",
                                    }}
                                  >
                                    View availability
                                  </Typography>
                                </Box>
                              </Stack>
                            </CardContent>
                          </CardActionArea>
                        </Card>
                      </Fade>
                    ))}
                  </Stack>
                </Stack>
              )}
            </Box>
          </Fade>

          <Typography variant="body2" sx={{ color: "text.secondary", textAlign: "center", mt: 2 }}>
            Staff? Go to{" "}
            <Box component="span" sx={{ fontWeight: 600 }}>
              /staff/login
            </Box>
            .
          </Typography>
        </Stack>
      </Container>
    </Box>
  );
}
