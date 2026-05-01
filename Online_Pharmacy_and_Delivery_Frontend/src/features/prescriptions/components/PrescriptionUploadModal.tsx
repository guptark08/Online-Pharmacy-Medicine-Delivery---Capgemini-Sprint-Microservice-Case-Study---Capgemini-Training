import { useRef, useState } from "react"
import { useUploadPrescription } from "../api/useUploadPrescription"

interface PrescriptionUploadModalProps {
  medicineName: string
  onClose: () => void
}

export function PrescriptionUploadModal({ medicineName, onClose }: PrescriptionUploadModalProps) {
  const upload = useUploadPrescription()
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [uploaded, setUploaded] = useState(false)

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    upload.mutate(file, {
      onSuccess: () => setUploaded(true),
    })
    e.target.value = ""
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div
        role="dialog"
        aria-modal="true"
        aria-label="Upload Prescription"
        className="bg-white rounded-2xl shadow-xl w-full max-w-md p-6 space-y-4"
      >
        <div className="flex items-start gap-3">
          <span className="text-2xl">⚕</span>
          <div>
            <h2 className="font-bold text-slate-800 text-base">Prescription Required</h2>
            <p className="text-sm text-slate-500 mt-0.5">
              <span className="font-medium text-slate-700">{medicineName}</span> requires a valid prescription.
            </p>
          </div>
        </div>

        <div className="bg-blue-50 border border-blue-200 rounded-xl p-3 text-xs text-blue-700">
          Upload your prescription now, or you can do it later from{" "}
          <span className="font-semibold">My Prescriptions</span>. You'll need an approved prescription at checkout.
        </div>

        {uploaded ? (
          <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-4 flex items-center gap-3 text-sm text-yellow-800">
            <span className="text-xl">⏳</span>
            <div>
              <p className="font-semibold">Prescription uploaded — Pending review</p>
              <p className="text-xs text-yellow-600 mt-0.5">
                A pharmacist will review it shortly. You'll be notified once approved.
              </p>
            </div>
          </div>
        ) : (
          <div className="space-y-2">
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*,.pdf"
              onChange={handleFileChange}
              className="hidden"
            />
            {upload.isError && (
              <p className="text-xs text-red-600">Upload failed. Please try again with a valid image or PDF.</p>
            )}
            <button
              onClick={() => fileInputRef.current?.click()}
              disabled={upload.isPending}
              className="w-full py-2.5 bg-green-600 text-white text-sm font-medium rounded-lg hover:bg-green-700 disabled:opacity-50 transition-colors"
            >
              {upload.isPending ? "Uploading…" : "Upload Prescription"}
            </button>
          </div>
        )}

        <button
          onClick={onClose}
          className="w-full py-2 border rounded-lg text-sm text-slate-600 hover:bg-slate-50 transition-colors"
        >
          {uploaded ? "Done" : "Upload Later"}
        </button>
      </div>
    </div>
  )
}
