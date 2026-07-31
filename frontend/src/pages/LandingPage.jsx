import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useTheme } from '../context/ThemeContext'
import ThemeToggle from '../components/ui/ThemeToggle'
import { TrendingUp, Shield, Zap, PieChart, Bell, FileText, ArrowRight, Check } from 'lucide-react'

const FEATURES = [
  { icon: TrendingUp, title: 'Real-time Analytics',    desc: 'Track income, expenses and net worth with live charts and trends.' },
  { icon: Shield,     title: 'Bank-level Security',    desc: 'JWT auth, BCrypt passwords, account locking, full audit trail.' },
  { icon: PieChart,   title: 'Smart Budgets',          desc: 'Set category limits, get alerts before you overspend.' },
  { icon: Bell,       title: 'Instant Alerts',         desc: 'Budget warnings, recurring auto-posts and login notifications.' },
  { icon: Zap,        title: 'Recurring Automation',   desc: 'Set once — salary, rent, subscriptions post automatically.' },
  { icon: FileText,   title: 'Export & Reports',       desc: 'CSV exports with UTF-8 BOM for instant Excel compatibility.' },
]

const PLANS = [
  {
    name: 'Free',
    price: '₹0',
    period: 'forever',
    color: 'border-slate-200 dark:border-slate-700',
    badge: '',
    features: ['Up to 100 records/month', '3 budgets', 'Basic dashboard', 'CSV export', 'Email alerts'],
    cta: 'Get Started Free',
    primary: false,
  },
  {
    name: 'Pro',
    price: '₹299',
    period: 'per month',
    color: 'border-blue-500',
    badge: 'Most Popular',
    features: ['Unlimited records', 'Unlimited budgets', 'Analytics & trends', 'Recurring automation', 'Priority support', 'PDF reports (coming soon)'],
    cta: 'Start 14-day Trial',
    primary: true,
  },
  {
    name: 'Team',
    price: '₹799',
    period: 'per month',
    color: 'border-purple-500',
    badge: '',
    features: ['Everything in Pro', 'Up to 5 users', 'RBAC (Admin/Analyst/Viewer)', 'Audit trail', 'Custom branding', 'API access'],
    cta: 'Contact Sales',
    primary: false,
  },
]

export default function LandingPage() {
  const nav = useNavigate()
  const { user } = useAuth()
  const { isDark } = useTheme()

  return (
    <div className="min-h-screen" style={{ background: 'var(--bg-page)', color: 'var(--text-main)' }}>

      {/* Nav */}
      <nav className="sticky top-0 z-50 border-b backdrop-blur-md"
           style={{ background: 'color-mix(in srgb, var(--bg-card) 85%, transparent)', borderColor: 'var(--border)' }}>
        <div className="max-w-6xl mx-auto px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-xl flex items-center justify-center" style={{ background: 'var(--brand)' }}>
              <TrendingUp className="w-4 h-4 text-white"/>
            </div>
            <span className="text-base font-bold" style={{ color: 'var(--text-main)' }}>FinancePro</span>
          </div>
          <div className="flex items-center gap-3">
            <ThemeToggle/>
            {user ? (
              <button onClick={() => nav('/dashboard')} className="btn-primary text-xs px-4 py-2">
                Go to Dashboard
              </button>
            ) : (
              <>
                <button onClick={() => nav('/login')}
                  className="text-sm font-medium px-4 py-2 rounded-lg transition-colors"
                  style={{ color: 'var(--text-muted)' }}>
                  Sign In
                </button>
                <button onClick={() => nav('/login')} className="btn-primary text-sm px-4 py-2">
                  Get Started
                </button>
              </>
            )}
          </div>
        </div>
      </nav>

      {/* Hero */}
      <section className="max-w-6xl mx-auto px-6 pt-20 pb-16 text-center">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-medium mb-6 animate-fade-in"
             style={{ background: 'var(--brand-light)', color: 'var(--brand)' }}>
          <span className="w-1.5 h-1.5 rounded-full animate-pulse" style={{ background: 'var(--brand)' }}/>
          Production-ready Finance Dashboard
        </div>
        <h1 className="text-5xl md:text-6xl font-bold leading-tight mb-6 animate-slide-up"
            style={{ color: 'var(--text-main)' }}>
          Take Control of{" "}
          <span className="relative inline-block">
            <span style={{ color: 'var(--brand)' }}>Your Money</span>
            <svg className="absolute -bottom-2 left-0 w-full" viewBox="0 0 200 8" fill="none">
              <path d="M0 6 Q100 0 200 6" stroke="var(--brand)" strokeWidth="2.5" strokeLinecap="round" fill="none"/>
            </svg>
          </span>
        </h1>
        <p className="text-lg max-w-2xl mx-auto mb-8 animate-slide-up"
           style={{ color: 'var(--text-muted)', animationDelay: '0.1s' }}>
          Track income, manage budgets, automate recurring transactions and visualise
          your financial health — all in one secure dashboard.
        </p>
        <div className="flex items-center justify-center gap-4 flex-wrap animate-slide-up"
             style={{ animationDelay: '0.2s' }}>
          <button onClick={() => nav('/login')} className="btn-primary px-6 py-3 text-base gap-2">
            Start Free <ArrowRight className="w-4 h-4"/>
          </button>
          <button onClick={() => nav('/login')}
            className="px-6 py-3 text-base font-medium rounded-lg border transition-colors"
            style={{ border: '1px solid var(--border)', color: 'var(--text-main)' }}>
            View Demo
          </button>
        </div>

        {/* Animated dashboard preview */}
        <div className="mt-16 relative animate-float">
          <div className="rounded-2xl border overflow-hidden shadow-2xl"
               style={{ background: 'var(--bg-card)', borderColor: 'var(--border)' }}>
            <div className="p-4 border-b flex items-center gap-2" style={{ borderColor: 'var(--border)' }}>
              <div className="flex gap-1.5">
                <div className="w-3 h-3 rounded-full bg-red-400"/>
                <div className="w-3 h-3 rounded-full bg-yellow-400"/>
                <div className="w-3 h-3 rounded-full bg-green-400"/>
              </div>
              <div className="flex-1 h-5 rounded" style={{ background: 'var(--border)', maxWidth: 200 }}/>
            </div>
            <div className="p-6 grid grid-cols-4 gap-4">
              {[['Income','₹85,000','text-green-500'],['Expense','₹34,200','text-red-500'],['Net','₹50,800','text-blue-500'],['Score','84/A','text-purple-500']].map(([label,val,color])=>(
                <div key={label} className="rounded-xl p-4 border" style={{ background: 'var(--bg-page)', borderColor: 'var(--border)' }}>
                  <p className="text-xs mb-1" style={{ color: 'var(--text-faint)' }}>{label}</p>
                  <p className={`text-lg font-bold ${color}`}>{val}</p>
                </div>
              ))}
            </div>
            <div className="px-6 pb-6">
              <div className="h-32 rounded-xl" style={{ background: 'var(--bg-page)', borderColor: 'var(--border)', border: '1px solid var(--border)' }}>
                <div className="h-full flex items-end justify-around px-4 pb-4 gap-2">
                  {[40,65,50,80,55,90,70].map((h,i)=>(
                    <div key={i} className="flex-1 rounded-t-sm" style={{ height: `${h}%`, background: i===5 ? 'var(--brand)' : 'var(--border)' }}/>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="max-w-6xl mx-auto px-6 py-16">
        <h2 className="text-3xl font-bold text-center mb-3" style={{ color: 'var(--text-main)' }}>
          Everything you need
        </h2>
        <p className="text-center mb-12" style={{ color: 'var(--text-muted)' }}>
          Built with Spring Boot 3 + React 18. Production-grade from day one.
        </p>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {FEATURES.map(({ icon: Icon, title, desc }, i) => (
            <div key={i} className="card p-6 group hover:border-blue-400 transition-colors"
                 style={{ animationDelay: `${i * 0.08}s` }}>
              <div className="w-10 h-10 rounded-lg flex items-center justify-center mb-4 transition-colors"
                   style={{ background: 'var(--brand-light)' }}>
                <Icon className="w-5 h-5" style={{ color: 'var(--brand)' }}/>
              </div>
              <h3 className="font-semibold mb-2" style={{ color: 'var(--text-main)' }}>{title}</h3>
              <p className="text-sm" style={{ color: 'var(--text-muted)' }}>{desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Pricing */}
      <section className="max-w-6xl mx-auto px-6 py-16">
        <h2 className="text-3xl font-bold text-center mb-3" style={{ color: 'var(--text-main)' }}>
          Simple, transparent pricing
        </h2>
        <p className="text-center mb-12" style={{ color: 'var(--text-muted)' }}>
          Start free. Upgrade when you need more.
        </p>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {PLANS.map((plan, i) => (
            <div key={i}
              className={`card p-6 flex flex-col border-2 ${plan.color} ${plan.primary ? 'relative' : ''}`}>
              {plan.badge && (
                <div className="absolute -top-3 left-1/2 -translate-x-1/2 px-3 py-1 rounded-full text-xs font-semibold text-white"
                     style={{ background: 'var(--brand)' }}>
                  {plan.badge}
                </div>
              )}
              <div className="mb-4">
                <h3 className="font-bold text-lg mb-1" style={{ color: 'var(--text-main)' }}>{plan.name}</h3>
                <div className="flex items-baseline gap-1">
                  <span className="text-3xl font-bold" style={{ color: 'var(--text-main)' }}>{plan.price}</span>
                  <span className="text-sm" style={{ color: 'var(--text-faint)' }}>/{plan.period}</span>
                </div>
              </div>
              <ul className="space-y-2 flex-1 mb-6">
                {plan.features.map((f, j) => (
                  <li key={j} className="flex items-start gap-2 text-sm" style={{ color: 'var(--text-muted)' }}>
                    <Check className="w-4 h-4 mt-0.5 shrink-0 text-green-500"/>
                    {f}
                  </li>
                ))}
              </ul>
              <button
                onClick={() => nav('/login')}
                className={plan.primary ? 'btn-primary w-full justify-center' : 'btn-secondary w-full justify-center'}>
                {plan.cta}
              </button>
            </div>
          ))}
        </div>
        <p className="text-center text-xs mt-6" style={{ color: 'var(--text-faint)' }}>
          Payment integration (Razorpay / Stripe) coming soon. All prices inclusive of GST.
        </p>
      </section>

      {/* Footer */}
      <footer className="border-t py-8" style={{ borderColor: 'var(--border)' }}>
        <div className="max-w-6xl mx-auto px-6 flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 rounded-lg flex items-center justify-center" style={{ background: 'var(--brand)' }}>
              <TrendingUp className="w-3 h-3 text-white"/>
            </div>
            <span className="text-sm font-semibold" style={{ color: 'var(--text-main)' }}>FinancePro</span>
          </div>
          <p className="text-xs" style={{ color: 'var(--text-faint)' }}>
            Designed & built by Asad Khan    © 2026 All rights reserved. · {new Date().getFullYear()}
          </p>
          <div className="flex gap-4">
            {['Privacy','Terms','Contact'].map(l => (
              <button key={l} className="text-xs hover:underline" style={{ color: 'var(--text-faint)' }}>{l}</button>
            ))}
          </div>
        </div>
      </footer>
    </div>
  )
}
