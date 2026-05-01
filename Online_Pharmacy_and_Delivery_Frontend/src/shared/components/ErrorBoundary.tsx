import { Component, type ErrorInfo, type ReactNode } from "react"

interface Props {
  children: ReactNode
  fallback?: ReactNode
}

interface State {
  hasError: boolean
}

export class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false }
  
  static getDerivedStateFromError(): State {
    return { hasError: true }
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error("[ErrorBoundary]", error, info.componentStack)
  }

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) return this.props.fallback
      return (
        <div className="flex flex-col items-center justify-center py-20 gap-3 text-slate-500">
          <span className="text-4xl" aria-hidden="true">⚠️</span>
          <p className="text-sm font-medium text-slate-700">Something went wrong.</p>
          <button
            onClick={() => this.setState({ hasError: false })}
            className="text-sm text-green-600 underline hover:text-green-800"
          >
            Try again
          </button>
        </div>
      )
    }
    return this.props.children
  }
}
