import { Component } from 'react'

export default class ErrorBoundary extends Component {
  state = { hasError: false, message: '' }

  static getDerivedStateFromError(err) {
    return { hasError: true, message: err?.message || 'Unexpected error' }
  }

  render() {
    if (!this.state.hasError) return this.props.children
    return (
      <div className="min-h-[200px] flex flex-col items-center justify-center p-8 text-center">
        <div className="text-4xl mb-4">⚠️</div>
        <h3 className="text-base font-semibold mb-2" style={{ color: 'var(--text-main)' }}>
          Something went wrong
        </h3>
        <p className="text-sm mb-4" style={{ color: 'var(--text-muted)' }}>
          {this.state.message}
        </p>
        <button
          onClick={() => this.setState({ hasError: false })}
          className="btn-primary"
        >
          Try Again
        </button>
      </div>
    )
  }
}
