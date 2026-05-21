#!/usr/bin/env pwsh
# ============================================================================
#  Конвертация файлов документации в UTF-8
# ============================================================================

$projectRoot = "D:\Projects\Android\MM\6.11.1\export\VoboostVoiceAssistant"

Write-Host "=== Конвертация файлов в UTF-8 ===" -ForegroundColor Cyan
Write-Host "Проект: $projectRoot" -ForegroundColor Gray
Write-Host ""

# Найти все .md файлы
$mdFiles = Get-ChildItem -Path $projectRoot -Filter "*.md" -Recurse

Write-Host "Найдено файлов: $($mdFiles.Count)" -ForegroundColor Yellow
Write-Host ""

$converted = 0
$errors = 0

foreach ($file in $mdFiles) {
    try {
        # Прочитать содержимое (автоматически определяет кодировку)
        $content = Get-Content -Path $file.FullName -Raw
        
        # Сохранить в UTF-8 без BOM
        $utf8NoBom = New-Object System.Text.UTF8Encoding $false
        [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
        
        Write-Host "  [OK] $($file.Name)" -ForegroundColor Green
        $converted++
    }
    catch {
        Write-Host "  [ERROR] $($file.Name): $($_.Exception.Message)" -ForegroundColor Red
        $errors++
    }
}

Write-Host ""
Write-Host "=== Готово ===" -ForegroundColor Cyan
Write-Host "Конвертировано: $converted" -ForegroundColor Green
Write-Host "Ошибок: $errors" -ForegroundColor $(if ($errors -eq 0) { "Green" } else { "Red" })
Write-Host ""
