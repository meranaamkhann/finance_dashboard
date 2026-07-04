import Sidebar from './Sidebar'
import Topbar from './Topbar'
import { useLocation } from 'react-router-dom'

const titles = {
  '/': 'Dashboard', '/records': 'Records', '/budgets': 'Budgets',
  '/recurring': 'Recurring Transactions', '/notifications': 'Notifications',
  '/analytics': 'Analytics', '/users': 'User Management', '/audit': 'Audit Trail',
  '/profile': 'My Profile',
}

export default function AppLayout({ children }) {
  const loc = useLocation()
  const title = titles[loc.pathname] ?? 'Finance Dashboard'
  return (
    <div className="flex min-h-screen bg-surface-secondary">
      <Sidebar/>
      <div className="flex-1 flex flex-col min-w-0">
        <Topbar title={title}/>
        <main className="flex-1 p-6">{children}</main>
      </div>
    </div>
  )
}
