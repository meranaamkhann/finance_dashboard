export const fmt = {
  currency: (v) => new Intl.NumberFormat('en-IN', { style:'currency', currency:'INR', maximumFractionDigits:0 }).format(v ?? 0),
  decimal:  (v) => new Intl.NumberFormat('en-IN', { maximumFractionDigits:2 }).format(v ?? 0),
  percent:  (v) => `${Number(v ?? 0).toFixed(1)}%`,
  date:     (v) => v ? new Date(v).toLocaleDateString('en-IN', { day:'2-digit', month:'short', year:'numeric' }) : '—',
  dateTime: (v) => v ? new Date(v).toLocaleString('en-IN', { day:'2-digit', month:'short', year:'numeric', hour:'2-digit', minute:'2-digit' }) : '—',
  dateInput:(v) => v ? v.toString().slice(0,10) : '',
}

export const CATEGORIES = [
  'SALARY','FREELANCE','INVESTMENT','RENTAL','BUSINESS','BONUS','GIFT','OTHER_INCOME',
  'FOOD','RENT','UTILITIES','TRANSPORT','HEALTHCARE','EDUCATION','ENTERTAINMENT',
  'SHOPPING','TRAVEL','INSURANCE','SAVINGS','EMI','SUBSCRIPTION','PERSONAL_CARE','HOME','OTHER_EXPENSE'
]
export const INCOME_CATS  = CATEGORIES.slice(0,8)
export const EXPENSE_CATS = CATEGORIES.slice(8)
export const FREQUENCIES  = ['DAILY','WEEKLY','BIWEEKLY','MONTHLY','QUARTERLY','YEARLY']

export const CAT_COLORS = {
  FOOD:'#f97316',RENT:'#8b5cf6',UTILITIES:'#06b6d4',TRANSPORT:'#10b981',
  ENTERTAINMENT:'#ec4899',SHOPPING:'#f59e0b',HEALTHCARE:'#ef4444',EDUCATION:'#3b82f6',
  TRAVEL:'#14b8a6',SUBSCRIPTION:'#a855f7',SALARY:'#22c55e',FREELANCE:'#6366f1',
  INVESTMENT:'#0ea5e9',SAVINGS:'#84cc16',EMI:'#f43f5e',INSURANCE:'#64748b',
  OTHER_INCOME:'#34d399',OTHER_EXPENSE:'#94a3b8',BONUS:'#fbbf24',GIFT:'#fb7185',
  PERSONAL_CARE:'#e879f9',HOME:'#78716c',BUSINESS:'#2dd4bf',RENTAL:'#a3e635',
}

export const budgetStatusBadge = (status) => ({
  ON_TRACK: 'badge-green', WARNING: 'badge-yellow',
  CRITICAL: 'badge-red',   EXCEEDED: 'badge-red'
})[status] ?? 'badge-gray'

export const gradeBadge = (grade) => ({
  A:'badge-green', B:'badge-blue', C:'badge-yellow', D:'badge-red', F:'badge-red'
})[grade] ?? 'badge-gray'
