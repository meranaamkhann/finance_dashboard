import { useEffect, useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import { authApi } from '../../services/api'

export default function SessionWarning() {
  const [show, setShow] = useState(false)
  const [extending, setExtending] = useState(false)
  const { logout } = useAuth()

  useEffect(() => {
    const handler = () => setShow(true)
    window.addEventListener('session-expiring', handler)
    return () => window.removeEventListener('session-expiring', handler)
  }, [])

  const extend = async () => {
    setExtending(true)
    const refresh = localStorage.getItem('refreshToken')
    try {
      const { data } = await authApi.refresh(refresh)
      localStorage.setItem('accessToken',   data.data.accessToken)
      localStorage.setItem('refreshToken',  data.data.refreshToken)
      localStorage.setItem('tokenExpiresAt', String(Date.now() + data.data.expiresIn * 1000))
      setShow(false)
    } catch { logout() }
    finally { setExtending(false) }
  }

  if (!show) return null

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
      <div className="absolute inset-0 bg-black/50 backdrop-blur-sm"/>
      <div className="relative card p-6 max-w-sm w-full text-center animate-slide-up">
        <div className="w-12 h-12 rounded-full bg-yellow-100 dark:bg-yellow-900 flex items-center justify-center mx-auto mb-4">
          <span className="text-2xl">⏱</span>
        </div>
        <h3 className="text-base font-semibold mb-1" style={{ color: 'var(--text-main)' }}>
          Session Expiring
        </h3>
        <p className="text-sm mb-5" style={{ color: 'var(--text-muted)' }}>
          Your session will expire in 5 minutes. Stay logged in?
        </p>
        <div className="flex gap-3">
          <button onClick={logout} className="btn-secondary flex-1">Log Out</button>
          <button onClick={extend} disabled={extending} className="btn-primary flex-1 justify-center">
            {extending ? 'Extending…' : 'Stay Logged In'}
          </button>
        </div>
      </div>
    </div>
  )
}
