import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Alert, Box, Button, Container, Paper, Stack, TextField, Typography } from "@mui/material";
import { useStaffAuth } from "../../staff/StaffAuthContext";
import { postApi } from "../../api/client";

type StaffLoginRequest = {
  email: string;
  password: string;
};

type StaffLoginResponse = {
  token: string;
};

export default function StaffLogin() {
  const navigate = useNavigate();
  const { login } = useStaffAuth();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const res = await postApi<StaffLoginResponse, StaffLoginRequest>("/staff/login", {
        email: email.trim(),
        password,
      });

      login(res.token);
      navigate("/staff", { replace: true });
    } catch (e: unknown) {
      const message =
        e instanceof Error ? e.message : "Something went wrong when logging in, please try again.";

      setError(message ?? "Login failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Container maxWidth="sm" sx={{ py: 4 }}>
      <Typography variant="h4" sx={{ mb: 2 }}>
        Staff Login
      </Typography>

      <Paper sx={{ p: 2, borderRadius: 2 }}>
        <Box component="form" onSubmit={onSubmit}>
          <Stack spacing={2}>
            <TextField
              label="Email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="username"
              required
              fullWidth
            />

            <TextField
              label="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              type="password"
              autoComplete="current-password"
              required
              fullWidth
            />

            {error && <Alert severity="error">{error}</Alert>}

            <Button type="submit" variant="contained" disabled={loading}>
              {loading ? "Signing in..." : "Sign in"}
            </Button>

            <Button
              variant="text"
              color="inherit"
              onClick={() => navigate("/branches")}
              disabled={loading}
            >
              Back to booking
            </Button>
          </Stack>
        </Box>
      </Paper>
    </Container>
  );
}
