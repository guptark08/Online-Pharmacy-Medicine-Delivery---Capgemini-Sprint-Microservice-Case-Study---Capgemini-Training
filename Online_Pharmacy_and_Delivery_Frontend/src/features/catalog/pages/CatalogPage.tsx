import { useState, useEffect } from "react"
import { useMedicines } from "../api/useMedicines"
import { useCategories } from "../api/useCategories"
import { useCatalogParams } from "../hooks/useCatalogParams"
import { useDebounce } from "@/shared/hooks/useDebounce"
import { MedicineCard } from "../components/MedicineCard"
import { MedicineCardSkeleton } from "../components/MedicineCardSkeleton"
import { Pagination } from "../components/Pagination"

const SORT_OPTIONS = [
  { label: "Name A–Z",  value: "name" },
  { label: "Price Low", value: "price" },
  { label: "Newest",    value: "createdAt" },
]

export default function CatalogPage() {
  const {
    params, apiParams,
    setKeyword, setCategoryId, setRequiresPrescription, setPage, setSortBy,
  } = useCatalogParams()

  const [localKeyword, setLocalKeyword] = useState(params.keyword)
  const debouncedKeyword = useDebounce(localKeyword, 300)

  useEffect(() => { setKeyword(debouncedKeyword) }, [debouncedKeyword, setKeyword])
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => { setLocalKeyword(params.keyword) }, [params.keyword])

  const { data, isLoading, isPlaceholderData, error } = useMedicines(apiParams)
  const { data: categories } = useCategories()

  const rxOptions = [
    { label: "All medicines",    value: undefined as boolean | undefined },
    { label: "Over the counter", value: false as boolean | undefined },
    { label: "Requires Rx",      value: true  as boolean | undefined },
  ]

  return (
    <div className="min-h-screen bg-slate-50">

      {/* ── Page header ──────────────────────────────────────────── */}
      <div className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-5">
          <div className="flex items-center justify-between flex-wrap gap-3">
            <div>
              <h1 className="text-xl font-extrabold text-slate-800">Medicine Catalog</h1>
              <p className="text-xs text-slate-500 mt-0.5">Browse genuine prescription & OTC medicines</p>
            </div>
            {data != null && (
              <span className="text-xs font-semibold text-slate-500 bg-slate-100 px-3 py-1.5 rounded-full">
                {data.totalElements.toLocaleString()} result{data.totalElements !== 1 ? "s" : ""}
              </span>
            )}
          </div>

          {/* Search + sort */}
          <div className="mt-4 flex gap-3">
            <div className="relative flex-1 max-w-xl">
              <svg className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
              </svg>
              <input
                id="medicine-search"
                type="text"
                value={localKeyword}
                onChange={(e) => setLocalKeyword(e.target.value)}
                placeholder="Search medicines, brands..."
                aria-label="Search medicines"
                className="w-full pl-9 pr-4 py-2.5 border-2 border-slate-200 focus:border-green-500 rounded-xl text-sm outline-none bg-white transition-colors"
              />
            </div>
            <select
              value={params.sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              aria-label="Sort by"
              className="px-3 py-2.5 border-2 border-slate-200 rounded-xl text-sm bg-white outline-none focus:border-green-500 transition-colors font-medium text-slate-700"
            >
              {SORT_OPTIONS.map((o) => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* ── Body ─────────────────────────────────────────────────── */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6">
        <div className="flex gap-6">

          {/* Sidebar */}
          <aside className="hidden md:block w-52 shrink-0 space-y-6">
            <div className="bg-white border rounded-2xl p-4 space-y-1">
              <h3 className="text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-3">Category</h3>
              <SidebarButton active={params.categoryId == null} onClick={() => setCategoryId(undefined)}>
                All Categories
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

            <div className="bg-white border rounded-2xl p-4 space-y-1">
              <h3 className="text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-3">Prescription</h3>
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
          </aside>

          {/* Main */}
          <div className="flex-1 min-w-0 space-y-5">
            {error && (
              <div className="bg-red-50 border border-red-200 rounded-2xl p-4 flex items-center gap-3">
                <div className="w-8 h-8 rounded-xl bg-red-100 flex items-center justify-center shrink-0">
                  <svg className="w-4 h-4 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z" />
                  </svg>
                </div>
                <div>
                  <p className="text-red-700 text-sm font-semibold">Failed to load medicines</p>
                  <p className="text-red-500 text-xs mt-0.5">{(error as Error).message}</p>
                </div>
              </div>
            )}

            <div className={`grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4 transition-opacity duration-150 ${isPlaceholderData ? "opacity-60" : "opacity-100"}`}>
              {isLoading
                ? Array.from({ length: 8 }).map((_, i) => <MedicineCardSkeleton key={i} />)
                : data?.medicines.map((m) => <MedicineCard key={m.id} medicine={m} />)
              }
            </div>

            {!isLoading && data?.medicines.length === 0 && (
              <div className="flex flex-col items-center py-20 gap-3 text-slate-400">
                <div className="w-16 h-16 rounded-2xl bg-slate-100 flex items-center justify-center">
                  <svg className="w-8 h-8 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
                  </svg>
                </div>
                <p className="font-semibold text-slate-500">No medicines found</p>
                <p className="text-sm text-slate-400">Try a different search term or filter</p>
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
      className={`w-full text-left px-3 py-2 rounded-xl text-sm transition-colors font-medium ${
        active
          ? "bg-green-50 text-green-700 border border-green-200"
          : "text-slate-600 hover:bg-slate-50 hover:text-slate-800"
      }`}
    >
      {children}
    </button>
  )
}
