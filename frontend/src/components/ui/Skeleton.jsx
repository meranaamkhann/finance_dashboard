export function Skeleton({ className = '' }) {
  return (
    <div className={`animate-pulse rounded-lg ${className}`}
         style={{ background: 'var(--border)' }}/>
  )
}

export function StatCardSkeleton() {
  return (
    <div className="stat-card">
      <Skeleton className="w-9 h-9 rounded-lg"/>
      <Skeleton className="h-8 w-24 mt-2"/>
      <Skeleton className="h-3 w-32 mt-1"/>
    </div>
  )
}

export function TableRowSkeleton({ cols = 6 }) {
  return (
    <tr className="table-row">
      {Array.from({ length: cols }).map((_, i) => (
        <td key={i} className="table-cell">
          <Skeleton className="h-4 w-full max-w-[120px]"/>
        </td>
      ))}
    </tr>
  )
}
