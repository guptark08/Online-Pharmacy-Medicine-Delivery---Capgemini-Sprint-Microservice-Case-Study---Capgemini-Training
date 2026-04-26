import { useState, useEffect } from "react"
import { useMedicines } from "../api/useMedicines"
import { useCategories } from "../api/useCategories"
import { useCatalogParams } from "../hooks/useCatalogParams"
import { useDebounce } from "@/shared/hooks/useDebounce"
import { MedicineCard } from "../components/MedicineCard"
import { MedicineCardSkeleton } from "../components/MedicineCardSkeleton"
import { Pagination } from "../components/Pagination"

const SORT_OPTIONS = [
  { label: "Name A–Z",   value: "name" },
  { label: "Price Low",  value: "price" },
  { label: "Newest",     value: "createdAt" },
]

export default function CatalogPage() {
  const {
    params, apiParams,
    setKeyword, setCategoryId, setRequiresPrescription, setPage, setSortBy,
  } = useCatalogParams()

  // Local keyword drives the visible input.
  // Debounce WRITES to the URL (and thus the query key) — not the display value.
  // This keeps the input snappy while avoiding a query on every keystroke.
  const [localKeyword, setLocalKeyword] = useState(params.keyword)
  const debouncedKeyword = useDebounce(localKeyword, 300)

  useEffect(() => {
    setKeyword(debouncedKeyword)
  }, [debouncedKeyword, setKeyword])

  // Sync local input if the URL keyword changes externally (browser back/forward)
  useEffect(() => {
    setLocalKeyword(params.keyword)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params.keyword])

  const { data, isLoading, isPlaceholderData } = useMedicines(apiParams)
  const { data: categories } = useCategories()

  const rxOptions = [
    { label: "All medicines",      value: undefined as boolean | undefined },
    { label: "Over the counter",   value: false as boolean | undefined },
    { label: "Requires Rx",        value: true  as boolean | undefined },
  ]

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6">
      <div className="flex gap-6">

        {/* ── Sidebar ── */}
        <aside className="hidden md:block w-52 shrink-0 space-y-6">
          <div>
            <h3 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">
              Category
            </h3>
            <div className="space-y-0.5">
              <SidebarButton
                active={params.categoryId == null}
                onClick={() => setCategoryId(undefined)}
              >
                All
              </SidebarButton>
              {categories?.map((cat) => (
                <SidebarButton
                  key={cat.id}
                  active={params.categoryId === cat.id}
                  onClick={() => setCategoryId(cat.id)}
                >
                  {cat.name}
                </SidebarButton>
              ))}
            </div>
          </div>

          <div>
            <h3 className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">
              Prescription
            </h3>
            <div className="space-y-0.5">
              {rxOptions.map(({ label, value }) => (
                <SidebarButton
                  key={label}
                  active={params.requiresPrescription === value}
                  onClick={() => setRequiresPrescription(value)}
                >
                  {label}
                </SidebarButton>
              ))}
            </div>
          </div>
        </aside>

        {/* ── Main ── */}
        <div className="flex-1 min-w-0 space-y-4">

          {/* Search + Sort bar */}
          <div className="flex gap-3">
            <div className="relative flex-1">
              <span className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-sm" aria-hidden="true">🔍</span>
              <input
                id="medicine-search"
                type="text"
                value={localKeyword}
                onChange={(e) => setLocalKeyword(e.target.value)}
                placeholder="Search medicines…"
                aria-label="Search medicines"
                className="w-full pl-8 pr-4 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-green-500 bg-white"
              />
            </div>
            <select
              value={params.sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              aria-label="Sort by"
              className="px-3 py-2 border rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-green-500"
            >
              {SORT_OPTIONS.map((o) => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </select>
          </div>

          {/* Result count */}
          {data != null && (
            <p className="text-sm text-slate-500">
              {data.totalElements.toLocaleString()} result{data.totalElements !== 1 ? "s" : ""}
            </p>
          )}

          {/* Grid — dim while fetching next page (placeholderData) */}
          <div
            className={`grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4 transition-opacity duration-150 ${
              isPlaceholderData ? "opacity-60" : "opacity-100"
            }`}
          >
            {isLoading
              ? Array.from({ length: 8 }).map((_, i) => <MedicineCardSkeleton key={i} />)
              : data?.medicines.map((m) => <MedicineCard key={m.id} medicine={m} />)
            }
          </div>

          {!isLoading && data?.medicines.length === 0 && (
            <div className="flex flex-col items-center py-20 gap-2 text-slate-400">
              <span className="text-5xl">🔍</span>
              <p className="text-sm">No medicines found. Try a different search or filter.</p>
            </div>
          )}

          <Pagination
            currentPage={params.page}
            totalPages={data?.totalPages ?? 0}
            onPageChange={setPage}
            isLoading={isPlaceholderData}
          />
        </div>
      </div>
    </div>
  )
}

function SidebarButton({
  children,
  active,
  onClick,
}: {
  children: React.ReactNode
  active: boolean
  onClick: () => void
}) {
  return (
    <button
      onClick={onClick}
      aria-pressed={active}
      className={`w-full text-left px-3 py-1.5 rounded-lg text-sm transition-colors ${
        active
          ? "bg-green-50 text-green-700 font-medium"
          : "text-slate-600 hover:bg-slate-50"
      }`}
    >
      {children}
    </button>
  )
}
