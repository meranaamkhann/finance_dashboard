import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { planApi } from '../services/planApi'
import { useToast } from '../components/ui/Toast'
import Spinner from '../components/ui/Spinner'
import { Check, Zap } from 'lucide-react'

export default function PricingPage() {
  const [plans, setPlans]       = useState([])
  const [billing, setBilling]   = useState('monthly')
  const [loading, setLoading]   = useState(true)
  const [paying, setPaying]     = useState(null)
  const { user }                = useAuth()
  const toast                   = useToast()
  const nav                     = useNavigate()

  useEffect(() => {
    planApi.getPlans()
      .then(r => {
        const data = r.data?.data
        setPlans(Array.isArray(data) ? data : [])
      })
      .catch(() => {
        toast('Failed to load plans', 'error')
        setPlans([])
      })
      .finally(() => setLoading(false))
  }, [])

  const checkout = async (plan) => {
    if (!user) { nav('/login'); return }
    if (plan.slug === 'free') {
      try {
        setPaying(plan.slug)
        await planApi.createOrder({ planSlug: 'free', billingCycle: 'MONTHLY' })
        toast('Free plan activated!', 'success')
        nav('/dashboard')
      } catch (e) {
        toast(e.response?.data?.message || 'Failed', 'error')
      } finally { setPaying(null) }
      return
    }

    try {
      setPaying(plan.slug)
      const { data } = await planApi.createOrder({
        planSlug: plan.slug,
        billingCycle: billing === 'yearly' ? 'YEARLY' : 'MONTHLY'
      })
      const order = data.data
      if (order.free) { toast('Plan activated!', 'success'); nav('/dashboard'); return }

      const options = {
        key:      order.keyId,
        amount:   order.amount,
        currency: order.currency,
        name:     'FinancePro',
        description: `${order.planName} — ${order.billingCycle}`,
        order_id: order.orderId,
        theme:    { color: '#2563eb' },
        handler: async (response) => {
          try {
            await planApi.verifyPayment({
              razorpayOrderId:   response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            })
            toast('Payment successful! Subscription activated.', 'success')
            nav('/dashboard')
          } catch (e) {
            toast('Payment verification failed. Contact support.', 'error')
          }
        },
        modal: { ondismiss: () => setPaying(null) }
      }
      new window.Razorpay(options).open()
    } catch (e) {
      toast(e.response?.data?.message || 'Payment failed', 'error')
      setPaying(null)
    }
  }

  if (loading) return (
    <div className="min-h-screen flex items-center justify-center" style={{ background: 'var(--bg-page)' }}>
      <Spinner size="lg"/>
    </div>
  )

  return (
    <div className="min-h-screen py-16 px-4" style={{ background: 'var(--bg-page)' }}>
      <div className="max-w-5xl mx-auto">
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold mb-3" style={{ color: 'var(--text-main)' }}>
            Simple, transparent pricing
          </h1>
          <p className="text-lg mb-6" style={{ color: 'var(--text-muted)' }}>
            Start free. Upgrade when you need more.
          </p>
          <div className="inline-flex rounded-xl p-1 gap-1" style={{ background: 'var(--border)' }}>
            {['monthly','yearly'].map(b => (
              <button key={b} onClick={() => setBilling(b)}
                className="px-5 py-2 rounded-lg text-sm font-medium transition-all"
                style={{
                  background: billing === b ? 'var(--bg-card)' : 'transparent',
                  color: billing === b ? 'var(--brand)' : 'var(--text-muted)',
                  boxShadow: billing === b ? '0 1px 3px rgba(0,0,0,.1)' : 'none'
                }}>
                {b === 'yearly' ? 'Yearly (save 17%)' : 'Monthly'}
              </button>
            ))}
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {plans.map((plan, i) => {
            const price = billing === 'yearly' ? plan.yearlyPrice : plan.monthlyPrice
            const isPro = plan.slug === 'pro'
            return (
              <div key={plan.id}
                className={`card p-6 flex flex-col relative ${isPro ? 'ring-2' : ''}`}
                style={{ ringColor: isPro ? 'var(--brand)' : 'transparent' }}>
                {isPro && (
                  <div className="absolute -top-3 left-1/2 -translate-x-1/2 px-3 py-1 rounded-full text-xs font-semibold text-white flex items-center gap-1"
                       style={{ background: 'var(--brand)' }}>
                    <Zap className="w-3 h-3"/> Most Popular
                  </div>
                )}
                <div className="mb-5">
                  <h3 className="font-bold text-lg mb-1" style={{ color: 'var(--text-main)' }}>
                    {plan.name}
                  </h3>
                  <p className="text-sm mb-3" style={{ color: 'var(--text-muted)' }}>
                    {plan.description}
                  </p>
                  <div className="flex items-baseline gap-1">
                    <span className="text-4xl font-bold" style={{ color: 'var(--text-main)' }}>
                      {price === 0 ? 'Free' : `₹${price}`}
                    </span>
                    {price > 0 && (
                      <span className="text-sm" style={{ color: 'var(--text-faint)' }}>
                        /{billing === 'yearly' ? 'year' : 'month'}
                      </span>
                    )}
                  </div>
                  {billing === 'yearly' && price > 0 && (
                    <p className="text-xs text-green-600 mt-1">
                      ₹{Math.round(price / 12)}/month billed yearly
                    </p>
                  )}
                </div>
                <ul className="space-y-2 flex-1 mb-6">
                  {(plan.features || []).map((f, j) => (
                    <li key={j} className="flex items-start gap-2 text-sm" style={{ color: 'var(--text-muted)' }}>
                      <Check className="w-4 h-4 mt-0.5 shrink-0 text-green-500"/>
                      {f}
                    </li>
                  ))}
                </ul>
                <button
                  onClick={() => checkout(plan)}
                  disabled={paying === plan.slug}
                  className={isPro ? 'btn-primary w-full justify-center py-2.5' : 'btn-secondary w-full justify-center py-2.5'}>
                  {paying === plan.slug ? 'Processing…'
                    : plan.slug === 'free' ? 'Get Started Free'
                    : `Subscribe to ${plan.name}`}
                </button>
              </div>
            )
          })}
        </div>

        <p className="text-center text-xs mt-8" style={{ color: 'var(--text-faint)' }}>
          All prices inclusive of applicable taxes. Cancel anytime.
          Payments secured by Razorpay.
        </p>
      </div>
    </div>
  )
}