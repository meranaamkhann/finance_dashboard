export default function HealthGauge({ score=0, grade='N/A', insights=[] }) {
  const color = score>=85?'#22c55e':score>=70?'#3b82f6':score>=55?'#f59e0b':score>=40?'#f97316':'#ef4444'
  const r=54, circ=2*Math.PI*r, offset=circ-(score/100)*circ
  const badgeColor = {A:'bg-green-100 text-green-700',B:'bg-blue-100 text-blue-700',C:'bg-yellow-100 text-yellow-700',D:'bg-orange-100 text-orange-700',F:'bg-red-100 text-red-700','N/A':'bg-slate-100 text-slate-600'}[grade]??'bg-slate-100 text-slate-600'
  return (
    <div className="flex flex-col items-center gap-4">
      <div className="relative">
        <svg width={140} height={140} className="-rotate-90">
          <circle cx={70} cy={70} r={r} fill="none" stroke="#f1f5f9" strokeWidth={10}/>
          <circle cx={70} cy={70} r={r} fill="none" stroke={color} strokeWidth={10}
            strokeDasharray={circ} strokeDashoffset={offset} strokeLinecap="round"
            style={{transition:'stroke-dashoffset 1s ease'}}/>
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span className="text-3xl font-bold text-slate-900">{score}</span>
          <span className="text-xs text-slate-400">/ 100</span>
        </div>
      </div>
      <span className={`px-3 py-1 rounded-full text-xs font-semibold ${badgeColor}`}>Grade {grade}</span>
      {insights.length > 0 && (
        <div className="w-full space-y-2">
          {insights.map((ins,i) => <p key={i} className="text-xs text-slate-600 bg-slate-50 rounded-lg px-3 py-2">{ins}</p>)}
        </div>
      )}
    </div>
  )
}
