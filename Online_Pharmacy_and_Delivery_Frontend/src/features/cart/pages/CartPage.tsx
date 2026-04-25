import { Link } from "react-router-dom"
import { useCart } from "../api/useCart"
import { useUpdateCartItem } from "../api/useUpdateCartItem"
import { useRemoveCartItem } from "../api/useRemoveCartItem"
import { useClearCart } from "../api/useClearCart"
import type { components } from "@/shared/types/api/order"

type CartItemResponse = components["schemas"]["CartItemResponse"]

export default function CartPage() {
  const { data: cart, isLoading } = useCart()
  const update = useUpdateCartItem()
  const remove = useRemoveCartItem()
  const clear  = useClearCart()

  if (isLoading) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-8 space-y-3">
        {Array.from({ length: 3 }).map((_, i) => (
          <div key={i} className="h-24 bg-slate-200 rounded-xl animate-pulse" />
        ))}
      </div>
    )
  }

  const items = cart?.items ?? []

  if (items.length === 0) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-20 text-center space-y-4">
        <p className="text-6xl">🛒</p>
        <p className="text-slate-500">Your cart is empty.</p>
        <Link
          to="/catalog"
          className="inline-block px-6 py-2.5 bg-green-600 text-white font-medium rounded-lg hover:bg-green-700 transition-colors"
        >
          Browse Medicines
        </Link>
      </div>
    )
  }

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 py-8 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-800">Your Cart</h1>
        <button
          onClick={() => clear.mutate()}
          disabled={clear.isPending}
          className="text-sm text-slate-400 hover:text-red-500 transition-colors disabled:opacity-50"
        >
          Clear all
        </button>
      </div>

      {/* Items */}
      <div className="space-y-3">
        {items.map((item: CartItemResponse) => (
          <CartItem
            key={item.id}
            item={item}
            onUpdate={(qty) => item.id && update.mutate({ itemId: item.id, quantity: qty })}
            onRemove={() => item.id && remove.mutate(item.id)}
          />
        ))}
      </div>

      {/* Summary */}
      <div className="bg-white border rounded-xl p-5 space-y-3">
        {cart?.hasRxItems && (
          <div className="bg-amber-50 border border-amber-200 rounded-lg p-3 text-xs text-amber-700">
            ⚕ Your cart contains prescription medicines. You'll link a prescription at checkout.
          </div>
        )}
        <div className="flex justify-between text-sm text-slate-600">
          <span>{cart?.totalItems} item{(cart?.totalItems ?? 0) !== 1 ? "s" : ""}</span>
          <span>₹{cart?.subtotal?.toFixed(2)}</span>
        </div>
        <div className="border-t pt-3 flex justify-between font-bold text-slate-800">
          <span>Total</span>
          <span>₹{cart?.subtotal?.toFixed(2)}</span>
        </div>
        <Link
          to="/checkout"
          className="block w-full text-center py-3 bg-green-600 text-white font-semibold rounded-lg hover:bg-green-700 transition-colors"
        >
          Proceed to Checkout →
        </Link>
      </div>
    </div>
  )
}

// Extracted so the parent stays readable
function CartItem({
  item,
  onUpdate,
  onRemove,
}: {
  item: CartItemResponse
  onUpdate: (qty: number) => void
  onRemove: () => void
}) {
  return (
    <div className="flex items-center gap-4 bg-white border rounded-xl p-4">
      <div className="flex-1 min-w-0">
        <p className="font-medium text-slate-800 truncate">{item.medicineName}</p>
        <p className="text-sm text-slate-500">₹{item.unitPrice} each</p>
        {item.requiresPrescription && (
          <span className="inline-block mt-1 text-[10px] bg-amber-100 text-amber-700 px-2 py-0.5 rounded-full">
            Rx required
          </span>
        )}
      </div>

      {/* Quantity stepper */}
      <div className="flex items-center gap-2 shrink-0">
        <button
          onClick={() => item.quantity && item.quantity > 1 && onUpdate(item.quantity - 1)}
          disabled={item.quantity === 1}
          className="w-7 h-7 rounded-full border flex items-center justify-center text-slate-600 text-lg
            hover:bg-slate-50 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
        >
          −
        </button>
        <span className="w-6 text-center font-semibold text-sm">{item.quantity}</span>
        <button
          onClick={() => item.quantity && item.quantity < 10 && onUpdate(item.quantity + 1)}
          disabled={item.quantity === 10}
          className="w-7 h-7 rounded-full border flex items-center justify-center text-slate-600 text-lg
            hover:bg-slate-50 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
        >
          +
        </button>
      </div>

      <span className="w-20 text-right font-semibold text-slate-800 shrink-0">
        ₹{item.lineTotal?.toFixed(2)}
      </span>

      <button
        onClick={onRemove}
        className="shrink-0 text-slate-300 hover:text-red-500 transition-colors ml-1 text-lg leading-none"
        aria-label="Remove"
      >
        ✕
      </button>
    </div>
  )
}
