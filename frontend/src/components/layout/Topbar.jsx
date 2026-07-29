import { Bell } from 'lucide-react'
import { useEffect, useState } from 'react'
import { notificationsApi } from '../../services/api'
import { useNavigate } from 'react-router-dom'
export default function Topbar({ title }) {
  const [unread, setUnread] = useState(0)
  const nav = useNavigate()
  useEffect(() => {
    const fetch = () => notificationsApi.getUnreadCount().then(r => setUnread(r.data.data.unreadCount)).catch(()=>{})
    fetch()
    const id = setInterval(fetch, 30000)
    return () => clearInterval(id)
  }, [])
  return (
    <header className="bg-white border-b border-surface-border px-6 py-3.5 flex items-center justify-between sticky top-0 z-10">
      <h1 className="page-title">{title}</h1>
      <button onClick={()=>nav('/notifications')} className="relative p-2 rounded-lg hover:bg-slate-100 transition-colors">
        <Bell className="w-4 h-4 text-slate-600"/>
        {unread > 0 && <span className="absolute top-1 right-1 w-4 h-4 bg-red-500 text-white text-xs rounded-full flex items-center justify-center font-bold">{unread>9?'9+':unread}</span>}
      </button>
    </header>
  )
}
