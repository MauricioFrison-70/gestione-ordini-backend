# Motore dinamico dei rapporti

Il motore consente a un DBA di creare, modificare e pubblicare rapporti senza
modificare o ricompilare il backend o il frontend. Il database è l'unica fonte
di configurazione.

Per la visione generale del sistema consultare il
[README principale](../../readme.md); per le regole funzionali destinate anche
all'assistente IA consultare la
[base di conoscenza](../ai/base-conoscenza-sistema.md).

## Installazione

Eseguire nel database `ProjectJava`, nell'ordine indicato:

1. `src/main/resources/db/reporting/001_ordini_vendita_per_periodo.sql`
2. `src/main/resources/db/reporting/002_motore_rapporti_dinamici.sql`
3. `src/main/resources/db/reporting/003_ranking_venditori_per_periodo.sql`
4. `src/main/resources/db/reporting/004_vendite_ultimi_dodici_mesi.sql`

Il secondo script crea:

- `reporting.rapporti`: catalogo dei rapporti;
- `reporting.parametri_rapporti`: metadati dei filtri;
- `reporting.colonne_rapporti`: etichette, ordine, visibilità e formato;
- procedure amministrative per registrazione e sincronizzazione;
- il ruolo di esecuzione `report_executor_role`;
- il ruolo amministrativo `report_designer_role`;
- il rapporto iniziale degli ordini di vendita.

Il terzo script pubblica la classifica dei venditori per periodo. La classifica
somma il valore degli ordini non annullati di ciascun venditore, utilizza la
stessa posizione in caso di parità e ordina dal valore maggiore al minore.

Il quarto script pubblica le vendite degli ultimi dodici mesi. Il periodo viene
calcolato automaticamente dalla data corrente del server SQL: comprende il mese
corrente e gli undici mesi precedenti. I mesi senza vendite vengono visualizzati
con valore zero e gli ordini annullati non vengono conteggiati.

Gli eventuali oggetti precedenti `dbo.rapporti` e `dbo.parametri_rapporti` non
sono più utilizzati dal backend. Non è necessario eliminarli per utilizzare il
nuovo motore.

## Connessione del backend

Configurare una connessione dedicata:

| Variabile | Contenuto |
| --- | --- |
| `REPORTING_DB_URL` | `jdbc:sqlserver://localhost:1433;databaseName=ProjectJava;encrypt=false` |
| `REPORTING_DB_USERNAME` | `report_executor` |
| `REPORTING_DB_PASSWORD` | password del login limitato |
| `REPORTING_MAX_ROWS` | limite opzionale, predefinito `1000` |
| `REPORTING_QUERY_TIMEOUT_SECONDS` | timeout opzionale, predefinito `30` |

Il login applicativo deve appartenere soltanto a `report_executor_role`:

```sql
USE master;
GO
CREATE LOGIN report_executor
WITH PASSWORD = 'SOSTITUIRE_CON_PASSWORD_SICURA',
     CHECK_POLICY = ON,
     CHECK_EXPIRATION = ON;
GO

USE ProjectJava;
GO
CREATE USER report_executor FOR LOGIN report_executor;
ALTER ROLE report_executor_role ADD MEMBER report_executor;
GO
```

Se login o utente esistono già, eseguire soltanto l'istruzione `ALTER ROLE`.
Non assegnare `db_datareader`, `db_datawriter` o `db_owner`.

## Contratto di una stored procedure di rapporto

Una procedura pubblicabile deve:

- appartenere allo schema `reporting`;
- restituire un solo result set;
- utilizzare alias di colonna univoci;
- non avere parametri `OUTPUT`;
- non eseguire modifiche ai dati;
- iniziare con `SET NOCOUNT ON` all'interno della procedura.

Esempio:

```sql
CREATE OR ALTER PROCEDURE reporting.usp_report_clienti_per_nome
    @Nome NVARCHAR(60) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        id,
        name AS nome,
        email
    FROM dbo.agenti
    WHERE agent_type = N'CLIENTE'
      AND (@Nome IS NULL OR name LIKE N'%' + @Nome + N'%')
    ORDER BY name;
END;
GO
```

## Pubblicazione di un nuovo rapporto

### 1. Registrare il rapporto

```sql
EXEC reporting.usp_registra_rapporto
    @Codice = N'CLIENTI_PER_NOME',
    @Titolo = N'Clienti per nome',
    @Descrizione = N'Ricerca dei clienti per nome',
    @NomeProcedura = N'reporting.usp_report_clienti_per_nome',
    @Attivo = 1,
    @Ordine = 10;
```

La registrazione concede automaticamente al ruolo di esecuzione il permesso
sulla procedura registrata.

### 2. Sincronizzare i parametri

```sql
EXEC reporting.usp_sincronizza_parametri
    @CodiceRapporto = N'CLIENTI_PER_NOME';
```

La sincronizzazione consulta `sys.parameters` e registra automaticamente nome,
tipo SQL, posizione, lunghezza, precisione e scala. I parametri rimossi dalla
procedure vengono disattivati nel catalogo.

### 3. Configurare la presentazione

```sql
EXEC reporting.usp_configura_parametro
    @CodiceRapporto = N'CLIENTI_PER_NOME',
    @NomeParametro = N'Nome',
    @Etichetta = N'Nome del cliente',
    @TipoCampo = N'TESTO',
    @Obbligatorio = 0,
    @Ordine = 1,
    @ValorePredefinito = NULL,
    @ProceduraOpzioni = NULL;
```

Tipi di campo disponibili:

- `TESTO`
- `INTERO`
- `DECIMALE`
- `DATA`
- `BOOLEANO`
- `SELEZIONE`

### 4. Configurare le colonne da visualizzare

Soltanto le colonne registrate in `reporting.colonne_rapporti` vengono restituite
dalla API e mostrate nel rapporto. Questa lista consentita evita che una nuova
colonna aggiunta accidentalmente alla stored procedure venga esposta. Registrare
ogni colonna che deve essere visibile:

```sql
EXEC reporting.usp_configura_colonna
    @CodiceRapporto = N'CLIENTI_PER_NOME',
    @NomeColonna = N'nome',
    @Etichetta = N'Cliente',
    @Formato = NULL,
    @Ordine = 1,
    @Visibile = 1,
    @Totalizzare = 0;
```

Formati riconosciuti dalla schermata generica:

- `VALUTA`: importo in euro secondo il formato italiano;
- `DATA`: sola data;
- `DATA_ORA`: data e ora;
- `NULL`: formato automatico in base al tipo restituito da SQL Server.

Impostare `@Totalizzare = 1` per visualizzare una riga finale con la somma della
colonna. La totalizzazione è consentita soltanto per colonne SQL numeriche e
viene riportata anche nelle esportazioni Excel e PDF.

Il rapporto pubblicato compare automaticamente nella voce **Rapporti** del
menu dell'applicazione. I filtri e la tabella dei risultati vengono costruiti
dai metadati, quindi non è necessario creare una nuova pagina nel frontend.

## Parametri di selezione

Una procedura di opzioni non deve avere parametri e deve restituire esattamente
le colonne `valore` ed `etichetta`:

```sql
CREATE OR ALTER PROCEDURE reporting.usp_opzioni_clienti
AS
BEGIN
    SET NOCOUNT ON;

    SELECT id AS valore, name AS etichetta
    FROM dbo.agenti
    WHERE agent_type = N'CLIENTE' AND is_archived = 0
    ORDER BY name;
END;
GO
```

Associarla al parametro:

```sql
EXEC reporting.usp_configura_parametro
    @CodiceRapporto = N'MIO_RAPPORTO',
    @NomeParametro = N'ClienteId',
    @Etichetta = N'Cliente',
    @TipoCampo = N'SELEZIONE',
    @Obbligatorio = 0,
    @Ordine = 1,
    @ProceduraOpzioni = N'reporting.usp_opzioni_clienti';
```

## Modifica di un rapporto esistente

Dopo ogni modifica alla firma della stored procedure, eseguire:

```sql
EXEC reporting.usp_sincronizza_parametri
    @CodiceRapporto = N'CODICE_DEL_RAPPORTO';
```

Se il DBA dimentica la sincronizzazione, il backend blocca l'esecuzione con un
errore di configurazione invece di associare valori ai parametri sbagliati.

## API disponibili

```text
GET  /api/rapporti
GET  /api/rapporti/{id}
GET  /api/rapporti/{id}/parametri
GET  /api/rapporti/{id}/parametri/{nome}/opzioni
POST /api/rapporti/{id}/esegui
```

Esempio di esecuzione:

```json
{
  "parametri": {
    "DataInizio": "2026-08-01",
    "DataFine": "2026-08-31",
    "ClienteId": null,
    "Stato": "RILASCIATO"
  }
}
```

Nel rapporto `ORDINI_VENDITA_PER_PERIODO`, il parametro facoltativo `Stato`
accetta `NON_RILASCIATO`, `RILASCIATO` oppure `ANNULLATO`. Inviare `null` per
includere tutti gli stati.

## Sicurezza

Il browser invia soltanto l'identificativo del rapporto e i valori dei
parametri. Il nome della procedura viene sempre letto dal catalogo e deve
appartenere allo schema `reporting`.

Le stored procedure dei rapporti devono essere revisionate per garantire che non
contengano comandi di modifica. Per una garanzia fisica di sola lettura negli
ambienti critici, eseguire i rapporti su una replica o su un database dedicato
configurato come read-only.

## Utilizzo nella dashboard e nell'assistente IA

La dashboard consuma la stessa API generica della pagina Rapporti. Per produrre
un grafico utilizzabile, il risultato deve contenere almeno una colonna
descrittiva e una colonna numerica. Il frontend privilegia come valore una
colonna con formato `VALUTA`, poi una colonna totalizzabile e infine la prima
colonna SQL numerica. Vengono rappresentate al massimo le prime 12 categorie.

Un futuro assistente IA può usare i rapporti come strumenti autorizzati per
rispondere a domande sui dati correnti. Non deve ricevere né eseguire SQL libero:
deve selezionare un rapporto pubblicato, validarne i parametri ed eseguirlo
attraverso `/api/rapporti/{id}/esegui`.
