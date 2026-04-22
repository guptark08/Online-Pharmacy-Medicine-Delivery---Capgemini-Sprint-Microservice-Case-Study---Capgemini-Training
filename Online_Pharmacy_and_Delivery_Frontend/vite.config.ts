import { defineConfig } from "vite"
import react from "@vitejs/plugin-react"
import tailwindcss from "@tailwindcss/vite"
import path from "path" // This allows us to work with file paths

export default defineConfig({
  plugins: [
    react(), 
    tailwindcss()
  ],
  resolve: {
    alias: {
      // This maps "@" to the "src" directory
      "@": path.resolve(__dirname, "./src"),
    },
  },
})