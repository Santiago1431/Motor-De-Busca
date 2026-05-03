from fastapi import FastAPI, UploadFile, File
from pix2tex.cli import LatexOCR
from PIL import Image
import io
import uvicorn

app = FastAPI()
model = LatexOCR()

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    try:
        img_bytes = await file.read()
        img = Image.open(io.BytesIO(img_bytes))
        res = model(img)
        return {"latex": res}
    except Exception as e:
        return {"error": str(e)}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8001)
