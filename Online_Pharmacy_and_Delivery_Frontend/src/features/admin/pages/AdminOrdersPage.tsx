import { useState } from "react"
import { Link } from "react-router-dom"
import { useAdminOrders } from "../api/useAdminOrders"
import { useUpdateOrderStatus } from "../api/useUpdateOrderStatus"
import { useCancelAdminOrder } from "../api/useCancelAdminOrder"
import type { components } from "@/shared/types/api/admin"

type OrderResponseDto = components["schemas"]["OrderResponseDto"]

const STATUS_COLOR: Record<string, string> = {
  CHECKOUT_STARTED:   "bg-slate-100 text-slate-700",
  PRESCRIPTION_PENDING:"bg-amber-100 text-amber-800",
  PAYMENT_PENDING:    "bg-yellow-100 text-yellow-800",
  PAID:               "bg-blue-100 text-blue-800",
  PACKED:             "bg-purple-100 text-purple-800",
  OUT_FOR_DELIVERY:   "bg-indigo-100 text-indigo-800",
  DELIVERED:          "bg-green-100 text-green-800",
  CUSTOMER_CANCELLED: "bg-red-100 text-red-800",
  ADMIN_CANCELLED:    "bg-red-100 text-red-800",
  PAYMENT_FAILED:     "bg-red-100 text-red-800",
}

const NEXT_STATUSES: Record<string, string[]> = {
  PAID:             ["PACKED"],
  PACKED:           ["OUT_FOR_DELIVERY"],
  OUT_FOR_DELIVERY: ["DELIVERED"],
  PAYMENT_PENDING:  ["PAID", "PAYMENT_FAILED"],
}

const ALL_STATUSES = [
  "ALL",
  "PAYMENT_PENDING",
  "PAID",
  "PACKED",
  "OUT_FOR_DELIVERY",
  "DELIVERED",
  "CUSTOMER_CANCELLED",
  "ADMIN_CANCELLED",
]

export default function AdminOrdersPage() {
  const { data: orders, isLoading } = useAdminOrders()
  const updateStatus = useUpdateOrderStatus()
  const cancelOrder  = useCancelAdminOrder()

  const [filterStatus, setFilterStatus] = useState("ALL")

  const filtered =
    filterStatus === "ALL"
      ? (orders ?? [])
      : (orders ?? []).filter((o) => o.status === filterStatus)

  if (isLoading) {
    return (
      <div className="p-6 space-y-3">
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="h-16 bg-slate-200 rounded-xl animate-pulse" />
        ))}
      </div>
    )
  }

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-bold text-slate-800">Orders</h1>

      {/* Status filter tabs */}
      <div className="flex gap-1.5 flex-wrap">
        {ALL_STATUSES.map((s) => (
          <button
            key={s}
            onClick={() => setFilterStatus(s)}
            className={`px-3 py-1.5 rounded-full text-xs font-medium transition-colors ${
              filterStatus === s
                ? "bg-green-600 text-white"
                : "bg-white border text-slate-600 hover:bg-slate-50"
            }`}
          >
            {s === "ALL" ? `All (${orders?.length ?? 0})` : s.replace(/_/g, " ")}
          </button>
        ))}
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
                  No orders found.
                </td>
              </tr>
            )}
            {filtered.map((order: OrderResponseDto) => {
              const nextStatuses = NEXT_STATUSES[order.status ?? ""] ?? []
              const canCancel =
                order.status &&
                !["DELIVERED", "CUSTOMER_CANCELLED", "ADMIN_CANCELLED"].includes(order.status)

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
                    <p className="text-slate-700">{order.userName ?? "—"}</p>
                    <p className="text-xs text-slate-400">{order.userEmail}</p>
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
                    <div className="flex items-center justify-end gap-1.5 flex-wrap">
                      {nextStatuses.map((s) => (
                        <button
                          key={s}
                          onClick={() => {
                            if (order.id != null) updateStatus.mutate({ id: order.id, status: s })
                          }}
                          disabled={updateStatus.isPending}
                          className="px-2.5 py-1 bg-green-600 text-white text-xs rounded-lg hover:bg-green-700 disabled:opacity-50 transition-colors"
                        >
                          → {s.replace(/_/g, " ")}
                        </button>
                      ))}
                      {canCancel && (
                        <button
                          onClick={() => {
                            if (order.id != null) cancelOrder.mutate({ id: order.id })
                          }}
                          disabled={cancelOrder.isPending}
                          className="px-2.5 py-1 bg-red-100 text-red-700 text-xs rounded-lg hover:bg-red-200 disabled:opacity-50 transition-colors"
                        >
                          Cancel
                        </button>
                      )}
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
