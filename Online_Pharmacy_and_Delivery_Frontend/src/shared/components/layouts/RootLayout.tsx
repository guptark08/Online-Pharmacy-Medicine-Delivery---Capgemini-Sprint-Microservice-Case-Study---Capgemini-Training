import { Outlet } from "react-router-dom"

export default function RootLayout() {
  return (
    <div className="min-h-screen bg-slate-50">
      {/* Navbar goes here later */}
      <main>
        <Outlet />
      </main>
    </div>
  )
}