# PowerShell скрипт для загрузки моделей Sherpa-ONNX

Write-Host "============================================"
Write-Host "Sherpa-ONNX Model Downloader (PowerShell)"
Write-Host "============================================"
Write-Host ""

$modelsDir = "sherpa-models"
if (!(Test-Path $modelsDir)) {
    New-Item -ItemType Directory -Path $modelsDir | Out-Null
}

# ASR Модель
Write-Host "Downloading Russian ASR model (Zipformer)..."
Write-Host "Model: sherpa-onnx-zipformer-ru-2024-09-18"
Write-Host "Size: ~300 MB"
Write-Host ""

$asrUrl = "https://huggingface.co/csukuangfj/sherpa-onnx-zipformer-ru-2024-09-18/resolve/main/sherpa-onnx-zipformer-ru-2024-09-18.tar.gz"
$asrFile = "$modelsDir/sherpa-onnx-zipformer-ru-2024-09-18.tar.gz"

try {
    Invoke-WebRequest -Uri $asrUrl -OutFile $asrFile -UseBasicParsing
    Write-Host "✓ ASR model downloaded successfully!"
    Write-Host ""
    Write-Host "Extracting..."
    
    # Распаковка
    tar -xzf $asrFile -C $modelsDir/
    
    Write-Host "✓ ASR model extracted to $modelsDir/sherpa-onnx-zipformer-ru-2024-09-18/"
}
catch {
    Write-Host "✗ Failed to download ASR model"
    Write-Host "Error: $($_.Exception.Message)"
    Write-Host ""
    Write-Host "Please download manually from:"
    Write-Host "https://huggingface.co/csukuangfj/sherpa-onnx-zipformer-ru-2024-09-18"
}

Write-Host ""
Write-Host "============================================"
Write-Host "Downloading Russian TTS model (VITS Piper)..."
Write-Host "Model: vits-piper-ru-ru-irina-low"
Write-Host "Size: ~50 MB"
Write-Host ""

$ttsUrl = "https://huggingface.co/csukuangfj/vits-piper-ru-ru-irina-low/resolve/main/ru_RU-irina-low.onnx"
$ttsFile = "$modelsDir/ru_RU-irina-low.onnx"

try {
    Invoke-WebRequest -Uri $ttsUrl -OutFile $ttsFile -UseBasicParsing
    Write-Host "✓ TTS model downloaded successfully!"
}
catch {
    Write-Host "✗ Failed to download TTS model"
    Write-Host "Error: $($_.Exception.Message)"
    Write-Host ""
    Write-Host "Please download manually from:"
    Write-Host "https://huggingface.co/csukuangfj/vits-piper-ru-ru-irina-low"
}

Write-Host ""
Write-Host "============================================"
Write-Host "Next steps:"
Write-Host "1. Copy ASR model to app/src/main/assets/sherpa/asr-ru-model/"
Write-Host "2. Copy TTS model to app/src/main/assets/sherpa/tts-ru-model/"
Write-Host ""
Write-Host "Or run: .\copy-sherpa-models.bat"
Write-Host "============================================"
Write-Host ""
