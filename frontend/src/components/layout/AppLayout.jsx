import Sidebar from './Sidebar'
import Topbar from './Topbar'
import SessionWarning from '../ui/SessionWarning'
import ErrorBoundary from '../ui/ErrorBoundary'
import { useLocation } from 'react-router-dom'

const TITLES = {
  '/dashboard': 'Dashboard', '/records': 'Records', '/budgets': 'Budgets',
  '/recurring': 'Recurring', '/analytics': 'Analytics',
  '/notifications': 'Notifications', '/users': 'Users', '/audit': 'Audit'
}

export default function AppLayout({ children }) {
  const loc = useLocation()
  return (
    <div className="flex min-h-screen" style={{ background: 'var(--bg-page)' }}>
      <Sidebar/>
      <div className="flex-1 flex flex-col min-w-0">
        <Topbar title={TITLES[loc.pathname] ?? 'Finance Dashboard'}/>
        <main className="flex-1 p-6">
          <ErrorBoundary>
            {children}
          </ErrorBoundary>
        </main>
      </div>
      <SessionWarning/>
    </div>
  )
}
