import Spinner from './Spinner'
export default function StatCard({ title, value, icon:Icon, color='blue', loading }) {
  const c = { blue:'bg-blue-50 text-blue-600', green:'bg-green-50 text-green-600', red:'bg-red-50 text-red-600', purple:'bg-purple-50 text-purple-600', orange:'bg-orange-50 text-orange-600' }[color]
  const [bg, ic] = c.split(' ')
  return (
    <div className="stat-card">
      <div className={`w-9 h-9 rounded-lg ${bg} flex items-center justify-center`}>
        <Icon className={`w-4 h-4 ${ic}`}/>
      </div>
      {loading ? <Spinner size="sm" className="mt-2"/> : <p className="text-2xl font-bold text-slate-900 mt-2 leading-none">{value}</p>}
      <p className="text-xs text-slate-500 mt-1">{title}</p>
    </div>
  )
}
