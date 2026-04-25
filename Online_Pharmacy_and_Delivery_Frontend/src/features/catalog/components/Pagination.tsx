interface PaginationProps {
  currentPage: number   // 1-indexed
  totalPages: number
  onPageChange: (page: number) => void
  isLoading?: boolean
}

export function Pagination({ currentPage, totalPages, onPageChange, isLoading }: PaginationProps) {
  if (totalPages <= 1) return null

  // Show up to 5 page numbers around the current page, always show first/last
  const allPages = Array.from({ length: totalPages }, (_, i) => i + 1)
  const visible = allPages.filter(
    (p) => p === 1 || p === totalPages || Math.abs(p - currentPage) <= 1
  )

  return (
    <div className={`flex items-center justify-center gap-1 transition-opacity ${isLoading ? "opacity-50 pointer-events-none" : ""}`}>
      <button
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 1}
        className="px-3 py-1.5 rounded-lg border text-sm font-medium hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
      >
        ← Prev
      </button>

      {visible.map((page, idx) => {
        const prev = visible[idx - 1]
        const showEllipsis = prev && page - prev > 1

        return (
          <span key={page} className="flex items-center gap-1">
            {showEllipsis && (
              <span className="px-1.5 text-slate-400 text-sm select-none">…</span>
            )}
            <button
              onClick={() => onPageChange(page)}
              className={`min-w-[36px] h-9 rounded-lg border text-sm font-medium transition-colors ${
                page === currentPage
                  ? "bg-green-600 text-white border-green-600"
                  : "hover:bg-slate-50 text-slate-700"
              }`}
            >
              {page}
            </button>
          </span>
        )
      })}

      <button
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages}
        className="px-3 py-1.5 rounded-lg border text-sm font-medium hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
      >
        Next →
      </button>
    </div>
  )
}
