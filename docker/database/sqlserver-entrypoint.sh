#!/bin/bash
set -euo pipefail

arquivo_senha=/run/secrets/mssql_sa_password

if [[ ! -s "$arquivo_senha" ]]; then
    echo "Segreto mssql_sa_password non trovato." >&2
    exit 1
fi

export MSSQL_SA_PASSWORD
MSSQL_SA_PASSWORD="$(<"$arquivo_senha")"

exec "$@"
