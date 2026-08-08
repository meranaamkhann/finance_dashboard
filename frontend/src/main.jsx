import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App'
import './index.css'

class RootErrorBoundary extends React.Component {
  state = { error: null }

  static getDerivedStateFromError(err) {
    return { error: err }
  }

  render() {
    if (this.state.error) {
      return (
        <div style={{
          minHeight: '100vh', display: 'flex', flexDirection: 'column',
          alignItems: 'center', justifyContent: 'center',
          fontFamily: 'sans-serif', background: '#f8fafc', padding: 24
        }}>
          <div style={{ fontSize: 48, marginBottom: 16 }}>⚠️</div>
          <h2 style={{ color: '#1e293b', marginBottom: 8 }}>Something went wrong</h2>
          <p style={{ color: '#64748b', marginBottom: 24, textAlign: 'center', maxWidth: 400 }}>
            {this.state.error?.message || 'An unexpected error occurred'}
          </p>
          <button
            onClick={() => { this.setState({ error: null }); window.location.href = '/' }}
            style={{
              background: '#2563eb', color: '#fff', border: 'none',
              padding: '10px 24px', borderRadius: 8, cursor: 'pointer', fontSize: 14
            }}>
            Reload App
          </button>
          <details style={{ marginTop: 24, fontSize: 12, color: '#94a3b8', maxWidth: 600 }}>
            <summary style={{ cursor: 'pointer' }}>Error details</summary>
            <pre style={{ marginTop: 8, whiteSpace: 'pre-wrap' }}>
              {this.state.error?.stack}
            </pre>
          </details>
        </div>
      )
    }
    return this.props.children
  }
}

ReactDOM.createRoot(document.getElementById('root')).render(
  <RootErrorBoundary>
    <BrowserRouter>
      <App/>
    </BrowserRouter>
  </RootErrorBoundary>
)