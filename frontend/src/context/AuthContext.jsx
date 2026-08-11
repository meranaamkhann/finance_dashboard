import { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react'
import { authApi, usersApi } from '../services/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null)
  const [loading, setLoading] = useState(true)
  const timerRef              = useRef(null)

  const clearTimer = () => {
    if (timerRef.current) clearTimeout(timerRef.current)
  }

  const scheduleWarning = useCallback((expiresIn) => {
    clearTimer()
    const ms = (expiresIn - 300) * 1000
    if (ms > 0) {
      timerRef.current = setTimeout(() => {
        window.dispatchEvent(new CustomEvent('session-expiring'))
      }, ms)
    }
  }, [])

  const storeTokens = useCallback((data) => {
    try {
      localStorage.setItem('accessToken',    data.accessToken)
      localStorage.setItem('refreshToken',   data.refreshToken)
      localStorage.setItem('tokenExpiresAt', String(Date.now() + data.expiresIn * 1000))
      scheduleWarning(data.expiresIn)
    } catch {}
  }, [scheduleWarning])

  const clearStorage = useCallback(() => {
    try {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('tokenExpiresAt')
    } catch {}
    clearTimer()
  }, [])

  const loadUser = useCallback(async () => {
    try {
      const token     = localStorage.getItem('accessToken')
      const expiresAt = localStorage.getItem('tokenExpiresAt')

      if (!token) { setLoading(false); return }

      if (expiresAt && Date.now() > Number(expiresAt)) {
        const refresh = localStorage.getItem('refreshToken')
        if (!refresh) { clearStorage(); setLoading(false); return }
        try {
          const { data } = await authApi.refresh(refresh)
          storeTokens(data.data)
        } catch {
          clearStorage(); setLoading(false); return
        }
      }

      const { data } = await usersApi.getMe()
        const u = data.data
        setUser({
          ...u,
          onTrial:       u.onTrial       ?? false,
          trialDaysLeft: u.trialDaysLeft ?? 0,
        })
      setUser(data.data)
      const exAt = localStorage.getItem('tokenExpiresAt')
      if (exAt) {
        scheduleWarning(Math.floor((Number(exAt) - Date.now()) / 1000))
      }
    } catch {
      clearStorage()
    } finally {
      setLoading(false)
    }
  }, [clearStorage, storeTokens, scheduleWarning])

  useEffect(() => {
    loadUser()
    return () => clearTimer()
  }, [loadUser])

  const login = async (creds) => {
    const { data } = await authApi.login(creds)
    storeTokens(data.data)
    setUser({ username: data.data.username, fullName: data.data.fullName, role: data.data.role, trialDaysLeft: data.data.trialDaysLeft, onTrial: data.data.onTrial })
    return data.data
  }

  const loginByEmail = async (creds) => {
    const { data } = await authApi.loginByEmail(creds)
    storeTokens(data.data)
    setUser({ username: data.data.username, fullName: data.data.fullName, role: data.data.role, trialDaysLeft: data.data.trialDaysLeft, onTrial: data.data.onTrial })
    return data.data
  }

  const logout = async () => {
    try {
      const refresh = localStorage.getItem('refreshToken')
      if (refresh) await authApi.logout(refresh)
    } catch {}
    clearStorage()
    setUser(null)
    window.location.href = '/'
  }

  const isAdmin   = () => user?.role === 'ADMIN'
  const isAnalyst = () => user?.role === 'ANALYST' || isAdmin()

  return (
    <AuthContext.Provider value={{ user, loading, login, loginByEmail, logout, isAdmin, isAnalyst }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}