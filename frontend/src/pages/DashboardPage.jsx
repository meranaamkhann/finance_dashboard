import { useEffect, useState } from 'react'
import { dashboardApi } from '../services/api'
import { fmt } from '../utils/format'
import { useAuth } from '../context/AuthContext'
import StatCard from '../components/ui/StatCard'
import Spinner from '../components/ui/Spinner'
import SpendingPieChart from '../components/charts/SpendingPieChart'
import MonthlyBarChart from '../components/charts/MonthlyBarChart'
import HealthGauge from '../components/charts/HealthGauge'
import { Wallet, TrendingUp, TrendingDown, PiggyBank, RefreshCw, Bell } from 'lucide-react'

export default function DashboardPage() {
  const { isAnalyst } = useAuth()
  const [summary, setSummary] = useState(null)
  const [trend, setTrend]     = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const load = async () => {
      setLoading(true)
      try {
        const [s, t] = await Promise.all([
          dashboardApi.getSummary(),
          isAnalyst() ? dashboardApi.getMonthlyTrend(6) : Promise.resolve({ data:{ data:[] } })
        ])
        setSummary(s.data.data)
        setTrend(t.data.data)
      } catch (e) { console.error(e) }
      finally { setLoading(false) }
    }
    load()
  }, [])

  if (loading) return <div className="flex items-center justify-center h-64"><Spinner size="lg"/></div>

  const d = summary ?? {}

  return (
    <div className="space-y-6">
      {/* KPI row */}
      <div className="grid grid-cols-2 lg:grid-cols-4 xl:grid-cols-6 gap-4">
        <StatCard title="Income (MTD)"     value={fmt.currency(d.totalIncome)}   icon={TrendingUp}  color="green"/>
        <StatCard title="Expense (MTD)"    value={fmt.currency(d.totalExpense)}   icon={TrendingDown} color="red"/>
        <StatCard title="Net Balance"      value={fmt.currency(d.netBalance)}     icon={Wallet}       color={d.netBalance>=0?'blue':'red'}/>
        <StatCard title="Savings Rate"     value={fmt.percent(d.savingsRate)}     icon={PiggyBank}    color="purple"/>
        <StatCard title="Active Budgets"   value={d.activeBudgets ?? '—'}         icon={PiggyBank}    color="orange"/>
        <StatCard title="Unread Alerts"    value={d.unreadNotifications ?? 0}     icon={Bell}         color={d.unreadNotifications>0?'red':'blue'}/>
      </div>

      {/* Charts row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
        {/* Health Score */}
        <div className="card p-5">
          <p className="section-title mb-4">Financial Health</p>
          <HealthGauge score={d.healthScore?.score} grade={d.healthScore?.grade} insights={d.healthScore?.insights ?? []}/>
        </div>

        {/* Pie */}
        <div className="card p-5">
          <p className="section-title mb-4">Spending by Category</p>
          <SpendingPieChart data={d.topCategories ?? []}/>
        </div>

        {/* Top categories table */}
        <div className="card p-5">
          <p className="section-title mb-4">Top Expenses</p>
          <div className="space-y-3">
            {(d.topCategories ?? []).slice(0,6).map((c,i) => (
              <div key={i} className="flex items-center gap-3">
                <span className="text-xs text-slate-500 w-5 text-right">{i+1}</span>
                <div className="flex-1 min-w-0">
                  <div className="flex justify-between mb-1">
                    <span className="text-xs font-medium text-slate-700 truncate">{c.category.replace(/_/g,' ')}</span>
                    <span className="text-xs font-semibold text-slate-900 shrink-0 ml-2">{fmt.currency(c.amount)}</span>
                  </div>
                  <div className="w-full bg-slate-100 rounded-full h-1">
                    <div className="bg-brand-500 h-1 rounded-full" style={{width:`${c.percentage}%`}}/>
                  </div>
                </div>
              </div>
            ))}
            {!d.topCategories?.length && <p className="text-xs text-slate-400 text-center py-4">No data this month</p>}
          </div>
        </div>
      </div>

      {/* Monthly trend */}
      {isAnalyst() && trend.length > 0 && (
        <div className="card p-5">
          <p className="section-title mb-4">Monthly Trend (6 months)</p>
          <MonthlyBarChart data={trend}/>
        </div>
      )}
    </div>
  )
}
