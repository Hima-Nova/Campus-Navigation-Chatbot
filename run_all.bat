@echo off
title Campus Navigation Chatbot Launcher
echo =========================================================================
echo    Campus Navigation Chatbot — Smart Campus Wayfinding System
echo    Demonstrating AI, ADSA, OOPJ, and Python
echo =========================================================================
echo.

echo [1/2] Starting Python Intent Classifier Microservice on Port 5000...
start "Python Intent Classifier" cmd /k "cd /d %~dp0python-classifier && python app.py"

timeout /t 2 /nobreak >nul

echo [2/2] Starting Java Spring Boot Backend & Web UI on Port 8080...
start "Java Spring Boot Backend" cmd /k "cd /d %~dp0backend-java && mvn spring-boot:run"

echo.
echo =========================================================================
echo    Services Launching!
echo    - Python Classifier: http://localhost:5000/health
echo    - Full Web Application: http://localhost:8080
echo =========================================================================
echo.
echo Opening Web Application in default browser in 6 seconds...
timeout /t 6 /nobreak >nul
start http://localhost:8080
