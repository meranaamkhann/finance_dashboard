import { useEffect, useState } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import api from '../services/api'
import { TrendingUp, CheckCircle, XCircle, Loader2 } from 'lucide-react'

export default function VerifyEmailPage() {
  const [params] = useSearchParams()
  const token = params.get('token')
  const nav   = useNavigate()
  const [status, setStatus] = useState('loading')

  useEffect(() => {
    if (!token) { setStatus('error'); return }
    api.get(`/auth/verify-email?token=${token}`)
      .then(() => setStatus('success'))
      .catch(() => setStatus('error'))
  }, [token])

  return (
    <div className="min-h-screen flex items-center justify-center p-4"
         style={{ background: 'var(--bg-page)' }}>
      <div className="w-full max-w-sm text-center">
        <button onClick={() => nav('/')} className="inline-block mb-8">
          <div className="w-12 h-12 rounded-2xl flex items-center justify-center mx-auto shadow-lg"
               style={{ background: 'var(--brand)' }}>
            <TrendingUp className="w-6 h-6 text-white"/>
          </div>
        </button>
        <div className="card p-8">
          {status === 'loading' && (
            <>
              <Loader2 className="w-12 h-12 animate-spin mx-auto mb-4 text-blue-500"/>
              <p className="font-medium" style={{ color: 'var(--text-main)' }}>Verifying your email…</p>
            </>
          )}
          {status === 'success' && (
            <>
              <CheckCircle className="w-12 h-12 mx-auto mb-4 text-green-500"/>
              <h2 className="text-xl font-bold mb-2" style={{ color: 'var(--text-main)' }}>
                Email Verified!
              </h2>
              <p className="text-sm mb-6" style={{ color: 'var(--text-muted)' }}>
                Your account is now fully active. Welcome to FinancePro!
              </p>
              <button onClick={() => nav('/dashboard')} className="btn-primary w-full justify-center">
                Go to Dashboard
              </button>
            </>
          )}
          {status === 'error' && (
            <>
              <XCircle className="w-12 h-12 mx-auto mb-4 text-red-500"/>
              <h2 className="text-xl font-bold mb-2" style={{ color: 'var(--text-main)' }}>
                Verification Failed
              </h2>
              <p className="text-sm mb-6" style={{ color: 'var(--text-muted)' }}>
                This link is invalid or expired. Request a new one.
              </p>
              <button onClick={() => nav('/login')} className="btn-primary w-full justify-center">
                Back to Login
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  )
}