#!/bin/bash
set -euo pipefail

sqlcmd=/opt/mssql-tools18/bin/sqlcmd
senha_sa="$(</run/secrets/mssql_sa_password)"
senha_ai="$(</run/secrets/ai_db_password)"

executa_sqlcmd() {
    SQLCMDPASSWORD="$senha_sa" "$sqlcmd" \
        -C -I -S database -U sa -d ProjectJava -b -l 30 "$@"
}

for script in /opt/gestione-ordini/scripts/reporting/*.sql; do
    echo "Esecuzione di $(basename "$script")..."
    executa_sqlcmd -i "$script"
done

executa_sqlcmd -i /opt/gestione-ordini/scripts/010_configura_utente_rapporti.sql
executa_sqlcmd \
    -v AI_DB_PASSWORD="$senha_ai" \
    -i /opt/gestione-ordini/scripts/ai/001_utente_lettura_ordini_vendita.sql
executa_sqlcmd -i /opt/gestione-ordini/scripts/020_dati_demo.sql

echo "Rapporti, permessi e dati dimostrativi inizializzati."
