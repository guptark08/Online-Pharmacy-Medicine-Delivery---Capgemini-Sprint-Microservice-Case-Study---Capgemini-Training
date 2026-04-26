import { Link } from "react-router-dom"
import { useOrders } from "../api/useOrders"
import type { components } from "@/shared/types/api/order"

type OrderResponse = components["schemas"]["OrderResponse"]
type OrderStatus   = NonNullable<OrderResponse["status"]>

const STATUS_META: Partial<Record<OrderStatus, { label: string; color: string }>> = {
  CHECKOUT_STARTED:      { label: "Processing",         color: "bg-slate-100 text-slate-700"   },
  PRESCRIPTION_PENDING:  { label: "Awaiting Rx Review", color: "bg-amber-100 text-amber-800"   },
  PRESCRIPTION_APPROVED: { label: "Rx Approved",        color: "bg-teal-100 text-teal-800"     },
  PRESCRIPTION_REJECTED: { label: "Rx Rejected",        color: "bg-rose-100 text-rose-800"     },
  PAYMENT_PENDING:       { label: "Payment Pending",    color: "bg-yellow-100 text-yellow-800" },
  PAYMENT_FAILED:        { label: "Payment Failed",     color: "bg-red-100 text-red-800"       },
  PAID:                  { label: "Paid",               color: "bg-blue-100 text-blue-800"     },
  PACKED:                { label: "Packed",             color: "bg-purple-100 text-purple-800" },
  OUT_FOR_DELIVERY:      { label: "Out for Delivery",   color: "bg-indigo-100 text-indigo-800" },
  DELIVERED:             { label: "Delivered",          color: "bg-green-100 text-green-800"   },
  CUSTOMER_CANCELLED:    { label: "Cancelled",          color: "bg-red-100 text-red-800"       },
  ADMIN_CANCELLED:       { label: "Cancelled",          color: "bg-red-100 text-red-800"       },
  RETURN_REQUESTED:      { label: "Return Requested",   color: "bg-orange-100 text-orange-800" },
  REFUND_INITIATED:      { label: "Refund Initiated",   color: "bg-orange-100 text-orange-800" },
  REFUND_COMPLETED:      { label: "Refunded",           color: "bg-green-100 text-green-800"   },
}

export default function OrderListPage() {
  const { data: orders, isLoading } = useOrders()

  if (isLoading) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-8 space-y-3">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="h-24 bg-slate-200 rounded-xl animate-pulse" />
        ))}
      </div>
    )
  }

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 py-8 space-y-4">
      <h1 className="text-2xl font-bold text-slate-800">My Orders</h1>

      {(orders?.length ?? 0) === 0 && (
        <div className="flex flex-col items-center py-20 gap-3 text-slate-400">
          <span className="text-5xl">📦</span>
          <p className="text-sm">No orders yet.</p>
          <Link
            to="/catalog"
            className="text-sm bg-green-600 text-white px-5 py-2 rounded-lg hover:bg-green-700 transition-colors"
          >
            Shop Now
          </Link>
        </div>
      )}

      {orders?.map((order: OrderResponse) => {
        const meta = STATUS_META[order.status as OrderStatus] ?? {
          label: order.status ?? "",
          color: "bg-slate-100 text-slate-700",
        }
        return (
          <Link
            key={order.id}
            to={`/orders/${order.id}`}
            className="block bg-white border rounded-xl p-4 hover:shadow-md transition-all duration-150 hover:-translate-y-0.5"
          >
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <p className="font-semibold text-slate-800">Order #{order.id}</p>
                <p className="text-sm text-slate-500 mt-0.5">
                  {order.items?.length ?? 0} item{(order.items?.length ?? 0) !== 1 ? "s" : ""}
                  {" · "}₹{(order.finalAmount ?? order.totalAmount ?? 0).toFixed(2)}
                </p>
                {order.createdAt && (
                  <p className="text-xs text-slate-400 mt-0.5">
                    {new Date(order.createdAt).toLocaleDateString()}
                  </p>
                )}
              </div>
              <span className={`shrink-0 text-xs px-3 py-1 rounded-full font-medium ${meta.color}`}>
                {meta.label}
              </span>
            </div>
          </Link>
        )
      })}
    </div>
  )
}
