#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Конвертация .md файлов из Windows-1251 в UTF-8
"""

import os
from pathlib import Path

# Путь к проекту
PROJECT_ROOT = Path(r"D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant")

# Кодировки для попытки чтения
ENCODINGS = ['cp1251', 'utf-8', 'cp866', 'koi8-r']

def detect_encoding(file_path: Path) -> str | None:
    """Определить кодировку файла перебором"""
    for encoding in ENCODINGS:
        try:
            with open(file_path, 'r', encoding=encoding) as f:
                f.read()
            return encoding
        except (UnicodeDecodeError, UnicodeError):
            continue
    return None

def convert_file(file_path: Path, source_encoding: str) -> bool:
    """Конвертировать файл в UTF-8"""
    try:
        # Читать в исходной кодировке
        with open(file_path, 'r', encoding=source_encoding) as f:
            content = f.read()
        
        # Записать в UTF-8 без BOM
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        
        return True
    except Exception as e:
        print(f"  ❌ Ошибка: {e}")
        return False

def main():
    print("=" * 60)
    print("Конвертация .md файлов из Windows-1251 в UTF-8")
    print("=" * 60)
    print(f"Проект: {PROJECT_ROOT}\n")
    
    # Найти все .md файлы
    md_files = list(PROJECT_ROOT.rglob("*.md"))
    
    if not md_files:
        print("❌ .md файлы не найдены")
        return
    
    print(f"Найдено файлов: {len(md_files)}\n")
    
    converted = 0
    errors = 0
    skipped = 0
    
    for file_path in md_files:
        # Определить кодировку
        encoding = detect_encoding(file_path)
        
        if encoding is None:
            print(f"  ⚠️  {file_path.name} — не удалось определить кодировку")
            errors += 1
            continue
        
        # Если уже UTF-8 — пропустить
        if encoding == 'utf-8':
            print(f"  ⏭️  {file_path.name} — уже в UTF-8")
            skipped += 1
            continue
        
        # Конвертировать
        relative_path = file_path.relative_to(PROJECT_ROOT)
        if convert_file(file_path, encoding):
            print(f"  ✅ {relative_path} — конвертирован ({encoding} → UTF-8)")
            converted += 1
        else:
            errors += 1
    
    print("\n" + "=" * 60)
    print("Результаты:")
    print(f"  ✅ Конвертировано: {converted}")
    print(f"  ⏭️  Пропущено (уже UTF-8): {skipped}")
    print(f"  ❌ Ошибок: {errors}")
    print("=" * 60)

if __name__ == "__main__":
    main()
