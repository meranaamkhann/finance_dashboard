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
import Spinner from './components/ui/Spinner'

function Guard({ children, analyst, admin }) {
  const { user, loading, isAdmin, isAnalyst } = useAuth()
  if (loading) return <div className="min-h-screen flex items-center justify-center"><Spinner size="lg"/></div>
  if (!user)                    return <Navigate to="/login" replace/>
  if (admin   && !isAdmin())    return <Navigate to="/" replace/>
  if (analyst && !isAnalyst())  return <Navigate to="/" replace/>
  return children
}

function AppRoutes() {
  const { user, loading } = useAuth()
  if (loading) return <div className="min-h-screen flex items-center justify-center"><Spinner size="lg"/></div>
  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" replace/> : <LoginPage/>}/>
      <Route path="/*" element={
        <Guard>
          <AppLayout>
            <Routes>
              <Route path="/"              element={<DashboardPage/>}/>
              <Route path="/records"       element={<RecordsPage/>}/>
              <Route path="/notifications" element={<NotificationsPage/>}/>
              <Route path="/budgets"       element={<Guard analyst><BudgetsPage/></Guard>}/>
              <Route path="/recurring"     element={<Guard analyst><RecurringPage/></Guard>}/>
              <Route path="/analytics"     element={<Guard analyst><AnalyticsPage/></Guard>}/>
              <Route path="/users"         element={<Guard admin><UsersPage/></Guard>}/>
              <Route path="/audit"         element={<Guard admin><AuditPage/></Guard>}/>
              <Route path="*"              element={<Navigate to="/" replace/>}/>
            </Routes>
          </AppLayout>
        </Guard>
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
