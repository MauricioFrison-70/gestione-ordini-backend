#!/bin/sh
set -eu

directory_segreti=/secrets
mkdir -p "$directory_segreti"

genera_se_mancante() {
    nome_file="$1"
    percorso="$directory_segreti/$nome_file"

    if [ ! -s "$percorso" ]; then
        casuale="$(LC_ALL=C tr -dc 'A-Za-z0-9' </dev/urandom | head -c 28)"
        arquivo_temporaneo="$percorso.tmp"
        printf 'Aa1!%s' "$casuale" > "$arquivo_temporaneo"
        chmod 0444 "$arquivo_temporaneo"
        mv "$arquivo_temporaneo" "$percorso"
        printf 'Segreto %s generato.\n' "$nome_file"
    else
        printf 'Segreto %s già presente.\n' "$nome_file"
    fi
}

genera_se_mancante mssql_sa_password
genera_se_mancante db_app_password
genera_se_mancante db_reporting_password
genera_se_mancante ai_db_password
