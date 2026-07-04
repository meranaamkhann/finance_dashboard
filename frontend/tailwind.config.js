export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        brand: { 50:'#f0f7ff', 100:'#e0effe', 500:'#3b82f6', 600:'#2563eb', 700:'#1d4ed8' },
        surface: { DEFAULT:'#ffffff', secondary:'#f8fafc', border:'#e2e8f0' },
      },
      fontFamily: { sans: ['Inter','system-ui','sans-serif'] },
      boxShadow: { card:'0 1px 3px 0 rgb(0 0 0 / .06), 0 1px 2px -1px rgb(0 0 0 / .04)' }
    }
  },
  plugins: []
}
