import { useEffect, useState } from 'react'
import { notificationsApi } from '../services/api'
import { fmt } from '../utils/format'
import { useToast } from '../components/ui/Toast'
import Spinner from '../components/ui/Spinner'
import EmptyState from '../components/ui/EmptyState'
import { Bell, CheckCheck, Check } from 'lucide-react'

const TYPE_STYLES = {
  BUDGET_WARNING:  { bg:'bg-yellow-50 border-yellow-200', dot:'bg-yellow-400', label:'Warning' },
  BUDGET_CRITICAL: { bg:'bg-orange-50 border-orange-200', dot:'bg-orange-500', label:'Critical' },
  BUDGET_EXCEEDED: { bg:'bg-red-50 border-red-200',       dot:'bg-red-500',    label:'Exceeded' },
  RECURRING_EXECUTED: { bg:'bg-green-50 border-green-200', dot:'bg-green-500', label:'Auto-posted' },
  SYSTEM:          { bg:'bg-blue-50 border-blue-200',     dot:'bg-blue-500',   label:'System' },
}

export default function NotificationsPage() {
  const toast = useToast()
  const [notifs, setNotifs] = useState([])
  const [loading, setLoading] = useState(true)
  const [unreadOnly, setUnreadOnly] = useState(false)
  const [page, setPage] = useState(0)
  const [total, setTotal] = useState(0)

  const load = async () => {
    setLoading(true)
    try {
      const { data } = await notificationsApi.getAll({ page, size:20, unreadOnly })
      setNotifs(data.data.content); setTotal(data.data.totalElements)
    } catch { toast('Failed to load','error') }
    finally { setLoading(false) }
  }
  useEffect(() => { load() }, [page, unreadOnly])

  const markRead = async (id) => {
    try { await notificationsApi.markRead(id); load() } catch {}
  }

  const markAll = async () => {
    try { await notificationsApi.markAllRead(); toast('All marked as read','success'); load() } catch {}
  }

  if (loading) return <div className="flex items-center justify-center h-64"><Spinner size="lg"/></div>

  return (
    <div className="space-y-4">
      <div className="flex items-center gap-3 justify-between flex-wrap">
        <div className="flex items-center gap-3">
          <label className="flex items-center gap-2 cursor-pointer">
            <input type="checkbox" checked={unreadOnly} onChange={e=>{ setUnreadOnly(e.target.checked); setPage(0) }} className="w-4 h-4 accent-brand-600"/>
            <span className="text-sm text-slate-600">Unread only</span>
          </label>
          <span className="text-xs text-slate-400">{total} notifications</span>
        </div>
        <button className="btn-secondary text-xs" onClick={markAll}><CheckCheck className="w-3.5 h-3.5"/>Mark all read</button>
      </div>

      <div className="space-y-2">
        {!notifs.length ? (
          <div className="card"><EmptyState icon={Bell} title="All caught up!" description="No notifications to show"/></div>
        ) : notifs.map(n => {
          const style = TYPE_STYLES[n.type] ?? TYPE_STYLES.SYSTEM
          return (
            <div key={n.id} className={`flex items-start gap-3 p-4 rounded-xl border transition-all ${style.bg} ${n.read?'opacity-60':''}`}>
              <div className={`w-2 h-2 rounded-full mt-1.5 shrink-0 ${style.dot}`}/>
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-0.5">
                  <span className="text-xs font-semibold text-slate-600">{style.label}</span>
                  <span className="text-xs text-slate-400">{fmt.dateTime(n.createdAt)}</span>
                  {!n.read && <span className="ml-auto badge-blue">NEW</span>}
                </div>
                <p className="text-sm text-slate-700 leading-relaxed">{n.message}</p>
              </div>
              {!n.read && (
                <button onClick={()=>markRead(n.id)} className="p-1.5 rounded hover:bg-white/60 text-slate-400 hover:text-green-600 transition-colors shrink-0" title="Mark as read">
                  <Check className="w-3.5 h-3.5"/>
                </button>
              )}
            </div>
          )
        })}
      </div>

      {total > 20 && (
        <div className="flex justify-center gap-2">
          <button disabled={page===0} onClick={()=>setPage(p=>p-1)} className="btn-secondary text-xs disabled:opacity-40">← Prev</button>
          <button disabled={(page+1)*20>=total} onClick={()=>setPage(p=>p+1)} className="btn-secondary text-xs disabled:opacity-40">Next →</button>
        </div>
      )}
    </div>
  )
}
