import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { authApi, usersApi } from '../services/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  const loadUser = useCallback(async () => {
    const token = localStorage.getItem('accessToken')
    if (!token) { setLoading(false); return }
    try {
      const { data } = await usersApi.getMe()
      setUser(data.data)
    } catch {
      localStorage.clear()
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { loadUser() }, [loadUser])

  const login = async (credentials) => {
    const { data } = await authApi.login(credentials)
    const { accessToken, refreshToken, role, username, fullName } = data.data
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    setUser({ username, fullName, role })
    return data.data
  }

  const logout = () => {
    localStorage.clear()
    setUser(null)
    window.location.href = '/login'
  }

  const isAdmin    = () => user?.role === 'ADMIN'
  const isAnalyst  = () => user?.role === 'ANALYST' || isAdmin()
  const isViewer   = () => !!user

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, isAdmin, isAnalyst, isViewer, refreshUser: loadUser }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
