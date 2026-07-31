import { useState } from 'react'
import { authApi } from '../../services/api'
import api from '../../services/api'

export default function RazorpayButton({ planName, amountInPaise, onSuccess }) {
  const [loading, setLoading] = useState(false)

  const pay = async () => {
    setLoading(true)
    try {
      const { data } = await api.get('/payments/config')
      const { keyId } = data.data

      const options = {
        key:         keyId,
        amount:      amountInPaise,
        currency:    'INR',
        name:        'FinancePro',
        description: planName + ' Plan',
        theme:       { color: '#2563eb' },
        handler: async (response) => {
          await api.post('/payments/verify', response)
          if (onSuccess) onSuccess(response)
        },
      }

      const rzp = new window.Razorpay(options)
      rzp.open()
    } catch (e) {
      console.error('Payment error:', e)
    } finally {
      setLoading(false)
    }
  }

  return (
    <button onClick={pay} disabled={loading} className="btn-primary w-full justify-center">
      {loading ? 'Loading…' : `Pay ₹${(amountInPaise / 100).toLocaleString('en-IN')}`}
    </button>
  )
}