$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$sdkmanager = "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat"

Write-Host "Updating Android SDK tools..."
& $sdkmanager --update

Write-Host "Accepting licenses..."
& $sdkmanager --licenses

Write-Host "Installing/Updating required packages..."
& $sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0" "cmdline-tools;latest"

Write-Host "SDK update complete!" 