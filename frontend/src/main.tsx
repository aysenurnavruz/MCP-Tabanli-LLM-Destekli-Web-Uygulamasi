import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { BrowserRouter } from 'react-router-dom'
import { TooltipProvider } from './components/ui/tooltip'

import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'sonner'

//document.documentElement.classList.add('dark')
const queryClient = new QueryClient()

createRoot(document.getElementById('root')!).render(
  
    <QueryClientProvider  client={queryClient}>
    <TooltipProvider>
      <BrowserRouter>
        <App />
        <Toaster position="top-right" richColors />
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
)
