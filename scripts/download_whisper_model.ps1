# Download Whisper tiny model for English
$modelUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.en.bin"
$outputPath = "../app/src/main/assets/whisper-tiny-en.tflite"

Write-Host "Downloading Whisper model..."
Invoke-WebRequest -Uri $modelUrl -OutFile $outputPath
Write-Host "Model downloaded successfully to $outputPath" 