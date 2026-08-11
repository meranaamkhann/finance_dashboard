import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../components/ui/Toast'
import ThemeToggle from '../components/ui/ThemeToggle'
import { TrendingUp, Eye, EyeOff, Loader2 } from 'lucide-react'
import api from '../services/api'

export default function RegisterPage() {
  const [form, setForm] = useState({
    username:  '',
    email:     '',
    fullName:  '',
    password:  '',
    confirm:   '',
  })
  const [showPw, setShowPw]   = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError]     = useState('')
  const { login }             = useAuth()
  const toast                 = useToast()
  const nav                   = useNavigate()

  const strength = (pw) => {
    let s = 0
    if (pw.length >= 8)          s++
    if (/[A-Z]/.test(pw))        s++
    if (/[0-9]/.test(pw))        s++
    if (/[@$!%*?&#]/.test(pw))   s++
    return s
  }
  const s      = strength(form.password)
  const sLabel = ['Too weak','Weak','Fair','Good','Strong'][s]
  const sColor = ['bg-red-500','bg-orange-400','bg-yellow-400','bg-blue-400','bg-green-500'][s]

const submit = async e => {
e.preventDefault()
setError('')

if (form.password !== form.confirm) { setError('Passwords do not match'); return }
if (s < 3) { setError('Password is too weak'); return }

setLoading(true)
try {
    const { data } = await api.post('/auth/register', {
    username: form.username,
    email:    form.email,
    fullName: form.fullName,
    password: form.password,
    })
    localStorage.setItem('accessToken',    data.data.accessToken)
    localStorage.setItem('refreshToken',   data.data.refreshToken)
    localStorage.setItem('tokenExpiresAt', String(Date.now() + data.data.expiresIn * 1000))
    window.location.href = '/dashboard'
} catch (err) {
    const errors = err.response?.data?.errors
    const msg = errors
    ? Object.values(errors).join(', ')
    : err.response?.data?.message || 'Registration failed'
    setError(msg)
} finally { setLoading(false) }
}

  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-4"
         style={{ background: 'var(--bg-page)' }}>
      <div className="absolute top-4 right-4"><ThemeToggle/></div>

      <div className="w-full max-w-sm">
        <div className="flex flex-col items-center mb-8">
          <button onClick={() => nav('/')} className="mb-4 hover:scale-105 transition-transform">
            <div className="w-12 h-12 rounded-2xl flex items-center justify-center shadow-lg"
                 style={{ background: 'var(--brand)' }}>
              <TrendingUp className="w-6 h-6 text-white"/>
            </div>
          </button>
          <h1 className="text-2xl font-bold" style={{ color: 'var(--text-main)' }}>
            Create account
          </h1>
          <p className="text-sm mt-1" style={{ color: 'var(--text-muted)' }}>
            Start tracking your finances for free
          </p>
        </div>

        <div className="card p-7" style={{ background: 'var(--bg-card)' }}>
          <form onSubmit={submit} className="space-y-4">
            {error && (
              <div className="px-3 py-2.5 rounded-lg text-sm border"
                   style={{ background: 'var(--bg-page)', borderColor: '#f87171', color: '#ef4444' }}>
                {error}
              </div>
            )}

            <div>
              <label className="label">Full Name</label>
              <input className="input" placeholder="Asad Khan" required
                value={form.fullName}
                onChange={e => setForm(f => ({...f, fullName: e.target.value}))}/>
            </div>

            <div>
              <label className="label">Username</label>
              <input className="input" placeholder="asadkhan" required
                pattern="^[a-zA-Z0-9_]+$"
                title="Letters, numbers and underscores only"
                value={form.username}
                onChange={e => setForm(f => ({...f, username: e.target.value.toLowerCase()}))}/>
            </div>

            <div>
              <label className="label">Email</label>
              <input className="input" type="email" placeholder="you@example.com" required
                value={form.email}
                onChange={e => setForm(f => ({...f, email: e.target.value}))}/>
            </div>

            <div>
              <label className="label">Password</label>
              <div className="relative">
                <input className="input pr-10"
                  type={showPw ? 'text' : 'password'}
                  placeholder="Min 8 chars"
                  required minLength={8}
                  value={form.password}
                  onChange={e => setForm(f => ({...f, password: e.target.value}))}/>
                <button type="button" onClick={() => setShowPw(v => !v)}
                  className="absolute right-3 top-1/2 -translate-y-1/2"
                  style={{ color: 'var(--text-faint)' }}>
                  {showPw ? <EyeOff className="w-4 h-4"/> : <Eye className="w-4 h-4"/>}
                </button>
              </div>
              {form.password && (
                <div className="mt-2">
                  <div className="flex gap-1 mb-1">
                    {[0,1,2,3].map(i => (
                      <div key={i}
                        className={`h-1 flex-1 rounded-full transition-colors ${
                          i < s ? sColor : 'bg-slate-200 dark:bg-slate-700'
                        }`}/>
                    ))}
                  </div>
                  <p className="text-xs" style={{ color: 'var(--text-faint)' }}>{sLabel}</p>
                </div>
              )}
            </div>

            <div>
              <label className="label">Confirm Password</label>
              <input className="input"
                type={showPw ? 'text' : 'password'}
                placeholder="Repeat password"
                required
                value={form.confirm}
                onChange={e => setForm(f => ({...f, confirm: e.target.value}))}/>
              {form.confirm && form.password !== form.confirm && (
                <p className="text-xs text-red-500 mt-1">Passwords do not match</p>
              )}
            </div>

            <button type="submit" disabled={loading}
              className="btn-primary w-full justify-center py-2.5 mt-1">
              {loading
                ? <><Loader2 className="w-4 h-4 animate-spin"/>Creating account…</>
                : 'Create Account'
              }
            </button>
          </form>

          <div className="mt-5 pt-4 border-t text-center" style={{ borderColor: 'var(--border)' }}>
            <p className="text-sm" style={{ color: 'var(--text-muted)' }}>
              Already have an account?{' '}
              <Link to="/login"
                className="font-medium hover:underline"
                style={{ color: 'var(--brand)' }}>
                Sign in
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}