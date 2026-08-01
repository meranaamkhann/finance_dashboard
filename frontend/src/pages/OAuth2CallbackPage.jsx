import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { usersApi } from '../services/api'
import Spinner from '../components/ui/Spinner'

export default function OAuth2CallbackPage() {
  const [params] = useSearchParams()
  const nav = useNavigate()
  const { login } = useAuth()
  const [error, setError] = useState('')

  useEffect(() => {
    const token   = params.get('token')
    const refresh = params.get('refresh')
    const err     = params.get('error')

    if (err) {
      setError(decodeURIComponent(err))
      setTimeout(() => nav('/login'), 3000)
      return
    }

    if (!token || !refresh) {
      setError('Authentication failed. Missing tokens.')
      setTimeout(() => nav('/login'), 3000)
      return
    }

    localStorage.setItem('accessToken',    token)
    localStorage.setItem('refreshToken',   refresh)
    localStorage.setItem('tokenExpiresAt', String(Date.now() + 86400 * 1000))

    usersApi.getMe()
      .then(({ data }) => {
        nav('/dashboard', { replace: true })
      })
      .catch(() => {
        setError('Failed to load user profile.')
        setTimeout(() => nav('/login'), 2000)
      })
  }, [])

  return (
    <div className="min-h-screen flex flex-col items-center justify-center"
         style={{ background: 'var(--bg-page)' }}>
      {error ? (
        <div className="text-center">
          <p className="text-red-500 font-medium mb-2">{error}</p>
          <p className="text-sm" style={{ color: 'var(--text-muted)' }}>Redirecting to login…</p>
        </div>
      ) : (
        <div className="text-center">
          <Spinner size="lg" className="mx-auto mb-4"/>
          <p className="text-sm" style={{ color: 'var(--text-muted)' }}>
            Completing sign in…
          </p>
        </div>
      )}
    </div>
  )
}