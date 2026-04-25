// Query key factory for the catalog feature.
// Nest everything under "catalog" so a single invalidation clears the whole feature.
export const catalogKeys = {
  all: ["catalog"] as const,

  medicines: () => [...catalogKeys.all, "medicines"] as const,

  // Full params object is part of the key — TanStack Query refetches when any
  // filter/page/sort param changes.
  medicinesList: (params: Record<string, unknown>) =>
    [...catalogKeys.medicines(), "list", params] as const,

  medicine: (id: number) => [...catalogKeys.medicines(), id] as const,

  categories: () => [...catalogKeys.all, "categories"] as const,
}
