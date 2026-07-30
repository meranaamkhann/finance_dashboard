import { Sun, Moon } from 'lucide-react'
import { useTheme } from '../../context/ThemeContext'

export default function ThemeToggle({ className = '' }) {
  const { isDark, toggle } = useTheme()
  return (
    <button
      onClick={toggle}
      className={`p-2 rounded-lg transition-colors hover:opacity-80 ${className}`}
      style={{ background: 'var(--border)', color: 'var(--text-main)' }}
      title={isDark ? 'Switch to light mode' : 'Switch to dark mode'}
    >
      {isDark ? <Sun className="w-4 h-4"/> : <Moon className="w-4 h-4"/>}
    </button>
  )
}
