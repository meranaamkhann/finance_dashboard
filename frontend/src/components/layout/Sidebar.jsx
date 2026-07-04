import { NavLink } from 'react-router-dom'
import { LayoutDashboard, ArrowLeftRight, PiggyBank, RefreshCw, Bell, Users, ClipboardList, LogOut, TrendingUp } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'

const nav = [
  { to:'/',      icon:LayoutDashboard, label:'Dashboard', roles:['VIEWER','ANALYST','ADMIN'] },
  { to:'/records',icon:ArrowLeftRight, label:'Records',   roles:['VIEWER','ANALYST','ADMIN'] },
  { to:'/budgets',icon:PiggyBank,      label:'Budgets',   roles:['ANALYST','ADMIN'] },
  { to:'/recurring',icon:RefreshCw,   label:'Recurring',  roles:['ANALYST','ADMIN'] },
  { to:'/notifications',icon:Bell,    label:'Alerts',     roles:['VIEWER','ANALYST','ADMIN'] },
  { to:'/analytics',icon:TrendingUp,  label:'Analytics',  roles:['ANALYST','ADMIN'] },
  { to:'/users',  icon:Users,          label:'Users',      roles:['ADMIN'] },
  { to:'/audit',  icon:ClipboardList,  label:'Audit',      roles:['ADMIN'] },
]

export default function Sidebar() {
  const { user, logout } = useAuth()
  const visible = nav.filter(n => n.roles.includes(user?.role))

  return (
    <aside className="w-56 shrink-0 bg-white border-r border-surface-border flex flex-col h-screen sticky top-0">
      {/* Logo */}
      <div className="px-5 py-5 border-b border-surface-border">
        <div className="flex items-center gap-2.5">
          <div className="w-7 h-7 rounded-lg bg-brand-600 flex items-center justify-center">
            <TrendingUp className="w-4 h-4 text-white"/>
          </div>
          <span className="text-sm font-bold text-slate-900">FinancePro</span>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 px-3 py-4 flex flex-col gap-0.5 overflow-y-auto">
        {visible.map(item => (
          <NavLink key={item.to} to={item.to} end={item.to==='/'} className={({isActive}) =>
            `flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
              isActive ? 'bg-brand-50 text-brand-700' : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
            }`}>
            <item.icon className="w-4 h-4 shrink-0"/>
            {item.label}
          </NavLink>
        ))}
      </nav>

      {/* User */}
      <div className="px-3 py-3 border-t border-surface-border">
        <div className="flex items-center gap-3 px-3 py-2 mb-1">
          <div className="w-7 h-7 rounded-full bg-brand-100 flex items-center justify-center text-xs font-bold text-brand-700">
            {user?.fullName?.[0]?.toUpperCase() ?? 'U'}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-xs font-semibold text-slate-900 truncate">{user?.fullName}</p>
            <p className="text-xs text-slate-400 truncate">{user?.role}</p>
          </div>
        </div>
        <button onClick={logout} className="flex items-center gap-3 w-full px-3 py-2 text-sm font-medium text-slate-600 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors">
          <LogOut className="w-4 h-4"/>Logout
        </button>
      </div>
    </aside>
  )
}
