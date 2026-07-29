import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import { CAT_COLORS, fmt } from '../../utils/format'
import EmptyState from '../ui/EmptyState'
import { PieChartIcon } from 'lucide-react'
export default function SpendingPieChart({ data=[] }) {
  if (!data.length) return <EmptyState icon={PieChartIcon} title="No expense data"/>
  const d = data.slice(0,8).map(x => ({ name:x.category.replace(/_/g,' '), value:Number(x.amount), cat:x.category }))
  return (
    <ResponsiveContainer width="100%" height={260}>
      <PieChart>
        <Pie data={d} cx="50%" cy="50%" innerRadius={60} outerRadius={100} paddingAngle={3} dataKey="value">
          {d.map((e,i) => <Cell key={i} fill={CAT_COLORS[e.cat]??'#94a3b8'} strokeWidth={0}/>)}
        </Pie>
        <Tooltip formatter={v=>[fmt.currency(v),'']} contentStyle={{borderRadius:10,border:'1px solid #e2e8f0',fontSize:12}}/>
        <Legend iconType="circle" iconSize={8} formatter={v=><span className="text-xs text-slate-600">{v}</span>}/>
      </PieChart>
    </ResponsiveContainer>
  )
}
