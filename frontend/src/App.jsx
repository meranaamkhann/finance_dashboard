import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import { ToastProvider } from './components/ui/Toast'
import AppLayout from './components/layout/AppLayout'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import RecordsPage from './pages/RecordsPage'
import BudgetsPage from './pages/BudgetsPage'
import RecurringPage from './pages/RecurringPage'
import NotificationsPage from './pages/NotificationsPage'
import AnalyticsPage from './pages/AnalyticsPage'
import UsersPage from './pages/UsersPage'
import AuditPage from './pages/AuditPage'
import ProfilePage from './pages/ProfilePage'
import Spinner from './components/ui/Spinner'

function ProtectedRoute({ children, requireAdmin, requireAnalyst }) {
  const { user, loading, isAdmin, isAnalyst } = useAuth()
  if (loading) return <div className="min-h-screen flex items-center justify-center"><Spinner size="lg"/></div>
  if (!user)          return <Navigate to="/login" replace/>
  if (requireAdmin   && !isAdmin())   return <Navigate to="/" replace/>
  if (requireAnalyst && !isAnalyst()) return <Navigate to="/" replace/>
  return children
}

function AppRoutes() {
  const { user, loading } = useAuth()
  if (loading) return <div className="min-h-screen flex items-center justify-center"><Spinner size="lg"/></div>

  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" replace/> : <LoginPage/>}/>
      <Route path="/*" element={
        <ProtectedRoute>
          <AppLayout>
            <Routes>
              <Route path="/"              element={<DashboardPage/>}/>
              <Route path="/records"       element={<RecordsPage/>}/>
              <Route path="/notifications" element={<NotificationsPage/>}/>
              <Route path="/budgets"       element={<ProtectedRoute requireAnalyst><BudgetsPage/></ProtectedRoute>}/>
              <Route path="/recurring"     element={<ProtectedRoute requireAnalyst><RecurringPage/></ProtectedRoute>}/>
              <Route path="/analytics"     element={<ProtectedRoute requireAnalyst><AnalyticsPage/></ProtectedRoute>}/>
              <Route path="/users"         element={<ProtectedRoute requireAdmin><UsersPage/></ProtectedRoute>}/>
              <Route path="/audit"         element={<ProtectedRoute requireAdmin><AuditPage/></ProtectedRoute>}/>
              <Route path="/profile"       element={<ProfilePage/>}/>
              <Route path="*"              element={<Navigate to="/" replace/>}/>
            </Routes>
          </AppLayout>
        </ProtectedRoute>
      }/>
    </Routes>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <ToastProvider>
        <AppRoutes/>
      </ToastProvider>
    </AuthProvider>
  )
}
