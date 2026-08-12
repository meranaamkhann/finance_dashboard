import { NavLink, useNavigate } from 'react-router-dom'
import {
  LayoutDashboard, ArrowLeftRight, PiggyBank, RefreshCw,
  Bell, Users, ClipboardList, LogOut, TrendingUp, User, BarChart2
} from 'lucide-react'
import { useAuth } from '../../context/AuthContext'

const NAV = [
  { to: '/dashboard',     icon: LayoutDashboard, label: 'Dashboard',   roles: ['VIEWER','ANALYST','ADMIN'] },
  { to: '/records',       icon: ArrowLeftRight,  label: 'Records',     roles: ['VIEWER','ANALYST','ADMIN'] },
  { to: '/notifications', icon: Bell,            label: 'Alerts',      roles: ['VIEWER','ANALYST','ADMIN'] },
  { to: '/profile',       icon: User,            label: 'Profile',     roles: ['VIEWER','ANALYST','ADMIN'] },
  { to: '/budgets',       icon: PiggyBank,       label: 'Budgets',     roles: ['ANALYST','ADMIN'] },
  { to: '/recurring',     icon: RefreshCw,       label: 'Recurring',   roles: ['ANALYST','ADMIN'] },
  { to: '/analytics',     icon: BarChart2,       label: 'Analytics',   roles: ['ANALYST','ADMIN'] },
  { to: '/users',         icon: Users,           label: 'Members',       roles: ['ANALYST','ADMIN'] },
  { to: '/audit',         icon: ClipboardList,   label: 'Audit',       roles: ['ADMIN'] },
]

export default function Sidebar() {
  const { user, logout } = useAuth()
  const nav = useNavigate()

  return (
    <aside
      className="w-56 shrink-0 flex flex-col h-screen sticky top-0 border-r"
      style={{ background: 'var(--bg-sidebar)', borderColor: 'var(--border)' }}
    >
      <button
        onClick={() => nav('/')}
        className="px-5 py-5 border-b flex items-center gap-2.5 hover:opacity-90 transition-opacity text-left"
        style={{ borderColor: 'var(--border)' }}
      >
        <div className="w-7 h-7 rounded-lg flex items-center justify-center shrink-0"
             style={{ background: 'var(--brand)' }}>
          <TrendingUp className="w-4 h-4 text-white"/>
        </div>
        <span className="text-sm font-bold" style={{ color: 'var(--text-main)' }}>FinancePro</span>
      </button>

      <nav className="flex-1 px-3 py-4 flex flex-col gap-0.5 overflow-y-auto">
        {NAV.filter(n => n.roles.includes(user?.role)).map(item => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                isActive ? 'text-white' : ''
              }`
            }
            style={({ isActive }) => ({
              background: isActive ? 'var(--brand)' : 'transparent',
              color:      isActive ? '#fff' : 'var(--text-muted)',
            })}
          >
            <item.icon className="w-4 h-4 shrink-0"/>
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="px-3 py-3 border-t" style={{ borderColor: 'var(--border)' }}>
        <div className="flex items-center gap-3 px-3 py-2 mb-1">
          <div className="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold text-white shrink-0"
               style={{ background: 'var(--brand)' }}>
            {(user?.fullName || user?.username || 'U')[0].toUpperCase()}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-xs font-semibold truncate" style={{ color: 'var(--text-main)' }}>
              {user?.fullName || user?.username}
            </p>
            <p className="text-xs" style={{ color: 'var(--text-faint)' }}>{user?.role}</p>
          </div>
        </div>
        <button
          onClick={logout}
          className="flex items-center gap-3 w-full px-3 py-2 text-sm font-medium rounded-lg transition-colors hover:text-red-500 hover:bg-red-50"
          style={{ color: 'var(--text-muted)' }}
        >
          <LogOut className="w-4 h-4"/>Logout
        </button>
      </div>
    </aside>
  )
}