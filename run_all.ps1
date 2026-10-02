# Campus Navigation Chatbot - PowerShell Launcher
Write-Host "=========================================================================" -ForegroundColor Cyan
Write-Host "   Campus Navigation Chatbot — Smart Campus Wayfinding System" -ForegroundColor Yellow
Write-Host "   Demonstrating AI, ADSA, OOPJ, and Python" -ForegroundColor Yellow
Write-Host "=========================================================================" -ForegroundColor Cyan

$scriptPath = $PSScriptRoot

Write-Host "`n[1/2] Launching Python Intent Classifier on http://localhost:5000..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$scriptPath\python-classifier'; python app.py"

Start-Sleep -Seconds 2

Write-Host "[2/2] Launching Java Spring Boot Backend on http://localhost:8080..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$scriptPath\backend-java'; mvn spring-boot:run"

Write-Host "`nServices are starting up!" -ForegroundColor Cyan
Write-Host "Python Health: http://localhost:5000/health"
Write-Host "Web UI:        http://localhost:8080"
Write-Host "`nOpening browser in 6 seconds..."
Start-Sleep -Seconds 6
Start-Process "http://localhost:8080"
