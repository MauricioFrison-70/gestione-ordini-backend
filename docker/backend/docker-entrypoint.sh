#!/bin/sh
set -eu

carica_segreto() {
    nome_variabile="$1"
    percorso_file="$2"

    if [ -s "$percorso_file" ]; then
        valore_segreto="$(cat "$percorso_file")"
        export "$nome_variabile=$valore_segreto"
    fi
}

carica_segreto DB_PASSWORD /run/secrets/db_app_password
carica_segreto REPORTING_DB_PASSWORD /run/secrets/db_reporting_password
carica_segreto AI_DB_PASSWORD /run/secrets/ai_db_password
carica_segreto GROQ_API_KEY /run/secrets/groq_api_key
carica_segreto GMAIL_OAUTH_CLIENT_ID /run/secrets/gmail_oauth_client_id
carica_segreto GMAIL_OAUTH_CLIENT_SECRET /run/secrets/gmail_oauth_client_secret
carica_segreto GMAIL_OAUTH_REFRESH_TOKEN /run/secrets/gmail_oauth_refresh_token

exec java -jar /app/app.jar
