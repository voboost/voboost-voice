#!/usr/bin/env python3
"""
NLU Test Bench — эмуляция NLUOrtEngine.kt на Python
Запуск: python nlu_test_bench.py
"""

import json
import re
import numpy as np
from sentence_transformers import SentenceTransformer, util
from dataclasses import dataclass
from typing import List, Dict, Optional
from collections import defaultdict

# ============ КОНФИГУРАЦИЯ (меняй и экспериментируй) ============

CONFIG = {
    "model": "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2",
    "threshold_1word": 0.75,
    "threshold_2to3": 0.65,
    "threshold_4plus": 0.55,
    "margin_1word": 0.15,
    "margin_2to3": 0.12,
    "margin_4plus": 0.08,
    "temperature": 2.5,           # для softmax 4+ слов
    "temperature_old": 5.0,       # для сравнения с твоим текущим
    "use_content_filter": True,   # двойное сравнение (полный / без действий)
    "content_boost": 1.05,        # множитель для content-score
}

# Стоп-слова (действия)
ACTION_WORDS = {
    "включи", "выключи", "отключи", "открой", "закрой",
    "сделай", "поставь", "перейди", "переключи", "активируй",
    "запусти", "останови", "дай", "давай", "хочу", "мне",
    "подними", "опусти", "раскрой", "прикрой", "проветри",
    "убери", "устрой", "закрыть", "открыть", "откройте",
    "закройте", "включите", "выключите", "сделайте", "поставьте",
    "перейдите", "переключите", "активируйте", "запустите",
    "остановите", "поднимите", "опустите", "раскройте", "прикройте",
    "в", "на", "по", "для", "и", "или", "не", "все", "весь",
}

# Тестовые запросы (добавляй свои)
TEST_QUERIES = [
    # Однословные
    "зарядка",
    "спорт",
    "эко",
    "мойка",
    "душно",
    "скользко",
    "сохранение",
    "бензобак",
    
    # Двухсловные
    "открой окно",
    "закрой стекло",
    "включи климат",
    "подними окно",
    "хочу проветрить",
    "мне жарко",
    "мне холодно",
    
    # Трёхсловные
    "открой все окна",
    "закрой все стекла",
    "включи кондиционер",
    "перейди в спорт",
    "сделай прохладнее",
    "подготовь к мойке",
    
    # Длинные
    "открой окно со стороны водителя",
    "хочу подзарядить машину",
    "перейди в режим комфорта",
    "активируй детский режим",
    "поставь на экономию топлива",
    
    # Спорные / граничные
    "проветри",           # window_all_open?
    "сделай сквозняк",    # window_all_open?
    "убери сквозняк",     # window_all_close?
    "погоняем",           # sport_driving_mode?
    "гололед",            # snow_driving_mode?
    "хочу погонять",      # sport vs fuel_tank_open?
    "хочу заправиться",   # fuel_tank_open?
    "хочу проветрить",    # window_open?
]

# ============ КЛАССЫ ============

@dataclass
class PatternMatch:
    cmd_id: str
    pattern: str
    score_full: float
    score_content: float
    
    @property
    def best_score(self) -> float:
        return max(self.score_full, self.score_content)

class NLUTestBench:
    def __init__(self, config_path: str):
        print(f"⏳ Загрузка модели: {CONFIG['model']}...")
        self.model = SentenceTransformer(CONFIG["model"])
        self.embed_dim = self.model.get_sentence_embedding_dimension()
        print(f"✅ Модель загружена, размерность: {self.embed_dim}")
        
        # Загрузка конфига
        with open(config_path, "r", encoding="utf-8") as f:
            self.config = json.load(f)
        
        self.patterns = self._load_patterns()
        self.pattern_embeddings_full = None
        self.pattern_embeddings_content = None
        self._precompute_embeddings()
        
        print(f"📊 Паттернов: {len(self.patterns)}")
        print(f"📝 Команд: {len(set(p['cmd_id'] for p in self.patterns))}")
    
    def _load_patterns(self) -> List[Dict]:
        patterns = []
        for cmd in self.config["commands"]:
            if not cmd.get("enabled", True):
                continue
            for pattern in cmd.get("patterns", []):
                clean = self._normalize_text(pattern)
                content = self._extract_content_words(clean)
                patterns.append({
                    "cmd_id": cmd["id"],
                    "original": pattern,
                    "clean": clean,
                    "content": content,
                })
        return patterns
    
    def _normalize_text(self, text: str) -> str:
        text = text.lower()
        text = text.replace("{temp}", "двадцать два")
        text = text.replace("{contact}", "иван")
        text = text.replace("{number}", "одиннадцать")
        text = re.sub(r'[^a-zа-я0-9\s\-]', ' ', text)
        text = re.sub(r'\s+', ' ', text).strip()
        return text
    
    def _extract_content_words(self, text: str) -> str:
        words = text.split()
        content = [w for w in words if w not in ACTION_WORDS]
        result = " ".join(content)
        return result if result else text
    
    def _precompute_embeddings(self):
        print("🔍 Предвычисление эмбеддингов...")
        texts = [p["clean"] for p in self.patterns]
        contents = [p["content"] for p in self.patterns]
        
        self.pattern_embeddings_full = self.model.encode(texts, convert_to_tensor=True)
        self.pattern_embeddings_content = self.model.encode(contents, convert_to_tensor=True)
        print("✅ Эмбеддинги готовы")
    
    def _cosine_similarity(self, a: np.ndarray, b: np.ndarray) -> float:
        # a: [dim], b: [n, dim]
        a_norm = a / (np.linalg.norm(a) + 1e-8)
        b_norm = b / (np.linalg.norm(b, axis=1, keepdims=True) + 1e-8)
        return float(np.dot(b_norm, a_norm))
    
    def parse_command(self, text: str, algorithm: str = "new") -> Optional[Dict]:
        """
        algorithm: "old" — твой текущий, "new" — предложенный, "exact" — только max
        """
        normalized = self._normalize_text(text)
        content_text = self._extract_content_words(normalized)
        
        # Эмбеддинг запроса
        query_emb_full = self.model.encode(normalized, convert_to_tensor=False)
        query_emb_content = self.model.encode(content_text, convert_to_tensor=False)
        
        word_count = len(normalized.split())
        
        # Сравнение со всеми паттернами
        all_matches = []
        for i, pat in enumerate(self.patterns):
            score_full = self._cosine_similarity(
                query_emb_full, 
                self.pattern_embeddings_full[i].cpu().numpy()
            )
            score_content = self._cosine_similarity(
                query_emb_content,
                self.pattern_embeddings_content[i].cpu().numpy()
            )
            all_matches.append(PatternMatch(
                cmd_id=pat["cmd_id"],
                pattern=pat["original"],
                score_full=score_full,
                score_content=score_content
            ))
        
        # Выбор агрегации
        if algorithm == "old":
            return self._aggregate_old(all_matches, word_count, text)
        elif algorithm == "exact":
            return self._aggregate_exact(all_matches, word_count, text)
        else:
            return self._aggregate_new(all_matches, word_count, text)
    
    def _aggregate_exact(self, matches: List[PatternMatch], word_count: int, text: str) -> Optional[Dict]:
        """Только максимум, без softmax"""
        cmd_scores = defaultdict(list)
        for m in matches:
            score = max(m.score_full, m.score_content * CONFIG["content_boost"]) if CONFIG["use_content_filter"] else m.score_full
            cmd_scores[m.cmd_id].append(score)
        
        result = {cmd: max(scores) for cmd, scores in cmd_scores.items()}
        return self._finalize(result, word_count, text, "exact")
    
    def _aggregate_old(self, matches: List[PatternMatch], word_count: int, text: str) -> Optional[Dict]:
        """Твой текущий алгоритм: softmax temperature=5.0"""
        cmd_scores = defaultdict(list)
        for m in matches:
            score = max(m.score_full, m.score_content * CONFIG["content_boost"]) if CONFIG["use_content_filter"] else m.score_full
            cmd_scores[m.cmd_id].append(score)
        
        result = {}
        for cmd, scores in cmd_scores.items():
            result[cmd] = self._softmax_score(scores, CONFIG["temperature_old"])
        
        return self._finalize(result, word_count, text, "old")
    
    def _aggregate_new(self, matches: List[PatternMatch], word_count: int, text: str) -> Optional[Dict]:
        """Новый алгоритм: разная агрегация по длине"""
        cmd_scores = defaultdict(list)
        for m in matches:
            score = max(m.score_full, m.score_content * CONFIG["content_boost"]) if CONFIG["use_content_filter"] else m.score_full
            cmd_scores[m.cmd_id].append(score)
        
        result = {}
        for cmd, scores in cmd_scores.items():
            if word_count == 1:
                # Exact: просто максимум
                result[cmd] = max(scores)
            elif word_count in [2, 3]:
                # Mean top-3
                top3 = sorted(scores, reverse=True)[:3]
                result[cmd] = sum(top3) / len(top3)
            else:
                # Softmax с temperature=2.5
                result[cmd] = self._softmax_score(scores, CONFIG["temperature"])
        
        return self._finalize(result, word_count, text, "new")
    
    def _softmax_score(self, scores: List[float], temperature: float) -> float:
        if not scores:
            return 0.0
        max_score = max(scores)
        exp_scores = [np.exp((s - max_score) * temperature) for s in scores]
        sum_exp = sum(exp_scores)
        if sum_exp == 0:
            return max_score
        return sum(s * (w / sum_exp) for s, w in zip(scores, exp_scores))
    
    def _finalize(self, cmd_scores: Dict[str, float], word_count: int, text: str, algo: str) -> Optional[Dict]:
        """Применение порогов и формирование результата"""
        if not cmd_scores:
            return None
        
        sorted_cmds = sorted(cmd_scores.items(), key=lambda x: x[1], reverse=True)
        best_id, best_score = sorted_cmds[0]
        second_id, second_score = sorted_cmds[1] if len(sorted_cmds) > 1 else ("none", 0.0)
        margin = best_score - second_score
        
        # Динамические пороги
        if word_count == 1:
            threshold = CONFIG["threshold_1word"]
            margin_thr = CONFIG["margin_1word"]
        elif word_count in [2, 3]:
            threshold = CONFIG["threshold_2to3"]
            margin_thr = CONFIG["margin_2to3"]
        else:
            threshold = CONFIG["threshold_4plus"]
            margin_thr = CONFIG["margin_4plus"]
        
        # Дополнительная проверка для 1-словных: глобальный margin
        global_margin_ok = True
        if word_count == 1:
            all_best_scores = [(cmd, score) for cmd, score in sorted_cmds]
            if len(all_best_scores) > 1:
                global_margin = all_best_scores[0][1] - all_best_scores[1][1]
                if global_margin < margin_thr:
                    global_margin_ok = False
        
        matched = best_score >= threshold and margin >= margin_thr and global_margin_ok
        
        return {
            "text": text,
            "algorithm": algo,
            "word_count": word_count,
            "best_id": best_id,
            "best_score": round(best_score, 4),
            "second_id": second_id,
            "second_score": round(second_score, 4),
            "margin": round(margin, 4),
            "threshold": threshold,
            "margin_threshold": margin_thr,
            "matched": matched,
            "top_5": [(cmd, round(score, 4)) for cmd, score in sorted_cmds[:5]]
        }

def print_result(r: Dict, show_top5: bool = True):
    status = "✅ MATCH" if r["matched"] else "❌ REJECT"
    print(f"\n{status} | '{r['text']}' ({r['word_count']} слов) | algo: {r['algorithm']}")
    print(f"   → {r['best_id']} = {r['best_score']} (порог: {r['threshold']})")
    print(f"   vs {r['second_id']} = {r['second_score']}")
    print(f"   margin: {r['margin']} (порог: {r['margin_threshold']})")
    if show_top5:
        print(f"   top-5: {r['top_5']}")

def compare_algorithms(bench: NLUTestBench, queries: List[str]):
    """Сравнение трёх алгоритмов на одних запросах"""
    print("\n" + "="*80)
    print("СРАВНЕНИЕ АЛГОРИТМОВ")
    print("="*80)
    
    for query in queries:
        print(f"\n{'─'*60}")
        print(f"Запрос: '{query}'")
        print("─"*60)
        
        for algo in ["old", "exact", "new"]:
            result = bench.parse_command(query, algorithm=algo)
            status = "✅" if result["matched"] else "❌"
            print(f"  {status} {algo:5} | {result['best_id']:25} | "
                  f"score={result['best_score']:.3f} | margin={result['margin']:.3f}")

def main():
    import sys
    config_path = sys.argv[1] if len(sys.argv) > 1 else "config.json"
    
    bench = NLUTestBench(config_path)
    
    # Режим 1: Сравнение алгоритмов
    compare_algorithms(bench, TEST_QUERIES)
    
    # Режим 2: Интерактивный ввод
    print("\n" + "="*80)
    print("ИНТЕРАКТИВНЫЙ РЕЖИМ (введи 'quit' для выхода)")
    print("="*80)
    
    while True:
        try:
            query = input("\n> ").strip()
            if query.lower() in ["quit", "exit", "q"]:
                break
            
            for algo in ["old", "new"]:
                result = bench.parse_command(query, algorithm=algo)
                print_result(result, show_top5=True)
                
        except EOFError:
            break
        except Exception as e:
            print(f"Ошибка: {e}")

if __name__ == "__main__":
    main()