import './style.css'
import { useLocation } from 'react-router-dom'

function ResultLinks() {

    const location = useLocation();
    const results = location.state?.results || [];
    const [page, setPage] = useState(1);
    const [loading, setLoading] = useState(false);

    async function handleNextPage() {
        setLoading(true);
        setPage(page + 1)
    }

    async function handlePreviousPage() {
        if (page > 1) {
            setLoading(true);
            setPage(page - 1)
        }
    }

    async function handlePage(page) {
        setLoading(true);
        setPage(page)
    }
    
    return (
        <div className='container-links'>
            {results.map(links => (
                <div className='links-results' key={links.url}>
                    <button type='button' className='button-title' onClick={() => window.open(links.url, '_blank')}><h3 className='title'>{links.title}</h3></button>
                    <p className='description'>{links.abs}</p>
                </div>
            ))}
        <div className='page-navigation'>
            <button onClick={handlePreviousPage}>Anterior</button>
            <button onClick={handlePage(1)}>1</button>
            <button onClick={handlePage(2)}>2</button>
            <button onClick={handlePage(3)}>3</button>
            <button onClick={handlePage(4)}>4</button>
            <button onClick={handlePage(5)}>5</button>
            <button onClick={handleNextPage}>Proximo</button>            
        </div>
        </div>
    )
}

export default ResultLinks