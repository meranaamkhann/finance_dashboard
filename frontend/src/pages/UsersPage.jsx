import { useEffect, useState } from 'react'
import { usersApi } from '../services/api'
import { fmt } from '../utils/format'
import { useToast } from '../components/ui/Toast'
import Modal from '../components/ui/Modal'
import Spinner from '../components/ui/Spinner'
import EmptyState from '../components/ui/EmptyState'
import { Users, Plus, UserCheck, UserX, Trash2 } from 'lucide-react'

const BLANK = { username:'', email:'', fullName:'', password:'', role:'VIEWER' }
const ROLE_BADGE = { ADMIN:'badge-purple', ANALYST:'badge-blue', VIEWER:'badge-gray' }

export default function UsersPage() {
  const toast = useToast()
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [modal, setModal] = useState(false)
  const [form, setForm] = useState(BLANK)
  const [saving, setSaving] = useState(false)

  const load = async () => {
    setLoading(true)
    try { const { data } = await usersApi.getAll({ size:50 }); setUsers(data.data.content) }
    catch { toast('Failed to load users','error') }
    finally { setLoading(false) }
  }
  useEffect(() => { load() }, [])

  const save = async () => {
    setSaving(true)
    try { await usersApi.create(form); toast('User created','success'); setModal(false); setForm(BLANK); load() }
    catch (e) { toast(Object.values(e.response?.data?.errors??{}).join(', ') || e.response?.data?.message || 'Failed','error') }
    finally { setSaving(false) }
  }

  const toggle = async (u) => {
    try {
      u.active ? await usersApi.deactivate(u.id) : await usersApi.activate(u.id)
      toast(`User ${u.active?'deactivated':'activated'}`,'success'); load()
    } catch { toast('Failed','error') }
  }

  const del = async (u) => {
    if (!confirm(`Delete user "${u.username}"?`)) return
    try { await usersApi.delete(u.id); toast('Deleted','success'); load() }
    catch { toast('Failed','error') }
  }

  if (loading) return <div className="flex items-center justify-center h-64"><Spinner size="lg"/></div>

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <button className="btn-primary" onClick={()=>setModal(true)}><Plus className="w-4 h-4"/>Add User</button>
      </div>
      <div className="card overflow-hidden">
        {!users.length ? <EmptyState icon={Users} title="No users found"/> : (
          <table className="w-full">
            <thead className="bg-slate-50 border-b border-surface-border">
              <tr>{['Name','Username','Email','Role','Status','Actions'].map(h=><th key={h} className="table-header">{h}</th>)}</tr>
            </thead>
            <tbody>
              {users.map(u=>(
                <tr key={u.id} className="table-row">
                  <td className="table-cell font-medium">{u.fullName}</td>
                  <td className="table-cell text-slate-500 text-xs">@{u.username}</td>
                  <td className="table-cell text-slate-500 text-xs">{u.email}</td>
                  <td className="table-cell"><span className={ROLE_BADGE[u.role]??'badge-gray'}>{u.role}</span></td>
                  <td className="table-cell"><span className={u.active?'badge-green':'badge-red'}>{u.active?'Active':'Inactive'}</span></td>
                  <td className="table-cell">
                    <div className="flex gap-1">
                      <button onClick={()=>toggle(u)} title={u.active?'Deactivate':'Activate'}
                        className={`p-1.5 rounded transition-colors ${u.active?'hover:bg-yellow-50 text-slate-400 hover:text-yellow-600':'hover:bg-green-50 text-slate-400 hover:text-green-600'}`}>
                        {u.active?<UserX className="w-3.5 h-3.5"/>:<UserCheck className="w-3.5 h-3.5"/>}
                      </button>
                      <button onClick={()=>del(u)} className="p-1.5 rounded hover:bg-red-50 text-slate-400 hover:text-red-600 transition-colors"><Trash2 className="w-3.5 h-3.5"/></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <Modal open={modal} onClose={()=>setModal(false)} title="Add New User">
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <div><label className="label">Username</label><input className="input" placeholder="john_doe" value={form.username} onChange={e=>setForm(f=>({...f,username:e.target.value}))}/></div>
            <div><label className="label">Role</label>
              <select className="input" value={form.role} onChange={e=>setForm(f=>({...f,role:e.target.value}))}>
                <option>VIEWER</option><option>ANALYST</option><option>ADMIN</option>
              </select>
            </div>
          </div>
          <div><label className="label">Full Name</label><input className="input" value={form.fullName} onChange={e=>setForm(f=>({...f,fullName:e.target.value}))}/></div>
          <div><label className="label">Email</label><input className="input" type="email" value={form.email} onChange={e=>setForm(f=>({...f,email:e.target.value}))}/></div>
          <div><label className="label">Password</label><input className="input" type="password" placeholder="Min 8 chars, uppercase, digit, special" value={form.password} onChange={e=>setForm(f=>({...f,password:e.target.value}))}/></div>
          <div className="flex gap-3 justify-end pt-2">
            <button className="btn-secondary" onClick={()=>setModal(false)}>Cancel</button>
            <button className="btn-primary" onClick={save} disabled={saving}>{saving?'Creating…':'Create User'}</button>
          </div>
        </div>
      </Modal>
    </div>
  )
}
