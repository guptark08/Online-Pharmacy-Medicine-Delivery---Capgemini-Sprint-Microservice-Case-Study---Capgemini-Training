import { Link } from "react-router-dom"
import { useAddToCart } from "@/features/cart/api/useAddToCart"
import type { components } from "@/shared/types/api/catalog"

type MedicineDTO = components["schemas"]["MedicineDTO"]

interface MedicineCardProps {
  medicine: MedicineDTO
}

export function MedicineCard({ medicine }: MedicineCardProps) {
  const addToCart = useAddToCart()

  const handleAddToCart = (e: React.MouseEvent) => {
    e.preventDefault() // don't navigate when clicking the button inside the Link
    if (!medicine.id) return
    addToCart.mutate({ medicineId: medicine.id, quantity: 1 })
  }

  const isOutOfStock = medicine.stock === 0
  const discount =
    medicine.discountedPrice && medicine.price
      ? Math.round(((medicine.price - medicine.discountedPrice) / medicine.price) * 100)
      : null

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

      {/* CTA */}
      <button
        onClick={handleAddToCart}
        disabled={addToCart.isPending || isOutOfStock}
        className="w-full py-1.5 text-sm font-medium rounded-lg transition-colors
          bg-green-600 text-white hover:bg-green-700
          disabled:opacity-50 disabled:cursor-not-allowed"
      >
        {isOutOfStock ? "Out of Stock" : addToCart.isPending ? "Adding…" : "Add to Cart"}
      </button>
    </Link>
  )
}
