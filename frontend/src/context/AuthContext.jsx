import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { authApi, usersApi } from '../services/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser]     = useState(null)
  const [loading, setLoading] = useState(true)

  const loadUser = useCallback(async () => {
    if (!localStorage.getItem('accessToken')) { setLoading(false); return }
    try {
      const { data } = await usersApi.getMe()
      setUser(data.data)
    } catch { localStorage.clear() }
    finally { setLoading(false) }
  }, [])

  useEffect(() => { loadUser() }, [loadUser])

  const login = async (creds) => {
    const { data } = await authApi.login(creds)
    localStorage.setItem('accessToken',  data.data.accessToken)
    localStorage.setItem('refreshToken', data.data.refreshToken)
    setUser({ username: data.data.username, fullName: data.data.fullName, role: data.data.role })
    return data.data
  }

  const logout = () => { localStorage.clear(); setUser(null); window.location.href = '/login' }
  const isAdmin   = () => user?.role === 'ADMIN'
  const isAnalyst = () => user?.role === 'ANALYST' || isAdmin()

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, isAdmin, isAnalyst }}>
      {children}
    </AuthContext.Provider>
  )
}
export const useAuth = () => useContext(AuthContext)

