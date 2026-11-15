@echo off
REM Run Java HPC Lab with one command
powershell -NoProfile -ExecutionPolicy Bypass -Command "& '%~dp0run_java.ps1' %*"
