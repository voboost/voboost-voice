#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Исправление "двойной кодировки" в .md файлах
Когда UTF-8 байты были прочитаны как Windows-1251 и сохранены снова
"""

import os
from pathlib import Path

PROJECT_ROOT = Path(r"D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant")

def fix_double_encoding(content: str) -> str:
    """
    Исправить двойную кодировку:
    UTF-8 байты → прочитаны как CP1251 → сохранены как UTF-8
    """
    try:
        # Закодировать как UTF-8, чтобы получить оригинальные байты
        utf8_bytes = content.encode('utf-8')
        # Декодировать как Latin-1 (ISO-8859-1) чтобы получить байты
        latin1_bytes = content.encode('latin-1')
        # Теперь декодировать как UTF-8
        return latin1_bytes.decode('utf-8')
    except (UnicodeDecodeError, UnicodeEncodeError):
        return content

def fix_file(file_path: Path) -> bool:
    """Исправить файл с двойной кодировкой"""
    try:
        # Читать как UTF-8 (сейчас там двойная кодировка)
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Проверить есть ли проблема (кракозябры)
        if 'Р' in content and 'Ў' in content:
            # Исправить двойную кодировку
            fixed_content = fix_double_encoding(content)
            
            # Записать исправленное
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(fixed_content)
            
            return True
        return False
    except Exception as e:
        print(f"  ❌ Ошибка: {e}")
        return False

def main():
    print("=" * 60)
    print("Исправление двойной кодировки в .md файлах")
    print("=" * 60)
    print(f"Проект: {PROJECT_ROOT}\n")
    
    md_files = list(PROJECT_ROOT.rglob("*.md"))
    
    if not md_files:
        print("❌ .md файлы не найдены")
        return
    
    print(f"Найдено файлов: {len(md_files)}\n")
    
    fixed = 0
    errors = 0
    skipped = 0
    
    for file_path in md_files:
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # Проверить есть ли проблема
            if 'Р' in content and ('Ў' in content or 'Рѕ' in content):
                relative_path = file_path.relative_to(PROJECT_ROOT)
                if fix_file(file_path):
                    print(f"  ✅ {relative_path} — исправлено")
                    fixed += 1
                else:
                    print(f"  ⚠️  {file_path.name} — не удалось исправить")
                    errors += 1
            else:
                print(f"  ⏭️  {file_path.name} — OK")
                skipped += 1
        except Exception as e:
            print(f"  ❌ {file_path.name}: {e}")
            errors += 1
    
    print("\n" + "=" * 60)
    print("Результаты:")
    print(f"  ✅ Исправлено: {fixed}")
    print(f"  ⏭️  Пропущено (OK): {skipped}")
    print(f"  ❌ Ошибок: {errors}")
    print("=" * 60)

if __name__ == "__main__":
    main()
