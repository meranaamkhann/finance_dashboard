import Spinner from './Spinner'
export default function StatCard({ title, value, sub, icon: Icon, color='blue', loading }) {
  const colors = {
    blue:   { bg:'bg-blue-50',   icon:'text-blue-600' },
    green:  { bg:'bg-green-50',  icon:'text-green-600' },
    red:    { bg:'bg-red-50',    icon:'text-red-600' },
    purple: { bg:'bg-purple-50', icon:'text-purple-600' },
    orange: { bg:'bg-orange-50', icon:'text-orange-600' },
  }
  const c = colors[color]
  return (
    <div className="stat-card">
      <div className="flex items-start justify-between">
        <div className={`w-9 h-9 rounded-lg ${c.bg} flex items-center justify-center`}>
          <Icon className={`w-4 h-4 ${c.icon}`}/>
        </div>
      </div>
      {loading
        ? <Spinner size="sm" className="mt-2"/>
        : <p className="text-2xl font-bold text-slate-900 mt-2 leading-none">{value}</p>
      }
      <p className="text-xs text-slate-500 mt-1">{title}</p>
      {sub && <p className="text-xs font-medium text-slate-400">{sub}</p>}
    </div>
  )
}
