import { Link } from "react-router-dom"
import { useOrders } from "../api/useOrders"
import type { components } from "@/shared/types/api/order"

type OrderResponse = components["schemas"]["OrderResponse"]
type OrderStatus   = NonNullable<OrderResponse["status"]>

const STATUS_META: Partial<Record<OrderStatus, { label: string; color: string; dot: string }>> = {
  CHECKOUT_STARTED:      { label: "Processing",         color: "bg-slate-100 text-slate-700",   dot: "bg-slate-400"   },
  PRESCRIPTION_PENDING:  { label: "Awaiting Rx Review", color: "bg-amber-100 text-amber-800",   dot: "bg-amber-500"   },
  PRESCRIPTION_APPROVED: { label: "Rx Approved",        color: "bg-teal-100 text-teal-800",     dot: "bg-teal-500"    },
  PRESCRIPTION_REJECTED: { label: "Rx Rejected",        color: "bg-rose-100 text-rose-800",     dot: "bg-rose-500"    },
  PAYMENT_PENDING:       { label: "Payment Pending",    color: "bg-yellow-100 text-yellow-800", dot: "bg-yellow-500"  },
  PAYMENT_FAILED:        { label: "Payment Failed",     color: "bg-red-100 text-red-800",       dot: "bg-red-500"     },
  PAID:                  { label: "Paid",               color: "bg-blue-100 text-blue-800",     dot: "bg-blue-500"    },
  PACKED:                { label: "Packed",             color: "bg-purple-100 text-purple-800", dot: "bg-purple-500"  },
  OUT_FOR_DELIVERY:      { label: "Out for Delivery",   color: "bg-indigo-100 text-indigo-800", dot: "bg-indigo-500"  },
  DELIVERED:             { label: "Delivered",          color: "bg-green-100 text-green-800",   dot: "bg-green-500"   },
  CUSTOMER_CANCELLED:    { label: "Cancelled",          color: "bg-red-100 text-red-800",       dot: "bg-red-400"     },
  ADMIN_CANCELLED:       { label: "Cancelled",          color: "bg-red-100 text-red-800",       dot: "bg-red-400"     },
  RETURN_REQUESTED:      { label: "Return Requested",   color: "bg-orange-100 text-orange-800", dot: "bg-orange-500"  },
  REFUND_INITIATED:      { label: "Refund Initiated",   color: "bg-orange-100 text-orange-800", dot: "bg-orange-500"  },
  REFUND_COMPLETED:      { label: "Refunded",           color: "bg-green-100 text-green-800",   dot: "bg-green-500"   },
}

export default function OrderListPage() {
  const { data: orders, isLoading } = useOrders()

  return (
    <div className="min-h-screen bg-slate-50">

      {/* ── Page header ──────────────────────────────────────────── */}
      <div className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-5">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-xl font-extrabold text-slate-800">My Orders</h1>
              <p className="text-xs text-slate-500 mt-0.5">Track all your deliveries in one place</p>
            </div>
            {!isLoading && (orders?.length ?? 0) > 0 && (
              <span className="text-xs font-semibold text-slate-500 bg-slate-100 px-3 py-1.5 rounded-full">
                {orders!.length} order{orders!.length !== 1 ? "s" : ""}
              </span>
            )}
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-6">

        {isLoading && (
          <div className="space-y-3">
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="h-24 bg-white border rounded-2xl animate-pulse" />
            ))}
          </div>
        )}

        {!isLoading && (orders?.length ?? 0) === 0 && (
          <div className="flex flex-col items-center py-20 gap-4 text-slate-400">
            <div className="w-16 h-16 rounded-2xl bg-white border flex items-center justify-center">
              <svg className="w-8 h-8 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 18.75a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h6m-9 0H3.375a1.125 1.125 0 01-1.125-1.125V14.25m17.25 4.5a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h1.125c.621 0 1.129-.504 1.09-1.124a17.902 17.902 0 00-3.213-9.193 2.056 2.056 0 00-1.58-.86H14.25M16.5 18.75h-2.25m0-11.177v-.958c0-.568-.422-1.048-.987-1.106a48.554 48.554 0 00-10.026 0 1.106 1.106 0 00-.987 1.106v7.635m12-6.677v6.677m0 4.5v-4.5m0 0h-12" />
              </svg>
            </div>
            <div className="text-center">
              <p className="font-semibold text-slate-600">No orders yet</p>
              <p className="text-sm text-slate-400 mt-1">Your order history will appear here</p>
            </div>
            <Link
              to="/catalog"
              className="inline-flex items-center gap-2 px-5 py-2.5 bg-green-600 hover:bg-green-700 text-white font-semibold rounded-xl text-sm transition-colors shadow-sm"
            >
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
              </svg>
              Browse Medicines
            </Link>
          </div>
        )}

        {!isLoading && (orders?.length ?? 0) > 0 && (
          <div className="bg-white border rounded-2xl overflow-hidden divide-y divide-slate-100">
            {orders?.map((order: OrderResponse) => {
              const meta = STATUS_META[order.status as OrderStatus] ?? {
                label: order.status ?? "",
                color: "bg-slate-100 text-slate-700",
                dot: "bg-slate-400",
              }
              return (
                <Link
                  key={order.id}
                  to={`/orders/${order.id}`}
                  className="flex items-center gap-4 px-5 py-4 hover:bg-slate-50 transition-colors group"
                >
                  <div className="w-10 h-10 rounded-xl bg-green-50 border border-green-100 flex items-center justify-center shrink-0">
                    <svg className="w-5 h-5 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 18.75a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h6m-9 0H3.375a1.125 1.125 0 01-1.125-1.125V14.25m17.25 4.5a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h1.125c.621 0 1.129-.504 1.09-1.124a17.902 17.902 0 00-3.213-9.193 2.056 2.056 0 00-1.58-.86H14.25M16.5 18.75h-2.25m0-11.177v-.958c0-.568-.422-1.048-.987-1.106a48.554 48.554 0 00-10.026 0 1.106 1.106 0 00-.987 1.106v7.635m12-6.677v6.677m0 4.5v-4.5m0 0h-12" />
                    </svg>
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-bold text-slate-800 text-sm">Order #{order.id}</p>
                    <p className="text-xs text-slate-500 mt-0.5">
                      {order.items?.length ?? 0} item{(order.items?.length ?? 0) !== 1 ? "s" : ""}
                      {" · "}₹{(order.finalAmount ?? order.totalAmount ?? 0).toFixed(2)}
                    </p>
                    {order.createdAt && (
                      <p className="text-[11px] text-slate-400 mt-0.5">
                        {new Date(order.createdAt).toLocaleDateString("en-IN", { day: "numeric", month: "short", year: "numeric" })}
                      </p>
                    )}
                  </div>
                  <div className="flex items-center gap-3 shrink-0">
                    <span className={`text-[11px] px-2.5 py-1 rounded-full font-bold ${meta.color}`}>
                      {meta.label}
                    </span>
                    <svg className="w-4 h-4 text-slate-300 group-hover:text-slate-500 transition-colors" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
                    </svg>
                  </div>
                </Link>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}
