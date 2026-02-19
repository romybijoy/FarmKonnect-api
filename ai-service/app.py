from fastapi import FastAPI
from pydantic import BaseModel
from transformers import pipeline

app = FastAPI()

classifier = pipeline(
    "zero-shot-classification",
    model="typeform/distilbert-base-uncased-mnli"
)

class ContentRequest(BaseModel):
    text: str

@app.get("/")
def home():
    return {"message": "AI Service Running"}

@app.post("/validate-content")
def validate_content(req: ContentRequest):
    labels = ["agriculture related", "not agriculture related"]

    result = classifier(req.text, labels)

    best_label = result["labels"][0]
    best_score = result["scores"][0]

    verdict = "agri" if best_label == "agriculture related" else "non-agri"

    return {
        "verdict": verdict,
        "score": best_score
    }
