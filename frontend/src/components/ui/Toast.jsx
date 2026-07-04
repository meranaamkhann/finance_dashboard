import { createContext, useContext, useState, useCallback } from 'react'
import { CheckCircle, XCircle, Info, X } from 'lucide-react'

const ToastContext = createContext(null)

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])
  const add = useCallback((msg, type='info') => {
    const id = Date.now()
    setToasts(t => [...t, { id, msg, type }])
    setTimeout(() => setToasts(t => t.filter(x => x.id !== id)), 4000)
  }, [])
  const remove = (id) => setToasts(t => t.filter(x => x.id !== id))

  const icons = { success:<CheckCircle className="w-4 h-4 text-green-600"/>, error:<XCircle className="w-4 h-4 text-red-600"/>, info:<Info className="w-4 h-4 text-blue-600"/> }
  const styles = { success:'border-green-200 bg-green-50', error:'border-red-200 bg-red-50', info:'border-blue-200 bg-blue-50' }

  return (
    <ToastContext.Provider value={add}>
      {children}
      <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2">
        {toasts.map(t => (
          <div key={t.id} className={`flex items-start gap-3 px-4 py-3 rounded-xl border shadow-lg max-w-sm text-sm font-medium text-slate-700 animate-slide-in ${styles[t.type]}`}>
            {icons[t.type]}
            <span className="flex-1">{t.msg}</span>
            <button onClick={()=>remove(t.id)} className="text-slate-400 hover:text-slate-600"><X className="w-3.5 h-3.5"/></button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}
export const useToast = () => useContext(ToastContext)
