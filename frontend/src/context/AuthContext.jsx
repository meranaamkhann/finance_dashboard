import { createContext, useContext, useState, useEffect, useCallback, useRef } from 'react'
import { authApi, usersApi } from '../services/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser]         = useState(null)
  const [loading, setLoading]   = useState(true)
  const autoLogoutRef           = useRef(null)

  const clearAutoLogout = () => {
    if (autoLogoutRef.current) clearTimeout(autoLogoutRef.current)
  }

  const scheduleAutoLogout = useCallback((expiresIn) => {
    clearAutoLogout()
    const warnAt = (expiresIn - 300) * 1000
    if (warnAt > 0) {
      autoLogoutRef.current = setTimeout(() => {
        window.dispatchEvent(new CustomEvent('session-expiring'))
      }, warnAt)
    }
  }, [])

  const storeTokens = (data) => {
    localStorage.setItem('accessToken',    data.accessToken)
    localStorage.setItem('refreshToken',   data.refreshToken)
    localStorage.setItem('tokenExpiresAt', String(Date.now() + data.expiresIn * 1000))
    scheduleAutoLogout(data.expiresIn)
  }

  const clearStorage = () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('tokenExpiresAt')
    clearAutoLogout()
  }

  const loadUser = useCallback(async () => {
    const token     = localStorage.getItem('accessToken')
    const expiresAt = localStorage.getItem('tokenExpiresAt')
    if (!token) { setLoading(false); return }
    if (expiresAt && Date.now() > Number(expiresAt)) {
      const refresh = localStorage.getItem('refreshToken')
      if (refresh) {
        try {
          const { data } = await authApi.refresh(refresh)
          storeTokens(data.data)
        } catch { clearStorage(); setLoading(false); return }
      } else { clearStorage(); setLoading(false); return }
    }
    try {
      const { data } = await usersApi.getMe()
      setUser(data.data)
      const remaining = expiresAt
        ? Math.floor((Number(expiresAt) - Date.now()) / 1000)
        : 86400
      scheduleAutoLogout(remaining)
    } catch { clearStorage() }
    finally { setLoading(false) }
  }, [scheduleAutoLogout])

  useEffect(() => { loadUser() }, [loadUser])

  const login = async (creds) => {
    const { data } = await authApi.login(creds)
    storeTokens(data.data)
    setUser({ username: data.data.username, fullName: data.data.fullName, role: data.data.role })
    return data.data
  }

  const loginByEmail = async (creds) => {
    const { data } = await authApi.loginByEmail(creds)
    storeTokens(data.data)
    setUser({ username: data.data.username, fullName: data.data.fullName, role: data.data.role })
    return data.data
  }

  const logout = async () => {
    const refresh = localStorage.getItem('refreshToken')
    try { if (refresh) await authApi.logout(refresh) } catch {}
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

export const useAuth = () => useContext(AuthContext)