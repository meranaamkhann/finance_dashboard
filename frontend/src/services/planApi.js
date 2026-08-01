import api from './api'

export const planApi = {
  getPlans:         ()      => api.get('/plans'),
  getPlan:          slug    => api.get(`/plans/${slug}`),
  createOrder:      data    => api.post('/billing/orders', data),
  verifyPayment:    data    => api.post('/billing/verify', data),
  getSubscription:  ()      => api.get('/billing/subscription'),
  getHistory:       ()      => api.get('/billing/subscriptions'),
  getPayments:      params  => api.get('/billing/payments', { params }),
  cancelSub:        reason  => api.post(`/billing/cancel?reason=${encodeURIComponent(reason)}`),
}