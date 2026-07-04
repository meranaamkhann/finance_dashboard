const variants = {
  green:  'bg-green-50 text-green-700',
  red:    'bg-red-50 text-red-700',
  yellow: 'bg-yellow-50 text-yellow-700',
  blue:   'bg-blue-50 text-blue-700',
  gray:   'bg-slate-100 text-slate-600',
  purple: 'bg-purple-50 text-purple-700',
}
export default function Badge({ label, variant='gray' }) {
  return <span className={`inline-flex items-center px-2 py-0.5 text-xs font-medium rounded-full ${variants[variant]}`}>{label}</span>
}
