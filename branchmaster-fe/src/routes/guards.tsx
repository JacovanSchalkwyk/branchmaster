import { Navigate } from "react-router-dom";
import { useStaffAuth } from "../staff/StaffAuthContext";
import type { JSX } from "react";

type Role = "ADMIN";

export function RequireStaff({ children }: { children: JSX.Element }) {
  const { isAuthed, loading } = useStaffAuth();
  if (loading) return <div />;
  if (!isAuthed) return <Navigate to="/staff/login" replace />;
  return children;
}

export function RequireRole({ anyOf, children }: { anyOf: Role[]; children: JSX.Element }) {
  const { isAuthed, hasRole, loading } = useStaffAuth();
  if (loading) return <div />;
  if (!isAuthed) return <Navigate to="/staff/login" replace />;
  if (!hasRole(...anyOf)) return <Navigate to="/staff" replace />;
  return children;
}
