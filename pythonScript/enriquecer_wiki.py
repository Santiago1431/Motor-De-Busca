import json
import requests
import re
import time
import os

def get_all_wikipedia_formulas(title):
    """Busca a página inteira na Wikipedia com sistema de tentativas (Retries)"""
    URL = "https://en.wikipedia.org/w/api.php"
    params = {
        "action": "parse",
        "page": title,
        "format": "json",
        "prop": "wikitext",
        "redirects": 1
    }
    
    max_tentativas = 3
    for tentativa in range(max_tentativas):
        try:
            # User-Agent identifica quem você é (bom para evitar bloqueios)
            headers = {'User-Agent': 'MotorDeBuscaEstudantil/1.0 (seuemail@exemplo.com)'}
            response = requests.get(URL, params=params, headers=headers, timeout=20)
            
            if response.status_code == 429: # Rate Limit
                print(f"   ! Muita pressa. Aguardando 10 segundos...")
                time.sleep(10)
                continue

            data = response.json()
            if "parse" in data:
                wikitext = data["parse"]["wikitext"]["*"]
                formulas = re.findall(r'<math>(.*?)</math>', wikitext, re.DOTALL)
                cleaned_formulas = [re.sub(r'<.*?>', '', f).strip() for f in formulas if f.strip()]
                return list(set(cleaned_formulas))
            return [] # Página sem fórmulas
            
        except (requests.exceptions.ConnectionError, requests.exceptions.Timeout):
            if tentativa < max_tentativas - 1:
                espera = 5 * (tentativa + 1)
                print(f"   ! Erro de conexão em {title}. Tentativa {tentativa+1}/{max_tentativas}. Aguardando {espera}s...")
                time.sleep(espera)
            else:
                return None # Falhou após todas as tentativas
    return None
def main():
    input_file = 'wiki.json'
    output_file = 'wiki_total.json'
    
    if not os.path.exists(input_file):
        print(f"Erro: {input_file} não encontrado!")
        return

    # Lendo o arquivo (suporta formato de um objeto por linha)
    database = []
    print("Lendo banco de dados original...")
    with open(input_file, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if not line: continue
            if line.startswith('[') or line.startswith(']'): continue
            if line.endswith(','): line = line[:-1]
            try:
                database.append(json.loads(line))
            except: continue

    # Controle de progresso
    if os.path.exists(output_file):
        with open(output_file, 'r', encoding='utf-8') as f:
            try:
                enriched_data = json.load(f)
                processed_titles = {item['title'] for item in enriched_data}
                print(f"Retomando: {len(processed_titles)} já extraídos.")
            except:
                enriched_data = []
                processed_titles = set()
    else:
        enriched_data = []
        processed_titles = set()

    total = len(database)

    try:
        for i, entry in enumerate(database):
            title = entry.get('title')
            if not title or title in processed_titles:
                continue

            # BUSCA TUDO: Não filtramos mais por '<som'
            print(f"[{i+1}/{total}] Extraindo TODAS as fórmulas de: {title}")
            formulas = get_all_wikipedia_formulas(title)
            
            if formulas is not None:
                entry['formulas_latex'] = formulas
                if len(formulas) > 0:
                    print(f"   -> Encontradas {len(formulas)} fórmulas.")
            else:
                print(f"   -> Erro de conexão. Pulando...")
            
            enriched_data.append(entry)
            processed_titles.add(title)

            # Checkpoint a cada 5 itens para maior segurança
            if len(enriched_data) % 5 == 0:
                with open(output_file, 'w', encoding='utf-8') as f:
                    json.dump(enriched_data, f, indent=2, ensure_ascii=False)
            
            # Pausa para não ser banido pela Wikipedia (importante!)
            time.sleep(0.5)

    except KeyboardInterrupt:
        print("\nInterrompido. Salvando progresso...")
    
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(enriched_data, f, indent=2, ensure_ascii=False)
    print(f"Concluído! Total de registros com fórmulas: {len(enriched_data)}")

if __name__ == "__main__":
    main()