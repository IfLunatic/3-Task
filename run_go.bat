@echo off
REM Run Go HPC Lab with one command
powershell -NoProfile -ExecutionPolicy Bypass -Command "& '%~dp0run_go.ps1' %*"
