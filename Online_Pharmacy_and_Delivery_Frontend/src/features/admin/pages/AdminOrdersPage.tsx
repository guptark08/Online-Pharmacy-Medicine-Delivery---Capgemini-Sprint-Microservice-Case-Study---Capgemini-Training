import { useMemo, useState } from "react"
import { Link } from "react-router-dom"
import { useAdminOrders } from "../api/useAdminOrders"
import { useUpdateOrderStatus } from "../api/useUpdateOrderStatus"
import type { components } from "@/shared/types/api/admin"

type OrderResponseDto = components["schemas"]["OrderResponseDto"]

const normalizeStatus = (s: string | null | undefined) =>
  (s ?? "").trim().toUpperCase()

const STATUS_COLOR: Record<string, string> = {
  CHECKOUT_STARTED:      "bg-slate-100 text-slate-700",
  PRESCRIPTION_PENDING:  "bg-amber-100 text-amber-800",
  PRESCRIPTION_APPROVED: "bg-teal-100 text-teal-800",
  PRESCRIPTION_REJECTED: "bg-rose-100 text-rose-800",
  PAYMENT_PENDING:       "bg-yellow-100 text-yellow-800",
  PAYMENT_FAILED:        "bg-red-100 text-red-800",
  PAID:                  "bg-blue-100 text-blue-800",
  PACKED:                "bg-purple-100 text-purple-800",
  OUT_FOR_DELIVERY:      "bg-indigo-100 text-indigo-800",
  DELIVERED:             "bg-green-100 text-green-800",
  CUSTOMER_CANCELLED:    "bg-red-100 text-red-800",
  ADMIN_CANCELLED:       "bg-red-100 text-red-800",
  RETURN_REQUESTED:      "bg-orange-100 text-orange-800",
  REFUND_INITIATED:      "bg-orange-100 text-orange-800",
  REFUND_COMPLETED:      "bg-green-100 text-green-800",
}

// All statuses admin can set — excludes system-only and customer-only entries
const ALL_ADMIN_STATUSES = [
  "PRESCRIPTION_PENDING",
  "PRESCRIPTION_APPROVED",
  "PRESCRIPTION_REJECTED",
  "PAYMENT_PENDING",
  "PAYMENT_FAILED",
  "PAID",
  "PACKED",
  "OUT_FOR_DELIVERY",
  "DELIVERED",
  "RETURN_REQUESTED",
  "REFUND_INITIATED",
  "REFUND_COMPLETED",
  "ADMIN_CANCELLED",
]

const TERMINAL_STATUSES = ["CUSTOMER_CANCELLED", "ADMIN_CANCELLED", "REFUND_COMPLETED"]

const ALL_STATUSES = [
  "ALL",
  "CHECKOUT_STARTED",
  "PRESCRIPTION_PENDING",
  "PRESCRIPTION_APPROVED",
  "PRESCRIPTION_REJECTED",
  "PAYMENT_PENDING",
  "PAYMENT_FAILED",
  "PAID",
  "PACKED",
  "OUT_FOR_DELIVERY",
  "DELIVERED",
  "RETURN_REQUESTED",
  "REFUND_INITIATED",
  "REFUND_COMPLETED",
  "CUSTOMER_CANCELLED",
  "ADMIN_CANCELLED",
]

export default function AdminOrdersPage() {
  const { data: orders, isLoading, isFetching, dataUpdatedAt, refetch } = useAdminOrders()
  const updateStatus = useUpdateOrderStatus()

  const [filterStatus, setFilterStatus] = useState("ALL")

  // Bucket orders by normalized status once per data update — both counts
  // and the filtered list read from the same source of truth.
  const buckets = useMemo(() => {
    const byStatus = new Map<string, OrderResponseDto[]>()
    for (const o of orders ?? []) {
      const key = normalizeStatus(o.status)
      const arr = byStatus.get(key) ?? []
      arr.push(o)
      byStatus.set(key, arr)
    }
    return byStatus
  }, [orders])

  const countByStatus = (status: string) =>
    status === "ALL"
      ? (orders?.length ?? 0)
      : (buckets.get(normalizeStatus(status))?.length ?? 0)

  const filtered =
    filterStatus === "ALL"
      ? (orders ?? [])
      : (buckets.get(normalizeStatus(filterStatus)) ?? [])

  const lastUpdated = dataUpdatedAt
    ? new Date(dataUpdatedAt).toLocaleTimeString()
    : null

  if (isLoading) {
    return (
      <div className="p-6 space-y-3">
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="h-16 bg-slate-200 rounded-xl animate-pulse" />
        ))}
      </div>
    )
  }

  if (!orders || orders.length === 0) {
    return (
      <div className="p-6 space-y-4">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-bold text-slate-800">Orders</h1>
          <button
            onClick={() => refetch()}
            disabled={isFetching}
            className="text-xs text-green-600 hover:text-green-800 disabled:opacity-50"
          >
            {isFetching ? "Refreshing…" : "↻ Refresh"}
          </button>
        </div>
        <div className="bg-white rounded-xl border p-8 text-center">
          <p className="text-4xl mb-2">📦</p>
          <p className="text-slate-500">No orders found.</p>
          <p className="text-xs text-slate-400 mt-2">
            If orders exist in the system but don't appear here, the backend services may be unavailable.
          </p>
        </div>
      </div>
    )
  }

  return (
    <div className="p-6 space-y-4">
      <div className="flex items-center justify-between gap-3 flex-wrap">
        <h1 className="text-2xl font-bold text-slate-800">Orders</h1>
        <div className="flex items-center gap-3">
          {lastUpdated && (
            <span className="text-xs text-slate-400">
              {isFetching ? (
                <span className="flex items-center gap-1">
                  <span className="w-2 h-2 rounded-full bg-green-400 animate-pulse inline-block" />
                  Updating…
                </span>
              ) : (
                `Updated ${lastUpdated}`
              )}
            </span>
          )}
          <button
            onClick={() => refetch()}
            disabled={isFetching}
            className="text-xs text-green-600 hover:text-green-800 disabled:opacity-50 font-medium"
          >
            ↻ Refresh
          </button>
        </div>
      </div>

      {/* Status filter tabs with live counts */}
      <div className="flex gap-1.5 flex-wrap">
        {ALL_STATUSES.map((s) => {
          const count = countByStatus(s)
          return (
            <button
              key={s}
              onClick={() => setFilterStatus(s)}
              className={`px-3 py-1.5 rounded-full text-xs font-medium transition-colors flex items-center gap-1 ${
                filterStatus === s
                  ? "bg-green-600 text-white"
                  : "bg-white border text-slate-600 hover:bg-slate-50"
              }`}
            >
              <span>
                {s === "ALL" ? "All" : s.replace(/_/g, " ")}
              </span>
              <span className={`rounded-full px-1.5 py-0.5 text-[10px] font-bold leading-none ${
                filterStatus === s
                  ? "bg-white/25 text-white"
                  : count > 0
                  ? "bg-slate-100 text-slate-600"
                  : "text-slate-300"
              }`}>
                {count}
              </span>
            </button>
          )
        })}
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-slate-50 border-b">
            <tr>
              <th className="px-4 py-3 text-left font-semibold text-slate-600">Order</th>
              <th className="px-4 py-3 text-left font-semibold text-slate-600 hidden md:table-cell">Customer</th>
              <th className="px-4 py-3 text-left font-semibold text-slate-600">Status</th>
              <th className="px-4 py-3 text-right font-semibold text-slate-600">Amount</th>
              <th className="px-4 py-3 text-right font-semibold text-slate-600">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {filtered.length === 0 && (
              <tr>
                <td colSpan={5} className="px-4 py-8 text-center text-slate-400">
                  No orders in {filterStatus === "ALL" ? "any status" : filterStatus.replace(/_/g, " ")}.
                  {filterStatus !== "ALL" && (
                    <button
                      onClick={() => setFilterStatus("ALL")}
                      className="ml-2 text-green-600 hover:text-green-800 underline"
                    >
                      Show all
                    </button>
                  )}
                </td>
              </tr>
            )}
            {filtered.map((order: OrderResponseDto) => {
              const isTerminal = TERMINAL_STATUSES.includes(order.status ?? "")
              const selectableStatuses = ALL_ADMIN_STATUSES.filter((s) => s !== order.status)

              return (
                <tr key={order.id} className="hover:bg-slate-50 transition-colors">
                  <td className="px-4 py-3">
                    <Link
                      to={`/admin/orders/${order.id}`}
                      className="font-semibold text-green-700 hover:underline"
                    >
                      #{order.id}
                    </Link>
                    {order.createdAt && (
                      <p className="text-xs text-slate-400">
                        {new Date(order.createdAt).toLocaleDateString()}
                      </p>
                    )}
                  </td>
                  <td className="px-4 py-3 hidden md:table-cell">
                    <p className="text-slate-700">
                      {order.userName ?? (order.userId != null ? `User #${order.userId}` : "—")}
                    </p>
                    {order.userEmail && (
                      <p className="text-xs text-slate-400">{order.userEmail}</p>
                    )}
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`text-[10px] px-2 py-0.5 rounded-full font-medium ${
                        STATUS_COLOR[order.status ?? ""] ?? "bg-slate-100 text-slate-700"
                      }`}
                    >
                      {order.status?.replace(/_/g, " ")}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right font-semibold text-slate-800">
                    ₹{order.totalAmount?.toFixed(0)}
                  </td>
                  <td className="px-4 py-3 text-right">
                    <div className="flex items-center justify-end gap-2">
                      {!isTerminal ? (
                        <select
                          key={order.status}
                          defaultValue={order.status ?? ""}
                          onChange={(e) => {
                            const s = e.target.value
                            if (s && s !== order.status && order.id != null) {
                              updateStatus.mutate({ id: order.id, status: s })
                            }
                          }}
                          disabled={updateStatus.isPending}
                          className="text-xs border border-slate-200 rounded-lg px-2 py-1.5 bg-white text-slate-700 focus:outline-none focus:border-green-500 disabled:opacity-50 cursor-pointer"
                        >
                          <option value={order.status ?? ""} disabled>
                            {(order.status ?? "").replace(/_/g, " ")} (current)
                          </option>
                          <optgroup label="── Change to ──">
                            {selectableStatuses.map((s) => (
                              <option key={s} value={s}>{s.replace(/_/g, " ")}</option>
                            ))}
                          </optgroup>
                        </select>
                      ) : (
                        <span className="text-xs text-slate-400 italic">Terminal</span>
                      )}
                      <Link
                        to={`/admin/orders/${order.id}`}
                        className="text-xs text-green-600 hover:text-green-800 font-medium whitespace-nowrap"
                      >
                        View →
                      </Link>
                    </div>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    </div>
  )
}
