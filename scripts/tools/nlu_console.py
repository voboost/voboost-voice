#!/usr/bin/env python3
"""
NLU Console — интерактивный тест семантического поиска
Фикс: exact match (score=1.0) пропускаем автоматически
"""

import json
import re
import numpy as np
from sentence_transformers import SentenceTransformer

# ============ НАСТРОЙКИ ============
TEMPERATURE = 3.0
TEMPERATURE_OLD = 5.0

#THRESHOLDS = {
#    1: (0.55, 0.1),
#    2: (0.55, 0.1),
#    3: (0.55, 0.1),
#    4: (0.55, 0.1),
#}

THRESHOLDS = {
    1: (0.75, 0.10),
    2: (0.65, 0.10),
    3: (0.60, 0.08),
    4: (0.55, 0.05),   # 4+ слова: margin почти не нужен
}

# Стоп-слова
ACTION_WORDS = {
    "включи", "выключи", "отключи", "открой", "закрой", "сделай", "поставь",
    "перейди", "переключи", "активируй", "запусти", "останови", "дай", "давай",
    "хочу", "мне", "подними", "опусти", "раскрой", "прикрой", "проветри",
    "убери", "устрой", "закрыть", "открыть",
}

# Антонимы
ANTONYMS = {
    "открой": "закрой", "закрой": "открой",
    "подними": "опусти", "опусти": "подними",
    "включи": "выключи", "выключи": "включи",
    "отключи": "включи",
}

print("⏳ Загрузка модели...")
model = SentenceTransformer("sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2")
print("✅ Модель загружена\n")

with open("config.json", "r", encoding="utf-8") as f:
    config = json.load(f)

def normalize(text):
    text = text.lower()
    text = text.replace("{temp}", "22").replace("{contact}", "иван").replace("{number}", "8800")
    text = re.sub(r'[^a-zа-я0-9\s\-]', ' ', text)
    return re.sub(r'\s+', ' ', text).strip()

def has_antonym_conflict(query, pattern):
    q_words = set(query.split())
    p_words = set(pattern.split())
    for word, opposite in ANTONYMS.items():
        if word in q_words and opposite in p_words:
            return True
        if opposite in q_words and word in p_words:
            return True
    return False

# Собираем паттерны
patterns = []
for cmd in config["commands"]:
    if not cmd.get("enabled", True): continue
    for p in cmd.get("patterns", []):
        clean = normalize(p)
        patterns.append({
            "cmd": cmd["id"],
            "orig": p,
            "clean": clean,
        })

print(f"📊 Паттернов: {len(patterns)} | Команд: {len(set(p['cmd'] for p in patterns))}")
print("🔍 Эмбеддинги...")

emb_full = model.encode([p["clean"] for p in patterns], convert_to_tensor=False)
emb_norm = emb_full / np.linalg.norm(emb_full, axis=1, keepdims=True)

print("✅ Готов к работе!\n")
print("=" * 60)
print("Вводи запросы. Команды:")
print("  :old     — старый алгоритм (temp=5.0)")
print("  :new     — новый алгоритм (temp=2.5)")
print("  :exact   — только max")
print("  :temp N  — задать temperature")
print("  :quit    — выход")
print("=" * 60)

algo = "new"

def infer(text):
    norm = normalize(text)
    wc = len(norm.split())
    
    q_emb = model.encode(norm, convert_to_tensor=False)
    q_norm = q_emb / (np.linalg.norm(q_emb) + 1e-8)
    
    similarities = emb_norm @ q_norm
    
    matches = []
    exact_match_idx = -1
    exact_match_cmd = None
    
    for i, pat in enumerate(patterns):
        score = float(similarities[i])
        
        # Проверяем exact match
        if pat["clean"] == norm:
            score = 0.998
            exact_match_idx = i
            exact_match_cmd = pat["cmd"]
        
        # Штраф за антонимы
        if has_antonym_conflict(norm, pat["clean"]):
            score *= 0.5
        
        matches.append({
            "cmd": pat["cmd"],
            "orig": pat["orig"],
            "clean": pat["clean"],
            "score": score,
            "is_exact": pat["clean"] == norm,
        })
    
    # Агрегация по командам
    cmd_pats = {}
    for m in matches:
        cmd = m["cmd"]
        if cmd not in cmd_pats:
            cmd_pats[cmd] = []
        cmd_pats[cmd].append(m["score"])
    
    cmd_scores = {}
    for cmd, scores in cmd_pats.items():
        if algo == "exact" or wc == 1:
            cmd_scores[cmd] = max(scores)
        elif wc in [2, 3]:
            top3 = sorted(scores, reverse=True)[:3]
            cmd_scores[cmd] = sum(top3) / len(top3)
        elif algo == "old":
            mx = max(scores)
            ex = [np.exp((s - mx) * TEMPERATURE_OLD) for s in scores]
            s = sum(ex)
            cmd_scores[cmd] = sum(sc * (w / s) for sc, w in zip(scores, ex))
        else:
            mx = max(scores)
            ex = [np.exp((s - mx) * TEMPERATURE) for s in scores]
            s = sum(ex)
            cmd_scores[cmd] = sum(sc * (w / s) for sc, w in zip(scores, ex))
    
    ranked = sorted(cmd_scores.items(), key=lambda x: x[1], reverse=True)
    thr, mrg = THRESHOLDS.get(min(wc, 4), THRESHOLDS[4])
    
    best_cmd, best_sc = ranked[0]
    sec_cmd, sec_sc = ranked[1] if len(ranked) > 1 else ("-", 0.0)
    margin = best_sc - sec_sc
    
    # Проверка exact match
    is_exact = exact_match_idx >= 0 and best_cmd == exact_match_cmd
    
    # Для 1 слова — глобальная проверка (кроме exact match)
    ambiguous = False
    if wc == 1 and not is_exact:
        top2_global = sorted(matches, key=lambda x: x["score"], reverse=True)[:2]
        if len(top2_global) > 1:
            glob_margin = top2_global[0]["score"] - top2_global[1]["score"]
            if glob_margin < mrg:
                ambiguous = True
    
    # Exact match → автоматический MATCH
    matched = is_exact or (best_sc >= thr and margin >= mrg and not ambiguous)
    
    # ===== ВЫВОД =====
    print(f"\n{'='*60}")
    print(f"🎤 '{text}'  ({wc} слов)  [алгоритм: {algo}]")
    print(f"{'='*60}")
    
    if is_exact:
        print(f"✅ 🎯 EXACT MATCH | {best_cmd} = {best_sc:.4f}")
    else:
        status = "✅ MATCH" if matched else "❌ REJECT"
        print(f"{status} | {best_cmd} = {best_sc:.4f}  (порог: {thr})")
        print(f"   vs {sec_cmd} = {sec_sc:.4f}  (margin: {margin:.4f}, порог: {mrg})")
    
    if wc == 1 and ambiguous and not is_exact:
        print(f"   ⚠️ Глобальная неоднозначность")
    
    print(f"\n📋 Топ-10 паттернов:")
    top_pats = sorted(matches, key=lambda x: x["score"], reverse=True)[:10]
    for i, m in enumerate(top_pats, 1):
        marker = "→" if m["cmd"] == best_cmd else " "
        exact_mark = " [EXACT]" if m["is_exact"] else ""
        ant = " [ANTONIM]" if has_antonym_conflict(norm, m["clean"]) else ""
        print(f"   {marker} {i:2}. [{m['cmd']:25}] {m['score']:.4f} | {m['orig']}{exact_mark}{ant}")
    
    print(f"\n🏆 Топ-5 команд:")
    for i, (cmd, sc) in enumerate(ranked[:5], 1):
        marker = "→" if i == 1 else " "
        print(f"   {marker} {i}. {cmd:25} = {sc:.4f}")
    
    return matched

# ============ ЦИКЛ ============

while True:
    try:
        text = input("\n> ").strip()
        if not text: continue
        
        if text == ":quit": break
        elif text == ":old": algo = "old"; print("Переключено на OLD"); continue
        elif text == ":new": algo = "new"; print("Переключено на NEW"); continue
        elif text == ":exact": algo = "exact"; print("Переключено на EXACT"); continue
        elif text.startswith(":temp "):
            try: TEMPERATURE = float(text.split()[1]); print(f"temp = {TEMPERATURE}")
            except: print("Использование: :temp 2.5")
            continue
        
        infer(text)
        
    except KeyboardInterrupt: break
    except Exception as e:
        print(f"Ошибка: {e}")
        import traceback
        traceback.print_exc()

print("\n👋 Пока!")