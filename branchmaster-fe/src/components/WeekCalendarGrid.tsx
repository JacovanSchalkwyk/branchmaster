import { Box, Paper, Typography, Tooltip } from "@mui/material";
import type { AvailabilityResponse, Timeslot } from "../types/availability";
import { format } from "date-fns";
import { floorToHour, ceilToHour, trimTime } from "../utils/time";

type WeekCalendarGridProps = {
  days: Date[];
  data: AvailabilityResponse;
  onSelect: (dateStr: string, slot: Timeslot) => void;
  slotMinutes: number;
  startHour: number;
  endHour: number;

  perDayMin?: Map<string, number>;
  perDayMax?: Map<string, number>;
};

export default function WeekCalendarGrid({
  days,
  data,
  onSelect,
  slotMinutes,
  startHour,
  endHour,
  perDayMin,
  perDayMax,
}: WeekCalendarGridProps) {
  const dayCount = days.length;

  const rowsPerHour = 60 / slotMinutes;
  const totalSlots = Math.max(1, (endHour - startHour) * rowsPerHour);

  const gridTemplateColumns = `72px repeat(${dayCount}, minmax(160px, 1fr))`;
  const gridTemplateRows = `56px repeat(${totalSlots}, 18px)`;

  const headerRow = 1;

  return (
    <Paper sx={{ borderRadius: 2, overflow: "auto" }}>
      <Box
        sx={{
          display: "grid",
          gridTemplateColumns,
          gridTemplateRows,
          minWidth: 1200,
          position: "relative",
        }}
      >
        <Box
          sx={{
            gridColumn: 1,
            gridRow: headerRow,
            borderBottom: "1px solid",
            borderColor: "divider",
            backgroundColor: "secondary.main",
            position: "sticky",
            top: 0,
            left: 0,
            zIndex: 4,
          }}
        />

        {/* Day headers */}
        {days.map((d, idx) => {
          const dateStr = format(d, "yyyy-MM-dd");
          return (
            <Box
              key={dateStr}
              sx={{
                gridColumn: idx + 2,
                gridRow: headerRow,
                px: 1.25,
                py: 1,
                borderLeft: "1px solid",
                borderBottom: "1px solid",
                borderColor: "divider",
                backgroundColor: "secondary.main",
                position: "sticky",
                top: 0,
                zIndex: 3,
              }}
            >
              <Typography variant="subtitle2" sx={{ fontWeight: 800 }}>
                {format(d, "EEE")}
              </Typography>
              <Typography variant="caption" sx={{ opacity: 0.8 }}>
                {format(d, "d MMM")}
              </Typography>
            </Box>
          );
        })}

        {/* Background grid */}
        {Array.from({ length: totalSlots }, (_, i) => {
          const minutesFromStart = i * slotMinutes;
          const hour = startHour + Math.floor(minutesFromStart / 60);
          const minute = minutesFromStart % 60;
          const showHourLabel = minute === 0;

          const gridRow = headerRow + 1 + i;

          return (
            <Box key={i} sx={{ display: "contents" }}>
              <Box
                sx={{
                  gridColumn: 1,
                  gridRow,
                  pr: 1,
                  borderBottom: "1px solid",
                  borderColor: "divider",
                  backgroundColor: "background.paper",
                  position: "sticky",
                  left: 0,
                  zIndex: 2,
                }}
              >
                {showHourLabel && (
                  <Typography variant="caption" sx={{ float: "right", opacity: 0.8 }}>
                    {String(hour).padStart(2, "0")}:00
                  </Typography>
                )}
              </Box>

              {days.map((d, dayIdx) => {
                const dateStr = format(d, "yyyy-MM-dd");

                const dayMin = perDayMin?.get(dateStr);
                const dayMax = perDayMax?.get(dateStr);

                const rowTimeMins = startHour * 60 + minutesFromStart;
                const withinBand =
                  dayMin != null &&
                  dayMax != null &&
                  rowTimeMins >= floorToHour(dayMin) &&
                  rowTimeMins < ceilToHour(dayMax);

                return (
                  <Box
                    key={dayIdx}
                    sx={{
                      gridColumn: dayIdx + 2,
                      gridRow,
                      borderLeft: "1px solid",
                      borderBottom: "1px solid",
                      borderColor: "divider",
                      backgroundColor:
                        minute === 0
                          ? "rgba(0,0,0,0.03)"
                          : withinBand
                            ? "rgba(25, 118, 210, 0.03)"
                            : "transparent",
                      pointerEvents: "none",
                    }}
                  />
                );
              })}
            </Box>
          );
        })}

        {/* Slot blocks */}
        {days.flatMap((d, dayIdx) => {
          const dateStr = format(d, "yyyy-MM-dd");
          const slots = data[dateStr] ?? [];

          return slots.map((slot, slotIdx) => {
            const startRow = timeToGridRow(slot.startTime, startHour, slotMinutes, headerRow);
            const endRow = timeToGridRow(slot.endTime, startHour, slotMinutes, headerRow);

            const isFullyBooked = slot.status === "FULLY_BOOKED";

            const todayStart = new Date();
            todayStart.setHours(0, 0, 0, 0);

            const dayStart = new Date(d);
            dayStart.setHours(0, 0, 0, 0);

            const isPastDay = dayStart.getTime() < todayStart.getTime();

            const isDisabled = isFullyBooked || isPastDay;

            const tooltipText = isPastDay
              ? "Unavailable"
              : isFullyBooked
                ? "Fully booked"
                : "Available — click to book";

            return (
              <Tooltip
                title={tooltipText}
                arrow
                placement="top"
                key={`${dateStr}-${dayIdx}-${slot.startTime}-${slot.endTime}`}
              >
                <Box
                  key={`${dateStr}-${slotIdx}-${slot.startTime}`}
                  onClick={isDisabled ? undefined : () => onSelect(dateStr, slot)}
                  sx={(theme) => ({
                    gridColumn: dayIdx + 2,
                    gridRow: `${startRow} / ${endRow}`,
                    mx: 0.6,
                    my: 0.25,
                    px: 1,
                    py: 0.25,
                    borderRadius: 1.25,
                    userSelect: "none",
                    zIndex: 5,
                    overflow: "hidden",
                    display: "flex",
                    alignItems: "center",

                    cursor: isDisabled ? "not-allowed" : "pointer",
                    opacity: isDisabled ? 0.4 : 1,

                    backgroundColor: isDisabled
                      ? theme.palette.grey[200]
                      : theme.palette.primary.light,

                    border: "1px solid",
                    borderColor: isDisabled ? theme.palette.grey[400] : theme.palette.primary.light,

                    color: isDisabled ? theme.palette.grey[700] : "inherit",

                    "&:hover": isDisabled
                      ? {}
                      : {
                          backgroundColor: theme.palette.primary.main,
                          color: theme.palette.primary.contrastText,
                          borderColor: theme.palette.primary.main,
                        },
                  })}
                >
                  <Typography variant="caption" sx={{ fontWeight: 800, lineHeight: 1.2 }}>
                    {trimTime(slot.startTime)} - {trimTime(slot.endTime)}
                  </Typography>
                </Box>
              </Tooltip>
            );
          });
        })}
      </Box>
    </Paper>
  );
}

function timeToGridRow(timeStr: string, startHour: number, slotMinutes: number, headerRow: number) {
  const [hStr, mStr] = timeStr.split(":");
  const h = Number(hStr);
  const m = Number(mStr);

  const minutesFromStart = (h - startHour) * 60 + m;
  const slotIndex = Math.floor(minutesFromStart / slotMinutes);

  return headerRow + 1 + slotIndex;
}
