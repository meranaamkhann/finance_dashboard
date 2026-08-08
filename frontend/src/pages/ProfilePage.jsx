import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { usersApi, planApi } from '../services/api'
import { useToast } from '../components/ui/Toast'
import Spinner from '../components/ui/Spinner'
import { User, Lock, CreditCard, FileText, LogOut } from 'lucide-react'

export default function ProfilePage() {
  const { user, logout }  = useAuth()
  const toast             = useToast()
  const [tab, setTab]     = useState('profile')
  const [loading, setLoading]       = useState(false)
  const [sub, setSub]               = useState(null)
  const [payments, setPayments]     = useState([])
  const [pwForm, setPwForm]         = useState({ currentPassword:'', newPassword:'', confirmPassword:'' })
  const [profileForm, setProfileForm] = useState({ fullName: user?.fullName || '' })

  useEffect(() => {
    if (tab === 'billing') {
      usersApi.getMe().catch(() => {})
      import('../services/planApi').then(m => {
        m.planApi.getSubscription().then(r => setSub(r.data.data)).catch(() => {})
        m.planApi.getPayments({ page: 0, size: 10 }).then(r => setPayments(r.data.data?.content || [])).catch(() => {})
      })
    }
  }, [tab])

  const saveProfile = async () => {
    setLoading(true)
    try {
      await usersApi.updateMe(profileForm)
      toast('Profile updated', 'success')
    } catch (e) {
      toast(e.response?.data?.message || 'Failed', 'error')
    } finally { setLoading(false) }
  }

  const changePassword = async () => {
    if (pwForm.newPassword !== pwForm.confirmPassword) {
      toast('Passwords do not match', 'error'); return
    }
    setLoading(true)
    try {
      await usersApi.changePassword(pwForm)
      toast('Password changed successfully', 'success')
      setPwForm({ currentPassword:'', newPassword:'', confirmPassword:'' })
    } catch (e) {
      toast(e.response?.data?.message || 'Failed', 'error')
    } finally { setLoading(false) }
  }

  const TABS = [
    { id: 'profile',  label: 'Profile',   icon: User },
    { id: 'security', label: 'Security',  icon: Lock },
    { id: 'billing',  label: 'Billing',   icon: CreditCard },
  ]

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="card p-6">
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 rounded-2xl flex items-center justify-center text-2xl font-bold text-white"
               style={{ background: 'var(--brand)' }}>
            {user?.fullName?.[0]?.toUpperCase() || 'U'}
          </div>
          <div>
            <h2 className="text-lg font-bold" style={{ color: 'var(--text-main)' }}>
              {user?.fullName}
            </h2>
            <p className="text-sm" style={{ color: 'var(--text-muted)' }}>
              @{user?.username} · {user?.role}
            </p>
          </div>
          <button onClick={logout} className="btn-secondary ml-auto gap-2">
            <LogOut className="w-4 h-4"/> Logout
          </button>
        </div>
      </div>

      <div className="flex gap-1 p-1 rounded-xl" style={{ background: 'var(--border)' }}>
        {TABS.map(t => (
          <button key={t.id} onClick={() => setTab(t.id)}
            className="flex-1 flex items-center justify-center gap-2 py-2 rounded-lg text-sm font-medium transition-all"
            style={{
              background: tab === t.id ? 'var(--bg-card)' : 'transparent',
              color: tab === t.id ? 'var(--brand)' : 'var(--text-muted)',
              boxShadow: tab === t.id ? '0 1px 3px rgba(0,0,0,.1)' : 'none'
            }}>
            <t.icon className="w-4 h-4"/> {t.label}
          </button>
        ))}
      </div>

      {tab === 'profile' && (
        <div className="card p-6 space-y-4">
          <h3 className="font-semibold" style={{ color: 'var(--text-main)' }}>Profile Information</h3>
          <div>
            <label className="label">Full Name</label>
            <input className="input" value={profileForm.fullName}
              onChange={e => setProfileForm(f => ({...f, fullName: e.target.value}))}/>
          </div>
          <div>
            <label className="label">Email</label>
            <input className="input" value={user?.email || ''} disabled
              style={{ opacity: 0.6, cursor: 'not-allowed' }}/>
          </div>
          <div>
            <label className="label">Role</label>
            <input className="input" value={user?.role || ''} disabled
              style={{ opacity: 0.6, cursor: 'not-allowed' }}/>
          </div>
          <button onClick={saveProfile} disabled={loading} className="btn-primary">
            {loading ? 'Saving…' : 'Save Changes'}
          </button>
        </div>
      )}

      {tab === 'security' && (
        <div className="card p-6 space-y-4">
          <h3 className="font-semibold" style={{ color: 'var(--text-main)' }}>Change Password</h3>
          <div>
            <label className="label">Current Password</label>
            <input className="input" type="password" value={pwForm.currentPassword}
              onChange={e => setPwForm(f => ({...f, currentPassword: e.target.value}))}/>
          </div>
          <div>
            <label className="label">New Password</label>
            <input className="input" type="password" value={pwForm.newPassword}
              onChange={e => setPwForm(f => ({...f, newPassword: e.target.value}))}/>
            <p className="text-xs mt-1" style={{ color: 'var(--text-faint)' }}>
              Min 8 chars, must include uppercase, lowercase, number and special character
            </p>
          </div>
          <div>
            <label className="label">Confirm New Password</label>
            <input className="input" type="password" value={pwForm.confirmPassword}
              onChange={e => setPwForm(f => ({...f, confirmPassword: e.target.value}))}/>
            {pwForm.confirmPassword && pwForm.newPassword !== pwForm.confirmPassword && (
              <p className="text-xs text-red-500 mt-1">Passwords do not match</p>
            )}
          </div>
          <button onClick={changePassword} disabled={loading} className="btn-primary">
            {loading ? 'Changing…' : 'Change Password'}
          </button>
        </div>
      )}

      {tab === 'billing' && (
        <div className="space-y-4">
          <div className="card p-6">
            <h3 className="font-semibold mb-4" style={{ color: 'var(--text-main)' }}>Current Plan</h3>
            {sub ? (
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="font-semibold text-lg" style={{ color: 'var(--text-main)' }}>
                    {sub.plan?.name} Plan
                  </span>
                  <span className={`text-xs px-2 py-1 rounded-full font-medium ${
                    sub.status === 'ACTIVE' ? 'badge-green' : 'badge-red'
                  }`}>{sub.status}</span>
                </div>
                <div className="grid grid-cols-2 gap-3 text-sm">
                  <div>
                    <p style={{ color: 'var(--text-faint)' }}>Billing</p>
                    <p className="font-medium" style={{ color: 'var(--text-main)' }}>{sub.billingCycle}</p>
                  </div>
                  <div>
                    <p style={{ color: 'var(--text-faint)' }}>Renews</p>
                    <p className="font-medium" style={{ color: 'var(--text-main)' }}>{sub.endDate}</p>
                  </div>
                  <div>
                    <p style={{ color: 'var(--text-faint)' }}>Days Remaining</p>
                    <p className="font-medium" style={{ color: 'var(--text-main)' }}>{sub.daysRemaining}</p>
                  </div>
                  <div>
                    <p style={{ color: 'var(--text-faint)' }}>Auto Renew</p>
                    <p className="font-medium" style={{ color: 'var(--text-main)' }}>
                      {sub.autoRenew ? 'Yes' : 'No'}
                    </p>
                  </div>
                </div>
                <div className="flex gap-3 pt-2">
                  <button onClick={() => window.location.href = '/pricing'}
                    className="btn-primary text-sm">Upgrade Plan</button>
                </div>
              </div>
            ) : (
              <div className="text-center py-6">
                <p className="text-sm mb-3" style={{ color: 'var(--text-muted)' }}>
                  You are on the Free plan
                </p>
                <button onClick={() => window.location.href = '/pricing'}
                  className="btn-primary">Upgrade Now</button>
              </div>
            )}
          </div>

          <div className="card p-6">
            <h3 className="font-semibold mb-4" style={{ color: 'var(--text-main)' }}>Payment History</h3>
            {payments.length === 0 ? (
              <p className="text-sm text-center py-4" style={{ color: 'var(--text-muted)' }}>
                No payments yet
              </p>
            ) : (
              <div className="space-y-3">
                {payments.map(p => (
                  <div key={p.id} className="flex items-center justify-between p-3 rounded-lg border"
                       style={{ borderColor: 'var(--border)' }}>
                    <div>
                      <p className="text-sm font-medium" style={{ color: 'var(--text-main)' }}>
                        {p.planName} — {p.billingCycle}
                      </p>
                      <p className="text-xs" style={{ color: 'var(--text-faint)' }}>
                        {p.invoiceNumber} · {p.paidAt?.slice(0, 10)}
                      </p>
                    </div>
                    <div className="flex items-center gap-3">
                      <span className="font-semibold text-sm" style={{ color: 'var(--text-main)' }}>
                        ₹{p.amount}
                      </span>
                      <span className={`text-xs px-2 py-0.5 rounded-full ${
                        p.status === 'SUCCESS' ? 'badge-green' : 'badge-red'
                      }`}>{p.status}</span>
                      {p.status === 'SUCCESS' && (
                        <a href={`http://localhost:8080/api/billing/payments/${p.id}/invoice`}
                          target="_blank" rel="noreferrer"
                          className="text-xs flex items-center gap-1 hover:underline"
                          style={{ color: 'var(--brand)' }}>
                          <FileText className="w-3.5 h-3.5"/> PDF
                        </a>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}