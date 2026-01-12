import { Navigate, Route, Routes } from "react-router-dom";

import BranchSelectPage from "./pages/BranchSelectPage";
import BranchCalendarPage from "./pages/BranchCalendarPage";

import { StaffAuthProvider } from "./staff/StaffAuthContext";
import { RequireStaff } from "./routes/guards";
import AdminPage from "./pages/admin/AdminPage.tsx";
import BookingConfirmationPage from "./pages/BookingConfirmationPage.tsx";
import StaffLogin from "./pages/admin/StaffLogin.tsx";

export default function App() {
  return (
    <StaffAuthProvider>
      <Routes>
        <Route path="/" element={<Navigate to="/branches" replace />} />
        <Route path="/branches" element={<BranchSelectPage />} />
        <Route path="/branches/:branchId/calendar" element={<BranchCalendarPage />} />
        <Route path="/staff/login" element={<StaffLogin />} />
        <Route
          path="/staff"
          element={
            <RequireStaff>
              <AdminPage />
            </RequireStaff>
          }
        />
        <Route path="/booking/confirmation" element={<BookingConfirmationPage />} />
        <Route path="*" element={<Navigate to="/branches" replace />} />
      </Routes>
    </StaffAuthProvider>
  );
}
