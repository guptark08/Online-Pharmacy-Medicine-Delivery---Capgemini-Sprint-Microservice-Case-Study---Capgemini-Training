// Query key factory for the catalog feature.
// Nest everything under "catalog" so a single invalidation clears the whole feature.
export const catalogKeys = {
  all: ["catalog"] as const,

  medicines: () => [...catalogKeys.all, "medicines"] as const,

  // Individual params are part of the key — TanStack Query refetches when any
  // filter/page/sort param changes.
  medicinesList: (params: Record<string, unknown>) =>
    [...catalogKeys.medicines(), "list", params.keyword ?? "", params.categoryId ?? "none", params.requiresPrescription ?? "none", params.page ?? 0, params.size ?? 12, params.sortBy ?? "name"] as const,

  medicine: (id: number) => [...catalogKeys.medicines(), id] as const,

  categories: () => [...catalogKeys.all, "categories"] as const,
}
