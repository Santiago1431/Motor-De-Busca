import './style.css'
import FindIcon from "../../assets/iconFind.png"
import api from "../../services/api.js"
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'


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
      navigate('/result-links',{state: {results: response.data}});
    } catch (error) {
      console.log(error);
    }finally {
      setLoading(false)
    }
  }

  return (
    <div className='container'>
      <h1 className='title'>Computation Search</h1>
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
    </div>
  )
}


export default Home
