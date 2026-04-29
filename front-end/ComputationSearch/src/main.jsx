import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import Home from './pages/Home'
import ResultLinks from './pages/ResultLinks'
import {BrowserRouter, Routes, Route} from 'react-router-dom'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path='/' element={<Home />} />
        <Route path='/result-links' element={<ResultLinks />} />
      </Routes>
    </BrowserRouter>
  </StrictMode>,
)
