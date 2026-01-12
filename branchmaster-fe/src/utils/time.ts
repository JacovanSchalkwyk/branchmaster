import { format, isValid } from "date-fns";

/** Parses "HH:mm" or "HH:mm:ss" */
export function parseHHMM(timeStr: string): { h: number; m: number } {
  const [hStr, mStr] = timeStr.split(":");
  return { h: Number(hStr), m: Number(mStr) };
}

/** Converts "HH:mm" or "HH:mm:ss" to minutes since midnight */
export function toMinutes(timeStr: string): number {
  const { h, m } = parseHHMM(timeStr);
  return h * 60 + m;
}

/** Clamp a number between min and max */
export function clamp(n: number, min: number, max: number): number {
  return Math.max(min, Math.min(max, n));
}

/** Floors minutes to the previous hour */
export function floorToHour(mins: number): number {
  return Math.floor(mins / 60) * 60;
}

/** Ceils minutes to the next hour */
export function ceilToHour(mins: number): number {
  return Math.ceil(mins / 60) * 60;
}

export function trimTime(t?: string): string {
  return t && t.length >= 5 ? t.slice(0, 5) : "";
}

/** Trims "HH:mm:ss" to "HH:mm" */
export function trimHHMM(t?: string | null) {
  return t ? t.slice(0, 5) : "";
}

export function isValidHHMM(v: string) {
  return /^([01]\d|2[0-3]):[0-5]\d$/.test(v);
}

export function toIsoDate(d: Date) {
  return format(d, "yyyy-MM-dd");
}

export function parseIsoDate(s?: string | null): Date | null {
  if (!s) return null;
  const d = new Date(s);
  return isValid(d) ? d : null;
}
