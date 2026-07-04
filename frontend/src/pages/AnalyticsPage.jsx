import { useEffect, useState } from 'react'
import { dashboardApi } from '../services/api'
import { fmt } from '../utils/format'
import { useToast } from '../components/ui/Toast'
import Spinner from '../components/ui/Spinner'
import SpendingPieChart from '../components/charts/SpendingPieChart'
import MonthlyBarChart from '../components/charts/MonthlyBarChart'
import HealthGauge from '../components/charts/HealthGauge'
import { BarChart2 } from 'lucide-react'

export default function AnalyticsPage() {
  const toast = useToast()
  const [health, setHealth]   = useState(null)
  const [trend, setTrend]     = useState([])
  const [cats, setCats]       = useState([])
  const [daySpend, setDaySpend] = useState({})
  const [months, setMonths]   = useState(6)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const load = async () => {
      setLoading(true)
      try {
        const [h, t, c, d] = await Promise.all([
          dashboardApi.getHealthScore(),
          dashboardApi.getMonthlyTrend(months),
          dashboardApi.getCategories(),
          dashboardApi.getSpendingByDay(),
        ])
        setHealth(h.data.data); setTrend(t.data.data); setCats(c.data.data); setDaySpend(d.data.data)
      } catch { toast('Failed to load analytics','error') }
      finally { setLoading(false) }
    }
    load()
  }, [months])

  if (loading) return <div className="flex items-center justify-center h-64"><Spinner size="lg"/></div>

  const days = Object.entries(daySpend).sort((a,b)=>Number(b[1])-Number(a[1]))

  return (
    <div className="space-y-5">
      {/* Controls */}
      <div className="flex items-center gap-3">
        <span className="text-sm text-slate-600">Trend period:</span>
        {[3,6,12].map(m=>(
          <button key={m} onClick={()=>setMonths(m)}
            className={`px-3 py-1.5 text-xs font-medium rounded-lg border transition-colors ${months===m?'bg-brand-600 text-white border-brand-600':'bg-white text-slate-600 border-surface-border hover:bg-slate-50'}`}>
            {m} months
          </button>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
        {/* Health */}
        <div className="card p-5">
          <p className="section-title mb-4">Financial Health Score</p>
          {health && <HealthGauge score={health.score} grade={health.grade} insights={health.insights}/>}
        </div>

        {/* Score breakdown */}
        {health?.breakdown && (
          <div className="card p-5">
            <p className="section-title mb-4">Score Breakdown</p>
            <div className="space-y-3">
              {Object.entries(health.breakdown).map(([k,v])=>(
                <div key={k}>
                  <div className="flex justify-between text-xs mb-1">
                    <span className="text-slate-600">{k.split('(')[0].trim()}</span>
                    <span className="font-semibold">{v} pts</span>
                  </div>
                  <div className="w-full bg-slate-100 rounded-full h-1.5">
                    <div className="bg-brand-500 h-1.5 rounded-full" style={{width:`${Math.min(v/30*100,100)}%`}}/>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Day of week */}
        <div className="card p-5">
          <p className="section-title mb-4">Spend by Day of Week</p>
          <div className="space-y-2.5">
            {days.length ? days.map(([day,amt])=>(
              <div key={day} className="flex items-center gap-3">
                <span className="text-xs text-slate-500 w-20">{day}</span>
                <div className="flex-1 bg-slate-100 rounded-full h-2">
                  <div className="bg-orange-400 h-2 rounded-full" style={{width:`${(Number(amt)/Number(days[0][1]))*100}%`}}/>
                </div>
                <span className="text-xs font-medium text-slate-700 w-20 text-right">{fmt.currency(amt)}</span>
              </div>
            )) : <p className="text-xs text-slate-400 text-center py-4">No data yet</p>}
          </div>
        </div>
      </div>

      {/* Trend chart */}
      <div className="card p-5">
        <p className="section-title mb-4">Monthly Income vs Expense</p>
        <MonthlyBarChart data={trend}/>
      </div>

      {/* Category pie */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        <div className="card p-5">
          <p className="section-title mb-4">Expense by Category</p>
          <SpendingPieChart data={cats}/>
        </div>
        <div className="card p-5">
          <p className="section-title mb-4">Category Table</p>
          <div className="space-y-2">
            {cats.slice(0,10).map((c,i)=>(
              <div key={i} className="flex items-center justify-between py-1.5 border-b border-surface-border last:border-0">
                <span className="text-sm text-slate-700">{c.category.replace(/_/g,' ')}</span>
                <div className="flex items-center gap-3">
                  <span className="text-xs text-slate-400">{fmt.percent(c.percentage)}</span>
                  <span className="text-sm font-semibold text-slate-900">{fmt.currency(c.amount)}</span>
                </div>
              </div>
            ))}
            {!cats.length && <p className="text-xs text-slate-400 text-center py-4">No expense data this month</p>}
          </div>
        </div>
      </div>
    </div>
  )
}
