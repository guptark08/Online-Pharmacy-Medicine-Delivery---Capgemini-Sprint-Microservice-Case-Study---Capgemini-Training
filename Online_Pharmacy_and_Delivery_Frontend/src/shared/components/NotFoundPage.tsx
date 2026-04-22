import { Link } from "react-router-dom"
import { Button } from "@/shared/ui/button"

export default function NotFoundPage() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center gap-4">
      <h1 className="text-4xl font-bold">404</h1>
      <p className="text-slate-600">Page not found</p>
      <Button asChild><Link to="/">Go home</Link></Button>
    </div>
  )
}