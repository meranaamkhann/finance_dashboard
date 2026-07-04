import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import { CAT_COLORS, fmt } from '../../utils/format'
import EmptyState from '../ui/EmptyState'
import { PieChartIcon } from 'lucide-react'

export default function SpendingPieChart({ data = [] }) {
  if (!data.length) return <EmptyState icon={PieChartIcon} title="No expense data" description="Add some expenses to see the breakdown"/>
  const chartData = data.slice(0, 8).map(d => ({ name: d.category.replace(/_/g,' '), value: Number(d.amount), cat: d.category }))
  return (
    <ResponsiveContainer width="100%" height={280}>
      <PieChart>
        <Pie data={chartData} cx="50%" cy="50%" innerRadius={65} outerRadius={105} paddingAngle={3} dataKey="value">
          {chartData.map((e, i) => <Cell key={i} fill={CAT_COLORS[e.cat] ?? '#94a3b8'} strokeWidth={0}/>)}
        </Pie>
        <Tooltip formatter={(v) => [fmt.currency(v), '']} contentStyle={{ borderRadius:10, border:'1px solid #e2e8f0', boxShadow:'0 4px 6px -1px rgb(0 0 0/.07)', fontSize:12 }}/>
        <Legend iconType="circle" iconSize={8} formatter={(v) => <span className="text-xs text-slate-600">{v}</span>}/>
      </PieChart>
    </ResponsiveContainer>
  )
}
