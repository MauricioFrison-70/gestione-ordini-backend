# Distribuzione su Microsoft Azure

Questa guida prepara il progetto per la seguente architettura:

```text
Azure Static Web Apps -> Azure Container Apps -> Azure SQL Database
```

La distribuzione locale con Docker Compose rimane invariata.

## 1. Stringa JDBC

Nel Container App configurare `DB_URL` con la stringa fornita da Azure SQL,
adattata al formato JDBC:

```text
jdbc:sqlserver://<server>.database.windows.net:1433;databaseName=<database>;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30
```

Non aggiungere nome utente o password alla URL. Usare secrets separati.

## 2. Creazione dell'utente applicativo

Collegarsi direttamente al database creato nel portale Azure usando il login
amministrativo. Non eseguire lo script nel database `master`.

In PowerShell, definire localmente una password nuova e forte:

```powershell
$env:APP_DB_PASSWORD = '<password-applicazione>'
sqlcmd -S '<server>.database.windows.net' -d '<database>' `
  -U '<amministratore-server>' -P '<password-amministratore>' -N `
  -v APP_DB_PASSWORD="$env:APP_DB_PASSWORD" `
  -i src/main/resources/db/azure/001_crea_utente_applicazione.sql
```

La password non deve essere scritta nei file del progetto né nei workflow.

## 3. Primo avvio del backend

Creare il Container App a partire dall'immagine pubblica:

```text
ghcr.io/mauriciofrison-70/gestione-ordini-backend:latest
```

Configurare come secrets:

```text
DB_PASSWORD
REPORTING_DB_PASSWORD
AI_DB_PASSWORD
GMAIL_OAUTH_CLIENT_SECRET
GMAIL_OAUTH_REFRESH_TOKEN
GROQ_API_KEY
```

Configurare come variabili d'ambiente:

```text
SPRING_PROFILES_ACTIVE=azure
DB_URL=<url-jdbc-azure-sql>
DB_USERNAME=gestione_ordini_app
DB_PASSWORD=<riferimento-al-secret>
CORS_ALLOWED_ORIGINS=https://<frontend>.azurestaticapps.net
```

Per la dimostrazione il profilo `azure` mantiene `ddl-auto=update`, così il
primo avvio crea le tabelle applicative. Attendere che
`/actuator/health` risponda con stato `UP` prima di proseguire.

## 4. Installazione dei rapporti

Collegarsi come amministratore direttamente al database applicativo ed eseguire
nell'ordine:

```text
src/main/resources/db/reporting/001_ordini_vendita_per_periodo.sql
src/main/resources/db/reporting/002_motore_rapporti_dinamici.sql
src/main/resources/db/reporting/003_ranking_venditori_per_periodo.sql
src/main/resources/db/reporting/004_vendite_ultimi_dodici_mesi.sql
```

Questi script non creano il database e possono essere eseguiti direttamente
su Azure SQL.

## 5. Utenti di sola lettura

Dopo l'installazione dei rapporti, eseguire:

```powershell
$env:REPORTING_DB_PASSWORD = '<password-reporting>'
$env:AI_DB_PASSWORD = '<password-ai>'
sqlcmd -S '<server>.database.windows.net' -d '<database>' `
  -U '<amministratore-server>' -P '<password-amministratore>' -N `
  -v REPORTING_DB_PASSWORD="$env:REPORTING_DB_PASSWORD" `
     AI_DB_PASSWORD="$env:AI_DB_PASSWORD" `
  -i src/main/resources/db/azure/002_crea_utenti_lettura.sql
```

Configurare quindi nel Container App:

```text
REPORTING_DB_URL=<stessa-url-jdbc>
REPORTING_DB_USERNAME=gestione_ordini_report
REPORTING_DB_PASSWORD=<riferimento-al-secret>

AI_ENABLED=true
AI_DB_ENABLED=true
AI_DB_URL=<stessa-url-jdbc>
AI_DB_USERNAME=gestione_ordini_ai
AI_DB_PASSWORD=<riferimento-al-secret>
```

`gestione_ordini_report` può leggere i metadati ed eseguire esclusivamente le
procedure autorizzate. `gestione_ordini_ai` può leggere soltanto
`reporting.vw_ordini_vendita`.

## 6. Frontend

In Azure Static Web Apps usare:

```text
app_location: /
output_location: dist
app_build_command: npm run build
```

Nel job di build definire:

```text
VITE_API_URL=https://<backend>.<regione>.azurecontainerapps.io/api
```

Il file `public/staticwebapp.config.json` del repository frontend mantiene le
rotte React funzionanti anche quando una pagina interna viene aperta o
aggiornata direttamente.

## 7. Sicurezza

- non usare il login amministrativo del server nell'applicazione;
- salvare credenziali soltanto nei secrets del Container App;
- configurare un minimo di repliche `0` e un massimo di `1` per la dimostrazione;
- restringere il firewall di Azure SQL dopo aver concluso la configurazione;
- mantenere `GMAIL_OAUTH_SETUP_ENABLED=false` durante l'uso normale;
- non esporre l'assistente IA pubblicamente senza autenticazione e limite di
  richieste.

In un ambiente di produzione, sostituire `ddl-auto=update` con migrazioni
versionate e `ddl-auto=validate`, rimuovendo quindi `db_ddladmin` dall'utente
applicativo.
