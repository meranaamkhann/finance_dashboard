export default function ProgressBar({ value, max=100, color }) {
  const pct = Math.min((value/max)*100, 100)
  const auto = pct>=100?'bg-red-500':pct>=90?'bg-red-400':pct>=80?'bg-yellow-400':'bg-green-500'
  return (
    <div className="w-full bg-slate-100 rounded-full h-1.5">
      <div className={`${color??auto} h-1.5 rounded-full transition-all duration-500`} style={{width:`${pct}%`}}/>
    </div>
  )
}
