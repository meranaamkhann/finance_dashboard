import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../components/ui/Toast'
import ThemeToggle from '../components/ui/ThemeToggle'
import { TrendingUp, Eye, EyeOff, Loader2, Mail, User } from 'lucide-react'

export default function LoginPage() {
  const [mode, setMode]     = useState('username')
  const [form, setForm]     = useState({ identifier: '', password: '' })
  const [showPw, setShowPw] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError]   = useState('')
  const { login, loginByEmail } = useAuth()
  const toast = useToast()
  const nav   = useNavigate()

  const submit = async e => {
    e.preventDefault(); setError(''); setLoading(true)
    try {
      if (mode === 'username') {
        await login({ username: form.identifier, password: form.password })
      } else {
        await loginByEmail({ email: form.identifier, password: form.password })
      }
      nav('/dashboard')
    } catch (err) {
      const msg = err.response?.data?.message ?? 'Invalid credentials'
      setError(msg)
    } finally { setLoading(false) }
  }

  const fill = (u, p) => setForm({ identifier: u, password: p })

  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-4"
         style={{ background: 'var(--bg-page)' }}>
      <div className="absolute top-4 right-4 flex items-center gap-2">
        <ThemeToggle/>
      </div>

      <div className="w-full max-w-sm">
        <div className="flex flex-col items-center mb-8">
          <button onClick={() => nav('/')} className="mb-4 hover:scale-105 transition-transform">
            <div className="w-12 h-12 rounded-2xl flex items-center justify-center shadow-lg"
                 style={{ background: 'var(--brand)' }}>
              <TrendingUp className="w-6 h-6 text-white"/>
            </div>
          </button>
          <h1 className="text-2xl font-bold" style={{ color: 'var(--text-main)' }}>Welcome back</h1>
          <p className="text-sm mt-1" style={{ color: 'var(--text-muted)' }}>Sign in to your dashboard</p>
        </div>

        <div className="card p-7" style={{ background: 'var(--bg-card)' }}>
          {/* Mode toggle */}
          <div className="flex rounded-lg p-1 mb-5" style={{ background: 'var(--bg-page)' }}>
            {[['username','Username',User],['email','Email',Mail]].map(([m,label,Icon])=>(
              <button key={m} onClick={() => { setMode(m); setForm({ identifier: '', password: '' }); setError('') }}
                className="flex-1 flex items-center justify-center gap-1.5 py-1.5 rounded-md text-sm font-medium transition-colors"
                style={{
                  background: mode===m ? 'var(--bg-card)' : 'transparent',
                  color: mode===m ? 'var(--brand)' : 'var(--text-faint)',
                  boxShadow: mode===m ? '0 1px 3px rgba(0,0,0,.1)' : 'none'
                }}>
                <Icon className="w-3.5 h-3.5"/>{label}
              </button>
            ))}
          </div>

          <form onSubmit={submit} className="space-y-4">
            {error && (
              <div className="px-3 py-2.5 rounded-lg text-sm border"
                   style={{ background: 'var(--bg-page)', borderColor: '#f87171', color: '#ef4444' }}>
                {error}
              </div>
            )}
            <div>
              <label className="label">{mode === 'username' ? 'Username' : 'Email address'}</label>
              <input
                className="input"
                type={mode === 'email' ? 'email' : 'text'}
                placeholder={mode === 'username' ? 'admin' : 'you@example.com'}
                value={form.identifier}
                onChange={e => setForm(f => ({...f, identifier: e.target.value}))}
                required autoFocus
              />
            </div>
            <div>
              <div className="flex items-center justify-between mb-1">
                <label className="label mb-0">Password</label>
                <Link to="/forgot-password"
                  className="text-xs hover:underline transition-colors"
                  style={{ color: 'var(--brand)' }}>
                  Forgot password?
                </Link>
              </div>
              <div className="relative">
                <input
                  className="input pr-10"
                  type={showPw ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={form.password}
                  onChange={e => setForm(f => ({...f, password: e.target.value}))}
                  required
                />
                <button type="button" onClick={() => setShowPw(v => !v)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 transition-colors"
                  style={{ color: 'var(--text-faint)' }}>
                  {showPw ? <EyeOff className="w-4 h-4"/> : <Eye className="w-4 h-4"/>}
                </button>
              </div>
            </div>
            <button type="submit" disabled={loading}
              className="btn-primary w-full justify-center py-2.5 mt-1">
              {loading
                ? <><Loader2 className="w-4 h-4 animate-spin"/>Signing in…</>
                : 'Sign In'
              }
            </button>
          </form>
             
             <div className="relative my-5">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t" style={{ borderColor: 'var(--border)' }}/>
            </div>
            <div className="relative flex justify-center text-xs">
              <span className="px-2 text-xs" style={{ background: 'var(--bg-card)', color: 'var(--text-faint)' }}>
                or continue with
              </span>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <a href="http://localhost:8080/oauth2/authorization/google"
              className="btn-secondary justify-center gap-2 py-2.5 no-underline">
              <svg className="w-4 h-4" viewBox="0 0 24 24">
                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
              </svg>
              Google
            </a>
            <a href="http://localhost:8080/oauth2/authorization/github"
              className="btn-secondary justify-center gap-2 py-2.5 no-underline">
              <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 0C5.374 0 0 5.373 0 12c0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23A11.509 11.509 0 0 1 12 5.803c1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576C20.566 21.797 24 17.3 24 12c0-6.627-5.373-12-12-12z"/>
              </svg>
              GitHub
            </a>
          </div>
          {/* Dev credentials */}
          <div className="mt-5 pt-4 border-t" style={{ borderColor: 'var(--border)' }}>
            <p className="text-xs font-medium mb-2" style={{ color: 'var(--text-faint)' }}>Dev credentials</p>
            {[['admin','Admin@1234','ADMIN'],['analyst','Analyst@1234','ANALYST'],['viewer','Viewer@1234','VIEWER']].map(([u,p,r])=>(
              <button key={u} onClick={() => { setMode('username'); fill(u,p) }}
                className="flex items-center justify-between w-full px-3 py-1.5 rounded-lg transition-colors mb-1 text-left"
                style={{ color: 'var(--text-main)' }}>
                <span className="text-xs font-medium">{u}</span>
                <span className={`text-xs px-1.5 py-0.5 rounded font-medium ${
                  r==='ADMIN'?'bg-purple-100 text-purple-700 dark:bg-purple-900 dark:text-purple-300'
                  :r==='ANALYST'?'bg-blue-100 text-blue-700 dark:bg-blue-900 dark:text-blue-300'
                  :'bg-slate-100 text-slate-600 dark:bg-slate-700 dark:text-slate-300'}`}>
                  {r}
                </span>
              </button>
            ))}
          </div>
        </div>

        <p className="text-center text-xs mt-4" style={{ color: 'var(--text-faint)' }}>
          By signing in you agree to our{" "}
          <button className="underline hover:opacity-80" style={{ color: 'var(--text-muted)' }}>Terms</button>
          {" & "}
          <button className="underline hover:opacity-80" style={{ color: 'var(--text-muted)' }}>Privacy Policy</button>
        </p>
      </div>
    </div>
  )
}
