import { useEffect, useState } from 'react'
import { usersApi } from '../services/api'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../components/ui/Toast'
import Spinner from '../components/ui/Spinner'
import { User, Lock } from 'lucide-react'

const ROLE_BADGE = { ADMIN:'badge-purple', ANALYST:'badge-blue', VIEWER:'badge-gray' }
const PASSWORD_RULE = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#^()_+\-=]).{8,64}$/

export default function ProfilePage() {
  const toast = useToast()
  const { refreshUser } = useAuth()
  const [me, setMe] = useState(null)
  const [loading, setLoading] = useState(true)
  const [savingInfo, setSavingInfo] = useState(false)
  const [savingPw, setSavingPw] = useState(false)
  const [info, setInfo] = useState({ fullName:'', email:'' })
  const [pw, setPw] = useState({ currentPassword:'', newPassword:'', confirmPassword:'' })

  useEffect(() => {
    usersApi.getMe().then(({ data }) => {
      setMe(data.data)
      setInfo({ fullName: data.data.fullName, email: data.data.email })
    }).catch(() => toast('Failed to load profile', 'error'))
      .finally(() => setLoading(false))
  }, [])

  const saveInfo = async () => {
    setSavingInfo(true)
    try {
      await usersApi.updateMe(info)
      toast('Profile updated', 'success')
      refreshUser()
    } catch (e) {
      toast(Object.values(e.response?.data?.errors ?? {}).join(', ') || e.response?.data?.message || 'Update failed', 'error')
    } finally { setSavingInfo(false) }
  }

  const savePassword = async () => {
    if (pw.newPassword !== pw.confirmPassword) return toast('New passwords do not match', 'error')
    if (!PASSWORD_RULE.test(pw.newPassword)) return toast('Password needs 8+ chars, uppercase, lowercase, digit and special character', 'error')
    setSavingPw(true)
    try {
      await usersApi.changePassword(pw)
      toast('Password changed successfully', 'success')
      setPw({ currentPassword:'', newPassword:'', confirmPassword:'' })
    } catch (e) {
      toast(Object.values(e.response?.data?.errors ?? {}).join(', ') || e.response?.data?.message || 'Password change failed', 'error')
    } finally { setSavingPw(false) }
  }

  if (loading) return <div className="flex items-center justify-center h-64"><Spinner size="lg"/></div>

  return (
    <div className="max-w-2xl space-y-6">
      <div className="card p-5">
        <div className="flex items-center gap-4">
          <div className="w-14 h-14 rounded-full bg-brand-100 flex items-center justify-center text-lg font-bold text-brand-700 shrink-0">
            {me?.fullName?.[0]?.toUpperCase() ?? 'U'}
          </div>
          <div className="min-w-0">
            <p className="font-semibold text-slate-900">{me?.fullName}</p>
            <p className="text-sm text-slate-500">@{me?.username}</p>
          </div>
          <span className={`${ROLE_BADGE[me?.role] ?? 'badge-gray'} ml-auto`}>{me?.role}</span>
        </div>
      </div>

      <div className="card p-5">
        <div className="flex items-center gap-2 mb-4">
          <User className="w-4 h-4 text-slate-500"/>
          <h2 className="font-semibold text-slate-900 text-sm">Profile Information</h2>
        </div>
        <div className="space-y-3">
          <div><label className="label">Full Name</label>
            <input className="input" value={info.fullName} onChange={e=>setInfo(f=>({...f,fullName:e.target.value}))}/>
          </div>
          <div><label className="label">Email</label>
            <input className="input" type="email" value={info.email} onChange={e=>setInfo(f=>({...f,email:e.target.value}))}/>
          </div>
          <div className="flex justify-end pt-1">
            <button className="btn-primary" onClick={saveInfo} disabled={savingInfo}>{savingInfo?'Saving…':'Save Changes'}</button>
          </div>
        </div>
      </div>

      <div className="card p-5">
        <div className="flex items-center gap-2 mb-4">
          <Lock className="w-4 h-4 text-slate-500"/>
          <h2 className="font-semibold text-slate-900 text-sm">Change Password</h2>
        </div>
        <div className="space-y-3">
          <div><label className="label">Current Password</label>
            <input className="input" type="password" value={pw.currentPassword} onChange={e=>setPw(f=>({...f,currentPassword:e.target.value}))}/>
          </div>
          <div><label className="label">New Password</label>
            <input className="input" type="password" placeholder="Min 8 chars, uppercase, lowercase, digit, special char"
              value={pw.newPassword} onChange={e=>setPw(f=>({...f,newPassword:e.target.value}))}/>
          </div>
          <div><label className="label">Confirm New Password</label>
            <input className="input" type="password" value={pw.confirmPassword} onChange={e=>setPw(f=>({...f,confirmPassword:e.target.value}))}/>
          </div>
          <div className="flex justify-end pt-1">
            <button className="btn-primary" onClick={savePassword} disabled={savingPw}>{savingPw?'Updating…':'Update Password'}</button>
          </div>
        </div>
      </div>
    </div>
  )
}