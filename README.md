# 🚀 Computation Search - Motor de Busca Inteligente

Este projeto é um motor de busca avançado que combina a robustez do **Elasticsearch** com a inteligência de **LLMs (Ollama)** para identificar e priorizar fórmulas matemáticas e conteúdos computacionais.

## 🌟 Funcionalidades Principais

*   **Busca Inteligente com LLM:** Identifica automaticamente nomes comuns de fórmulas LaTeX (ex: ao buscar uma fórmula, o LLM identifica como "Teorema de Pitágoras") e usa essa informação para dar boost nos resultados.
*   **Otimização de Latência:**
    *   **Cache com Caffeine:** Consultas repetidas ao LLM são cacheadas em memória para resposta instantânea.
    *   **Execução Paralela:** O backend processa o LLM e a busca básica em paralelo com um timeout rigoroso de 1.5s para garantir que o usuário nunca fique esperando.
*   **Filtro de Duplicatas:** Uso de `Collapse` no Elasticsearch para garantir que apenas uma página por URL seja exibida nos resultados.
*   **Ranking Avançado:** Pesos customizados para títulos (100f) com correspondência estrita (`AND`) e conteúdo (50f).
*   **Renderização Matemática:** Frontend integrado com **KaTeX** para exibir fórmulas LaTeX de forma elegante.

## 🏗️ Arquitetura

### Backend (Spring Boot)
*   **Java 21** & **Spring Boot 3.5.14**
*   **Elasticsearch Java Client 8.11.1**
*   **Spring AI (Ollama):** Integração com Llama 3.1 para processamento de linguagem natural.
*   **Caffeine Cache:** Gerenciamento de cache em memória.

### Frontend (React)
*   **Vite** & **React 18**
*   **KaTeX:** Renderização de alta performance para matemática.
*   **React Router Dom:** Gerenciamento de rotas e estados de navegação.

## 🛠️ Configuração

### Pré-requisitos
1.  **Ollama** instalado e rodando o modelo `llama3.1:8b`.
2.  **Elasticsearch 8.x** com o índice `wikipedia` populado.
3.  **Node.js** para o frontend.

### Variáveis de Ambiente
O projeto utiliza as seguintes chaves no `application.yml`:
```yaml
environments:
  elastic:
    elasticPWD: seu_password
    host: localhost
    port: 9200
```

## 🚀 Como Executar

### Backend
```bash
mvn spring-boot:run
```

### Frontend
```bash
cd front-end/ComputationSearch
npm install
npm run dev
```

## 🔍 Query no Kibana DevTools

Para testar o comportamento da busca diretamente no Kibana, a estrutura da query gerada pelo backend é equivalente a esta:

```json
GET /wikipedia/_search
{
  "from": 0,
  "size": 10,
  "query": {
    "bool": {
      "must": [
        {
          "multi_match": {
            "fields": ["formulas_latex", "content"],
            "query": "termo_da_busca",
            "boost": 30
          }
        }
      ],
      "should": [
        {
          "match": {
            "title": {
              "query": "nome_identificado_pela_llm",
              "operator": "and",
              "boost": 100
            }
          }
        },
        {
          "match": {
            "content": {
              "query": "nome_identificado_pela_llm",
              "boost": 50
            }
          }
        }
      ]
    }
  },
  "collapse": {
    "field": "url.keyword"
  }
}
```

---
Desenvolvido para fins de pesquisa e busca computacional avançada.
