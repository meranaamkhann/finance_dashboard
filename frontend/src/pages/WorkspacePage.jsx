import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../services/api'
import { useToast } from '../components/ui/Toast'
import Modal from '../components/ui/Modal'
import Spinner from '../components/ui/Spinner'
import { Users, Plus, Trash2, Crown, Edit2, AlertCircle } from 'lucide-react'

const ROLE_BADGE = {
  OWNER:   'badge-blue',
  ANALYST: 'badge-green',
  VIEWER:  'badge-gray',
}

export default function WorkspacePage() {
  const toast = useToast()
  const nav   = useNavigate()
  const [workspace, setWorkspace] = useState(null)
  const [loading, setLoading]     = useState(true)
  const [error, setError]         = useState('')
  const [modal, setModal]         = useState(null)
  const [editing, setEditing]     = useState(null)
  const [form, setForm]           = useState({ email: '', role: 'VIEWER' })
  const [saving, setSaving]       = useState(false)

  const load = async () => {
    setLoading(true)
    setError('')
    try {
      const { data } = await api.get('/workspace')
      setWorkspace(data.data)
    } catch (e) {
      const msg = e.response?.data?.message || 'Failed to load workspace'
      setError(msg)
    } finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const invite = async () => {
    if (!form.email.trim()) return
    setSaving(true)
    try {
      await api.post('/workspace/members', form)
      toast('Member invited successfully', 'success')
      setModal(null)
      setForm({ email: '', role: 'VIEWER' })
      load()
    } catch (e) {
      toast(e.response?.data?.message || 'Failed to invite', 'error')
    } finally { setSaving(false) }
  }

  const changeRole = async (userId, newRole) => {
    try {
      await api.put(`/workspace/members/${userId}/role?role=${newRole}`)
      toast('Role updated', 'success')
      load()
    } catch (e) {
      toast(e.response?.data?.message || 'Failed', 'error')
    }
  }

  const remove = async (userId, name) => {
    if (!confirm(`Remove ${name} from workspace?`)) return
    try {
      await api.delete(`/workspace/members/${userId}`)
      toast('Member removed', 'success')
      load()
    } catch (e) {
      toast(e.response?.data?.message || 'Failed', 'error')
    }
  }

  if (loading) return (
    <div className="flex items-center justify-center h-64"><Spinner size="lg"/></div>
  )

  if (error) return (
    <div className="max-w-xl mx-auto mt-8">
      <div className="card p-8 text-center">
        <AlertCircle className="w-12 h-12 mx-auto mb-4 text-red-500"/>
        <h3 className="font-semibold mb-2" style={{ color: 'var(--text-main)' }}>
          Could not load workspace
        </h3>
        <p className="text-sm mb-4" style={{ color: 'var(--text-muted)' }}>{error}</p>
        <button onClick={load} className="btn-primary">Try Again</button>
      </div>
    </div>
  )

  const isOwner = workspace?.members?.find(
    m => m.role === 'OWNER'
  )
  const canInvite = workspace && workspace.memberCount < workspace.maxMembers
  const analystCount = workspace?.members?.filter(
    m => m.role === 'ANALYST' || m.role === 'OWNER'
  ).length || 0

  return (
    <div className="space-y-5 max-w-3xl">

      <div className="card p-5">
        <div className="flex items-start justify-between flex-wrap gap-3">
          <div>
            <h2 className="font-semibold text-lg" style={{ color: 'var(--text-main)' }}>
              {workspace?.name}
            </h2>
            <p className="text-xs mt-1" style={{ color: 'var(--text-muted)' }}>
              {workspace?.memberCount} / {workspace?.maxMembers} analyst seats used
              &nbsp;·&nbsp;
              Unlimited viewers
            </p>
          </div>
          <button
            onClick={() => setModal('invite')}
            disabled={!canInvite && form.role === 'ANALYST'}
            className="btn-primary gap-2">
            <Plus className="w-4 h-4"/> Invite Member
          </button>
        </div>

        {!canInvite && (
          <div className="mt-4 flex items-start gap-2 px-3 py-2.5 rounded-lg text-sm"
               style={{ background: 'var(--brand-light)', color: 'var(--brand)' }}>
            <AlertCircle className="w-4 h-4 shrink-0 mt-0.5"/>
            <span>
              Analyst seats full ({analystCount}/{workspace?.maxMembers}).
              You can still invite unlimited <strong>Viewers</strong>.{' '}
              <a href="/pricing" className="underline font-semibold">Upgrade to Team</a>{' '}
              for 5 analyst seats.
            </span>
          </div>
        )}
      </div>

      <div className="card overflow-hidden">
        <div className="px-5 py-3.5 border-b"
             style={{ borderColor: 'var(--border)', background: 'var(--bg-page)' }}>
          <p className="text-xs font-semibold uppercase tracking-wide"
             style={{ color: 'var(--text-faint)' }}>
            Workspace Members ({workspace?.members?.length || 0})
          </p>
        </div>

        {!workspace?.members?.length ? (
          <div className="flex flex-col items-center justify-center py-12">
            <Users className="w-10 h-10 mb-3" style={{ color: 'var(--text-faint)' }}/>
            <p className="text-sm font-medium" style={{ color: 'var(--text-main)' }}>
              No members yet
            </p>
            <p className="text-xs mt-1 mb-4" style={{ color: 'var(--text-faint)' }}>
              Invite team members to collaborate
            </p>
            <button onClick={() => setModal('invite')} className="btn-primary gap-2">
              <Plus className="w-4 h-4"/> Invite First Member
            </button>
          </div>
        ) : (
          <div>
            {workspace.members.map(m => (
              <div key={m.id}
                className="flex items-center justify-between px-5 py-4 border-b last:border-0"
                style={{ borderColor: 'var(--border)' }}>
                <div className="flex items-center gap-3 min-w-0">
                  <div className="w-9 h-9 rounded-full flex items-center justify-center text-sm font-bold text-white shrink-0"
                       style={{ background: 'var(--brand)' }}>
                    {(m.fullName || m.username)[0].toUpperCase()}
                  </div>
                  <div className="min-w-0">
                    <div className="flex items-center gap-2">
                      <p className="text-sm font-medium truncate" style={{ color: 'var(--text-main)' }}>
                        {m.fullName}
                      </p>
                      {m.role === 'OWNER' && <Crown className="w-3.5 h-3.5 text-amber-500 shrink-0"/>}
                    </div>
                    <p className="text-xs truncate" style={{ color: 'var(--text-faint)' }}>
                      @{m.username} · {m.email}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-2 shrink-0 ml-3">
                  <span className={ROLE_BADGE[m.role] || 'badge-gray'}>
                    {m.role}
                  </span>
                  {m.role !== 'OWNER' && (
                    <>
                      <select
                        value={m.role}
                        onChange={e => changeRole(m.userId, e.target.value)}
                        className="text-xs border rounded-lg px-2 py-1 cursor-pointer"
                        style={{
                          borderColor: 'var(--border)',
                          background: 'var(--bg-card)',
                          color: 'var(--text-main)'
                        }}>
                        <option value="ANALYST">Analyst</option>
                        <option value="VIEWER">Viewer</option>
                      </select>
                      <button
                        onClick={() => remove(m.userId, m.fullName || m.username)}
                        className="p-1.5 rounded hover:bg-red-50 transition-colors text-slate-400 hover:text-red-500">
                        <Trash2 className="w-4 h-4"/>
                      </button>
                    </>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <Modal open={modal === 'invite'} onClose={() => setModal(null)} title="Invite Team Member">
        <div className="space-y-4">
          <div className="px-3 py-2.5 rounded-lg text-sm"
               style={{ background: 'var(--bg-page)', color: 'var(--text-muted)' }}>
            The person must already have a FinancePro account registered with this email.
          </div>
          <div>
            <label className="label">Email address</label>
            <input className="input" type="email" placeholder="colleague@example.com"
              value={form.email}
              onChange={e => setForm(f => ({...f, email: e.target.value}))}/>
          </div>
          <div>
            <label className="label">Role</label>
            <select className="input" value={form.role}
              onChange={e => setForm(f => ({...f, role: e.target.value}))}>
              <option value="VIEWER">Viewer — read only, no limits</option>
              <option value="ANALYST">Analyst — can add/edit records (uses analyst seat)</option>
            </select>
            {form.role === 'ANALYST' && !canInvite && (
              <p className="text-xs text-red-500 mt-1">
                No analyst seats available. Upgrade or choose Viewer.
              </p>
            )}
          </div>
          <div className="flex gap-3 justify-end pt-2">
            <button className="btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button
              className="btn-primary"
              onClick={invite}
              disabled={saving || !form.email.trim() || (form.role === 'ANALYST' && !canInvite)}>
              {saving ? 'Inviting…' : 'Send Invite'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  )
}