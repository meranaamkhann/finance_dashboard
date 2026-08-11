import { Bell, AlertTriangle } from 'lucide-react'
import { useEffect, useState } from 'react'
import { notificationsApi } from '../../services/api'
import { useNavigate } from 'react-router-dom'
import ThemeToggle from '../ui/ThemeToggle'
import { useAuth } from '../../context/AuthContext'

export default function Topbar({ title }) {
  const [unread, setUnread] = useState(0)
  const nav = useNavigate()
  const { user } = useAuth()

  useEffect(() => {
    const fetch = () =>
      notificationsApi.getUnreadCount()
        .then(r => setUnread(r.data.data.unreadCount))
        .catch(() => {})
    fetch()
    const id = setInterval(fetch, 30000)
    return () => clearInterval(id)
  }, [])

  const trialDays = user?.trialDaysLeft ?? 0
  const onTrial   = user?.onTrial

  return (
    <div>
      {onTrial && trialDays <= 14 && (
        <div className="flex items-center justify-center gap-2 px-4 py-2 text-xs font-medium text-white"
             style={{ background: trialDays <= 3 ? '#dc2626' : '#d97706' }}>
          <AlertTriangle className="w-3.5 h-3.5"/>
          {trialDays > 0
            ? `Free trial — ${trialDays} day${trialDays === 1 ? '' : 's'} remaining. Upgrade to keep full access.`
            : 'Your trial has expired. Upgrade now to continue.'}
          <button onClick={() => nav('/pricing')}
            className="ml-2 px-2 py-0.5 rounded text-xs font-semibold bg-white"
            style={{ color: trialDays <= 3 ? '#dc2626' : '#d97706' }}>
            Upgrade
          </button>
        </div>
      )}
      <header className="border-b px-6 py-3.5 flex items-center justify-between sticky top-0 z-10"
              style={{ background: 'var(--bg-card)', borderColor: 'var(--border)' }}>
        <h1 className="page-title">{title}</h1>
        <div className="flex items-center gap-2">
          <ThemeToggle/>
          <button onClick={() => nav('/notifications')}
            className="relative p-2 rounded-lg transition-colors"
            style={{ color: 'var(--text-muted)' }}>
            <Bell className="w-4 h-4"/>
            {unread > 0 && (
              <span className="absolute top-1 right-1 w-4 h-4 bg-red-500 text-white text-xs rounded-full flex items-center justify-center font-bold leading-none">
                {unread > 9 ? '9+' : unread}
              </span>
            )}
          </button>
        </div>
      </header>
    </div>
  )
}