$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $Root

Write-Host ''
Write-Host '=== RestServer 1.21.11 source build ===' -ForegroundColor Cyan
Write-Host 'This builds the actual patched server core; it does not wrap a Paper/Leaf runtime jar.' -ForegroundColor DarkGray
Write-Host ''

if (-not (Test-Path '.git')) {
    throw 'This directory is not a Git checkout. Clone the RestServer bundle first.'
}

$head = (git rev-parse --short=12 HEAD).Trim()
Write-Host "Source commit: $head"

try {
    $javaText = (& java -version 2>&1 | Out-String)
    Write-Host $javaText.Trim()
} catch {
    throw 'Java was not found. Install a JDK compatible with this 1.21.11 source tree and reopen PowerShell.'
}

Write-Host ''
Write-Host '[1/2] Applying upstream + RestServer source patches...' -ForegroundColor Cyan
Write-Host '[2/2] Building runnable RestServer jar...' -ForegroundColor Cyan

& .\gradlew.bat --no-daemon applyAllPatches createMojmapPaperclipJar
if ($LASTEXITCODE -ne 0) {
    throw "Gradle build failed with exit code $LASTEXITCODE"
}

$libs = Join-Path $Root 'leaf-server\build\libs'
if (-not (Test-Path $libs)) {
    throw "Build completed but output directory was not found: $libs"
}

$candidates = Get-ChildItem $libs -File -Filter '*.jar' |
    Where-Object { $_.Name -match 'paperclip|mojmap|restserver' } |
    Sort-Object LastWriteTime -Descending

if (-not $candidates) {
    throw "No runnable jar found in $libs"
}

$sourceJar = $candidates[0].FullName
$finalJar = Join-Path $Root 'RestServer.jar'
Copy-Item -Force $sourceJar $finalJar

$hash = Get-FileHash -Algorithm SHA256 $finalJar
Write-Host ''
Write-Host 'BUILD COMPLETE' -ForegroundColor Green
Write-Host "Jar: $finalJar" -ForegroundColor Green
Write-Host "SHA256: $($hash.Hash)"
Write-Host ''
Write-Host 'Run:' -ForegroundColor Cyan
Write-Host 'java -Xms4G -Xmx4G -jar RestServer.jar nogui'
