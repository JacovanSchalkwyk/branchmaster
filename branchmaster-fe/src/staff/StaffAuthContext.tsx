import React, { createContext, useContext, useEffect, useMemo, useState } from "react";
import { getApi, setStaffToken } from "../api/client";

type Role = "ADMIN";
type StaffMe = { id: string; email: string; roles: Role[]; employeeId: string };

type StaffAuthState = {
  me: StaffMe | null;
  isAuthed: boolean;
  login: (token: string) => void;
  logout: () => void;
  hasRole: (...roles: Role[]) => boolean;
  loading: boolean;
};

const Ctx = createContext<StaffAuthState | null>(null);

export function StaffAuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(localStorage.getItem("staff_token"));
  const [me, setMe] = useState<StaffMe | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    setStaffToken(token);
  }, [token]);

  useEffect(() => {
    let alive = true;

    async function load() {
      setLoading(true);

      if (!token) {
        if (alive) {
          setMe(null);
          setLoading(false);
        }
        return;
      }

      try {
        const staffMe = await getApi<StaffMe>("/staff/me");
        if (alive) setMe(staffMe);
      } catch {
        localStorage.removeItem("staff_token");
        if (alive) {
          setToken(null);
          setMe(null);
        }
      } finally {
        if (alive) setLoading(false);
      }
    }

    load();
    return () => {
      alive = false;
    };
  }, [token]);

  const value = useMemo<StaffAuthState>(() => {
    const hasRole = (...roles: Role[]) => !!me && roles.some((r) => me.roles.includes(r));

    return {
      me,
      isAuthed: !!token && !!me,
      loading,
      login: (t: string) => {
        localStorage.setItem("staff_token", t);
        setToken(t);
      },
      logout: () => {
        localStorage.removeItem("staff_token");
        setToken(null);
        setMe(null);
      },
      hasRole,
    };
  }, [token, me, loading]);

  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export function useStaffAuth() {
  const v = useContext(Ctx);
  if (!v) throw new Error("useStaffAuth must be used within StaffAuthProvider");
  return v;
}
