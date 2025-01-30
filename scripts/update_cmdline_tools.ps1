# Set the download URL for the latest command-line tools
$toolsUrl = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
$downloadPath = "cmdline-tools.zip"
$sdkRoot = "$env:LOCALAPPDATA\Android\Sdk"
$toolsPath = "$sdkRoot\cmdline-tools"

Write-Host "Downloading latest command-line tools..."
Invoke-WebRequest -Uri $toolsUrl -OutFile $downloadPath

Write-Host "Creating tools directory..."
if (-not (Test-Path "$toolsPath")) {
    New-Item -ItemType Directory -Force -Path "$toolsPath"
}

Write-Host "Extracting tools..."
# First, remove any existing cmdline-tools installation
if (Test-Path "$toolsPath\latest") {
    Remove-Item -Path "$toolsPath\latest" -Force -Recurse
}

# Extract to a temp directory first
Expand-Archive -Path $downloadPath -DestinationPath "$toolsPath\temp" -Force

# Move the contents correctly preserving the directory structure
if (Test-Path "$toolsPath\temp\cmdline-tools") {
    if (-not (Test-Path "$toolsPath\latest")) {
        New-Item -ItemType Directory -Force -Path "$toolsPath\latest"
    }
    Move-Item -Path "$toolsPath\temp\cmdline-tools\*" -Destination "$toolsPath\latest" -Force
}

Write-Host "Cleaning up..."
Remove-Item -Path $downloadPath -Force
Remove-Item -Path "$toolsPath\temp" -Force -Recurse

Write-Host "Setting up environment..."
$env:PATH = "$toolsPath\latest\bin;$env:PATH"

Write-Host "Accepting licenses..."
echo "y" | cmd /c "$toolsPath\latest\bin\sdkmanager.bat" --licenses

Write-Host "Installing/Updating packages..."
cmd /c "$toolsPath\latest\bin\sdkmanager.bat" --update
cmd /c "$toolsPath\latest\bin\sdkmanager.bat" "platform-tools" "platforms;android-34" "build-tools;34.0.0" "cmdline-tools;latest"

Write-Host "Command-line tools installation complete!" 