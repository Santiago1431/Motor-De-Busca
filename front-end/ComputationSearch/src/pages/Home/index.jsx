import './style.css'
import FindIcon from "../../assets/iconFind.png"
import CameraIcon from "../../assets/cameraIcon.png"
import api from "../../services/api.js"
import { useState, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import 'katex/dist/katex.min.css'
import { InlineMath } from 'react-katex'


function Home() {

  const navigate = useNavigate()
  const fileInputRef = useRef(null)
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSearch = async (event) => {
    event.preventDefault();
    if (!query.trim()) return;

    try {
      setLoading(true);
      const response = await api.get(`/search`, {
        params: {
          query: query,
          page: 1
        }
      });
      navigate('/result-links', { state: { results: response.data, query: query } });
    } catch (error) {
      console.log(error);
    } finally {
      setLoading(false)
    }
  }

  const handleImageSearch = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);

    try {
      setLoading(true);
      const response = await api.post('/search/image', formData);
      navigate('/result-links', { 
        state: { 
          results: response.data, 
          query: response.data.query || "Busca por Imagem (LaTeX)" 
        } 
      });
    } catch (error) {
      console.error("Erro na busca por imagem:", error);
      alert("Falha ao processar imagem. Verifique se o serviço de OCR está rodando.");
    } finally {
      setLoading(false);
    }
  }

  const triggerFileInput = () => {
    fileInputRef.current.click();
  }

  return (
    <div className='container'>
      <h1 className='title'>Computation Search</h1>
      <div className='search-container-home'>
        <form className='search-form' onSubmit={handleSearch}>
          <input name='computational-query'
            type='text'
            placeholder='Faça sua busca computacional'
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            disabled={loading}
          />
          <div className='button-group'>
            <button className='icon-camera' type='button' onClick={triggerFileInput} disabled={loading} title="Buscar por Imagem">
              <img src={CameraIcon} alt="Camera Icon" />
            </button>
            <button className='icon-find' type='submit' disabled={loading}>
              <img src={FindIcon} alt="Find Icon" />
            </button>
          </div>
          <input 
            type="file" 
            ref={fileInputRef} 
            style={{ display: 'none' }} 
            accept="image/*"
            onChange={handleImageSearch}
          />
        </form>
        {loading && <div className="loading-spinner">Processando...</div>}
        {query && (query.includes('\\') || query.includes('_') || query.includes('^')) && (
          <div className='latex-preview-home'>
            <InlineMath math={query} />
          </div>
        )}
      </div>
    </div>
  )
}


export default Home
