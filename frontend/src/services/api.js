import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// Attach token on every request
api.interceptors.request.use(cfg => {
  const token = localStorage.getItem('accessToken')
  if (token) cfg.headers.Authorization = `Bearer ${token}`
  return cfg
})

// Auto-refresh on 401
api.interceptors.response.use(
  res => res,
  async err => {
    const original = err.config
    if (err.response?.status === 401 && !original._retry) {
      original._retry = true
      const refresh = localStorage.getItem('refreshToken')
      if (refresh) {
        try {
          const { data } = await axios.post('/api/auth/refresh', { refreshToken: refresh })
          const newToken = data.data.accessToken
          localStorage.setItem('accessToken', newToken)
          localStorage.setItem('refreshToken', data.data.refreshToken)
          original.headers.Authorization = `Bearer ${newToken}`
          return api(original)
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

// ── Auth ──────────────────────────────────────────────────────────────────
export const authApi = {
  login: (data) => api.post('/auth/login', data),
  refresh: (token) => api.post('/auth/refresh', { refreshToken: token }),
}

// ── Dashboard ─────────────────────────────────────────────────────────────
export const dashboardApi = {
  getSummary: () => api.get('/dashboard/summary'),
  getSummaryRange: (from, to) => api.get('/dashboard/summary/range', { params: { from, to } }),
  getCategories: (from, to) => api.get('/dashboard/categories', { params: { from, to } }),
  getMonthlyTrend: (months = 6) => api.get('/dashboard/trends/monthly', { params: { months } }),
  getTopExpenses: (from, to, limit = 5) => api.get('/dashboard/top-expenses', { params: { from, to, limit } }),
  getSpendingByDay: (from, to) => api.get('/dashboard/spending-by-day', { params: { from, to } }),
  getHealthScore: () => api.get('/dashboard/health-score'),
}

// ── Records ───────────────────────────────────────────────────────────────
export const recordsApi = {
  getAll: (params) => api.get('/records', { params }),
  getById: (id) => api.get(`/records/${id}`),
  create: (data) => api.post('/records', data),
  update: (id, data) => api.put(`/records/${id}`, data),
  delete: (id) => api.delete(`/records/${id}`),
  exportCsv: (params) => api.get('/records/export/csv', { params, responseType: 'blob' }),
}

// ── Budgets ───────────────────────────────────────────────────────────────
export const budgetsApi = {
  getAll: () => api.get('/budgets'),
  getById: (id) => api.get(`/budgets/${id}`),
  create: (data) => api.post('/budgets', data),
  update: (id, data) => api.put(`/budgets/${id}`, data),
  delete: (id) => api.delete(`/budgets/${id}`),
}

// ── Recurring ─────────────────────────────────────────────────────────────
export const recurringApi = {
  getAll: () => api.get('/recurring'),
  create: (data) => api.post('/recurring', data),
  update: (id, data) => api.put(`/recurring/${id}`, data),
  delete: (id) => api.delete(`/recurring/${id}`),
}

// ── Notifications ─────────────────────────────────────────────────────────
export const notificationsApi = {
  getAll: (params) => api.get('/notifications', { params }),
  getUnreadCount: () => api.get('/notifications/unread-count'),
  markRead: (id) => api.patch(`/notifications/${id}/read`),
  markAllRead: () => api.patch('/notifications/read-all'),
}

// ── Users ─────────────────────────────────────────────────────────────────
export const usersApi = {
  getMe: () => api.get('/users/me'),
  updateMe: (data) => api.put('/users/me', data),
  changePassword: (data) => api.post('/users/me/change-password', data),
  getAll: (params) => api.get('/users', { params }),
  create: (data) => api.post('/users', data),
  update: (id, data) => api.put(`/users/${id}`, data),
  activate: (id) => api.patch(`/users/${id}/activate`),
  deactivate: (id) => api.patch(`/users/${id}/deactivate`),
  delete: (id) => api.delete(`/users/${id}`),
}

// ── Audit ─────────────────────────────────────────────────────────────────
export const auditApi = {
  getAll: (params) => api.get('/audit', { params }),
  getByActor: (username, params) => api.get(`/audit/actor/${username}`, { params }),
}
