import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { authApi } from '../services/api'
import { TrendingUp, Loader2, Eye, EyeOff, CheckCircle } from 'lucide-react'

export default function ResetPasswordPage() {
  const [params] = useSearchParams()
  const token    = params.get('token') || ''
  const nav      = useNavigate()
  const [form, setForm]     = useState({ newPassword: '', confirmPassword: '' })
  const [showPw, setShowPw] = useState(false)
  const [loading, setLoading] = useState(false)
  const [done, setDone]     = useState(false)
  const [error, setError]   = useState('')

  useEffect(() => { if (!token) nav('/forgot-password') }, [token])

  const strength = (pw) => {
    let s = 0
    if (pw.length >= 8) s++
    if (/[A-Z]/.test(pw)) s++
    if (/[0-9]/.test(pw)) s++
    if (/[@$!%*?&#]/.test(pw)) s++
    return s
  }
  const s = strength(form.newPassword)
  const labels = ['Too weak','Weak','Fair','Good','Strong']
  const colors = ['bg-red-500','bg-orange-400','bg-yellow-400','bg-blue-400','bg-green-500']

  const submit = async e => {
    e.preventDefault(); setError(''); setLoading(true)
    try { await authApi.resetPassword({ token, ...form }); setDone(true) }
    catch (err) { setError(err.response?.data?.message || 'Reset failed. Link may have expired.') }
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
          <h1 className="text-2xl font-bold text-slate-900 dark:text-white">New Password</h1>
          <p className="text-sm text-slate-500 mt-1">Choose a strong password</p>
        </div>
        <div className="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 p-7 shadow-sm">
          {done ? (
            <div className="text-center">
              <CheckCircle className="w-12 h-12 text-green-500 mx-auto mb-4"/>
              <p className="text-sm text-slate-600 dark:text-slate-300 mb-4">
                Password changed. You have been logged out of all devices.
              </p>
              <button onClick={() => nav('/login')} className="btn-primary w-full justify-center">
                Log In
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
                <label className="label">New password</label>
                <div className="relative">
                  <input className="input pr-10" type={showPw ? 'text' : 'password'} required
                    minLength={8} placeholder="Min 8 chars"
                    value={form.newPassword} onChange={e => setForm(f => ({...f, newPassword: e.target.value}))}/>
                  <button type="button" onClick={() => setShowPw(v => !v)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400">
                    {showPw ? <EyeOff className="w-4 h-4"/> : <Eye className="w-4 h-4"/>}
                  </button>
                </div>
                {form.newPassword && (
                  <div className="mt-2">
                    <div className="flex gap-1 mb-1">
                      {[0,1,2,3].map(i => (
                        <div key={i} className={`h-1 flex-1 rounded-full transition-colors ${i < s ? colors[s] : 'bg-slate-200'}`}/>
                      ))}
                    </div>
                    <p className="text-xs text-slate-400">{labels[s]}</p>
                  </div>
                )}
              </div>
              <div>
                <label className="label">Confirm password</label>
                <input className="input" type={showPw ? 'text' : 'password'} required
                  value={form.confirmPassword} onChange={e => setForm(f => ({...f, confirmPassword: e.target.value}))}/>
                {form.confirmPassword && form.newPassword !== form.confirmPassword && (
                  <p className="text-xs text-red-500 mt-1">Passwords do not match</p>
                )}
              </div>
              <button type="submit" disabled={loading || s < 3} className="btn-primary w-full justify-center py-2.5">
                {loading ? <><Loader2 className="w-4 h-4 animate-spin"/>Saving…</> : 'Set New Password'}
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  )
}