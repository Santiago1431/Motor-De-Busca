import './style.css'
import FindIcon from "../../assets/iconFind.png"
import api from "../../services/api.js"
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import 'katex/dist/katex.min.css'
import { InlineMath } from 'react-katex'


function Home() {

  const navigate = useNavigate()
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSearch = async (event) => {

   
    event.preventDefault();

    try {
      setLoading(true);
      const response = await api.get(`/search`, {
        params: {
          query: query,
          page: 1
        }
      });
      navigate('/result-links',{state: {results: response.data, query: query}});
    } catch (error) {
      console.log(error);
    }finally {
      setLoading(false)
    }
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
          />
          <button className='icon-find' type='submit'>
            <img src={FindIcon} alt="Find Icon" />
          </button>
        </form>
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
