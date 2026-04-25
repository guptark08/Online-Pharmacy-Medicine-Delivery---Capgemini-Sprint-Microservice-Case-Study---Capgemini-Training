import { useState } from "react"
import { useAdminMedicines } from "../api/useAdminMedicines"
import { useAdminCategories } from "../api/useAdminCategories"
import { useAddMedicine } from "../api/useAddMedicine"
import { useUpdateMedicine } from "../api/useUpdateMedicine"
import { useDeleteMedicine } from "../api/useDeleteMedicine"
import { useUpdateStock } from "../api/useUpdateStock"
import type { components } from "@/shared/types/api/admin"

type MedicineResponseDto = components["schemas"]["MedicineResponseDto"]
type MedicineRequestDto  = components["schemas"]["MedicineRequestDto"]

const INPUT_CLS =
  "w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-green-500 bg-white"

const EMPTY_FORM: Partial<MedicineRequestDto> = {
  name: "",
  genericName: "",
  manufacturer: "",
  dosageForm: "",
  strength: "",
  price: 0,
  mrp: 0,
  stock: 0,
  requiresPrescription: false,
  categoryId: 0,
  imageUrl: "",
  description: "",
  expiryDate: "",
  sku: "",
}

function MedicineFormModal({
  initial,
  categories,
  onClose,
}: {
  initial?: MedicineResponseDto | null
  categories: components["schemas"]["CategoryResponseDto"][]
  onClose: () => void
}) {
  const [form, setForm] = useState<Partial<MedicineRequestDto>>(
    initial
      ? {
          name:                 initial.name,
          genericName:          initial.genericName,
          manufacturer:         initial.manufacturer,
          dosageForm:           initial.dosageForm,
          strength:             initial.strength,
          price:                initial.price,
          mrp:                  initial.mrp,
          stock:                initial.stock,
          requiresPrescription: initial.requiresPrescription,
          categoryId:           initial.categoryId,
          imageUrl:             initial.imageUrl,
          description:          initial.description,
          expiryDate:           initial.expiryDate,
          sku:                  initial.sku,
        }
      : EMPTY_FORM
  )

  const addMedicine    = useAddMedicine()
  const updateMedicine = useUpdateMedicine()

  const isPending = addMedicine.isPending || updateMedicine.isPending
  const isError   = addMedicine.isError   || updateMedicine.isError

  const handleSubmit = () => {
    const payload = form as MedicineRequestDto
    if (initial?.id) {
      updateMedicine.mutate({ id: initial.id, ...payload }, { onSuccess: onClose })
    } else {
      addMedicine.mutate(payload, { onSuccess: onClose })
    }
  }

  const set = (key: keyof MedicineRequestDto, value: unknown) =>
    setForm((prev) => ({ ...prev, [key]: value }))

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4 overflow-y-auto">
      <div role="dialog" aria-modal="true" aria-label={initial ? "Edit Medicine" : "Add Medicine"} className="bg-white rounded-2xl shadow-xl w-full max-w-2xl my-4 p-6 space-y-4">
        <h2 className="text-lg font-bold text-slate-800">
          {initial ? "Edit Medicine" : "Add Medicine"}
        </h2>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">Name *</label>
            <input value={form.name ?? ""} onChange={(e) => set("name", e.target.value)} className={INPUT_CLS} />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">Generic Name</label>
            <input value={form.genericName ?? ""} onChange={(e) => set("genericName", e.target.value)} className={INPUT_CLS} />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">Manufacturer</label>
            <input value={form.manufacturer ?? ""} onChange={(e) => set("manufacturer", e.target.value)} className={INPUT_CLS} />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">Dosage Form</label>
            <input value={form.dosageForm ?? ""} onChange={(e) => set("dosageForm", e.target.value)} placeholder="Tablet, Syrup…" className={INPUT_CLS} />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">Strength</label>
            <input value={form.strength ?? ""} onChange={(e) => set("strength", e.target.value)} placeholder="500mg" className={INPUT_CLS} />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">SKU</label>
            <input value={form.sku ?? ""} onChange={(e) => set("sku", e.target.value)} className={INPUT_CLS} />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">Price *</label>
            <input type="number" min={0} value={form.price ?? ""} onChange={(e) => set("price", parseFloat(e.target.value))} className={INPUT_CLS} />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">MRP</label>
            <input type="number" min={0} value={form.mrp ?? ""} onChange={(e) => set("mrp", parseFloat(e.target.value))} className={INPUT_CLS} />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">Stock *</label>
            <input type="number" min={0} value={form.stock ?? ""} onChange={(e) => set("stock", parseInt(e.target.value))} className={INPUT_CLS} />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">Category *</label>
            <select value={form.categoryId ?? ""} onChange={(e) => set("categoryId", parseInt(e.target.value))} className={INPUT_CLS}>
              <option value="">Select category…</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">Expiry Date *</label>
            <input type="date" value={form.expiryDate ?? ""} onChange={(e) => set("expiryDate", e.target.value)} className={INPUT_CLS} />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">Image URL</label>
            <input value={form.imageUrl ?? ""} onChange={(e) => set("imageUrl", e.target.value)} className={INPUT_CLS} />
          </div>
          <div className="sm:col-span-2">
            <label className="block text-xs font-medium text-slate-600 mb-1">Description</label>
            <textarea value={form.description ?? ""} onChange={(e) => set("description", e.target.value)} rows={2} className={`${INPUT_CLS} resize-none`} />
          </div>
          <div className="flex items-center gap-2">
            <input
              id="rx-required"
              type="checkbox"
              checked={form.requiresPrescription ?? false}
              onChange={(e) => set("requiresPrescription", e.target.checked)}
              className="w-4 h-4 accent-green-600"
            />
            <label htmlFor="rx-required" className="text-sm text-slate-700">Requires Prescription</label>
          </div>
        </div>

        {isError && (
          <p className="text-xs text-red-600">Failed to save medicine. Please check the form and try again.</p>
        )}

        <div className="flex gap-2 pt-2">
          <button onClick={onClose} className="flex-1 py-2.5 border rounded-lg text-sm font-medium hover:bg-slate-50 transition-colors">
            Cancel
          </button>
          <button
            onClick={handleSubmit}
            disabled={isPending || !form.name || !form.price || !form.expiryDate || !form.categoryId}
            className="flex-1 py-2.5 bg-green-600 text-white rounded-lg text-sm font-medium hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {isPending ? "Saving…" : initial ? "Update" : "Add Medicine"}
          </button>
        </div>
      </div>
    </div>
  )
}

function StockModal({
  medicine,
  onClose,
}: {
  medicine: MedicineResponseDto
  onClose: () => void
}) {
  const [stock, setStock] = useState(medicine.stock ?? 0)
  const updateStock = useUpdateStock()

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
      <div role="dialog" aria-modal="true" aria-label={`Update Stock: ${medicine.name}`} className="bg-white rounded-2xl shadow-xl w-full max-w-sm p-6 space-y-4">
        <h2 className="font-bold text-slate-800">Update Stock: {medicine.name}</h2>
        <p className="text-sm text-slate-500">Current: {medicine.stock} units</p>
        <input
          type="number"
          min={0}
          value={stock}
          onChange={(e) => { const v = parseInt(e.target.value, 10); if (!isNaN(v)) setStock(v) }}
          className={INPUT_CLS}
        />
        <div className="flex gap-2">
          <button onClick={onClose} className="flex-1 py-2.5 border rounded-lg text-sm font-medium hover:bg-slate-50">Cancel</button>
          <button
            onClick={() => {
              if (medicine.id != null) updateStock.mutate({ id: medicine.id, stock }, { onSuccess: onClose })
            }}
            disabled={updateStock.isPending}
            className="flex-1 py-2.5 bg-green-600 text-white rounded-lg text-sm font-medium hover:bg-green-700 disabled:opacity-50"
          >
            {updateStock.isPending ? "Saving…" : "Update"}
          </button>
        </div>
      </div>
    </div>
  )
}

export default function AdminMedicinesPage() {
  const { data: medicines, isLoading } = useAdminMedicines()
  const { data: categories = [] }      = useAdminCategories()
  const deleteMedicine = useDeleteMedicine()

  const [search, setSearch]     = useState("")
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing]   = useState<MedicineResponseDto | null>(null)
  const [updatingStock, setUpdatingStock] = useState<MedicineResponseDto | null>(null)

  const filtered = (medicines ?? []).filter((m) =>
    !search ||
    m.name?.toLowerCase().includes(search.toLowerCase()) ||
    m.manufacturer?.toLowerCase().includes(search.toLowerCase())
  )

  if (isLoading) {
    return (
      <div className="p-6 space-y-3">
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="h-14 bg-slate-200 rounded-xl animate-pulse" />
        ))}
      </div>
    )
  }

  return (
    <div className="p-6 space-y-4">
      <div className="flex items-center justify-between gap-3">
        <h1 className="text-2xl font-bold text-slate-800">Medicines</h1>
        <button
          onClick={() => { setEditing(null); setShowForm(true) }}
          className="px-4 py-2 bg-green-600 text-white text-sm font-medium rounded-lg hover:bg-green-700 transition-colors"
        >
          + Add Medicine
        </button>
      </div>

      <input
        type="text"
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        placeholder="Search medicines…"
        className="w-full max-w-sm px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-green-500 bg-white"
      />

      <div className="bg-white rounded-xl border overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-slate-50 border-b">
            <tr>
              <th className="px-4 py-3 text-left font-semibold text-slate-600">Medicine</th>
              <th className="px-4 py-3 text-left font-semibold text-slate-600 hidden md:table-cell">Category</th>
              <th className="px-4 py-3 text-right font-semibold text-slate-600">Price</th>
              <th className="px-4 py-3 text-right font-semibold text-slate-600">Stock</th>
              <th className="px-4 py-3 text-left font-semibold text-slate-600 hidden lg:table-cell">Status</th>
              <th className="px-4 py-3 text-right font-semibold text-slate-600">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {filtered.length === 0 && (
              <tr>
                <td colSpan={6} className="px-4 py-8 text-center text-slate-400">No medicines found.</td>
              </tr>
            )}
            {filtered.map((m) => (
              <tr key={m.id} className="hover:bg-slate-50 transition-colors">
                <td className="px-4 py-3">
                  <p className="font-medium text-slate-800">{m.name}</p>
                  <p className="text-xs text-slate-400">{m.manufacturer}</p>
                  {m.requiresPrescription && (
                    <span className="text-[10px] bg-amber-100 text-amber-700 px-1.5 py-0.5 rounded-full">Rx</span>
                  )}
                </td>
                <td className="px-4 py-3 text-slate-600 hidden md:table-cell">{m.categoryName}</td>
                <td className="px-4 py-3 text-right font-semibold text-slate-800">₹{m.price}</td>
                <td className="px-4 py-3 text-right">
                  <span className={m.stock === 0 ? "text-red-600 font-semibold" : m.stock! < 10 ? "text-amber-600 font-semibold" : "text-slate-800"}>
                    {m.stock}
                  </span>
                </td>
                <td className="px-4 py-3 hidden lg:table-cell">
                  <span className={`text-[10px] px-2 py-0.5 rounded-full font-medium ${m.active ? "bg-green-100 text-green-700" : "bg-slate-100 text-slate-500"}`}>
                    {m.active ? "Active" : "Inactive"}
                  </span>
                </td>
                <td className="px-4 py-3 text-right">
                  <div className="flex items-center justify-end gap-1">
                    <button
                      onClick={() => setUpdatingStock(m)}
                      className="px-2.5 py-1 bg-blue-50 text-blue-700 text-xs rounded-lg hover:bg-blue-100 transition-colors"
                    >
                      Stock
                    </button>
                    <button
                      onClick={() => { setEditing(m); setShowForm(true) }}
                      className="px-2.5 py-1 bg-slate-100 text-slate-700 text-xs rounded-lg hover:bg-slate-200 transition-colors"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => { if (m.id != null) deleteMedicine.mutate(m.id) }}
                      disabled={deleteMedicine.isPending}
                      className="px-2.5 py-1 bg-red-50 text-red-700 text-xs rounded-lg hover:bg-red-100 disabled:opacity-50 transition-colors"
                    >
                      Del
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {(showForm || editing) && (
        <MedicineFormModal
          initial={editing}
          categories={categories}
          onClose={() => { setShowForm(false); setEditing(null) }}
        />
      )}
      {updatingStock && (
        <StockModal
          medicine={updatingStock}
          onClose={() => setUpdatingStock(null)}
        />
      )}
    </div>
  )
}
