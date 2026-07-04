import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import { fmt } from '../../utils/format'
import EmptyState from '../ui/EmptyState'
import { BarChart2 } from 'lucide-react'

export default function MonthlyBarChart({ data = [] }) {
  if (!data.length) return <EmptyState icon={BarChart2} title="No trend data" description="Record transactions to see your monthly trend"/>
  const chartData = data.map(d => ({ name: d.monthLabel, Income: Number(d.income), Expense: Number(d.expense), Net: Number(d.net) }))
  return (
    <ResponsiveContainer width="100%" height={280}>
      <BarChart data={chartData} barGap={4} barCategoryGap="30%">
        <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false}/>
        <XAxis dataKey="name" tick={{ fontSize:11, fill:'#94a3b8' }} axisLine={false} tickLine={false}/>
        <YAxis tick={{ fontSize:11, fill:'#94a3b8' }} axisLine={false} tickLine={false} tickFormatter={v=>fmt.currency(v).replace('₹','')}/>
        <Tooltip formatter={(v) => [fmt.currency(v), '']} contentStyle={{ borderRadius:10, border:'1px solid #e2e8f0', fontSize:12 }}/>
        <Legend iconType="circle" iconSize={8} formatter={(v) => <span className="text-xs text-slate-600">{v}</span>}/>
        <Bar dataKey="Income" fill="#22c55e" radius={[4,4,0,0]}/>
        <Bar dataKey="Expense" fill="#f97316" radius={[4,4,0,0]}/>
      </BarChart>
    </ResponsiveContainer>
  )
}
