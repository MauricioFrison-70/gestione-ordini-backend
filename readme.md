# Gestione Ordini — Backend

API REST per la gestione di agenti, prodotti, ordini di vendita, ordini di
acquisto, giacenze e rapporti dinamici. Il progetto è sviluppato in Java e
Spring Boot e dimostra l'applicazione di regole di dominio, transazioni,
integrazione con SQL Server, notifiche tramite Gmail API e test automatizzati.

L'interfaccia e i messaggi applicativi sono principalmente in italiano.

## Funzionalità

- gestione di clienti, venditori, trasportatori e fornitori come tipi di agente;
- gestione dei prodotti con prezzi, scorta minima, stato di archiviazione e
  giacenza controllata dal sistema;
- ordini di vendita con righe, rilascio, annullamento e verifica preventiva
  della disponibilità di tutti i prodotti;
- ordini di acquisto con righe, ricevimento merce e aggiornamento della giacenza;
- aggiornamento atomico della giacenza all'interno di transazioni di database;
- notifiche e-mail asincrone quando la scorta scende sotto il minimo o viene
  ripristinata;
- rapporti configurabili tramite metadati e stored procedure SQL Server;
- API generica utilizzata dalla pagina Rapporti e dalla dashboard del frontend;
- assistente IA dedicato alle domande sul sistema, con base di conoscenza
  controllata e rifiuto delle richieste fuori ambito;
- documentazione OpenAPI e Swagger UI.

I numeri degli ordini vengono generati dal sistema:

- ordine di vendita: `OV-AAAA-000001`;
- ordine di acquisto: `OA-AAAA-000001`.

## Tecnologie

- Java 17
- Spring Boot 3.3.4
- Spring Web, Validation e Actuator
- Spring Data JPA e Hibernate
- SQL Server e HikariCP
- Springdoc OpenAPI
- Gmail API con OAuth 2.0
- API Groq compatibile con OpenAI e risposte JSON strutturate
- JUnit 5, Mockito e Spring Boot Test
- Testcontainers per i test di integrazione con SQL Server

## Architettura

Il codice è organizzato per responsabilità e per contesto funzionale sotto il
package `com.gestioneOrdini`:

```text
application/       DTO, porte e casi d'uso
domain/            modelli, regole, eventi, eccezioni e repository di dominio
infrastructure/    persistenza JPA/JDBC, configurazioni, e-mail ed eventi
presentation/      controller REST
exception/         gestione uniforme degli errori HTTP
```

La descrizione completa dei flussi, delle transazioni e delle dipendenze è in
[docs/architettura.md](docs/architettura.md).

## Prerequisiti

- JDK 17 o versione successiva compatibile;
- SQL Server raggiungibile dall'applicazione;
- Docker in esecuzione soltanto per i test di integrazione con Testcontainers;
- credenziali OAuth Google soltanto se si desidera inviare notifiche reali.

Il Maven Wrapper incluso nel repository consente di eseguire il progetto senza
installare Maven separatamente.

## Configurazione

### Database applicativo

| Variabile | Obbligatoria | Descrizione |
| --- | --- | --- |
| `DB_USERNAME` | sì | Utente SQL Server dell'applicazione. |
| `DB_PASSWORD` | sì | Password dell'utente applicativo. |

Per lo sviluppo locale, URL e database sono definiti in
`src/main/resources/application.yaml`. In un ambiente diverso, sovrascrivere
le proprietà Spring tramite variabili di ambiente o un profilo dedicato.

> Non salvare password, token OAuth o file di credenziali nel repository.

### Connessione dedicata ai rapporti

| Variabile | Valore predefinito |
| --- | --- |
| `REPORTING_DB_URL` | URL del database applicativo |
| `REPORTING_DB_USERNAME` | `DB_USERNAME` |
| `REPORTING_DB_PASSWORD` | `DB_PASSWORD` |
| `REPORTING_DB_MAX_POOL_SIZE` | `3` |
| `REPORTING_DB_CONNECTION_TIMEOUT_MS` | `10000` |
| `REPORTING_MAX_ROWS` | `1000` |
| `REPORTING_QUERY_TIMEOUT_SECONDS` | `30` |

In un ambiente reale è raccomandato un utente dedicato con il solo ruolo
`report_executor_role`. L'installazione e la pubblicazione dei rapporti sono
descritte in [docs/reporting/README.md](docs/reporting/README.md).

### Notifiche Gmail con OAuth 2.0

| Variabile | Descrizione |
| --- | --- |
| `GMAIL_OAUTH_CLIENT_ID` | ID del client OAuth Google. |
| `GMAIL_OAUTH_CLIENT_SECRET` | Segreto del client OAuth Google. |
| `GMAIL_OAUTH_REFRESH_TOKEN` | Refresh token autorizzato con scope `gmail.send`. |
| `GMAIL_SENDER_EMAIL` | Indirizzo Gmail mittente. |
| `EMAIL_RESPONSABILE_SCORTA` | Destinatario degli avvisi di giacenza. |
| `GMAIL_OAUTH_REDIRECT_URI` | Facoltativa; predefinita `http://localhost:8081/login/oauth2/code/google`. |
| `GMAIL_OAUTH_SETUP_ENABLED` | Abilita temporaneamente il flusso iniziale; predefinita `false`. |

Per ottenere il refresh token la prima volta:

1. impostare temporaneamente `GMAIL_OAUTH_SETUP_ENABLED=true`;
2. avviare il backend;
3. aprire `http://localhost:8081/api/setup/gmail/oauth/authorize`;
4. completare il consenso Google e salvare il refresh token come variabile;
5. impostare `GMAIL_OAUTH_SETUP_ENABLED=false` e riavviare l'applicazione.

L'endpoint di configurazione deve rimanere disabilitato durante il normale
utilizzo dell'applicazione.

### Assistente IA

L'assistente è disabilitato per impostazione predefinita. Per abilitarlo:

| Variabile | Valore predefinito | Descrizione |
| --- | --- | --- |
| `AI_ENABLED` | `false` | Abilita le chiamate al provider IA. |
| `GROQ_API_KEY` | vuoto | Chiave privata usata soltanto dal backend. |
| `AI_MODEL` | `openai/gpt-oss-20b` | Modello configurato nel provider. |
| `AI_BASE_URL` | endpoint chat completions Groq | Endpoint compatibile con OpenAI. |
| `AI_TIMEOUT_SECONDS` | `30` | Timeout massimo della richiesta. |
| `AI_MAX_COMPLETION_TOKENS` | `600` | Limite della risposta generata. |
| `AI_DB_ENABLED` | valore di `AI_ENABLED` | Abilita il contesto dinamico degli ordini di vendita. |
| `AI_DB_URL` | vuoto | URL JDBC del database consultato dall'assistente. |
| `AI_DB_USERNAME` | vuoto | Login SQL dedicato `gestione_ordini_ai`. |
| `AI_DB_PASSWORD` | vuoto | Password del login SQL dedicato. |
| `AI_DB_MAX_ROWS_PER_SECTION` | `20` | Limite per classifiche ed elenco degli ordini recenti. |
| `AI_DB_QUERY_TIMEOUT_SECONDS` | `10` | Timeout delle consultazioni di sola lettura. |

La chiave non deve essere inserita nel frontend né salvata nel repository. La
base caricata dal backend è `docs/ai/base-conoscenza-sistema.md`. Le domande
fuori ambito, i tentativi di ottenere segreti e le richieste di SQL libero
vengono rifiutati.

Poiché il progetto non dispone ancora di autenticazione, non esporre l'endpoint
IA su Internet senza aggiungere autenticazione, autorizzazione e limitazione
delle richieste. Il CORS del browser non protegge direttamente l'API.

## Database dei rapporti

Eseguire una sola volta, nel database `ProjectJava`, gli script presenti in
`src/main/resources/db/reporting` nell'ordine numerico:

1. `001_ordini_vendita_per_periodo.sql`
2. `002_motore_rapporti_dinamici.sql`
3. `003_ranking_venditori_per_periodo.sql`
4. `004_vendite_ultimi_dodici_mesi.sql`

Gli script devono essere mantenuti insieme alle modifiche del catalogo dei
rapporti.

### Accesso dati dedicato all'assistente IA

Lo script
`src/main/resources/db/ai/001_utente_lettura_ordini_vendita.sql` crea il login
`gestione_ordini_ai` e un ruolo che concede `SELECT` esclusivamente sulla view
`reporting.vw_ordini_vendita`. Non assegna `db_datareader`, `db_datawriter` o
`db_owner` e nega la lettura dello schema `dbo` e l'esecuzione di procedure.

La password deve essere fornita come variabile SQLCMD e non deve essere salvata
nel repository:

```powershell
sqlcmd -S localhost -d master -U $env:DB_USERNAME -P $env:DB_PASSWORD `
  -v AI_DB_PASSWORD="$env:AI_DB_PASSWORD" `
  -i src/main/resources/db/ai/001_utente_lettura_ordini_vendita.sql
```

Quando `AI_DB_ENABLED=true`, il backend usa una fonte dati separata e di sola
lettura per costruire un contesto corrente con riepilogo generale, stato,
venditore, cliente, ultimi dodici mesi e ordini recenti. Le query sono definite
nel backend e consultano esclusivamente `reporting.vw_ordini_vendita`: il modello
non riceve credenziali e non può inviare SQL, nomi di tabelle o procedure.

## Avvio locale

Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux o macOS:

```bash
./mvnw spring-boot:run
```

Il backend viene esposto su `http://localhost:8081`.

## API e documentazione interattiva

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- specifica OpenAPI: `http://localhost:8081/v3/api-docs`
- stato dell'applicazione: `http://localhost:8081/actuator/health`

Risorse principali:

| Risorsa | Percorso base |
| --- | --- |
| Agenti | `/api/agenti` |
| Tipi di agente | `/api/tipo-agente` |
| Prodotti | `/api/prodotti` |
| Ordini di vendita | `/api/ordini-vendita` |
| Righe degli ordini di vendita | `/api/ordini-vendita/{ordineId}/righe` |
| Ordini di acquisto | `/api/ordini-acquisto` |
| Righe degli ordini di acquisto | `/api/ordini-acquisto/{ordineId}/righe` |
| Rapporti | `/api/rapporti` |
| Assistente IA | `/api/assistente/domande` |

## Test

Test unitari e web, senza i test di integrazione:

```powershell
.\mvnw.cmd test
```

Suite completa con Testcontainers e SQL Server:

```powershell
.\mvnw.cmd verify -Pintegration
```

Il test che invia un'e-mail reale è intenzionalmente disabilitato. Deve essere
eseguito soltanto in modo esplicito, con le variabili Gmail configurate e con
`RUN_REAL_EMAIL_TEST=true`.

## Documentazione del progetto

- [Architettura e flussi tecnici](docs/architettura.md)
- [Motore dinamico dei rapporti](docs/reporting/README.md)
- [Base di conoscenza funzionale per utenti e IA](docs/ai/base-conoscenza-sistema.md)
- [Guida di integrazione dell'assistente IA](docs/ai/README.md)
- [Checklist per un nuovo modulo](docs/checklists/nuovo-modulo-ddd.md)
- [Riferimenti tecnici](HELP.md)

Quando una regola funzionale viene modificata, aggiornare nello stesso commit la
base di conoscenza. In questo modo la documentazione e l'assistente del
sistema rimangono coerenti con il comportamento applicativo.

## Sicurezza

- nessuna credenziale deve essere inserita nel frontend o versionata in Git;
- il setup OAuth è disabilitato per impostazione predefinita;
- il motore dei rapporti accetta soltanto procedure registrate nello schema
  `reporting` e colonne esplicitamente autorizzate;
- il frontend locale è autorizzato dal CORS su `localhost:5173` e
  `127.0.0.1:5173`; gli ambienti distribuiti richiedono una configurazione
  specifica;
- l'autenticazione degli utenti applicativi non è ancora implementata.

## Autore

Mauricio Frison — Java Backend Developer, Sirmione (BS), Italia.
