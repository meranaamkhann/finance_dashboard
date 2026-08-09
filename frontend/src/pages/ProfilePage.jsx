import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { usersApi, planApi } from '../services/api'
import { useToast } from '../components/ui/Toast'
import { User, Lock, CreditCard, FileText, LogOut, ExternalLink, Eye, EyeOff } from 'lucide-react'

export default function ProfilePage() {
  const { user, logout }    = useAuth()
  const toast               = useToast()
  const nav                 = useNavigate()
  const [tab, setTab]       = useState('profile')
  const [loading, setLoading]   = useState(false)
  const [sub, setSub]           = useState(null)
  const [payments, setPayments] = useState([])
  const [profileForm, setProfileForm] = useState({
    fullName: user?.fullName || '',
    email:    user?.email    || '',
  })
  const [pwForm, setPwForm] = useState({
    currentPassword:  '',
    newPassword:      '',
    confirmPassword:  '',
  })
const [showPw, setShowPw] = useState({ current: false, new: false, confirm: false })

  useEffect(() => {
    if (tab === 'billing') {
      planApi.getSubscription()
        .then(r => setSub(r.data.data))
        .catch(() => setSub(null))
      planApi.getPayments({ page: 0, size: 10 })
        .then(r => setPayments(r.data.data?.content || []))
        .catch(() => setPayments([]))
    }
  }, [tab])

  const saveProfile = async () => {
    setLoading(true)
    try {
      await usersApi.updateMe({ fullName: profileForm.fullName })
      toast('Profile updated successfully', 'success')
    } catch (e) {
      toast(e.response?.data?.message || 'Failed to update profile', 'error')
    } finally { setLoading(false) }
  }

  const changePassword = async () => {
    if (!pwForm.currentPassword || !pwForm.newPassword) {
      toast('Please fill all password fields', 'error'); return
    }
    if (pwForm.newPassword !== pwForm.confirmPassword) {
      toast('Passwords do not match', 'error'); return
    }
    setLoading(true)
    try {
      await usersApi.changePassword({
        currentPassword: pwForm.currentPassword,
        newPassword:     pwForm.newPassword,
        confirmPassword: pwForm.confirmPassword,
      })
      toast('Password changed successfully', 'success')
      setPwForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
    } catch (e) {
      toast(e.response?.data?.message || 'Failed to change password', 'error')
    } finally { setLoading(false) }
  }

  const TABS = [
    { id: 'profile',  label: 'Profile',  icon: User },
    { id: 'security', label: 'Security', icon: Lock },
    { id: 'billing',  label: 'Billing',  icon: CreditCard },
  ]

  const pwStrength = (pw) => {
    let s = 0
    if (pw.length >= 8)           s++
    if (/[A-Z]/.test(pw))         s++
    if (/[0-9]/.test(pw))         s++
    if (/[@$!%*?&#]/.test(pw))    s++
    return s
  }
  const s      = pwStrength(pwForm.newPassword)
  const sLabel = ['Too weak','Weak','Fair','Good','Strong'][s]
  const sColor = ['bg-red-500','bg-orange-400','bg-yellow-400','bg-blue-400','bg-green-500'][s]

  return (
    <div className="max-w-2xl mx-auto space-y-5">

      {/* Header card */}
      <div className="card p-6">
        <div className="flex items-center gap-4 flex-wrap">
          <div className="w-14 h-14 rounded-2xl flex items-center justify-center text-xl font-bold text-white shrink-0"
               style={{ background: 'var(--brand)' }}>
            {(user?.fullName || user?.username || 'U')[0].toUpperCase()}
          </div>
          <div className="flex-1 min-w-0">
            <p className="font-semibold text-base truncate" style={{ color: 'var(--text-main)' }}>
              {user?.fullName || user?.username}
            </p>
            <p className="text-xs mt-0.5" style={{ color: 'var(--text-muted)' }}>
              @{user?.username} &nbsp;·&nbsp;
              <span className="px-1.5 py-0.5 rounded text-xs font-medium"
                    style={{ background: 'var(--brand-light)', color: 'var(--brand)' }}>
                {user?.role}
              </span>
            </p>
          </div>
          <button onClick={logout}
            className="flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium text-red-600 hover:bg-red-50 transition-colors">
            <LogOut className="w-4 h-4"/> Logout
          </button>
        </div>
      </div>

      {/* Tab bar */}
      <div className="flex gap-1 p-1 rounded-xl" style={{ background: 'var(--border)' }}>
        {TABS.map(t => (
          <button key={t.id} onClick={() => setTab(t.id)}
            className="flex-1 flex items-center justify-center gap-2 py-2 rounded-lg text-sm font-medium transition-all"
            style={{
              background: tab === t.id ? 'var(--bg-card)' : 'transparent',
              color:      tab === t.id ? 'var(--brand)'   : 'var(--text-muted)',
              boxShadow:  tab === t.id ? '0 1px 3px rgba(0,0,0,.08)' : 'none',
            }}>
            <t.icon className="w-4 h-4"/>
            {t.label}
          </button>
        ))}
      </div>

      {/* Profile tab */}
      {tab === 'profile' && (
        <div className="card p-6 space-y-4">
          <h3 className="font-semibold" style={{ color: 'var(--text-main)' }}>
            Profile Information
          </h3>
          <div>
            <label className="label">Full Name</label>
            <input className="input" value={profileForm.fullName}
              onChange={e => setProfileForm(f => ({ ...f, fullName: e.target.value }))}
              placeholder="Your full name"/>
          </div>
          <div>
            <label className="label">Email</label>
            <input className="input" value={user?.email || ''} disabled
              style={{ opacity: 0.5, cursor: 'not-allowed' }}/>
            <p className="text-xs mt-1" style={{ color: 'var(--text-faint)' }}>
              Email cannot be changed here
            </p>
          </div>
          <div>
            <label className="label">Username</label>
            <input className="input" value={user?.username || ''} disabled
              style={{ opacity: 0.5, cursor: 'not-allowed' }}/>
          </div>
          <button onClick={saveProfile} disabled={loading} className="btn-primary">
            {loading ? 'Saving…' : 'Save Changes'}
          </button>
        </div>
      )}

      {/* Security tab */}
    {tab === 'security' && (
    <div className="card p-6 space-y-4">
        <h3 className="font-semibold" style={{ color: 'var(--text-main)' }}>
        Change Password
        </h3>

        <div>
        <label className="label">Current Password</label>
        <div className="relative">
            <input className="input pr-10" type={showPw.current ? 'text' : 'password'}
            value={pwForm.currentPassword}
            onChange={e => setPwForm(f => ({ ...f, currentPassword: e.target.value }))}
            placeholder="Enter current password"/>
            <button type="button"
            onClick={() => setShowPw(s => ({ ...s, current: !s.current }))}
            className="absolute right-3 top-1/2 -translate-y-1/2 transition-colors"
            style={{ color: 'var(--text-faint)' }}>
            {showPw.current ? <EyeOff className="w-4 h-4"/> : <Eye className="w-4 h-4"/>}
            </button>
        </div>
        </div>

        <div>
        <label className="label">New Password</label>
        <div className="relative">
            <input className="input pr-10" type={showPw.new ? 'text' : 'password'}
            value={pwForm.newPassword}
            onChange={e => setPwForm(f => ({ ...f, newPassword: e.target.value }))}
            placeholder="At least 8 chars"/>
            <button type="button"
            onClick={() => setShowPw(s => ({ ...s, new: !s.new }))}
            className="absolute right-3 top-1/2 -translate-y-1/2 transition-colors"
            style={{ color: 'var(--text-faint)' }}>
            {showPw.new ? <EyeOff className="w-4 h-4"/> : <Eye className="w-4 h-4"/>}
            </button>
        </div>
        {pwForm.newPassword && (
            <div className="mt-2">
            <div className="flex gap-1 mb-1">
                {[0,1,2,3].map(i => (
                <div key={i}
                    className={`h-1 flex-1 rounded-full transition-colors ${i < s ? sColor : 'bg-slate-200 dark:bg-slate-700'}`}/>
                ))}
            </div>
            <p className="text-xs" style={{ color: 'var(--text-faint)' }}>{sLabel}</p>
            </div>
        )}
        </div>

        <div>
        <label className="label">Confirm New Password</label>
        <div className="relative">
            <input className="input pr-10" type={showPw.confirm ? 'text' : 'password'}
            value={pwForm.confirmPassword}
            onChange={e => setPwForm(f => ({ ...f, confirmPassword: e.target.value }))}
            placeholder="Repeat new password"/>
            <button type="button"
            onClick={() => setShowPw(s => ({ ...s, confirm: !s.confirm }))}
            className="absolute right-3 top-1/2 -translate-y-1/2 transition-colors"
            style={{ color: 'var(--text-faint)' }}>
            {showPw.confirm ? <EyeOff className="w-4 h-4"/> : <Eye className="w-4 h-4"/>}
            </button>
        </div>
        {pwForm.confirmPassword && pwForm.newPassword !== pwForm.confirmPassword && (
            <p className="text-xs text-red-500 mt-1">Passwords do not match</p>
        )}
        </div>

        <button onClick={changePassword} disabled={loading || s < 3} className="btn-primary">
        {loading ? 'Changing…' : 'Change Password'}
        </button>
    </div>
    )}
      {/* Billing tab */}
      {tab === 'billing' && (
        <div className="space-y-4">
          <div className="card p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-semibold" style={{ color: 'var(--text-main)' }}>
                Current Plan
              </h3>
              <button onClick={() => nav('/pricing')} className="btn-secondary text-xs gap-1">
                <ExternalLink className="w-3.5 h-3.5"/> Upgrade
              </button>
            </div>
            {sub ? (
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="font-bold text-lg" style={{ color: 'var(--text-main)' }}>
                    {sub.plan?.name} Plan
                  </span>
                  <span className={sub.active ? 'badge-green' : 'badge-red'}>
                    {sub.status}
                  </span>
                </div>
                <div className="grid grid-cols-2 gap-3">
                  {[
                    ['Billing Cycle', sub.billingCycle],
                    ['Renews',        sub.endDate],
                    ['Days Left',     sub.daysRemaining + ' days'],
                    ['Auto Renew',    sub.autoRenew ? 'Yes' : 'No'],
                  ].map(([label, value]) => (
                    <div key={label} className="p-3 rounded-lg" style={{ background: 'var(--bg-page)' }}>
                      <p className="text-xs mb-1" style={{ color: 'var(--text-faint)' }}>{label}</p>
                      <p className="text-sm font-medium" style={{ color: 'var(--text-main)' }}>{value}</p>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <div className="text-center py-6">
                <p className="text-sm mb-4" style={{ color: 'var(--text-muted)' }}>
                  You are on the Free plan
                </p>
                <button onClick={() => nav('/pricing')} className="btn-primary">
                  Upgrade Now
                </button>
              </div>
            )}
          </div>

          <div className="card p-6">
            <h3 className="font-semibold mb-4" style={{ color: 'var(--text-main)' }}>
              Payment History
            </h3>
            {payments.length === 0 ? (
              <p className="text-sm text-center py-6" style={{ color: 'var(--text-muted)' }}>
                No payments yet
              </p>
            ) : (
              <div className="space-y-2">
                {payments.map(p => (
                  <div key={p.id}
                    className="flex items-center justify-between p-3 rounded-xl border transition-colors"
                    style={{ borderColor: 'var(--border)' }}>
                    <div className="min-w-0 flex-1">
                      <p className="text-sm font-medium truncate" style={{ color: 'var(--text-main)' }}>
                        {p.planName} — {p.billingCycle}
                      </p>
                      <p className="text-xs mt-0.5" style={{ color: 'var(--text-faint)' }}>
                        {p.invoiceNumber} · {p.paidAt?.slice(0, 10) || p.createdAt?.slice(0, 10)}
                      </p>
                    </div>
                    <div className="flex items-center gap-2 shrink-0 ml-3">
                      <span className="text-sm font-semibold" style={{ color: 'var(--text-main)' }}>
                        ₹{p.amount}
                      </span>
                      <span className={p.status === 'SUCCESS' ? 'badge-green' : 'badge-red'}>
                        {p.status}
                      </span>
                      {p.status === 'SUCCESS' && (
                        <a href={`/api/billing/payments/${p.id}/invoice`}
                          target="_blank" rel="noreferrer"
                          className="p-1.5 rounded hover:opacity-80 transition-opacity"
                          style={{ color: 'var(--brand)' }}
                          title="Download PDF invoice">
                          <FileText className="w-4 h-4"/>
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