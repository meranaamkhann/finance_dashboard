import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use(cfg => {
  const token = localStorage.getItem('accessToken')
  if (token) cfg.headers.Authorization = `Bearer ${token}`
  return cfg
})

api.interceptors.response.use(
  res => res,
  async err => {
    const orig = err.config
    if (err.response?.status === 401 && !orig._retry) {
      orig._retry = true
      const refresh = localStorage.getItem('refreshToken')
      if (refresh) {
        try {
          const { data } = await axios.post('/api/auth/refresh', { refreshToken: refresh })
          localStorage.setItem('accessToken',   data.data.accessToken)
          localStorage.setItem('refreshToken',  data.data.refreshToken)
          localStorage.setItem('tokenExpiresAt', String(Date.now() + data.data.expiresIn * 1000))
          orig.headers.Authorization = `Bearer ${data.data.accessToken}`
          return api(orig)
        } catch {
          localStorage.clear()
          window.location.href = '/login'
        }
      } else {
        localStorage.clear()
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  }
)

export default api

export const authApi = {
  login:          d  => api.post('/auth/login', d),
  loginByEmail:   d  => api.post('/auth/login/email', d),
  refresh:        rt => api.post('/auth/refresh', { refreshToken: rt }),
  logout:         rt => api.post('/auth/logout', { refreshToken: rt }),
  logoutAll:      () => api.post('/auth/logout-all'),
  forgotPassword: d  => api.post('/auth/forgot-password', d),
  resetPassword:  d  => api.post('/auth/reset-password', d),
}

export const dashboardApi = {
  getSummary:       ()        => api.get('/dashboard/summary'),
  getSummaryRange:  (f, t)    => api.get('/dashboard/summary/range', { params: { from: f, to: t } }),
  getCategories:    (f, t)    => api.get('/dashboard/categories', { params: { from: f, to: t } }),
  getMonthlyTrend:  (m = 6)   => api.get('/dashboard/trends/monthly', { params: { months: m } }),
  getWeeklyTrend:   (w = 12)  => api.get('/dashboard/trends/weekly',  { params: { weeks: w } }),
  getHealthScore:   ()        => api.get('/dashboard/health-score'),
  getSpendingByDay: (f, t)    => api.get('/dashboard/spending-by-day', { params: { from: f, to: t } }),
  getTopExpenses:   (f, t, l) => api.get('/dashboard/top-expenses', { params: { from: f, to: t, limit: l } }),
}

export const recordsApi = {
  getAll:    p       => api.get('/records', { params: p }),
  getById:   id      => api.get(`/records/${id}`),
  create:    d       => api.post('/records', d),
  update:    (id, d) => api.put(`/records/${id}`, d),
  delete:    id      => api.delete(`/records/${id}`),
  exportCsv: p       => api.get('/records/export/csv', { params: p, responseType: 'blob' }),
}

export const budgetsApi = {
  getAll:        ()      => api.get('/budgets'),
  create:        d       => api.post('/budgets', d),
  update:        (id, d) => api.put(`/budgets/${id}`, d),
  delete:        id      => api.delete(`/budgets/${id}`),
}

export const recurringApi = {
  getAll:        ()      => api.get('/recurring'),
  create:        d       => api.post('/recurring', d),
  update:        (id, d) => api.put(`/recurring/${id}`, d),
  delete:        id      => api.delete(`/recurring/${id}`),
}

export const notificationsApi = {
  getAll:         p  => api.get('/notifications', { params: p }),
  getUnreadCount: () => api.get('/notifications/unread-count'),
  markRead:       id => api.patch(`/notifications/${id}/read`),
  markAllRead:    () => api.patch('/notifications/mark-all-read'),
}

export const usersApi = {
  getMe:          ()      => api.get('/users/me'),
  updateMe:       d       => api.put('/users/me', d),
  changePassword: d       => api.post('/users/me/change-password', d),
  getAll:         p       => api.get('/users', { params: p }),
  create:         d       => api.post('/users', d),
  update:         (id, d) => api.put(`/users/${id}`, d),
  activate:       id      => api.patch(`/users/${id}/activate`),
  deactivate:     id      => api.patch(`/users/${id}/deactivate`),
  delete:         id      => api.delete(`/users/${id}`),
}

export const auditApi = {
  getAll:    p         => api.get('/audit', { params: p }),
  getByActor:(u, p)    => api.get(`/audit/by-actor/${u}`, { params: p }),
  getByDate: (f, t, p) => api.get('/audit/by-date-range', { params: { from: f, to: t, ...p } }),
}

export const planApi = {
  getPlans:        ()      => api.get('/plans'),
  getPlan:         slug    => api.get(`/plans/${slug}`),
  createOrder:     d       => api.post('/billing/orders', d),
  verifyPayment:   d       => api.post('/billing/verify', d),
  getSubscription: ()      => api.get('/billing/subscription'),
  getHistory:      ()      => api.get('/billing/subscriptions'),
  getPayments:     p       => api.get('/billing/payments', { params: p }),
  cancelSub:       reason  => api.post(`/billing/cancel?reason=${encodeURIComponent(reason || '')}`),
}

export const downloadInvoice = async (paymentId) => {
  const token = localStorage.getItem('accessToken')
  const res = await fetch(`http://localhost:8080/api/billing/payments/${paymentId}/invoice`, {
    headers: { Authorization: `Bearer ${token}` }
  })
  if (!res.ok) throw new Error('Download failed')
  const blob = await res.blob()
  const url  = URL.createObjectURL(blob)
  const a    = document.createElement('a')
  a.href     = url
  a.download = `FinancePro-Invoice-${paymentId}.pdf`
  a.click()
  URL.revokeObjectURL(url)
}