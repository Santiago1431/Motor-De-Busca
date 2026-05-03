import './style.css'
import { useLocation, useNavigate } from 'react-router-dom'
import { useState, useEffect, useRef } from 'react'
import api from '../../services/api'
import FindIcon from "../../assets/iconFind.png"
import CameraIcon from "../../assets/cameraIcon.png"
import 'katex/dist/katex.min.css'
import { InlineMath, BlockMath } from 'react-katex'

function ResultLinks() {

    const location = useLocation();
    const navigate = useNavigate();
    const fileInputRef = useRef(null);
    const [results, setResults] = useState(location.state?.results?.results || []);
    const [totalPages, setTotalPages] = useState(location.state?.results?.totalPages || 1);
    const [query, setQuery] = useState(location.state?.query || '');
    const [page, setPage] = useState(1);
    const [loading, setLoading] = useState(false);
    const isFirstMount = useRef(true);
 
    useEffect(() => {
        if (isFirstMount.current) {
            isFirstMount.current = false;
            if (results.length > 0) return;
        }
        handleFetchResults(page);
    }, [page]);

    async function handleFetchResults(targetPage = page) {
        try {
            setLoading(true);
            setResults([]); // Limpa os resultados atuais para mostrar o loading de forma limpa
            const response = await api.get(`/search`, {
                params: {
                    query: query,
                    page: targetPage
                }
            });
            setResults(response.data.results || []);
            setTotalPages(response.data.totalPages || 1);
            window.scrollTo(0, 0); // Volta para o topo da página
        } catch (error) {
            console.log(error);
        } finally {
            setLoading(false);
        }
    }

    const handleSearch = async (event) => {
        event.preventDefault();
        if (page === 1) {
            handleFetchResults(1);
        } else {
            setPage(1); // Isso disparará o useEffect que chama handleFetchResults(1)
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
            setResults(response.data.results || []);
            setTotalPages(response.data.totalPages || 1);
            setQuery(response.data.query || "Busca por Imagem (LaTeX)");
            setPage(1);
            window.scrollTo(0, 0);
        } catch (error) {
            console.error("Erro na busca por imagem:", error);
            alert("Falha ao processar imagem.");
        } finally {
            setLoading(false);
        }
    }

    const triggerFileInput = () => {
        fileInputRef.current.click();
    }

    async function handleNextPage() {
        setPage(prev => prev + 1)
    }

    async function handlePreviousPage() {
        if (page > 1) {
            setPage(prev => prev - 1)
        }
    }

    function handlePage(targetPage) {
        setPage(targetPage)
    }
    const getPageRange = () => {
        const rangeSize = 10;
        let start = Math.floor((page - 1) / rangeSize) * rangeSize + 1;
        let end = Math.min(start + rangeSize - 1, totalPages);
        
        const pages = [];
        for (let i = start; i <= end; i++) {
            pages.push(i);
        }
        return pages;
    };

    const isLatex = (str) => {
        if (!str) return false;
        return str.includes('\\') || str.includes('_') || str.includes('^') || str.includes('{') || str.includes('$');
    };

    const LatexRenderer = ({ text }) => {
        if (!text) return null;
        
        // Split text by $...$ delimiters
        const parts = text.split(/(\$.*?\$)/g);
        
        return (
            <span>
                {parts.map((part, index) => {
                    if (part.startsWith('$') && part.endsWith('$')) {
                        const math = part.slice(1, -1);
                        return <InlineMath key={index} math={math} />;
                    }
                    return <span key={index}>{part}</span>;
                })}
            </span>
        );
    };
    
    return (
        <div className='result-container'>
            <header className='result-header'>
                <h1 className='header-title' onClick={() => navigate('/')}>Computation Search</h1>
                <div className='search-container-results'>
                    <form className='search-form-results' onSubmit={handleSearch}>
                        <input name='computational-query'
                            type='text'
                            placeholder='Faça sua busca computacional'
                            value={query}
                            onChange={(e) => setQuery(e.target.value)}
                        />
                        <div className='header-button-group'>
                            <button className='icon-camera-header' type='button' onClick={triggerFileInput} title="Buscar por Imagem">
                                <img src={CameraIcon} alt="Camera Icon" />
                            </button>
                            <button className='icon-find' type='submit'>
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
                    {isLatex(query) && (
                        <div className='latex-preview'>
                            <InlineMath math={query} />
                        </div>
                    )}
                </div>
            </header>
            
            <div className='container-links'>
        
                {results.length > 0 ? (
                    results.map((links, index) => (
                        <div className='links-results' key={`${links.url}-${index}`}>
                            <button type='button' className='button-title' onClick={() => window.open(links.url, '_blank')}>
                                <h3 className='title'>{links.title}</h3>
                            </button>
                            <p className='description'>
                                <LatexRenderer text={links.abs} />
                            </p>
                            {links.formulasLatex && links.formulasLatex.length > 0 && (
                                <div className='formulas-container'>
                                    {links.formulasLatex.map((formula, fIndex) => (
                                        formula && formula.trim() && (
                                            <div key={fIndex} className='formula-item'>
                                                <InlineMath math={formula} />
                                            </div>
                                        )
                                    ))}
                                </div>
                            )}
                        </div>
                    ))
                ) : (
                    !loading && <p className='no-results'>Nenhum resultado encontrado.</p>
                )}
                {loading && <p className='loading-text'>Carregando...</p>}
            <div className='page-navigation'>
                <button onClick={handlePreviousPage} disabled={page === 1}>Anterior</button>
                {getPageRange().map(p => (
                    <button 
                        key={p} 
                        onClick={() => handlePage(p)}
                        className={page === p ? 'active' : 'inactive'}
                    >
                        {p}
                    </button>
                ))}
                <button onClick={handleNextPage} disabled={page === totalPages}>Proximo</button>            
            </div>
            </div>
        </div>
    )
}

export default ResultLinks