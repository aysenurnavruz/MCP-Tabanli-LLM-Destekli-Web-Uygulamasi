import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { BrowserRouter } from 'react-router-dom'
import { TooltipProvider } from './components/ui/tooltip'

document.documentElement.classList.add('dark')

createRoot(document.getElementById('root')!).render(
  
    <TooltipProvider>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </TooltipProvider>
 
)
