@echo off
setlocal
cd /d "%~dp0"

docker compose down
if errorlevel 1 (
  echo Arresto non riuscito. Verificare che Docker Desktop sia in esecuzione.
  pause
  exit /b 1
)

echo Gestione Ordini e stato arrestato. I dati dimostrativi sono stati conservati.
pause
