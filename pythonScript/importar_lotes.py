import json
import requests
import time
from requests.auth import HTTPBasicAuth

# --- CONFIGURAÇÕES ---
FILE_PATH = 'wiki_total.json'
ES_URL = "https://localhost:9200/wikipedia/_bulk"
ES_USER = "elastic"
ES_PASS = "14311982"
BATCH_SIZE = 500  # Enviar 500 documentos por vez evita o erro 429

def main():
    print(f"Lendo arquivo {FILE_PATH}...")

    try:
        with open(FILE_PATH, 'r', encoding='utf-8') as f:
            data = json.load(f)
    except Exception as e:
        print(f"Erro ao ler o JSON: {e}")
        return

    total_docs = len(data)
    print(f"Total de documentos para importar: {total_docs}")

    headers = {"Content-Type": "application/x-ndjson"}

    # Processamento em lotes
    for i in range(0, total_docs, BATCH_SIZE):
        batch = data[i : i + BATCH_SIZE]
        payload = ""

        for entry in batch:
            # Comando de indexação para o Elasticsearch
            payload += json.dumps({"index": {}}) + "\n"
            # Dados do documento
            payload += json.dumps(entry) + "\n"

        try:
            # Envio para a API Bulk
            # verify=False por causa do certificado auto-assinado (insecure)
            response = requests.post(
                ES_URL,
                data=payload,
                headers=headers,
                auth=HTTPBasicAuth(ES_USER, ES_PASS),
                verify=False,
                timeout=60
            )

            if response.status_code == 200:
                print(f"Progresso: {min(i + BATCH_SIZE, total_docs)}/{total_docs} importados.")
            else:
                print(f"Erro no lote {i}: {response.status_code} - {response.text}")

        except Exception as e:
            print(f"Falha na conexão: {e}")
            time.sleep(2) # Espera um pouco antes de tentar o próximo lote

    print("Processo finalizado!")

if __name__ == "__main__":
    main()