#!/bin/bash
set -euo pipefail

sqlcmd=/opt/mssql-tools18/bin/sqlcmd
senha_sa="$(</run/secrets/mssql_sa_password)"
senha_app="$(</run/secrets/db_app_password)"
senha_reporting="$(</run/secrets/db_reporting_password)"

executa_sqlcmd() {
    SQLCMDPASSWORD="$senha_sa" "$sqlcmd" \
        -C -I -S database -U sa -b -l 30 "$@"
}

verifiche_consecutive=0
database_pronto=false

for tentativa in {1..60}; do
    if executa_sqlcmd -Q "SET NOCOUNT ON;
        IF DB_ID(N'ProjectJava') IS NOT NULL
           AND DATABASEPROPERTYEX(N'ProjectJava', N'Status') <> N'ONLINE'
            THROW 55003, N'ProjectJava non è ancora online.', 1;
        SELECT 1;" >/dev/null 2>&1; then
        verifiche_consecutive=$((verifiche_consecutive + 1))
        if [[ "$verifiche_consecutive" -ge 3 ]]; then
            database_pronto=true
            break
        fi
    else
        verifiche_consecutive=0
    fi

    sleep 2
done

if [[ "$database_pronto" != true ]]; then
    echo "SQL Server non è diventato disponibile in tempo." >&2
    exit 1
fi

executa_sqlcmd \
    -v APP_DB_PASSWORD="$senha_app" REPORTING_DB_PASSWORD="$senha_reporting" \
    -i /opt/gestione-ordini/scripts/000_crea_database_utenti.sql

echo "Database e utenti applicativi inizializzati."
