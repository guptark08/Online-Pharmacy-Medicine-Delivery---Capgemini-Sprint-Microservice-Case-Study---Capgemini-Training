import { Link } from "react-router-dom"
import { useAddToCart } from "@/features/cart/api/useAddToCart"
import { useCart } from "@/features/cart/api/useCart"
import { useUpdateCartItem } from "@/features/cart/api/useUpdateCartItem"
import { useRemoveCartItem } from "@/features/cart/api/useRemoveCartItem"
import type { components } from "@/shared/types/api/catalog"

type MedicineDTO = components["schemas"]["MedicineDTO"]

interface MedicineCardProps {
  medicine: MedicineDTO
}

export function MedicineCard({ medicine }: MedicineCardProps) {
  const addToCart  = useAddToCart()
  const updateCart = useUpdateCartItem()
  const removeCart = useRemoveCartItem()
  const { data: cart } = useCart()

  const cartItem = cart?.items?.find((item) => item.medicineId === medicine.id)
  const isOutOfStock = medicine.stock === 0
  const discount =
    medicine.discountedPrice && medicine.price
      ? Math.round(((medicine.price - medicine.discountedPrice) / medicine.price) * 100)
      : null

  const handleAddToCart = (e: React.MouseEvent) => {
    e.preventDefault()
    if (!medicine.id) return
    addToCart.mutate({ medicineId: medicine.id, quantity: 1 })
  }

  const handleIncrement = (e: React.MouseEvent) => {
    e.preventDefault()
    if (!cartItem?.id || !cartItem.quantity) return
    updateCart.mutate({ itemId: cartItem.id, quantity: cartItem.quantity + 1 })
  }

  const handleDecrement = (e: React.MouseEvent) => {
    e.preventDefault()
    if (!cartItem?.id || !cartItem.quantity) return
    if (cartItem.quantity <= 1) {
      removeCart.mutate(cartItem.id)
    } else {
      updateCart.mutate({ itemId: cartItem.id, quantity: cartItem.quantity - 1 })
    }
  }

  return (
    <Link
      to={`/catalog/${medicine.id}`}
      className="group flex flex-col rounded-xl border bg-white p-4 hover:shadow-lg transition-all duration-200 hover:-translate-y-0.5"
    >
      {/* Image */}
      <div className="relative h-40 w-full mb-3">
        {medicine.imageUrl ? (
          <img
            src={medicine.imageUrl}
            alt={medicine.name}
            className="h-full w-full object-contain rounded-lg bg-slate-50"
          />
        ) : (
          <div className="h-full w-full bg-slate-100 rounded-lg flex items-center justify-center text-4xl">
            💊
          </div>
        )}
        {discount && (
          <span className="absolute top-1 right-1 bg-green-600 text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full">
            -{discount}%
          </span>
        )}
        {medicine.requiresPrescription && (
          <span className="absolute top-1 left-1 bg-amber-100 text-amber-800 text-[10px] font-bold px-1.5 py-0.5 rounded-full">
            Rx
          </span>
        )}
      </div>

      {/* Info */}
      <div className="flex-1 space-y-1 mb-3">
        <p className="font-semibold text-slate-800 truncate text-sm leading-tight">{medicine.name}</p>
        <p className="text-xs text-slate-500 truncate">{medicine.manufacturer}</p>
      </div>

      {/* Price */}
      <div className="mb-3">
        {medicine.discountedPrice ? (
          <div className="flex items-baseline gap-1.5">
            <span className="font-bold text-green-700">₹{medicine.discountedPrice}</span>
            <span className="text-xs text-slate-400 line-through">₹{medicine.price}</span>
          </div>
        ) : (
          <span className="font-bold text-slate-800">₹{medicine.price}</span>
        )}
      </div>

      {/* CTA — stepper if in cart, button otherwise */}
      {cartItem ? (
        <div
          onClick={(e) => e.preventDefault()}
          className="flex items-center justify-between w-full border border-green-600 rounded-lg overflow-hidden"
        >
          <button
            onClick={handleDecrement}
            disabled={removeCart.isPending || updateCart.isPending}
            className="flex-1 py-1.5 text-lg font-bold text-green-700 hover:bg-green-50 disabled:opacity-40 transition-colors"
          >
            −
          </button>
          <span className="px-3 font-semibold text-sm text-slate-800">{cartItem.quantity}</span>
          <button
            onClick={handleIncrement}
            disabled={updateCart.isPending || (cartItem.quantity ?? 0) >= 10}
            className="flex-1 py-1.5 text-lg font-bold text-green-700 hover:bg-green-50 disabled:opacity-40 transition-colors"
          >
            +
          </button>
        </div>
      ) : (
        <button
          onClick={handleAddToCart}
          disabled={addToCart.isPending || isOutOfStock}
          className="w-full py-1.5 text-sm font-medium rounded-lg transition-colors
            bg-green-600 text-white hover:bg-green-700
            disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isOutOfStock ? "Out of Stock" : addToCart.isPending ? "Adding…" : "Add to Cart"}
        </button>
      )}
    </Link>
  )
}
