# Set SDK root path
$sdkRoot = "$env:LOCALAPPDATA\Android\Sdk"
$toolsPath = "$sdkRoot\cmdline-tools"

Write-Host "Cleaning up old command-line tools..."

# Remove old tools directory
if (Test-Path "$toolsPath\tools") {
    Write-Host "Removing $toolsPath\tools..."
    Remove-Item -Path "$toolsPath\tools" -Force -Recurse
}

# Remove version-specific directory
if (Test-Path "$toolsPath\8.0") {
    Write-Host "Removing $toolsPath\8.0..."
    Remove-Item -Path "$toolsPath\8.0" -Force -Recurse
}

Write-Host "Cleanup complete!" 