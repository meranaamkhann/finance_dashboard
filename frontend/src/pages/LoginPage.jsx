import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../components/ui/Toast'
import { TrendingUp, Eye, EyeOff, Loader2 } from 'lucide-react'

export default function LoginPage() {
  const [form, setForm] = useState({ username:'', password:'' })
  const [showPw, setShowPw] = useState(false)
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()

  const submit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await login(form)
      navigate('/')
    } catch (err) {
      const msg = err.response?.data?.message ?? 'Login failed'
      toast(msg, 'error')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-slate-100 flex items-center justify-center p-4">
      <div className="w-full max-w-sm">
        {/* Brand */}
        <div className="flex flex-col items-center mb-8">
          <div className="w-12 h-12 rounded-2xl bg-brand-600 flex items-center justify-center mb-3 shadow-lg">
            <TrendingUp className="w-6 h-6 text-white"/>
          </div>
          <h1 className="text-2xl font-bold text-slate-900">FinancePro</h1>
          <p className="text-sm text-slate-500 mt-1">Sign in to your dashboard</p>
        </div>

        {/* Card */}
        <div className="card p-7">
          <form onSubmit={submit} className="space-y-4">
            <div>
              <label className="label">Username</label>
              <input className="input" placeholder="admin" value={form.username}
                onChange={e => setForm(f => ({...f, username:e.target.value}))} required autoFocus/>
            </div>
            <div>
              <label className="label">Password</label>
              <div className="relative">
                <input className="input pr-10" type={showPw?'text':'password'} placeholder="••••••••"
                  value={form.password} onChange={e => setForm(f => ({...f, password:e.target.value}))} required/>
                <button type="button" onClick={()=>setShowPw(v=>!v)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600">
                  {showPw ? <EyeOff className="w-4 h-4"/> : <Eye className="w-4 h-4"/>}
                </button>
              </div>
            </div>
            <button type="submit" disabled={loading} className="btn-primary w-full justify-center py-2.5 mt-1">
              {loading ? <><Loader2 className="w-4 h-4 animate-spin"/>Signing in…</> : 'Sign In'}
            </button>
          </form>

          {/* Dev hint */}
          <div className="mt-5 pt-4 border-t border-surface-border">
            <p className="text-xs text-slate-400 font-medium mb-2">Dev credentials</p>
            {[['admin','Admin@1234','ADMIN'],['analyst','Analyst@1234','ANALYST'],['viewer','Viewer@1234','VIEWER']].map(([u,p,r])=>(
              <button key={u} onClick={()=>setForm({username:u,password:p})}
                className="flex items-center justify-between w-full px-3 py-1.5 rounded-lg hover:bg-slate-50 transition-colors text-left mb-1">
                <span className="text-xs font-medium text-slate-700">{u}</span>
                <span className={`text-xs px-1.5 py-0.5 rounded font-medium ${r==='ADMIN'?'bg-purple-100 text-purple-700':r==='ANALYST'?'bg-blue-100 text-blue-700':'bg-slate-100 text-slate-600'}`}>{r}</span>
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
