import { useEffect, useState } from 'react'
import { auditApi } from '../services/api'
import { fmt } from '../utils/format'
import { useToast } from '../components/ui/Toast'
import Spinner from '../components/ui/Spinner'
import EmptyState from '../components/ui/EmptyState'
import { ClipboardList, ChevronLeft, ChevronRight } from 'lucide-react'

const ACTION_BADGE = {
  LOGIN_SUCCESS:'badge-green', LOGIN_FAILURE:'badge-red', USER_CREATED:'badge-blue',
  RECORD_CREATED:'badge-blue', RECORD_DELETED:'badge-red', BUDGET_EXCEEDED:'badge-red',
  CSV_EXPORTED:'badge-purple', PASSWORD_CHANGED:'badge-yellow',
}

export default function AuditPage() {
  const toast = useToast()
  const [logs, setLogs] = useState([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [total, setTotal] = useState(0)

  const load = async () => {
    setLoading(true)
    try { const { data } = await auditApi.getAll({ page, size:30 }); setLogs(data.data.content); setTotal(data.data.totalElements) }
    catch { toast('Failed to load audit logs','error') }
    finally { setLoading(false) }
  }
  useEffect(() => { load() }, [page])

  if (loading) return <div className="flex items-center justify-center h-64"><Spinner size="lg"/></div>

  return (
    <div className="space-y-4">
      <div className="card overflow-hidden">
        {!logs.length ? <EmptyState icon={ClipboardList} title="No audit logs yet"/> : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-slate-50 border-b border-surface-border">
                <tr>{['Time','Actor','Action','Entity','IP','Detail'].map(h=><th key={h} className="table-header">{h}</th>)}</tr>
              </thead>
              <tbody>
                {logs.map(l=>(
                  <tr key={l.id} className="table-row">
                    <td className="table-cell text-xs text-slate-400 whitespace-nowrap">{fmt.dateTime(l.createdAt)}</td>
                    <td className="table-cell text-xs font-medium">@{l.actorUsername}</td>
                    <td className="table-cell"><span className={ACTION_BADGE[l.action]??'badge-gray text-xs'}>{l.action?.replace(/_/g,' ')}</span></td>
                    <td className="table-cell text-xs text-slate-500">{l.entityType && `${l.entityType} #${l.entityId??''}`}</td>
                    <td className="table-cell text-xs text-slate-400">{l.ipAddress??'—'}</td>
                    <td className="table-cell text-xs text-slate-500 max-w-xs truncate">{l.detail??'—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      {total > 30 && (
        <div className="flex items-center justify-between">
          <p className="text-xs text-slate-500">{total} total entries</p>
          <div className="flex gap-2">
            <button disabled={page===0} onClick={()=>setPage(p=>p-1)} className="btn-secondary px-2 py-1 text-xs disabled:opacity-40"><ChevronLeft className="w-3.5 h-3.5"/></button>
            <button disabled={(page+1)*30>=total} onClick={()=>setPage(p=>p+1)} className="btn-secondary px-2 py-1 text-xs disabled:opacity-40"><ChevronRight className="w-3.5 h-3.5"/></button>
          </div>
        </div>
      )}
    </div>
  )
}
