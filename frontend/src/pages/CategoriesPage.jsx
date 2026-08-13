import { useState, useEffect } from 'react'
import { categoriesApi } from '../services/api'
import { useToast } from '../components/ui/Toast'
import Modal from '../components/ui/Modal'
import Spinner from '../components/ui/Spinner'
import { Tag, Plus, Pencil, Trash2, Lock } from 'lucide-react'

const COLORS = ['#6366f1','#3b82f6','#10b981','#f59e0b','#ef4444',
                 '#8b5cf6','#ec4899','#14b8a6','#f97316','#06b6d4']

export default function CategoriesPage() {
  const toast = useToast()
  const [cats, setCats]     = useState([])
  const [loading, setLoading] = useState(true)
  const [modal, setModal]   = useState(null)
  const [editing, setEditing] = useState(null)
  const [form, setForm]     = useState({ name: '', color: '#6366f1', type: 'EXPENSE' })
  const [saving, setSaving] = useState(false)
  const [filter, setFilter] = useState('ALL')

  const load = async () => {
    setLoading(true)
    try {
      const { data } = await categoriesApi.getAll()
      setCats(data.data || [])
    } catch { toast('Failed to load categories', 'error') }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const openCreate = () => {
    setEditing(null)
    setForm({ name: '', color: '#6366f1', type: 'EXPENSE' })
    setModal('form')
  }

  const openEdit = (cat) => {
    setEditing(cat)
    setForm({ name: cat.name, color: cat.color, type: cat.type })
    setModal('form')
  }

  const save = async () => {
    setSaving(true)
    try {
      if (editing) {
        await categoriesApi.update(editing.id, form)
        toast('Category updated', 'success')
      } else {
        await categoriesApi.create(form)
        toast('Category created', 'success')
      }
      setModal(null)
      load()
    } catch (e) {
      toast(e.response?.data?.message || 'Failed', 'error')
    } finally { setSaving(false) }
  }

  const del = async (cat) => {
    if (!confirm(`Delete "${cat.name}"? This cannot be undone.`)) return
    try {
      await categoriesApi.delete(cat.id)
      toast('Deleted', 'success')
      load()
    } catch (e) {
      toast(e.response?.data?.message || 'Failed', 'error')
    }
  }

  const filtered = cats.filter(c =>
    filter === 'ALL' || c.type === filter
  )

  if (loading) return (
    <div className="flex items-center justify-center h-64"><Spinner size="lg"/></div>
  )

  return (
    <div className="space-y-5 max-w-3xl">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div className="flex gap-2">
          {['ALL','INCOME','EXPENSE'].map(f => (
            <button key={f} onClick={() => setFilter(f)}
              className="px-3 py-1.5 text-xs font-medium rounded-lg border transition-colors"
              style={{
                background: filter === f ? 'var(--brand)' : 'var(--bg-card)',
                color: filter === f ? '#fff' : 'var(--text-muted)',
                borderColor: filter === f ? 'var(--brand)' : 'var(--border)'
              }}>
              {f}
            </button>
          ))}
        </div>
        <button onClick={openCreate} className="btn-primary gap-2">
          <Plus className="w-4 h-4"/> New Category
        </button>
      </div>

      <div className="card overflow-hidden">
        {filtered.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-14">
            <Tag className="w-10 h-10 mb-3" style={{ color: 'var(--text-faint)' }}/>
            <p className="text-sm font-medium" style={{ color: 'var(--text-main)' }}>
              No categories yet
            </p>
            <p className="text-xs mt-1" style={{ color: 'var(--text-faint)' }}>
              Create your first custom category
            </p>
          </div>
        ) : (
          <div className="divide-y" style={{ borderColor: 'var(--border)' }}>
            {filtered.map(cat => (
              <div key={cat.id} className="flex items-center justify-between px-5 py-3.5">
                <div className="flex items-center gap-3">
                  <div className="w-3 h-3 rounded-full shrink-0"
                       style={{ background: cat.color || '#6366f1' }}/>
                  <div>
                    <p className="text-sm font-medium" style={{ color: 'var(--text-main)' }}>
                      {cat.name}
                    </p>
                    <p className="text-xs" style={{ color: 'var(--text-faint)' }}>{cat.type}</p>
                  </div>
                  {cat.system && (
                    <span className="flex items-center gap-1 text-xs px-2 py-0.5 rounded-full"
                          style={{ background: 'var(--border)', color: 'var(--text-faint)' }}>
                      <Lock className="w-2.5 h-2.5"/> System
                    </span>
                  )}
                </div>
                {!cat.system && (
                  <div className="flex gap-1">
                    <button onClick={() => openEdit(cat)}
                      className="p-1.5 rounded hover:opacity-80 transition-opacity"
                      style={{ color: 'var(--brand)' }}>
                      <Pencil className="w-4 h-4"/>
                    </button>
                    <button onClick={() => del(cat)}
                      className="p-1.5 rounded hover:opacity-80 transition-opacity text-red-500">
                      <Trash2 className="w-4 h-4"/>
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      <Modal open={modal === 'form'} onClose={() => setModal(null)}
             title={editing ? 'Edit Category' : 'New Category'}>
        <div className="space-y-4">
          <div>
            <label className="label">Name</label>
            <input className="input" placeholder="e.g. Freelance Income" maxLength={50}
              value={form.name}
              onChange={e => setForm(f => ({...f, name: e.target.value}))}/>
          </div>
          <div>
            <label className="label">Type</label>
            <select className="input" value={form.type}
              onChange={e => setForm(f => ({...f, type: e.target.value}))}>
              <option value="EXPENSE">Expense</option>
              <option value="INCOME">Income</option>
            </select>
          </div>
          <div>
            <label className="label">Color</label>
            <div className="flex gap-2 flex-wrap mt-1">
              {COLORS.map(c => (
                <button key={c} onClick={() => setForm(f => ({...f, color: c}))}
                  className="w-7 h-7 rounded-full border-2 transition-transform hover:scale-110"
                  style={{
                    background: c,
                    borderColor: form.color === c ? 'var(--text-main)' : 'transparent'
                  }}/>
              ))}
              <input type="color" value={form.color}
                onChange={e => setForm(f => ({...f, color: e.target.value}))}
                className="w-7 h-7 rounded-full cursor-pointer border-0 p-0"
                title="Custom color"/>
            </div>
          </div>
          <div className="flex gap-3 justify-end pt-2">
            <button className="btn-secondary" onClick={() => setModal(null)}>Cancel</button>
            <button className="btn-primary" onClick={save} disabled={saving || !form.name.trim()}>
              {saving ? 'Saving…' : editing ? 'Save Changes' : 'Create'}
            </button>
          </div>
        </div>
      </Modal>
    </div>
  )
}