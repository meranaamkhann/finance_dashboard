import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../services/api'
import { TrendingUp, ArrowLeft, Loader2, Mail } from 'lucide-react'

export default function ForgotPasswordPage() {
  const [email, setEmail]     = useState('')
  const [loading, setLoading] = useState(false)
  const [sent, setSent]       = useState(false)
  const [error, setError]     = useState('')
  const nav = useNavigate()

  const submit = async e => {
    e.preventDefault(); setLoading(true); setError('')
    try { await authApi.forgotPassword({ email }); setSent(true) }
    catch { setError('Something went wrong. Please try again.') }
    finally { setLoading(false) }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-slate-50 dark:bg-slate-900">
      <div className="w-full max-w-sm">
        <div className="flex flex-col items-center mb-8">
          <button onClick={() => nav('/')} className="mb-4">
            <div className="w-12 h-12 rounded-2xl bg-blue-600 flex items-center justify-center shadow-lg">
              <TrendingUp className="w-6 h-6 text-white"/>
            </div>
          </button>
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white">Reset Password</h1>
          <p className="text-sm text-slate-500 mt-1">
            {sent ? 'Check your inbox' : 'Enter your email to get a reset link'}
          </p>
        </div>
        <div className="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 p-7 shadow-sm">
          {sent ? (
            <div className="text-center">
              <div className="w-14 h-14 rounded-full bg-green-100 flex items-center justify-center mx-auto mb-4">
                <Mail className="w-7 h-7 text-green-600"/>
              </div>
              <p className="text-sm text-slate-600 dark:text-slate-300 mb-4">
                If an account exists for <strong>{email}</strong>, a link has been sent.
                Check your spam folder too.
              </p>
              <button onClick={() => nav('/login')} className="btn-primary w-full justify-center">
                Back to Login
              </button>
            </div>
          ) : (
            <form onSubmit={submit} className="space-y-4">
              {error && (
                <div className="px-3 py-2 rounded-lg text-sm bg-red-50 text-red-700 border border-red-200">
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
                className="flex items-center justify-center gap-2 w-full text-sm text-slate-500 hover:text-slate-700">
                <ArrowLeft className="w-4 h-4"/>Back to Login
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  )
}