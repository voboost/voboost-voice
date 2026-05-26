import json
import re
import numpy as np
from sentence_transformers import SentenceTransformer, util

# 1. Та же модель, что в вашем ONNX-пайплайне
MODEL_NAME = "paraphrase-multilingual-MiniLM-L12-v2"
print(f"⏳ Загрузка модели: {MODEL_NAME}...")
model = SentenceTransformer(MODEL_NAME)

# 2. Очистка JSON от хвостовых пробелов в ключах и значениях
def clean_json(obj):
    if isinstance(obj, dict):
        return {k.strip(): clean_json(v) for k, v in obj.items()}
    elif isinstance(obj, list):
        return [clean_json(i) for i in obj]
    elif isinstance(obj, str):
        return obj.strip()
    return obj

# 3. Загрузка конфига
with open("config.json", "r", encoding="utf-8") as f:
    config = clean_json(json.load(f))

# 4. Сбор паттернов с нормализацией (1:1 как в normalizeText() Kotlin)
patterns = []
for cmd in config["commands"]:
    for pattern in cmd.get("patterns", []):
        clean = pattern.lower()
        clean = clean.replace("{temp}", "<NUM>") \
                     .replace("{contact}", "<NAME>") \
                     .replace("{number}", "<PHONE>")
        # Удаляем всё, кроме слов, пробелов, <, >, /, -
        clean = re.sub(r'[^\w\s<>/\-]', ' ', clean)
        clean = re.sub(r'\s+', ' ', clean).strip()
        
        patterns.append({
            "cmd_id": cmd["id"],
            "original": pattern,
            "clean": clean
        })

print(f"📊 Найдено паттернов: {len(patterns)}")
print("🔍 Вычисляем эмбеддинги и ищем кросс-конфликты...\n")

# 5. Инференс
texts = [p["clean"] for p in patterns]
embeddings = model.encode(texts, convert_to_tensor=True)

# 6. Попарное косинусное сходство
cos_sim = util.cos_sim(embeddings, embeddings)

# 7. Фильтрация ТОЛЬКО кросс-командных конфликтов
THRESHOLD = 0.78  # Оптимально для поиска ложных пересечений между разными командами
cross_conflicts = []

for i in range(len(patterns)):
    for j in range(i + 1, len(patterns)):
        score = cos_sim[i][j].item()
        # Игнорируем паттерны внутри одной команды, берём только РАЗНЫЕ
        if score >= THRESHOLD and patterns[i]["cmd_id"] != patterns[j]["cmd_id"]:
            cross_conflicts.append((patterns[i], patterns[j], score))

# Сортируем от самых похожих к менее похожим
cross_conflicts.sort(key=lambda x: x[2], reverse=True)

# 8. Вывод
if not cross_conflicts:
    print("✅ Отлично! Кросс-командных конфликтов не обнаружено.")
else:
    print(f"⚠️ Найдено {len(cross_conflicts)} кросс-конфликтов (сходство >= {THRESHOLD}):\n")
    for p1, p2, score in cross_conflicts:
        print(f"🔴 Сходство: {score:.3f}")
        print(f"  [{p1['cmd_id']}] {p1['original']}")
        print(f"  [{p2['cmd_id']}] {p2['original']}")
        print("-" * 50)

print("\n💡 Рекомендация: измените или удалите один из паттернов в паре, "
      "либо добавьте уникальные ключевые слова, чтобы развести команды.")