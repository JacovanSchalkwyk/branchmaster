import { createTheme } from "@mui/material/styles";

export const theme = createTheme({
  palette: {
    mode: "light",
    primary: { main: "rgba(0, 127, 206, 1)", light: "#75e57dff", dark: "#3730A3" },
    secondary: { main: "rgba(0, 127, 206, 0.59)" },
    background: { default: "rgba(244, 244, 244, 1)", paper: "#FFFFFF" },
    text: { primary: "#111827", secondary: "#4B5563" },
    success: { main: "#22C55E" },
    warning: { main: "#F59E0B" },
    error: { main: "#EF4444" },
    info: { main: "#3B82F6" },
  },
  shape: { borderRadius: 10 },
  typography: {
    fontFamily: `"Inter", "Roboto", "Helvetica", "Arial", sans-serif`,
    button: { textTransform: "none", fontWeight: 500 },
    h1: { fontWeight: 600 },
    h2: { fontWeight: 600 },
    h3: { fontWeight: 600 },
    h4: { fontWeight: 600 },
  },
  components: {
    MuiButton: { styleOverrides: { root: { borderRadius: 10 } } },
    MuiPaper: { styleOverrides: { root: { borderRadius: 14 } } },
    MuiAppBar: { styleOverrides: { root: { backgroundColor: "#111827" } } },
  },
});
