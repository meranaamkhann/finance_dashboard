import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../services/api'
import { TrendingUp, ArrowLeft, Loader2, Mail } from 'lucide-react'
import ThemeToggle from '../components/ui/ThemeToggle'

export default function ForgotPasswordPage() {
  const [email, setEmail]       = useState('')
  const [loading, setLoading]   = useState(false)
  const [sent, setSent]         = useState(false)
  const [error, setError]       = useState('')
  const nav = useNavigate()

  const submit = async e => {
    e.preventDefault(); setLoading(true); setError('')
    try {
      await authApi.forgotPassword({ email })
      setSent(true)
    } catch { setError('Something went wrong. Please try again.') }
    finally { setLoading(false) }
  }

  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-4"
         style={{ background: 'var(--bg-page)' }}>
      <div className="absolute top-4 right-4"><ThemeToggle/></div>
      <div className="w-full max-w-sm">
        <div className="flex flex-col items-center mb-8">
          <button onClick={() => nav('/')} className="mb-4">
            <div className="w-12 h-12 rounded-2xl flex items-center justify-center shadow-lg"
                 style={{ background: 'var(--brand)' }}>
              <TrendingUp className="w-6 h-6 text-white"/>
            </div>
          </button>
          <h1 className="text-2xl font-bold" style={{ color: 'var(--text-main)' }}>Reset Password</h1>
          <p className="text-sm mt-1" style={{ color: 'var(--text-muted)' }}>
            {sent ? 'Check your inbox' : 'Enter your email to receive a reset link'}
          </p>
        </div>
        <div className="card p-7">
          {sent ? (
            <div className="text-center">
              <div className="w-14 h-14 rounded-full bg-green-100 dark:bg-green-900 flex items-center justify-center mx-auto mb-4">
                <Mail className="w-7 h-7 text-green-600 dark:text-green-400"/>
              </div>
              <p className="text-sm mb-4" style={{ color: 'var(--text-muted)' }}>
                If an account exists for <strong>{email}</strong>, a reset link has been sent.
                Check your spam folder if you don't see it.
              </p>
              <button onClick={() => nav('/login')} className="btn-primary w-full justify-center">
                Back to Login
              </button>
            </div>
          ) : (
            <form onSubmit={submit} className="space-y-4">
              {error && (
                <div className="px-3 py-2 rounded-lg text-sm bg-red-50 dark:bg-red-900 text-red-700 dark:text-red-300">
                  {error}
                </div>
              )}
              <div>
                <label className="label">Email address</label>
                <input className="input" type="email" required placeholder="you@example.com"
                  value={email} onChange={e => setEmail(e.target.value)} autoFocus/>
              </div>
              <button type="submit" disabled={loading} className="btn-primary w-full justify-center py-2.5">
                {loading ? <><Loader2 className="w-4 h-4 animate-spin"/>Sending…</> : 'Send Reset Link'}
              </button>
              <button type="button" onClick={() => nav('/login')}
                className="flex items-center justify-center gap-2 w-full text-sm transition-colors"
                style={{ color: 'var(--text-muted)' }}>
                <ArrowLeft className="w-4 h-4"/>Back to Login
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  )
}
