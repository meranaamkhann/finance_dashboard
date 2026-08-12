import { useEffect, useState } from 'react'
import api from '../services/api'
import { useToast } from '../components/ui/Toast'
import Modal from '../components/ui/Modal'
import Spinner from '../components/ui/Spinner'
import EmptyState from '../components/ui/EmptyState'
import { Users, Plus, Trash2, Crown, UserCheck } from 'lucide-react'

const ROLE_COLORS = {
  OWNER:   'badge-blue',
  ANALYST: 'badge-green',
  VIEWER:  'badge-gray',
}

export default function WorkspacePage() {
  const toast = useToast()
  const [workspace, setWorkspace] = useState(null)
  const [loading, setLoading]     = useState(true)
  const [modal, setModal]         = useState(false)
  const [form, setForm]           = useState({ email: '', role: 'VIEWER' })
  const [saving, setSaving]       = useState(false)

  const load = async () => {
    setLoading(true)
    try {
      const { data } = await api.get('/workspace')
      setWorkspace(data.data)
    } catch { toast('Failed to load workspace', 'error') }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const invite = async () => {
    setSaving(true)
    try {
      await api.post('/workspace/members', form)
      toast('Member invited successfully', 'success')
      setModal(false)
      setForm({ email: '', role: 'VIEWER' })
      load()
    } catch (e) {
      toast(e.response?.data?.message || 'Failed to invite', 'error')
    } finally { setSaving(false) }
  }

  const remove = async (userId, username) => {
    if (!confirm(`Remove ${username} from workspace?`)) return
    try {
      await api.delete(`/workspace/members/${userId}`)
      toast('Member removed', 'success')
      load()
    } catch (e) {
      toast(e.response?.data?.message || 'Failed', 'error')
    }
  }

  if (loading) return (
    <div className="flex items-center justify-center h-64">
      <Spinner size="lg"/>
    </div>
  )

  const canInvite = workspace && workspace.memberCount < workspace.maxMembers
  const isOwner   = m => m.role === 'OWNER'

  return (
    <div className="space-y-5 max-w-3xl">

      <div className="card p-5">
        <div className="flex items-center justify-between flex-wrap gap-3">
          <div>
            <h2 className="font-semibold text-base" style={{ color: 'var(--text-main)' }}>
              {workspace?.name}
            </h2>
            <p className="text-xs mt-0.5" style={{ color: 'var(--text-muted)' }}>
              {workspace?.memberCount} / {workspace?.maxMembers} members
            </p>
          </div>
          <button
            onClick={() => setModal(true)}
            disabled={!canInvite}
            className="btn-primary text-sm gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
            title={!canInvite ? 'Upgrade your plan to add more members' : ''}>
            <Plus className="w-4 h-4"/> Invite Member
          </button>
        </div>

        {!canInvite && (
          <div className="mt-3 px-3 py-2 rounded-lg text-xs"
               style={{ background: 'var(--brand-light)', color: 'var(--brand)' }}>
            You've reached your plan's member limit.{' '}
            <a href="/pricing" style={{ fontWeight: 600, textDecoration: 'underline' }}>
              Upgrade to Team
            </a>{' '}
            to add up to 5 members.
          </div>
        )}
      </div>

      <div className="card overflow-hidden">
        {!workspace?.members?.length ? (
          <EmptyState icon={Users} title="No members yet"
            description="Invite team members to collaborate on this workspace"/>
        ) : (
          <table className="w-full">
            <thead className="border-b" style={{ background: 'var(--bg-page)', borderColor: 'var(--border)' }}>
              <tr>
                {['Member','Email','Role',''].map(h => (
                  <th key={h} className="table-header">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {workspace.members.map(m => (
                <tr key={m.id} className="table-row">
                  <td className="table-cell">
                    <div className="flex items-center gap-2.5">
                      <div className="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold text-white shrink-0"
                           style={{ background: 'var(--brand)' }}>
                        {(m.fullName || m.username)[0].toUpperCase()}
                      </div>
                      <div>
                        <p className="text-sm font-medium" style={{ color: 'var(--text-main)' }}>
                          {m.fullName}
                        </p>
                        <p className="text-xs" style={{ color: 'var(--text-faint)' }}>
                          @{m.username}
                        </p>
                      </div>
                    </div>
                  </td>
                  <td className="table-cell text-xs" style={{ color: 'var(--text-muted)' }}>
                    {m.email}
                  </td>
                  <td className="table-cell">
                    <span className={ROLE_COLORS[m.role] || 'badge-gray'}>
                      {m.role === 'OWNER' && <Crown className="w-3 h-3 mr-1 inline"/>}
                      {m.role}
                    </span>
                  </td>
                  <td className="table-cell">
                    {!isOwner(m) && (
                      <button
                        onClick={() => remove(m.userId, m.username)}
                        className="p-1.5 rounded hover:bg-red-50 transition-colors text-slate-400 hover:text-red-500">
                        <Trash2 className="w-4 h-4"/>
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <Modal open={modal} onClose={() => setModal(false)} title="Invite Team Member">
        <div className="space-y-4">
          <p className="text-sm" style={{ color: 'var(--text-muted)' }}>
            The person must already have a FinancePro account.
          </p>
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
              <option value="ANALYST">Analyst — can add and edit records</option>
              <option value="VIEWER">Viewer — read only</option>
            </select>
          </div>
          <div className="flex gap-3 justify-end pt-2">
            <button className="btn-secondary" onClick={() => setModal(false)}>Cancel</button>
            <button className="btn-primary" onClick={invite} disabled={saving || !form.email}>
              {saving ? 'Inviting…' : 'Send Invite'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  )
}