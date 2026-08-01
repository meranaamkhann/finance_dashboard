import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import { ThemeProvider } from './context/ThemeContext'
import { ToastProvider } from './components/ui/Toast'
import AppLayout from './components/layout/AppLayout'
import LandingPage from './pages/LandingPage'
import LoginPage from './pages/LoginPage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import ResetPasswordPage from './pages/ResetPasswordPage'
import DashboardPage from './pages/DashboardPage'
import RecordsPage from './pages/RecordsPage'
import BudgetsPage from './pages/BudgetsPage'
import RecurringPage from './pages/RecurringPage'
import NotificationsPage from './pages/NotificationsPage'
import AnalyticsPage from './pages/AnalyticsPage'
import UsersPage from './pages/UsersPage'
import AuditPage from './pages/AuditPage'
import Spinner from './components/ui/Spinner'
import PricingPage from './pages/PricingPage'
import OAuth2CallbackPage from './pages/OAuth2CallbackPage'

function Guard({ children, analyst = false, admin = false }) {
  const { user, loading, isAdmin, isAnalyst } = useAuth()
  if (loading) return (
    <div className="min-h-screen flex items-center justify-center" style={{ background: 'var(--bg-page)' }}>
      <Spinner size="lg"/>
    </div>
  )
  if (!user)                   return <Navigate to="/login" replace/>
  if (admin   && !isAdmin())   return <Navigate to="/dashboard" replace/>
  if (analyst && !isAnalyst()) return <Navigate to="/dashboard" replace/>
  return children
}

function AppRoutes() {
  const { user, loading } = useAuth()

  if (loading) return (
    <div className="min-h-screen flex items-center justify-center" style={{ background: 'var(--bg-page)' }}>
      <Spinner size="lg"/>
    </div>
  )

  return (
    <Routes>
      <Route path="/"                element={<LandingPage/>}/>
      <Route path="/login"           element={user ? <Navigate to="/dashboard" replace/> : <LoginPage/>}/>
      <Route path="/forgot-password" element={<ForgotPasswordPage/>}/>
      <Route path="/reset-password"  element={<ResetPasswordPage/>}/>

      <Route path="/dashboard"      element={<Guard><AppLayout><DashboardPage/></AppLayout></Guard>}/>
      <Route path="/records"        element={<Guard><AppLayout><RecordsPage/></AppLayout></Guard>}/>
      <Route path="/notifications"  element={<Guard><AppLayout><NotificationsPage/></AppLayout></Guard>}/>
      <Route path="/budgets"        element={<Guard analyst><AppLayout><BudgetsPage/></AppLayout></Guard>}/>
      <Route path="/recurring"      element={<Guard analyst><AppLayout><RecurringPage/></AppLayout></Guard>}/>
      <Route path="/analytics"      element={<Guard analyst><AppLayout><AnalyticsPage/></AppLayout></Guard>}/>
      <Route path="/users"          element={<Guard admin><AppLayout><UsersPage/></AppLayout></Guard>}/>
      <Route path="/audit"          element={<Guard admin><AppLayout><AuditPage/></AppLayout></Guard>}/>

      <Route path="/pricing"        element={<PricingPage/>}/>
      <Route path="*" element={<Navigate to="/" replace/>}/>
      <Route path="/oauth2/callback" element={<OAuth2CallbackPage/>}/>
    </Routes>
  )
}

export default function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <ToastProvider>
          <AppRoutes/>
        </ToastProvider>
      </AuthProvider>
    </ThemeProvider>
  )
}
