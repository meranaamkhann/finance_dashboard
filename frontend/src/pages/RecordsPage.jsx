import { useEffect, useState, useCallback } from 'react'
import { recordsApi } from '../services/api'
import api from '../services/api'
import { fmt } from '../utils/format'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../components/ui/Toast'
import Modal from '../components/ui/Modal'
import Spinner from '../components/ui/Spinner'
import EmptyState from '../components/ui/EmptyState'
import {
  Plus,
  Search,
  Download,
  Trash2,
  Pencil,
  ArrowLeftRight,
  ChevronLeft,
  ChevronRight
} from 'lucide-react'

const BLANK = {
  type: 'EXPENSE',
  category: '',
  amount: '',
  date: fmt.dateInput(new Date()),
  description: '',
  tags: ''
}

export default function RecordsPage() {
  const { isAdmin, isAnalyst } = useAuth()
  const toast = useToast()

  const [records, setRecords] = useState([])
  const [total, setTotal] = useState(0)
  const [categories, setCategories] = useState([])

  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [categoriesLoading, setCategoriesLoading] = useState(true)

  const [modal, setModal] = useState(null)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(BLANK)
  const [saving, setSaving] = useState(false)

  const [filters, setFilters] = useState({
    type: '',
    keyword: '',
    dateFrom: '',
    dateTo: ''
  })

  // --------------------------------------------------
  // LOAD RECORDS
  // --------------------------------------------------

  const load = useCallback(async () => {
    setLoading(true)

    try {
      const params = {
        page,
        size: 15,
        sortBy: 'date',
        sortDir: 'desc',
        ...Object.fromEntries(
          Object.entries(filters).filter(([, v]) => v)
        )
      }

      const { data } = await recordsApi.getAll(params)

      setRecords(data.data.content)
      setTotal(data.data.totalElements)
    } catch {
      toast('Failed to load records', 'error')
    } finally {
      setLoading(false)
    }
  }, [page, filters, toast])

  // --------------------------------------------------
  // LOAD CATEGORIES
  // --------------------------------------------------

  const loadCategories = useCallback(async () => {
    setCategoriesLoading(true)

    try {
      const { data } = await api.get('/categories')

      setCategories(data.data || [])
    } catch (e) {
      console.error('Failed to load categories:', e)
      toast('Failed to load categories', 'error')
    } finally {
      setCategoriesLoading(false)
    }
  }, [toast])

  useEffect(() => {
    load()
  }, [load])

  useEffect(() => {
    loadCategories()
  }, [loadCategories])

  // --------------------------------------------------
  // FILTER CATEGORIES BY TYPE
  // --------------------------------------------------

  const cats = categories.filter(
    c => c.type === form.type
  )

  // --------------------------------------------------
  // OPEN CREATE MODAL
  // --------------------------------------------------

  const openCreate = () => {
    const firstCategory = categories.find(
      c => c.type === 'EXPENSE'
    )

    setForm({
      ...BLANK,
      category: firstCategory?.name || ''
    })

    setEditing(null)
    setModal('create')
  }

  // --------------------------------------------------
  // OPEN EDIT MODAL
  // --------------------------------------------------

  const openEdit = r => {
    setEditing(r)

    setForm({
      type: r.type,
      category: r.category,
      amount: r.amount,
      date: r.date,
      description: r.description ?? '',
      tags: r.tags ?? ''
    })

    setModal('edit')
  }

  // --------------------------------------------------
  // CLOSE MODAL
  // --------------------------------------------------

  const closeModal = () => {
    setModal(null)
    setEditing(null)

    setForm({
      ...BLANK,
      category: ''
    })
  }

  // --------------------------------------------------
  // SAVE RECORD
  // --------------------------------------------------

  const save = async () => {
    setSaving(true)

    try {
      if (modal === 'create') {
        await recordsApi.create(form)
      } else {
        await recordsApi.update(editing.id, form)
      }

      toast(
        modal === 'create'
          ? 'Record created'
          : 'Record updated',
        'success'
      )

      closeModal()
      load()
    } catch (e) {
      toast(
        e.response?.data?.message ?? 'Save failed',
        'error'
      )
    } finally {
      setSaving(false)
    }
  }

  // --------------------------------------------------
  // DELETE RECORD
  // --------------------------------------------------

  const del = async id => {
    if (!confirm('Delete this record?')) return

    try {
      await recordsApi.delete(id)

      toast('Deleted', 'success')
      load()
    } catch {
      toast('Delete failed', 'error')
    }
  }

  // --------------------------------------------------
  // EXPORT CSV
  // --------------------------------------------------

  const exportCsv = async () => {
    try {
      const { data } = await recordsApi.exportCsv(
        Object.fromEntries(
          Object.entries(filters).filter(([, v]) => v)
        )
      )

      const url = URL.createObjectURL(
        new Blob([data], { type: 'text/csv' })
      )

      const a = document.createElement('a')
      a.href = url
      a.download = 'records.csv'
      a.click()

      URL.revokeObjectURL(url)
    } catch {
      toast('Export failed', 'error')
    }
  }

  // --------------------------------------------------
  // RENDER
  // --------------------------------------------------

  return (
    <div className="space-y-4">

      {/* FILTERS */}
      <div className="flex flex-wrap items-center gap-3">

        <div className="relative flex-1 min-w-48">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-slate-400" />

          <input
            className="input pl-8"
            placeholder="Search description…"
            value={filters.keyword}
            onChange={e =>
              setFilters(f => ({
                ...f,
                keyword: e.target.value
              }))
            }
          />
        </div>

        <select
          className="input w-auto"
          value={filters.type}
          onChange={e =>
            setFilters(f => ({
              ...f,
              type: e.target.value
            }))
          }
        >
          <option value="">All types</option>
          <option>INCOME</option>
          <option>EXPENSE</option>
        </select>

        <input
          type="date"
          className="input w-auto"
          value={filters.dateFrom}
          onChange={e =>
            setFilters(f => ({
              ...f,
              dateFrom: e.target.value
            }))
          }
        />

        <input
          type="date"
          className="input w-auto"
          value={filters.dateTo}
          onChange={e =>
            setFilters(f => ({
              ...f,
              dateTo: e.target.value
            }))
          }
        />

        <div className="flex gap-2 ml-auto">

          {(isAdmin() || isAnalyst()) && (
            <button
              className="btn-primary"
              onClick={openCreate}
            >
              <Plus className="w-4 h-4" />
              Add
            </button>
          )}

          <button
            className="btn-secondary"
            onClick={exportCsv}
          >
            <Download className="w-4 h-4" />
            CSV
          </button>

        </div>
      </div>

      {/* RECORDS TABLE */}
      <div className="card overflow-hidden">

        <div className="overflow-x-auto">

          <table className="w-full">

            <thead className="bg-slate-50 border-b border-surface-border">

              <tr>
                {[
                  'Date',
                  'Type',
                  'Category',
                  'Amount',
                  'Description',
                  ''
                ].map(h => (
                  <th
                    key={h}
                    className="table-header"
                  >
                    {h}
                  </th>
                ))}
              </tr>

            </thead>

            <tbody>

              {loading ? (

                <tr>
                  <td
                    colSpan={6}
                    className="py-16 text-center"
                  >
                    <Spinner className="mx-auto" />
                  </td>
                </tr>

              ) : !records.length ? (

                <tr>
                  <td colSpan={6}>
                    <EmptyState
                      icon={ArrowLeftRight}
                      title="No records found"
                    />
                  </td>
                </tr>

              ) : (

                records.map(r => (

                  <tr
                    key={r.id}
                    className="table-row"
                  >

                    <td className="table-cell text-slate-500 whitespace-nowrap">
                      {fmt.date(r.date)}
                    </td>

                    <td className="table-cell">
                      <span
                        className={
                          r.type === 'INCOME'
                            ? 'badge-green'
                            : 'badge-red'
                        }
                      >
                        {r.type}
                      </span>
                    </td>

                    <td className="table-cell text-xs">
                      {r.category.replace(/_/g, ' ')}
                    </td>

                    <td
                      className={`table-cell font-semibold ${
                        r.type === 'INCOME'
                          ? 'text-green-700'
                          : 'text-red-700'
                      }`}
                    >
                      {fmt.currency(r.amount)}
                    </td>

                    <td className="table-cell text-slate-500 max-w-xs truncate">
                      {r.description ?? '—'}
                    </td>

                    <td className="table-cell">

                      {(isAdmin() || isAnalyst()) && (

                        <div className="flex gap-1">

                          <button
                            onClick={() => openEdit(r)}
                            className="p-1.5 rounded hover:bg-slate-100 text-slate-400 hover:text-brand-600"
                          >
                            <Pencil className="w-3.5 h-3.5" />
                          </button>

                          <button
                            onClick={() => del(r.id)}
                            className="p-1.5 rounded hover:bg-red-50 text-slate-400 hover:text-red-600"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>

                        </div>

                      )}

                    </td>

                  </tr>

                ))

              )}

            </tbody>

          </table>

        </div>

        {/* PAGINATION */}

        {total > 15 && (

          <div className="flex items-center justify-between px-4 py-3 border-t border-surface-border">

            <p className="text-xs text-slate-500">
              {page * 15 + 1}–
              {Math.min((page + 1) * 15, total)}
              {' '}of {total}
            </p>

            <div className="flex gap-2">

              <button
                disabled={page === 0}
                onClick={() =>
                  setPage(p => p - 1)
                }
                className="btn-secondary px-2 py-1 text-xs disabled:opacity-40"
              >
                <ChevronLeft className="w-3.5 h-3.5" />
              </button>

              <button
                disabled={(page + 1) * 15 >= total}
                onClick={() =>
                  setPage(p => p + 1)
                }
                className="btn-secondary px-2 py-1 text-xs disabled:opacity-40"
              >
                <ChevronRight className="w-3.5 h-3.5" />
              </button>

            </div>

          </div>

        )}

      </div>

      {/* ADD / EDIT MODAL */}

      <Modal
        open={!!modal}
        onClose={closeModal}
        title={
          modal === 'create'
            ? 'Add Record'
            : 'Edit Record'
        }
      >

        <div className="space-y-4">

          <div className="grid grid-cols-2 gap-3">

            {/* TYPE */}

            <div>

              <label className="label">
                Type
              </label>

              <select
                className="input"
                value={form.type}
                onChange={e => {

                  const newType = e.target.value

                  const firstCategory =
                    categories.find(
                      c => c.type === newType
                    )

                  setForm(f => ({
                    ...f,
                    type: newType,
                    category:
                      firstCategory?.name || ''
                  }))

                }}
              >
                <option>INCOME</option>
                <option>EXPENSE</option>
              </select>

            </div>

            {/* CATEGORY */}

            <div>

              <label className="label">
                Category
              </label>

              <select
                className="input"
                value={form.category}
                disabled={categoriesLoading}
                onChange={e =>
                  setForm(f => ({
                    ...f,
                    category: e.target.value
                  }))
                }
              >

                {categoriesLoading ? (

                  <option>
                    Loading categories...
                  </option>

                ) : cats.length === 0 ? (

                  <option value="">
                    No categories available
                  </option>

                ) : (

                  cats.map(c => (

                    <option
                      key={c.id}
                      value={c.name}
                    >
                      {c.name.replace(/_/g, ' ')}
                      {c.system
                        ? ' (System)'
                        : ' (Custom)'}
                    </option>

                  ))

                )}

              </select>

            </div>

          </div>

          {/* AMOUNT + DATE */}

          <div className="grid grid-cols-2 gap-3">

            <div>

              <label className="label">
                Amount (Rs.)
              </label>

              <input
                className="input"
                type="number"
                min="0.01"
                step="0.01"
                value={form.amount}
                onChange={e =>
                  setForm(f => ({
                    ...f,
                    amount: e.target.value
                  }))
                }
              />

            </div>

            <div>

              <label className="label">
                Date
              </label>

              <input
                type="date"
                className="input"
                value={form.date}
                onChange={e =>
                  setForm(f => ({
                    ...f,
                    date: e.target.value
                  }))
                }
              />

            </div>

          </div>

          {/* DESCRIPTION */}

          <div>

            <label className="label">
              Description
            </label>

            <input
              className="input"
              placeholder="e.g. Monthly groceries"
              value={form.description}
              onChange={e =>
                setForm(f => ({
                  ...f,
                  description: e.target.value
                }))
              }
            />

          </div>

          {/* ACTIONS */}

          <div className="flex gap-3 justify-end pt-2">

            <button
              className="btn-secondary"
              onClick={closeModal}
            >
              Cancel
            </button>

            <button
              className="btn-primary"
              onClick={save}
              disabled={
                saving ||
                !form.category ||
                categoriesLoading
              }
            >
              {saving
                ? 'Saving…'
                : 'Save Record'}
            </button>

          </div>

        </div>

      </Modal>

    </div>
  )
}