import { useState } from "react"
import { useSalesReport } from "../api/useSalesReport"
import { useInventoryReport } from "../api/useInventoryReport"
import { useExportReport } from "../api/useExportReport"

function today() {
  return new Date().toISOString().split("T")[0]
}

function monthStart() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-01`
}

export default function AdminReportsPage() {
  const [tab, setTab] = useState<"sales" | "inventory">("sales")

  const [startDate, setStartDate] = useState(monthStart())
  const [endDate, setEndDate]     = useState(today())
  const [query, setQuery]         = useState({ startDate: monthStart(), endDate: today() })

  const sales     = useSalesReport(query, tab === "sales")
  const inventory = useInventoryReport()
  const exportReport = useExportReport()

  const applyPreset = (start: string, end: string) => {
    setStartDate(start)
    setEndDate(end)
    setQuery({ startDate: start, endDate: end })
  }

  return (
    <div className="p-6 space-y-5">
      <h1 className="text-2xl font-bold text-slate-800">Reports</h1>

      {/* Tabs */}
      <div className="flex gap-1 bg-slate-100 p-1 rounded-lg w-fit">
        {(["sales", "inventory"] as const).map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`px-4 py-1.5 rounded-md text-sm font-medium capitalize transition-colors ${
              tab === t ? "bg-white shadow-sm text-slate-900" : "text-slate-500 hover:text-slate-900"
            }`}
          >
            {t}
          </button>
        ))}
      </div>

      {/* ── Sales Report ── */}
      {tab === "sales" && (
        <div className="space-y-5">
          {/* Date range picker + presets + export */}
          <div className="bg-white rounded-xl border p-4 space-y-3">
            <div className="flex flex-wrap gap-2">
              {[
                { label: "Today", fn: () => applyPreset(today(), today()) },
                { label: "This Month", fn: () => applyPreset(monthStart(), today()) },
                { label: "Last 7 Days", fn: () => { const d = new Date(); d.setDate(d.getDate() - 7); applyPreset(d.toISOString().split("T")[0], today()) } },
                { label: "Last 30 Days", fn: () => { const d = new Date(); d.setDate(d.getDate() - 30); applyPreset(d.toISOString().split("T")[0], today()) } },
              ].map(({ label, fn }) => (
                <button key={label} onClick={fn}
                  className="px-3 py-1 bg-slate-100 text-slate-700 text-xs rounded-lg hover:bg-slate-200 transition-colors">
                  {label}
                </button>
              ))}
            </div>
            <div className="flex flex-wrap items-end gap-3">
              <div>
                <label className="block text-xs font-medium text-slate-600 mb-1">From</label>
                <input type="date" value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  className="px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-green-500"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-600 mb-1">To</label>
                <input type="date" value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  className="px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-green-500"
                />
              </div>
              <button onClick={() => setQuery({ startDate, endDate })}
                className="px-4 py-2 bg-green-600 text-white text-sm font-medium rounded-lg hover:bg-green-700 transition-colors">
                Generate
              </button>
              {sales.data && (
                <div className="flex gap-2 ml-auto">
                  <button
                    onClick={() => exportReport.mutate({ format: "csv", startDate: query.startDate, endDate: query.endDate })}
                    disabled={exportReport.isPending}
                    className="px-3 py-2 border text-slate-700 text-sm rounded-lg hover:bg-slate-50 disabled:opacity-50 transition-colors">
                    ↓ CSV
                  </button>
                  <button
                    onClick={() => exportReport.mutate({ format: "pdf", startDate: query.startDate, endDate: query.endDate })}
                    disabled={exportReport.isPending}
                    className="px-3 py-2 border text-slate-700 text-sm rounded-lg hover:bg-slate-50 disabled:opacity-50 transition-colors">
                    ↓ PDF
                  </button>
                </div>
              )}
            </div>
          </div>

          {sales.isLoading && (
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="h-24 bg-slate-200 rounded-xl animate-pulse" />
              ))}
            </div>
          )}

          {sales.data && (
            <>
              {/* Summary cards */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                {[
                  { label: "Total Orders",     value: sales.data.totalOrders },
                  { label: "Delivered",         value: sales.data.deliveredOrders },
                  { label: "Cancelled",         value: sales.data.cancelledOrders },
                  { label: "Payment Failed",    value: sales.data.failedPaymentOrders },
                ].map((s) => (
                  <div key={s.label} className="bg-white rounded-xl border p-4 space-y-1">
                    <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">{s.label}</p>
                    <p className="text-2xl font-bold text-slate-800">{s.value ?? "—"}</p>
                  </div>
                ))}
              </div>

              <div className="grid sm:grid-cols-2 gap-4">
                <div className="bg-white rounded-xl border p-4 space-y-1">
                  <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Total Revenue</p>
                  <p className="text-3xl font-bold text-green-700">
                    ₹{sales.data.totalRevenue?.toLocaleString()}
                  </p>
                </div>
                <div className="bg-white rounded-xl border p-4 space-y-1">
                  <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Avg Order Value</p>
                  <p className="text-3xl font-bold text-slate-800">
                    ₹{sales.data.averageOrderValue?.toFixed(2)}
                  </p>
                </div>
              </div>

              {/* Top Medicines */}
              {(sales.data.topMedicines?.length ?? 0) > 0 && (
                <div className="bg-white rounded-xl border p-5">
                  <h2 className="font-semibold text-slate-800 mb-3">Top Selling Medicines</h2>
                  <table className="w-full text-sm">
                    <thead className="bg-slate-50">
                      <tr>
                        <th className="px-3 py-2 text-left font-semibold text-slate-600">Medicine</th>
                        <th className="px-3 py-2 text-right font-semibold text-slate-600">Qty Sold</th>
                        <th className="px-3 py-2 text-right font-semibold text-slate-600">Revenue</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y">
                      {sales.data.topMedicines?.map((m, i) => (
                        <tr key={i} className="hover:bg-slate-50">
                          <td className="px-3 py-2.5 text-slate-700">{m.name}</td>
                          <td className="px-3 py-2.5 text-right text-slate-600">{m.quantitySold}</td>
                          <td className="px-3 py-2.5 text-right font-semibold text-slate-800">₹{m.revenue?.toFixed(0)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}

              {/* Daily Revenue */}
              {(sales.data.dailyRevenue?.length ?? 0) > 0 && (
                <div className="bg-white rounded-xl border p-5">
                  <h2 className="font-semibold text-slate-800 mb-3">Daily Revenue</h2>
                  <div className="overflow-x-auto">
                    <table className="w-full text-sm">
                      <thead className="bg-slate-50">
                        <tr>
                          <th className="px-3 py-2 text-left font-semibold text-slate-600">Date</th>
                          <th className="px-3 py-2 text-right font-semibold text-slate-600">Orders</th>
                          <th className="px-3 py-2 text-right font-semibold text-slate-600">Revenue</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y">
                        {sales.data.dailyRevenue?.map((d, i) => (
                          <tr key={i} className="hover:bg-slate-50">
                            <td className="px-3 py-2.5 text-slate-700">{d.date}</td>
                            <td className="px-3 py-2.5 text-right text-slate-600">{d.orderCount}</td>
                            <td className="px-3 py-2.5 text-right font-semibold text-slate-800">₹{d.revenue?.toFixed(0)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      )}

      {/* ── Inventory Report ── */}
      {tab === "inventory" && (
        <div className="space-y-5">
          {inventory.isLoading && (
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
              {Array.from({ length: 6 }).map((_, i) => (
                <div key={i} className="h-24 bg-slate-200 rounded-xl animate-pulse" />
              ))}
            </div>
          )}

          {inventory.data && (
            <>
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
                {[
                  { label: "Total Medicines",    value: inventory.data.totalMedicines },
                  { label: "Out of Stock",        value: inventory.data.outOfStockCount,    color: "text-red-600" },
                  { label: "Low Stock",           value: inventory.data.lowStockCount,      color: "text-amber-600" },
                  { label: "Expiring (30 days)",  value: inventory.data.expiringIn30DaysCount, color: "text-amber-600" },
                  { label: "Already Expired",     value: inventory.data.alreadyExpiredCount, color: "text-red-600" },
                  { label: "Inventory Value",     value: inventory.data.totalInventoryValue != null ? `₹${inventory.data.totalInventoryValue.toLocaleString()}` : undefined, color: "text-green-700" },
                ].map((s) => (
                  <div key={s.label} className="bg-white rounded-xl border p-4 space-y-1">
                    <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">{s.label}</p>
                    <p className={`text-2xl font-bold ${s.color ?? "text-slate-800"}`}>{s.value ?? "—"}</p>
                  </div>
                ))}
              </div>

              {(inventory.data.lowStockItems?.length ?? 0) > 0 && (
                <div className="bg-white rounded-xl border p-5">
                  <h2 className="font-semibold text-slate-800 mb-3">⚠ Low Stock Items</h2>
                  <table className="w-full text-sm">
                    <thead className="bg-slate-50">
                      <tr>
                        <th className="px-3 py-2 text-left font-semibold text-slate-600">Medicine</th>
                        <th className="px-3 py-2 text-left font-semibold text-slate-600 hidden md:table-cell">Category</th>
                        <th className="px-3 py-2 text-right font-semibold text-slate-600">Stock</th>
                        <th className="px-3 py-2 text-right font-semibold text-slate-600">Price</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y">
                      {inventory.data.lowStockItems?.map((item) => (
                        <tr key={item.id} className="hover:bg-slate-50">
                          <td className="px-3 py-2.5 text-slate-700 font-medium">{item.name}</td>
                          <td className="px-3 py-2.5 text-slate-500 hidden md:table-cell">{item.categoryName}</td>
                          <td className="px-3 py-2.5 text-right font-bold text-amber-600">{item.stock}</td>
                          <td className="px-3 py-2.5 text-right text-slate-700">₹{item.price}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}

              {(inventory.data.expiringItems?.length ?? 0) > 0 && (
                <div className="bg-white rounded-xl border p-5">
                  <h2 className="font-semibold text-slate-800 mb-3">📅 Expiring Soon</h2>
                  <table className="w-full text-sm">
                    <thead className="bg-slate-50">
                      <tr>
                        <th className="px-3 py-2 text-left font-semibold text-slate-600">Medicine</th>
                        <th className="px-3 py-2 text-left font-semibold text-slate-600 hidden md:table-cell">Category</th>
                        <th className="px-3 py-2 text-right font-semibold text-slate-600">Stock</th>
                        <th className="px-3 py-2 text-right font-semibold text-slate-600">Expires</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y">
                      {inventory.data.expiringItems?.map((item) => (
                        <tr key={item.id} className="hover:bg-slate-50">
                          <td className="px-3 py-2.5 text-slate-700 font-medium">{item.name}</td>
                          <td className="px-3 py-2.5 text-slate-500 hidden md:table-cell">{item.categoryName}</td>
                          <td className="px-3 py-2.5 text-right text-slate-700">{item.stock}</td>
                          <td className="px-3 py-2.5 text-right font-medium text-amber-600">{item.expiryDate}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  )
}
