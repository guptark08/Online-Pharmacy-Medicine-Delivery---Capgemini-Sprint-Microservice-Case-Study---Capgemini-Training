import { useParams, useNavigate } from "react-router-dom"
import { useMedicine } from "../api/useMedicine"
import { useAddToCart } from "@/features/cart/api/useAddToCart"

export default function MedicineDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const { data: medicine, isLoading, isError } = useMedicine(Number(id))
  const addToCart = useAddToCart()

  if (isLoading) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-8 animate-pulse">
        <div className="grid md:grid-cols-2 gap-8">
          <div className="h-80 bg-slate-200 rounded-xl" />
          <div className="space-y-4">
            <div className="h-8 bg-slate-200 rounded w-2/3" />
            <div className="h-4 bg-slate-200 rounded w-1/3" />
            <div className="h-6 bg-slate-200 rounded w-1/4" />
            <div className="h-24 bg-slate-200 rounded" />
            <div className="h-10 bg-slate-200 rounded-lg" />
          </div>
        </div>
      </div>
    )
  }

  if (isError || !medicine) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-20 text-center text-slate-500 space-y-3">
        <p className="text-5xl">💊</p>
        <p>Medicine not found or unavailable.</p>
        <button
          onClick={() => navigate(-1)}
          className="text-sm text-green-600 underline hover:text-green-800"
        >
          Go back
        </button>
      </div>
    )
  }

  const discount =
    medicine.discountedPrice && medicine.price
      ? Math.round(((medicine.price - medicine.discountedPrice) / medicine.price) * 100)
      : null

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 py-8">
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-1 text-sm text-slate-500 hover:text-slate-800 mb-6 transition-colors"
      >
        ← Back
      </button>

      <div className="grid md:grid-cols-2 gap-8">
        {/* Image */}
        <div>
          {medicine.imageUrl ? (
            <img
              src={medicine.imageUrl}
              alt={medicine.name}
              className="w-full h-80 object-contain rounded-xl bg-slate-50 p-6"
              onError={(e) => {
                e.currentTarget.style.display = "none"
                e.currentTarget.nextElementSibling?.removeAttribute("style")
              }}
            />
          ) : null}
          <div
            className="w-full h-80 bg-slate-100 rounded-xl flex items-center justify-center text-7xl"
            style={medicine.imageUrl ? { display: "none" } : undefined}
          >
            💊
          </div>
        </div>

        {/* Info */}
        <div className="space-y-4">
          <div>
            <h1 className="text-2xl font-bold text-slate-800">{medicine.name}</h1>
            <p className="text-slate-500 mt-0.5">{medicine.manufacturer}</p>
            {medicine.categoryName && (
              <span className="inline-block mt-2 text-xs bg-slate-100 text-slate-600 px-2.5 py-0.5 rounded-full">
                {medicine.categoryName}
              </span>
            )}
          </div>

          {medicine.requiresPrescription && (
            <div className="flex items-center gap-2 bg-amber-50 border border-amber-200 rounded-lg p-3">
              <span className="text-amber-500 text-lg">⚕</span>
              <div>
                <p className="text-sm font-semibold text-amber-800">Prescription Required</p>
                <p className="text-xs text-amber-600">You'll need to upload a valid Rx to checkout.</p>
              </div>
            </div>
          )}

          {/* Price */}
          <div className="flex items-baseline gap-2">
            {medicine.discountedPrice ? (
              <>
                <span className="text-3xl font-bold text-green-700">₹{medicine.discountedPrice}</span>
                <span className="text-slate-400 line-through">₹{medicine.price}</span>
                <span className="text-sm font-medium text-green-600">{discount}% off</span>
              </>
            ) : (
              <span className="text-3xl font-bold text-slate-800">₹{medicine.price}</span>
            )}
          </div>

          {medicine.description && (
            <p className="text-sm text-slate-600 leading-relaxed">{medicine.description}</p>
          )}

          {medicine.dosageInfo && (
            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">Dosage</p>
              <p className="text-sm text-slate-600">{medicine.dosageInfo}</p>
            </div>
          )}

          {medicine.sideEffects && (
            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">Side Effects</p>
              <p className="text-sm text-slate-600">{medicine.sideEffects}</p>
            </div>
          )}

          <div className="flex items-center justify-between text-sm text-slate-500 pt-1">
            <span>
              {medicine.stock === 0
                ? "⚠ Out of stock"
                : `${medicine.stock} in stock`}
            </span>
            {medicine.expiryDate && <span>Expires {medicine.expiryDate}</span>}
          </div>

          <button
            onClick={() => {
              if (medicine.id != null) addToCart.mutate({ medicineId: medicine.id, quantity: 1 })
            }}
            disabled={addToCart.isPending || medicine.stock === 0}
            className="w-full py-3 font-semibold bg-green-600 text-white rounded-lg
              hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {medicine.stock === 0
              ? "Out of Stock"
              : addToCart.isPending
              ? "Adding…"
              : "Add to Cart"}
          </button>
        </div>
      </div>
    </div>
  )
}
