// BranchAutocomplete.tsx
import { Autocomplete, Box, CircularProgress, TextField, Typography } from "@mui/material";
import { useMemo } from "react";
import type { BranchFull } from "../types/branch";

type Props = {
  branches: BranchFull[];
  loading: boolean;
  value: BranchFull | null;
  onChange: (branch: BranchFull | null) => void;

  query?: string;
  onQueryChange?: (q: string) => void;

  label?: string;
  placeholder?: string;
  size?: "small" | "medium";
};

export default function BranchAutocomplete({
  branches,
  loading,
  value,
  onChange,
  query,
  onQueryChange,
  label = "Select a branch",
  placeholder = "Start typing…",
  size = "medium",
}: Props) {
  const filterOptions = useMemo(() => {
    return (options: BranchFull[], state: { inputValue: string }) => {
      const q = state.inputValue.trim().toLowerCase();
      if (!q) return options;

      return options.filter((b) => {
        const hay =
          `${b.name} ${b.friendlyAddress ?? ""} ${b.city ?? ""} ${b.suburb ?? ""} ${b.province ?? ""}`.toLowerCase();
        return hay.includes(q);
      });
    };
  }, []);

  return (
    <Autocomplete
      options={branches}
      loading={loading}
      value={value}
      inputValue={query}
      onInputChange={(_, v) => onQueryChange?.(v)}
      onChange={(_, v) => onChange(v)}
      getOptionLabel={(o) => o.name}
      isOptionEqualToValue={(o, v) => o.branchId === v.branchId}
      filterOptions={filterOptions}
      renderInput={(params) => (
        <TextField
          {...params}
          label={label}
          placeholder={placeholder}
          fullWidth
          size={size}
          InputProps={{
            ...params.InputProps,
            endAdornment: (
              <>
                {loading ? <CircularProgress size={18} /> : null}
                {params.InputProps.endAdornment}
              </>
            ),
          }}
        />
      )}
      renderOption={(props, option) => (
        <li {...props} key={option.branchId}>
          <Box sx={{ py: 0.5 }}>
            <Typography sx={{ fontWeight: 650 }}>{option.name}</Typography>
            <Typography variant="body2" sx={{ color: "text.secondary", lineHeight: 1.25 }}>
              {option.friendlyAddress}
            </Typography>
          </Box>
        </li>
      )}
    />
  );
}
