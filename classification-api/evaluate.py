# 정답.py(40건 라벨링된 홀드아웃)로 로컬 FastAPI(/predict) 모델의 실제 정확도를 확인한다.
#
# 실행 방법:
#   1) uvicorn main:app --port 8000 로 서버를 먼저 띄워둔다
#   2) (같은 폴더에서) python evaluate.py
#
# 정답 데이터가 다 진보/보수/중립/판단불가 4종류에서 어떻게 흩어지는지,
# 모델이 실제로 값을 구분해서 내는지(all-진보 편향은 아닌지) 확인하기 위한 스크립트다.

import ast
import json
import urllib.request
from collections import Counter
from pathlib import Path

DATA_FILE = Path(__file__).parent / "정답.py"
PREDICT_URL = "http://localhost:8000/predict"


def predict(title: str, body: str) -> dict:
    payload = json.dumps({"title": title, "body": body}).encode("utf-8")
    req = urllib.request.Request(
        PREDICT_URL, data=payload, headers={"Content-Type": "application/json"}, method="POST"
    )
    with urllib.request.urlopen(req, timeout=60) as resp:
        return json.loads(resp.read().decode("utf-8"))


def read_py_data(path: Path) -> list:
    tree = ast.parse(path.read_text(encoding="utf-8"))
    for node in tree.body:
        if isinstance(node, ast.Assign) and any(getattr(t, "id", None) == "data" for t in node.targets):
            return list(ast.literal_eval(node.value))
    raise ValueError(f"{path}: `data = [...]` 를 찾지 못했습니다.")


def main():
    rows = read_py_data(DATA_FILE)
    print(f"총 {len(rows)}건 로드")

    correct = 0
    pred_counter = Counter()
    true_counter = Counter()
    confusion = Counter()  # (정답, 예측) -> count
    mistakes = []

    for i, row in enumerate(rows):
        title = row.get("타이틀") or row.get("title") or ""
        body = row.get("본문") or row.get("body") or ""
        answer = row.get("정치성향") or row.get("정답") or row.get("label") or ""

        result = predict(title, body)
        predicted = result["정치성향"]
        confidence = result["확신도"]

        true_counter[answer] += 1
        pred_counter[predicted] += 1
        confusion[(answer, predicted)] += 1

        is_correct = predicted == answer
        if is_correct:
            correct += 1
        else:
            mistakes.append((i, title[:30], answer, predicted, confidence))

        print(f"[{i:2d}] 정답={answer:5s} 예측={predicted:5s} 확신도={confidence:.3f} {'OK' if is_correct else 'X'}")

    print("\n=== 요약 ===")
    print(f"정확도: {correct}/{len(rows)} = {correct / len(rows):.1%}")
    print(f"정답 분포: {dict(true_counter)}")
    print(f"예측 분포: {dict(pred_counter)}")

    print("\n=== 오답 목록 ===")
    for i, title, answer, predicted, conf in mistakes:
        print(f"[{i:2d}] {title}... | 정답={answer} 예측={predicted} (확신도 {conf:.3f})")


if __name__ == "__main__":
    main()
