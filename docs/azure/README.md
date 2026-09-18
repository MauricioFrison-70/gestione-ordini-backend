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

## 8. Distribuzione automatica del backend

Il workflow `.github/workflows/pubblica-immagini.yml` esegue automaticamente:

1. tutti i test Maven configurati nel profilo `integration`;
2. la costruzione e pubblicazione delle immagini nel GHCR;
3. la distribuzione del backend nel Container App quando il commit entra in
   `main`;
4. il controllo di `/actuator/health` e il ripristino del traffico verso la
   revisione precedente in caso di errore.

La distribuzione usa la tag immutabile `sha-<commit>` e non `latest`. Il
Container App deve essere configurato in modalità di revisione **Multiple**.
Le variabili d'ambiente e i secrets già presenti nel Container App non vengono
sostituiti dal workflow.

### Autenticazione GitHub-Azure con OIDC

Creare in Microsoft Entra ID un'applicazione dedicata, con una credenziale
federata GitHub limitata a:

```text
Organizzazione: MauricioFrison-70
Organization ID: 243792191
Repository: gestione-ordini-backend
Repository ID: 1299334940
Tipo di entità: Branch
Branch: main
Audience: api://AzureADTokenExchange
```

I campi ID sono richiesti dalla nuova configurazione GitHub OIDC basata su
identificatori immutabili. Il portale genera automaticamente l'identificatore
del soggetto dopo la selezione della branch; non modificarlo manualmente.

Assegnare a questa identità il ruolo **Container Apps Contributor** sul solo
Container App `ca-gestione-ordini-api`. Se il ruolo non è disponibile nella
sottoscrizione, usare **Contributor** mantenendo lo stesso ambito ristretto.

In GitHub, aprire **Settings > Secrets and variables > Actions > Variables** e
creare le seguenti repository variables:

```text
AZURE_CLIENT_ID=<ID applicazione Entra>
AZURE_TENANT_ID=<ID directory tenant>
AZURE_SUBSCRIPTION_ID=<ID sottoscrizione Azure>
```

Non è necessario creare un client secret Azure. GitHub richiede un token OIDC
temporaneo a ogni esecuzione del workflow.

### Quando viene effettuata la distribuzione

- pull request verso `main`: esegue i test, ma non pubblica né distribuisce;
- merge/push su `main`: esegue i test, pubblica le immagini e distribuisce il
  backend in Azure;
- tag `v*`: esegue i test e pubblica le immagini versionate, senza modificare
  il Container App;
- esecuzione manuale sulla branch `main`: ripete pubblicazione e distribuzione.

Il frontend continua a essere distribuito dal workflow Azure Static Web Apps
presente nel repository frontend.
