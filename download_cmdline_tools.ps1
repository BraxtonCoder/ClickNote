$toolsUrl = "https://dl.google.com/android/repository/commandlinetools-win-9477386_latest.zip"
$downloadPath = "cmdline-tools.zip"
$sdkRoot = "$env:LOCALAPPDATA\Android\Sdk"
$toolsPath = "$sdkRoot\cmdline-tools"

Write-Host "Downloading latest command-line tools..."
Invoke-WebRequest -Uri $toolsUrl -OutFile $downloadPath

Write-Host "Creating tools directory..."
if (-not (Test-Path "$toolsPath\latest")) {
    New-Item -ItemType Directory -Force -Path "$toolsPath\latest"
}

Write-Host "Extracting tools..."
Expand-Archive -Path $downloadPath -DestinationPath "$toolsPath\temp" -Force
Move-Item -Path "$toolsPath\temp\cmdline-tools\*" -Destination "$toolsPath\latest" -Force

Write-Host "Cleaning up..."
Remove-Item -Path $downloadPath -Force
Remove-Item -Path "$toolsPath\temp" -Force -Recurse

Write-Host "Setting up environment..."
$env:PATH = "$toolsPath\latest\bin;$env:PATH"

Write-Host "Accepting licenses..."
echo "y" | cmd /c "$toolsPath\latest\bin\sdkmanager.bat" --licenses

Write-Host "Installing/Updating packages..."
cmd /c "$toolsPath\latest\bin\sdkmanager.bat" "platform-tools" "platforms;android-34" "build-tools;34.0.0"

Write-Host "Command-line tools installation complete!" 