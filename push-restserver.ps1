$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

Write-Host "RestServer source overlay" -ForegroundColor Cyan
$head = (git rev-parse HEAD).Trim()
Write-Host "Current HEAD: $head"

if ($head -ne '3606c2f52b74b4226d979a1936c294555870387a') {
    Write-Host "Note: HEAD differs from the original Leaf 1.21.11 tag. Continuing with current tree." -ForegroundColor Yellow
}

git add RESTSERVER.md build-restserver.ps1 gradle.properties settings.gradle.kts leaf-server .github/workflows/restserver-build.yml
if ($LASTEXITCODE -ne 0) { throw 'git add failed' }

git diff --cached --stat
if ((git diff --cached --name-only | Measure-Object).Count -eq 0) {
    Write-Host 'Nothing new to commit. Pushing current main...' -ForegroundColor Yellow
} else {
    git commit -m "RestServer 1.21.11 core alpha + CI"
    if ($LASTEXITCODE -ne 0) { throw 'git commit failed' }
}

git push origin main
if ($LASTEXITCODE -ne 0) { throw 'git push failed' }

Write-Host ''
Write-Host 'DONE - GitHub Actions build should start now.' -ForegroundColor Green
