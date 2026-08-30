# Documentazione per l'assistente IA

## Obiettivo

Questa cartella prepara una fonte controllata per un assistente che risponda
soltanto a domande su Gestione Ordini. Il modello linguistico non viene
“addestrato soltanto sul sistema”: riceve invece istruzioni di ambito, una base
di conoscenza selezionata e strumenti backend autorizzati.

## Fonti autorizzate

1. [base-conoscenza-sistema.md](base-conoscenza-sistema.md), per terminologia e
   regole funzionali;
2. [../architettura.md](../architettura.md), per domande tecniche;
3. [../reporting/README.md](../reporting/README.md), per amministrazione dei
   rapporti;
4. catalogo runtime dei rapporti, per titoli, parametri e disponibilità
   correnti;
5. endpoint applicativi inseriti esplicitamente nella lista degli strumenti
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

## Preparazione del contesto

Per le dimensioni attuali, il backend può caricare i file Markdown UTF-8 e
dividerli per titolo. Se la documentazione crescerà, sarà possibile aggiungere
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
