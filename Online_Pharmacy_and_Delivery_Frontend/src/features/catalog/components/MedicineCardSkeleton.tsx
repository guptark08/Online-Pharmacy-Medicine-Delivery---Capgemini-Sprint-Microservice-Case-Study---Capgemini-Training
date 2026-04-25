// Skeleton shown while medicines are loading.
// Shape matches MedicineCard so there is zero layout shift on load.
export function MedicineCardSkeleton() {
  return (
    <div className="animate-pulse rounded-xl border bg-white p-4 space-y-3">
      <div className="h-40 bg-slate-200 rounded-lg" />
      <div className="space-y-2">
        <div className="h-4 bg-slate-200 rounded w-3/4" />
        <div className="h-3 bg-slate-200 rounded w-1/2" />
      </div>
      <div className="h-5 bg-slate-200 rounded w-1/3" />
      <div className="h-8 bg-slate-200 rounded-lg" />
    </div>
  )
}
