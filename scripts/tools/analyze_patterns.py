# pip install sentence-transformers numpy
import json
import numpy as np
from sentence_transformers import SentenceTransformer, util

# 1. Загрузка модели (та же архитектура, что в вашем ONNX-пайплайне)
MODEL_NAME = "paraphrase-multilingual-MiniLM-L12-v2"
print(f"⏳ Загрузка модели: {MODEL_NAME}...")
model = SentenceTransformer(MODEL_NAME)

# 2. Утилита для очистки JSON от хвостовых пробелов в ключах/значениях
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

# 4. Сбор паттернов с нормализацией (как в вашем Kotlin-коде)
patterns = []
for cmd in config["commands"]:
    for pattern in cmd.get("patterns", []):
        clean = pattern.lower()
        # Заменяем плейсхолдеры на токены, как в normalizeText()
        clean = clean.replace("{temp}", "<NUM>") \
                     .replace("{contact}", "<NAME>") \
                     .replace("{number}", "<PHONE>")
        # Удаляем лишние пробелы
        clean = " ".join(clean.split())
        
        patterns.append({
            "cmd_id": cmd["id"],
            "original": pattern.strip(),
            "clean": clean
        })

print(f"📊 Найдено паттернов: {len(patterns)}")
print("🔍 Вычисляем эмбеддинги и сходства...\n")

# 5. Инференс
texts = [p["clean"] for p in patterns]
embeddings = model.encode(texts, convert_to_tensor=True)

# 6. Попарное косинусное сходство
cos_sim = util.cos_sim(embeddings, embeddings)

# 7. Фильтрация похожих пар
THRESHOLD = 0.95  # Порог "почти идентично"
similar_pairs = []

for i in range(len(patterns)):
    for j in range(i + 1, len(patterns)):
        score = cos_sim[i][j].item()
        if score >= THRESHOLD:
            similar_pairs.append((patterns[i], patterns[j], score))

# Сортируем по убыванию сходства
similar_pairs.sort(key=lambda x: x[2], reverse=True)

# 8. Вывод результатов
if not similar_pairs:
    print("✅ Отлично! Паттернов-дубликатов не обнаружено.")
else:
    print(f"⚠️ Найдено {len(similar_pairs)} пар с сходством >= {THRESHOLD}:\n")
    for p1, p2, score in similar_pairs:
        same_cmd = "🔴 (ОДНА КОМАНДА)" if p1["cmd_id"] == p2["cmd_id"] else "🟡 (РАЗНЫЕ КОМАНДЫ)"
        print(f"Сходство: {score:.3f} {same_cmd}")
        print(f"  [{p1['cmd_id']}] {p1['original']}")
        print(f"  [{p2['cmd_id']}] {p2['original']}")
        print("-" * 50)

print("\n💡 Рекомендация: удалите один из пары с 🔴, если они не нужны для ASR-устойчивости.")