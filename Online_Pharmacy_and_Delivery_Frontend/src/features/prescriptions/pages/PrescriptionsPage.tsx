import { useEffect, useRef, useState } from "react"
import { useMyPrescriptions } from "../api/useMyPrescriptions"
import { useUploadPrescription } from "../api/useUploadPrescription"
import { api } from "@/shared/api/client"
import type { components } from "@/shared/types/api/catalog"

type PrescriptionResponseDTO = components["schemas"]["PrescriptionResponseDTO"]

const STATUS_STYLES: Record<string, { color: string; bg: string; border: string }> = {
  PENDING:  { color: "text-yellow-800", bg: "bg-yellow-50", border: "border-yellow-200" },
  APPROVED: { color: "text-green-800",  bg: "bg-green-50",  border: "border-green-200"  },
  REJECTED: { color: "text-red-800",    bg: "bg-red-50",    border: "border-red-200"    },
}

const IMAGE_TYPES = ["PNG", "JPG", "JPEG", "WEBP", "GIF"]
function isImageType(fileType: string | null | undefined) {
  return IMAGE_TYPES.includes((fileType ?? "").toUpperCase())
}

function PrescriptionPreview({ rx }: { rx: PrescriptionResponseDTO }) {
  const [open, setOpen]       = useState(false)
  const [blobUrl, setBlobUrl] = useState<string | null>(null)
  const [error, setError]     = useState(false)

  useEffect(() => {
    if (!open || !rx.fileUrl) return
    let url: string | null = null
    api.get(rx.fileUrl, { responseType: "blob" })
      .then((res) => { url = URL.createObjectURL(res.data); setBlobUrl(url) })
      .catch(() => setError(true))
    return () => { if (url) URL.revokeObjectURL(url) }
  }, [open, rx.fileUrl])

  if (!rx.fileUrl) return null

  return (
    <div>
      <button
        onClick={() => setOpen((v) => !v)}
        className="inline-flex items-center gap-1 text-xs text-green-600 hover:text-green-800 font-semibold transition-colors"
      >
        <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
          <path strokeLinecap="round" strokeLinejoin="round" d={open ? "M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" : "M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z M15 12a3 3 0 11-6 0 3 3 0 016 0z"} />
        </svg>
        {open ? "Hide Preview" : "View Prescription"}
      </button>

      {open && (
        <div className="mt-3 border rounded-2xl bg-slate-50 overflow-hidden">
          {error ? (
            <p className="text-xs text-slate-400 text-center py-6">Unable to load file.</p>
          ) : !blobUrl ? (
            <div className="py-8 flex items-center justify-center">
              <div className="w-5 h-5 border-2 border-green-600 border-t-transparent rounded-full animate-spin" />
            </div>
          ) : isImageType(rx.fileType) ? (
            <div className="space-y-2 p-3">
              <img src={blobUrl} alt={rx.fileName ?? "Prescription"} className="max-h-72 mx-auto rounded-xl object-contain" />
              <div className="text-center">
                <button onClick={() => window.open(blobUrl, "_blank")} className="text-xs text-green-600 hover:text-green-800 font-semibold">
                  Open full size ↗
                </button>
              </div>
            </div>
          ) : (
            <div className="text-center py-8 space-y-2">
              <p className="text-sm text-slate-500">{rx.fileType ?? "Document"}</p>
              <button onClick={() => window.open(blobUrl, "_blank")} className="text-sm text-green-600 hover:text-green-800 font-semibold underline">
                Open / Download ↗
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  )
}

export default function PrescriptionsPage() {
  const { data: prescriptions, isLoading } = useMyPrescriptions()
  const upload     = useUploadPrescription()
  const fileInputRef = useRef<HTMLInputElement>(null)

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    upload.mutate(file)
    e.target.value = ""
  }

  return (
    <div className="min-h-screen bg-slate-50">

      {/* ── Page header ──────────────────────────────────────────── */}
      <div className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-5">
          <div className="flex items-center justify-between flex-wrap gap-3">
            <div>
              <h1 className="text-xl font-extrabold text-slate-800">My Prescriptions</h1>
              <p className="text-xs text-slate-500 mt-0.5">Upload and track your prescription approvals</p>
            </div>
            <button
              onClick={() => fileInputRef.current?.click()}
              disabled={upload.isPending}
              className="inline-flex items-center gap-2 px-4 py-2.5 bg-green-600 hover:bg-green-700 text-white text-sm font-semibold rounded-xl disabled:opacity-50 transition-colors shadow-sm"
            >
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
              </svg>
              {upload.isPending ? "Uploading…" : "Upload Prescription"}
            </button>
            <input ref={fileInputRef} type="file" accept="image/*,.pdf" onChange={handleFileChange} className="hidden" />
          </div>
        </div>
      </div>

      <div className="max-w-3xl mx-auto px-4 sm:px-6 py-6 space-y-5">

        {/* Feedback messages */}
        {upload.isSuccess && (
          <div className="bg-green-50 border border-green-200 rounded-2xl p-4 flex items-center gap-3">
            <div className="w-8 h-8 rounded-xl bg-green-100 flex items-center justify-center shrink-0">
              <svg className="w-4 h-4 text-green-700" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <p className="text-sm font-semibold text-green-800">Prescription uploaded — a pharmacist will review it shortly.</p>
          </div>
        )}
        {upload.isError && (
          <div className="bg-red-50 border border-red-200 rounded-2xl p-4 flex items-center gap-3">
            <div className="w-8 h-8 rounded-xl bg-red-100 flex items-center justify-center shrink-0">
              <svg className="w-4 h-4 text-red-700" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z" />
              </svg>
            </div>
            <p className="text-sm font-semibold text-red-700">Upload failed. Please try a valid image or PDF (max 10 MB).</p>
          </div>
        )}

        {/* How it works */}
        <div className="bg-blue-50 border border-blue-200 rounded-2xl p-5">
          <p className="text-sm font-bold text-blue-800 mb-3">How it works</p>
          <ol className="space-y-2">
            {[
              "Upload a clear photo or PDF of your prescription.",
              "A pharmacist reviews it — usually within a few hours.",
              "Once Approved, you can proceed to checkout for Rx medicines.",
            ].map((step, i) => (
              <li key={i} className="flex items-start gap-3 text-sm text-blue-700">
                <span className="w-5 h-5 rounded-full bg-blue-200 text-blue-800 text-[11px] font-extrabold flex items-center justify-center shrink-0 mt-0.5">
                  {i + 1}
                </span>
                {step}
              </li>
            ))}
          </ol>
        </div>

        {/* Loading */}
        {isLoading && (
          <div className="space-y-3">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="h-24 bg-white border rounded-2xl animate-pulse" />
            ))}
          </div>
        )}

        {/* Empty state */}
        {!isLoading && (prescriptions?.length ?? 0) === 0 && (
          <div className="flex flex-col items-center py-16 gap-4 text-slate-400">
            <div className="w-16 h-16 rounded-2xl bg-white border flex items-center justify-center">
              <svg className="w-8 h-8 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
              </svg>
            </div>
            <div className="text-center">
              <p className="font-semibold text-slate-600">No prescriptions yet</p>
              <p className="text-sm text-slate-400 mt-1 max-w-xs">Upload your doctor's prescription to order medicines that require one.</p>
            </div>
          </div>
        )}

        {/* Prescriptions list */}
        {!isLoading && (prescriptions?.length ?? 0) > 0 && (
          <div className="bg-white border rounded-2xl overflow-hidden divide-y divide-slate-100">
            {prescriptions?.map((rx: PrescriptionResponseDTO) => {
              const s = STATUS_STYLES[rx.status ?? ""] ?? { color: "text-slate-700", bg: "bg-slate-50", border: "border-slate-200" }
              return (
                <div key={rx.id} className="p-5 space-y-3">
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="w-9 h-9 rounded-xl bg-slate-50 border flex items-center justify-center shrink-0">
                        <svg className="w-4.5 h-4.5 text-slate-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                          <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
                        </svg>
                      </div>
                      <div className="min-w-0">
                        <p className="font-bold text-slate-800 text-sm truncate">
                          {rx.fileName ?? `Prescription #${rx.id}`}
                        </p>
                        <div className="flex items-center gap-2 mt-0.5 flex-wrap">
                          {rx.uploadedAt && (
                            <p className="text-[11px] text-slate-400">
                              {new Date(rx.uploadedAt).toLocaleDateString("en-IN", { day: "numeric", month: "short", year: "numeric" })}
                            </p>
                          )}
                          {rx.fileType && <p className="text-[11px] text-slate-400">{rx.fileType}</p>}
                        </div>
                      </div>
                    </div>
                    <span className={`text-[11px] px-2.5 py-1 rounded-full font-bold border shrink-0 ${s.color} ${s.bg} ${s.border}`}>
                      {rx.status}
                    </span>
                  </div>

                  {rx.status === "REJECTED" && rx.reviewNotes && (
                    <div className="bg-red-50 border border-red-200 rounded-xl px-4 py-3 text-xs text-red-700">
                      <span className="font-bold">Rejection reason:</span> {rx.reviewNotes}
                    </div>
                  )}

                  {rx.status === "APPROVED" && (
                    <div className="bg-green-50 rounded-xl px-4 py-3 text-xs text-green-700 flex items-center gap-2">
                      <svg className="w-3.5 h-3.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      Approved — can be used for checkout.
                      {rx.reviewedAt && (
                        <span className="text-green-600 ml-1">
                          Reviewed {new Date(rx.reviewedAt).toLocaleDateString("en-IN", { day: "numeric", month: "short" })}
                        </span>
                      )}
                    </div>
                  )}

                  <PrescriptionPreview rx={rx} />
                </div>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}
