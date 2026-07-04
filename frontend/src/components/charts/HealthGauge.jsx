import { gradeBadge } from '../../utils/format'
import Badge from '../ui/Badge'

export default function HealthGauge({ score = 0, grade = 'N/A', insights = [] }) {
  const color = score >= 85 ? '#22c55e' : score >= 70 ? '#3b82f6' : score >= 55 ? '#f59e0b' : score >= 40 ? '#f97316' : '#ef4444'
  const r = 54, circ = 2 * Math.PI * r, offset = circ - (score / 100) * circ

  return (
    <div className="flex flex-col items-center gap-5">
      {/* Circular gauge */}
      <div className="relative">
        <svg width={140} height={140} className="-rotate-90">
          <circle cx={70} cy={70} r={r} fill="none" stroke="#f1f5f9" strokeWidth={10}/>
          <circle cx={70} cy={70} r={r} fill="none" stroke={color} strokeWidth={10}
            strokeDasharray={circ} strokeDashoffset={offset} strokeLinecap="round"
            style={{ transition: 'stroke-dashoffset 1s ease' }}/>
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center rotate-0">
          <span className="text-3xl font-bold text-slate-900">{score}</span>
          <span className="text-xs text-slate-400">/ 100</span>
        </div>
      </div>

      <Badge label={`Grade ${grade}`} variant={gradeBadge(grade).replace('badge-','')}/>

      {/* Insights */}
      {insights.length > 0 && (
        <div className="w-full space-y-2">
          {insights.map((ins, i) => (
            <p key={i} className="text-xs text-slate-600 leading-relaxed bg-slate-50 rounded-lg px-3 py-2">{ins}</p>
          ))}
        </div>
      )}
    </div>
  )
}
