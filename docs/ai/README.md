# Documentazione per l'assistente IA

## Obiettivo

Questa cartella contiene la fonte controllata dell'assistente che risponde
soltanto a domande su Gestione Ordini. Il modello linguistico non viene
“addestrato soltanto sul sistema”: riceve invece istruzioni di ambito, una base
di conoscenza selezionata e strumenti backend autorizzati.

## Fonti autorizzate

La versione attuale carica direttamente:

1. [base-conoscenza-sistema.md](base-conoscenza-sistema.md), per terminologia e
   regole funzionali.

Le evoluzioni future potranno aggiungere esplicitamente:

1. [../architettura.md](../architettura.md), per domande tecniche;
2. [../reporting/README.md](../reporting/README.md), per amministrazione dei
   rapporti;
3. catalogo runtime dei rapporti, per titoli, parametri e disponibilità
   correnti;
4. endpoint applicativi inseriti esplicitamente nella lista degli strumenti
   consentiti.

README generici, log, file temporanei, codice compilato e credenziali non devono
essere indicizzati come conoscenza.

## Informazioni statiche e dinamiche

La documentazione risponde a domande statiche, per esempio “quando posso
rilasciare un ordine?”. Domande come “quanto abbiamo venduto questo mese?”
richiedono dati dinamici e devono essere soddisfatte eseguendo un rapporto
autorizzato con parametri validati.

Il modello non deve costruire SQL. Il backend deve scegliere l'operazione da una
lista consentita, eseguirla con un utente di sola lettura e restituire al modello
soltanto i dati necessari alla risposta.

Nella versione attuale l'assistente combina la base di conoscenza statica con
una fotografia corrente e controllata degli ordini di vendita. Il backend legge
riepiloghi e righe recenti esclusivamente dalla view autorizzata; il modello non
genera e non esegue SQL.

## Identità SQL di sola lettura

Lo script
`../../src/main/resources/db/ai/001_utente_lettura_ordini_vendita.sql` prepara
il login `gestione_ordini_ai`. L'utente appartiene soltanto al ruolo
`ai_ordini_vendita_reader_role`, che può eseguire `SELECT` sulla view
`reporting.vw_ordini_vendita` e non sulle tabelle `dbo`.

La password viene fornita tramite la variabile SQLCMD `AI_DB_PASSWORD`. La
connessione dedicata del backend usa:

- `AI_DB_URL`;
- `AI_DB_USERNAME`;
- `AI_DB_PASSWORD`.

Questa identità non deve essere riutilizzata dal datasource applicativo o dal
motore generico dei rapporti. La sua presenza non autorizza SQL prodotto dal
modello: tutte le consultazioni sono predefinite nel backend, limitate e consultano
soltanto la view autorizzata.

## Implementazione attuale

Il frontend invia domanda e una cronologia limitata a
`POST /api/assistente/domande`. Il backend carica la base autorizzata, consulta
il contesto corrente tramite il datasource IA e aggiunge entrambi alle istruzioni
di ambito. Solo allora chiama l'endpoint chat completions di Groq richiedendo una
risposta JSON strutturata con `inAmbito` e `risposta`.

Il contesto dinamico contiene:

- numero e valore totale degli ordini;
- totali per stato;
- classifiche per venditore e cliente;
- totali degli ultimi dodici mesi;
- gli ordini registrati più recentemente.

Le classifiche e le righe recenti sono limitate da
`AI_DB_MAX_ROWS_PER_SECTION` per evitare richieste troppo grandi al provider.

La classificazione è applicata nuovamente dal caso d'uso: quando `inAmbito` è
falso, il testo prodotto dal modello viene ignorato e viene restituita la frase
di rifiuto definita dall'applicazione. Il servizio rimane disabilitato finché
`AI_ENABLED=true` e `GROQ_API_KEY` non sono configurati.

## Preparazione del contesto

Per le dimensioni attuali, il backend carica l'intera base Markdown UTF-8 dal
classpath. Se la documentazione crescerà, sarà possibile dividerla per titolo e aggiungere
un indice vettoriale mantenendo:

- percorso del documento;
- titolo della sezione;
- versione o hash del contenuto;
- testo della sezione;
- data dell'ultimo aggiornamento applicativo.

La risposta dovrebbe conservare riferimenti alle sezioni utilizzate, così
l'utente può verificare l'informazione.

## Protezioni minime

- chiave del provider IA soltanto nel backend e nelle variabili di ambiente;
- limite di dimensione della domanda e della risposta;
- timeout e gestione degli errori del provider;
- istruzione esplicita di rifiuto per domande fuori ambito;
- nessun SQL libero e nessun nome di procedura ricevuto dal browser;
- strumenti di sola lettura per le domande informative;
- esclusione di credenziali, dati sensibili e stack trace dal contesto;
- log tecnici senza contenuto sensibile;
- test per ambito, rifiuto, prompt injection e indisponibilità del provider.

## Manutenzione

Ogni modifica a una regola, schermata, rapporto o limitazione deve valutare
l'aggiornamento della base di conoscenza. Prima di pubblicare una nuova versione:

1. confrontare la documentazione con dominio, casi d'uso e controller;
2. verificare i rapporti realmente attivi nel database;
3. rimuovere esempi con dati personali o credenziali;
4. eseguire le domande di controllo dell'assistente;
5. registrare la versione della base caricata dal backend.
