@echo off
setlocal
cd /d "%~dp0"

where docker >nul 2>&1
if errorlevel 1 (
  echo Docker Desktop non e installato oppure il comando docker non e disponibile.
  echo Installare Docker Desktop e riprovare.
  goto errore
)

docker info >nul 2>&1
if errorlevel 1 (
  echo Docker Desktop non e in esecuzione.
  echo Avviarlo, attendere che sia pronto e riprovare.
  goto errore
)

echo Download delle immagini e avvio di Gestione Ordini...
docker compose up --detach --pull always --wait --wait-timeout 300
if errorlevel 1 goto errore

echo.
echo Gestione Ordini e pronto: http://localhost:5173
start "" "http://localhost:5173"
exit /b 0

:errore
echo.
echo Avvio non riuscito. Consultare LEGGIMI.txt per maggiori informazioni.
pause
exit /b 1
