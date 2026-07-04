import { useEffect, useState } from 'react'
import { budgetsApi } from '../services/api'
import { fmt } from '../utils/format'
import { useToast } from '../components/ui/Toast'
import Modal from '../components/ui/Modal'
import EmptyState from '../components/ui/EmptyState'
import Spinner from '../components/ui/Spinner'
import ProgressBar from '../components/ui/ProgressBar'
import { PiggyBank, Plus, Trash2, Pencil } from 'lucide-react'

const BLANK = { category:'FOOD', limitAmount:'', periodStart:'', periodEnd:'' }
const CATS = ['FOOD','RENT','UTILITIES','TRANSPORT','HEALTHCARE','EDUCATION','ENTERTAINMENT','SHOPPING','TRAVEL','INSURANCE','SAVINGS','EMI','SUBSCRIPTION','PERSONAL_CARE','HOME','OTHER_EXPENSE']
const STATUS_COLOR = { ON_TRACK:'text-green-600', WARNING:'text-yellow-600', CRITICAL:'text-red-600', EXCEEDED:'text-red-700' }

export default function BudgetsPage() {
  const toast = useToast()
  const [budgets, setBudgets] = useState([])
  const [loading, setLoading] = useState(true)
  const [modal, setModal] = useState(null)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(BLANK)
  const [saving, setSaving] = useState(false)

  const load = async () => {
    setLoading(true)
    try { const { data } = await budgetsApi.getAll(); setBudgets(data.data) }
    catch { toast('Failed to load budgets','error') }
    finally { setLoading(false) }
  }
  useEffect(() => { load() }, [])

  const openCreate = () => { setForm(BLANK); setModal('create') }
  const openEdit   = (b) => { setEditing(b); setForm({ category:b.category, limitAmount:b.limitAmount, periodStart:b.periodStart, periodEnd:b.periodEnd }); setModal('edit') }
  const close = () => { setModal(null); setEditing(null) }

  const save = async () => {
    setSaving(true)
    try {
      if (modal==='create') await budgetsApi.create(form)
      else await budgetsApi.update(editing.id, form)
      toast(modal==='create'?'Budget created':'Budget updated','success')
      close(); load()
    } catch (e) {
      toast(e.response?.data?.message ?? 'Save failed','error')
    } finally { setSaving(false) }
  }

  const del = async (id) => {
    if (!confirm('Deactivate this budget?')) return
    try { await budgetsApi.delete(id); toast('Budget deactivated','success'); load() }
    catch { toast('Failed','error') }
  }

  if (loading) return <div className="flex items-center justify-center h-64"><Spinner size="lg"/></div>

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <button className="btn-primary" onClick={openCreate}><Plus className="w-4 h-4"/>New Budget</button>
      </div>

      {!budgets.length ? (
        <div className="card"><EmptyState icon={PiggyBank} title="No budgets yet" description="Create budgets to track your spending limits" action={<button className="btn-primary" onClick={openCreate}><Plus className="w-4 h-4"/>Create Budget</button>}/></div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          {budgets.map(b => (
            <div key={b.id} className="card p-5">
              <div className="flex items-start justify-between mb-3">
                <div>
                  <p className="text-sm font-semibold text-slate-900">{b.category.replace(/_/g,' ')}</p>
                  <p className="text-xs text-slate-400 mt-0.5">{fmt.date(b.periodStart)} – {fmt.date(b.periodEnd)}</p>
                </div>
                <div className="flex gap-1">
                  <button onClick={()=>openEdit(b)} className="p-1.5 rounded hover:bg-slate-100 text-slate-400 hover:text-brand-600 transition-colors"><Pencil className="w-3.5 h-3.5"/></button>
                  <button onClick={()=>del(b.id)} className="p-1.5 rounded hover:bg-red-50 text-slate-400 hover:text-red-600 transition-colors"><Trash2 className="w-3.5 h-3.5"/></button>
                </div>
              </div>

              <div className="mb-3">
                <div className="flex justify-between text-xs mb-1.5">
                  <span className="text-slate-500">Spent: <span className="font-semibold text-slate-800">{fmt.currency(b.spentAmount)}</span></span>
                  <span className="text-slate-500">Limit: <span className="font-semibold">{fmt.currency(b.limitAmount)}</span></span>
                </div>
                <ProgressBar value={b.usagePercent} max={100}/>
              </div>

              <div className="flex items-center justify-between">
                <span className={`text-xs font-bold ${STATUS_COLOR[b.status]}`}>{b.status.replace('_',' ')}</span>
                <span className="text-xs text-slate-500">{fmt.percent(b.usagePercent)} used</span>
              </div>

              <div className="mt-2 pt-2 border-t border-surface-border">
                <p className="text-xs text-slate-500">Remaining: <span className={`font-semibold ${b.remainingAmount>0?'text-green-600':'text-red-600'}`}>{fmt.currency(Math.abs(b.remainingAmount))}</span></p>
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal open={!!modal} onClose={close} title={modal==='create'?'New Budget':'Edit Budget'}>
        <div className="space-y-4">
          <div>
            <label className="label">Category</label>
            <select className="input" value={form.category} onChange={e=>setForm(f=>({...f,category:e.target.value}))}>
              {CATS.map(c=><option key={c}>{c}</option>)}
            </select>
          </div>
          <div>
            <label className="label">Limit Amount (₹)</label>
            <input className="input" type="number" min="1" placeholder="0.00" value={form.limitAmount} onChange={e=>setForm(f=>({...f,limitAmount:e.target.value}))}/>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div><label className="label">Period Start</label><input className="input" type="date" value={form.periodStart} onChange={e=>setForm(f=>({...f,periodStart:e.target.value}))}/></div>
            <div><label className="label">Period End</label><input className="input" type="date" value={form.periodEnd} onChange={e=>setForm(f=>({...f,periodEnd:e.target.value}))}/></div>
          </div>
          <div className="flex gap-3 justify-end pt-2">
            <button className="btn-secondary" onClick={close}>Cancel</button>
            <button className="btn-primary" onClick={save} disabled={saving}>{saving?'Saving…':'Save Budget'}</button>
          </div>
        </div>
      </Modal>
    </div>
  )
}
