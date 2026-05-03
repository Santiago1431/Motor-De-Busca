# 🚀 Computation Search - Motor de Busca Inteligente

Este projeto é um motor de busca avançado que combina a robustez do **Elasticsearch** com a inteligência de **LLMs (Ollama)** e **OCR especializado (LatexOCR)** para identificar e priorizar fórmulas matemáticas e conteúdos computacionais.

## 🌟 Funcionalidades Principais

*   **Busca por Imagem (LaTeX OCR):** Permite o upload de imagens contendo fórmulas matemáticas. O sistema utiliza um microserviço em Python com **pix2tex (LatexOCR)** para extrair o código LaTeX com alta precisão.
*   **Busca Inteligente com LLM:** Identifica automaticamente nomes comuns de fórmulas LaTeX (ex: ao buscar uma fórmula, o LLM identifica como "Teorema de Pitágoras") e usa essa informação para dar boost nos resultados.
*   **Otimização de Latência:**
    *   **Cache com Caffeine:** Consultas repetidas ao LLM são cacheadas em memória para resposta instantânea.
    *   **Execução Paralela:** O backend processa o LLM e a busca básica em paralelo para garantir performance.
*   **Filtro de Duplicatas:** Uso de `Collapse` no Elasticsearch para garantir que apenas uma página por URL seja exibida nos resultados.
*   **Ranking Avançado:** Pesos customizados para títulos com correspondência estrita e conteúdo.
*   **Renderização Matemática:** Frontend integrado com **KaTeX** para exibir fórmulas LaTeX de forma elegante.

## 🏗️ Arquitetura

### Backend (Spring Boot)
*   **Java 21** & **Spring Boot 3.x**
*   **Elasticsearch Java Client**
*   **Spring AI (Ollama):** Integração com `llama3.1:8b` para identificação de fórmulas e `llava` para visão básica.
*   **Caffeine Cache:** Gerenciamento de cache em memória.

### Microserviço de OCR (Python)
*   **FastAPI:** API de alta performance para o serviço de OCR.
*   **LatexOCR (pix2tex):** Modelo Transformer especializado em converter imagens de fórmulas em código LaTeX.

### Frontend (React)
*   **Vite** & **React 18**
*   **KaTeX:** Renderização de alta performance para matemática.

## 🛠️ Configuração

### Pré-requisitos
1.  **Ollama** instalado e rodando os modelos `llama3.1:8b` e `llava`.
2.  **Elasticsearch 8.x** populado.
3.  **Python 3.12+** (para o serviço de OCR).
4.  **Node.js** para o frontend.

### Configuração do Serviço de OCR (Python)
```bash
cd pythonScript
source venv/bin/activate
pip install -r requirements.txt  # ou instalar pix2tex fastapi uvicorn
python ocr_service.py
```
O serviço rodará por padrão na porta **8001**.

## 🚀 Como Executar

### Backend Java
```bash
mvn spring-boot:run
```

### Frontend
```bash
cd front-end/ComputationSearch
npm install
npm run dev
```

---
Desenvolvido para fins de pesquisa e busca computacional avançada.
