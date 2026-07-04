import { useEffect, useState } from 'react'
import { recurringApi } from '../services/api'
import { fmt, CATEGORIES, INCOME_CATS, FREQUENCIES } from '../utils/format'
import { useToast } from '../components/ui/Toast'
import Modal from '../components/ui/Modal'
import EmptyState from '../components/ui/EmptyState'
import Spinner from '../components/ui/Spinner'
import { RefreshCw, Plus, Trash2, Pencil } from 'lucide-react'

const BLANK = { name:'', type:'EXPENSE', category:'FOOD', amount:'', frequency:'MONTHLY', startDate:'', endDate:'' }

export default function RecurringPage() {
  const toast = useToast()
  const [rules, setRules] = useState([])
  const [loading, setLoading] = useState(true)
  const [modal, setModal] = useState(null)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(BLANK)
  const [saving, setSaving] = useState(false)

  const load = async () => {
    setLoading(true)
    try { const { data } = await recurringApi.getAll(); setRules(data.data) }
    catch { toast('Failed to load','error') }
    finally { setLoading(false) }
  }
  useEffect(() => { load() }, [])

  const openCreate = () => { setForm(BLANK); setModal('create') }
  const openEdit   = (r) => { setEditing(r); setForm({ name:r.name, type:r.type, category:r.category, amount:r.amount, frequency:r.frequency, startDate:r.startDate, endDate:r.endDate??'' }); setModal('edit') }
  const close = () => { setModal(null); setEditing(null) }

  const save = async () => {
    setSaving(true)
    try {
      const payload = { ...form, endDate: form.endDate || null }
      if (modal==='create') await recurringApi.create(payload)
      else await recurringApi.update(editing.id, payload)
      toast('Saved','success'); close(); load()
    } catch (e) { toast(e.response?.data?.message??'Save failed','error') }
    finally { setSaving(false) }
  }

  const del = async (id) => {
    if (!confirm('Deactivate this rule?')) return
    try { await recurringApi.delete(id); toast('Deactivated','success'); load() }
    catch { toast('Failed','error') }
  }

  const cats = form.type==='INCOME' ? INCOME_CATS : CATEGORIES.slice(8)

  if (loading) return <div className="flex items-center justify-center h-64"><Spinner size="lg"/></div>

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <button className="btn-primary" onClick={openCreate}><Plus className="w-4 h-4"/>New Rule</button>
      </div>
      {!rules.length ? (
        <div className="card"><EmptyState icon={RefreshCw} title="No recurring rules" description="Automate salary, rent, subscriptions and more"/></div>
      ) : (
        <div className="card overflow-hidden">
          <table className="w-full">
            <thead className="bg-slate-50 border-b border-surface-border">
              <tr>{['Name','Type','Category','Amount','Frequency','Next Run',''].map(h=><th key={h} className="table-header">{h}</th>)}</tr>
            </thead>
            <tbody>
              {rules.map(r=>(
                <tr key={r.id} className="table-row">
                  <td className="table-cell font-medium">{r.name}</td>
                  <td className="table-cell"><span className={r.type==='INCOME'?'badge-green':'badge-red'}>{r.type}</span></td>
                  <td className="table-cell text-xs text-slate-500">{r.category.replace(/_/g,' ')}</td>
                  <td className={`table-cell font-semibold ${r.type==='INCOME'?'text-green-700':'text-red-700'}`}>{fmt.currency(r.amount)}</td>
                  <td className="table-cell"><span className="badge-blue">{r.frequency}</span></td>
                  <td className="table-cell text-slate-500 text-xs">{fmt.date(r.nextExecutionDate)}</td>
                  <td className="table-cell">
                    <div className="flex gap-1">
                      <button onClick={()=>openEdit(r)} className="p-1.5 rounded hover:bg-slate-100 text-slate-400 hover:text-brand-600 transition-colors"><Pencil className="w-3.5 h-3.5"/></button>
                      <button onClick={()=>del(r.id)} className="p-1.5 rounded hover:bg-red-50 text-slate-400 hover:text-red-600 transition-colors"><Trash2 className="w-3.5 h-3.5"/></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={!!modal} onClose={close} title={modal==='create'?'New Recurring Rule':'Edit Rule'}>
        <div className="space-y-4">
          <div><label className="label">Name</label><input className="input" placeholder="e.g. Monthly Rent" value={form.name} onChange={e=>setForm(f=>({...f,name:e.target.value}))}/></div>
          <div className="grid grid-cols-2 gap-3">
            <div><label className="label">Type</label>
              <select className="input" value={form.type} onChange={e=>setForm(f=>({...f,type:e.target.value,category:e.target.value==='INCOME'?'SALARY':'FOOD'}))}>
                <option>INCOME</option><option>EXPENSE</option>
              </select>
            </div>
            <div><label className="label">Category</label>
              <select className="input" value={form.category} onChange={e=>setForm(f=>({...f,category:e.target.value}))}>
                {cats.map(c=><option key={c}>{c}</option>)}
              </select>
            </div>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div><label className="label">Amount (₹)</label><input className="input" type="number" min="0.01" value={form.amount} onChange={e=>setForm(f=>({...f,amount:e.target.value}))}/></div>
            <div><label className="label">Frequency</label>
              <select className="input" value={form.frequency} onChange={e=>setForm(f=>({...f,frequency:e.target.value}))}>
                {FREQUENCIES.map(f=><option key={f}>{f}</option>)}
              </select>
            </div>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div><label className="label">Start Date</label><input className="input" type="date" value={form.startDate} onChange={e=>setForm(f=>({...f,startDate:e.target.value}))}/></div>
            <div><label className="label">End Date (optional)</label><input className="input" type="date" value={form.endDate} onChange={e=>setForm(f=>({...f,endDate:e.target.value}))}/></div>
          </div>
          <div className="flex gap-3 justify-end pt-2">
            <button className="btn-secondary" onClick={close}>Cancel</button>
            <button className="btn-primary" onClick={save} disabled={saving}>{saving?'Saving…':'Save Rule'}</button>
          </div>
        </div>
      </Modal>
    </div>
  )
}
