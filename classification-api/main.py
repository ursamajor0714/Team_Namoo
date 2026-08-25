# 로컬 FastAPI 분류 서버
#
# 콜랩(pipeline.py)에서 klue/bert-base를 파인튜닝해 Drive의
# political_pipeline/models/latest 에 저장한 모델을 로컬에서 서빙한다.
# pipeline.py의 predict()/classify() 로직을 그대로 옮긴 것이다.
#
# 실행 방법:
#   1) pip install -r requirements.txt
#   2) 콜랩 Drive의 political_pipeline/models/latest 폴더 전체를
#      (config.json, tokenizer 관련 파일들, 모델 가중치 포함해서)
#      이 파일과 같은 위치의 model/latest 폴더로 복사
#   3) uvicorn main:app --port 8000
#   4) http://localhost:8000/health 로 뜨는지 확인

import os
from pathlib import Path

import torch
from fastapi import FastAPI
from pydantic import BaseModel
from transformers import AutoModelForSequenceClassification, AutoTokenizer

LABELS = ["진보", "중립", "보수", "판단불가"]
ID2LABEL = {i: x for i, x in enumerate(LABELS)}
MAX_LENGTH = 512

MODEL_DIR = Path(os.getenv("MODEL_DIR", "model/latest"))

app = FastAPI()

_model = None
_tokenizer = None


def _load():
    global _model, _tokenizer
    if _model is None:
        if not (MODEL_DIR / "config.json").exists():
            raise RuntimeError(
                f"{MODEL_DIR} 에 학습된 모델이 없습니다. "
                "콜랩 Drive의 political_pipeline/models/latest 를 이 경로로 복사하세요."
            )
        _tokenizer = AutoTokenizer.from_pretrained(str(MODEL_DIR))
        _model = AutoModelForSequenceClassification.from_pretrained(str(MODEL_DIR))
        _model.to("cuda" if torch.cuda.is_available() else "cpu")
        _model.eval()
    return _model, _tokenizer


class PredictRequest(BaseModel):
    title: str = ""
    body: str = ""


class PredictResponse(BaseModel):
    정치성향: str
    확신도: float


@app.on_event("startup")
def _startup():
    _load()


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/predict", response_model=PredictResponse)
def predict(req: PredictRequest):
    model, tok = _load()
    text = f"{req.title}\n{req.body}"
    enc = tok(
        [text], truncation=True, max_length=MAX_LENGTH, padding=True, return_tensors="pt"
    ).to(model.device)
    with torch.no_grad():
        logits = model(**enc).logits
    prob = torch.softmax(logits, dim=-1)
    conf, idx = torch.max(prob, dim=-1)
    return PredictResponse(정치성향=ID2LABEL[int(idx[0])], 확신도=round(float(conf[0]), 4))
