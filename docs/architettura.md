# Architettura del backend

## Obiettivo

Il backend separa regole di business, orchestrazione dei casi d'uso e dettagli
tecnici. L'organizzazione è ispirata a DDD e all'architettura esagonale, adattata
alle dimensioni dimostrative del progetto.

## Livelli

| Livello | Responsabilità | Dipendenze ammesse |
| --- | --- | --- |
| `domain` | Entità, invarianti, eventi, eccezioni e contratti dei repository. | Java e altri elementi del dominio. |
| `application` | DTO, porte e casi d'uso che coordinano il dominio. | Dominio e astrazioni applicative. |
| `infrastructure` | JPA, JDBC, SQL Server, Gmail API, configurazione ed elaborazione degli eventi. | Application, domain e framework. |
| `presentation` | Controller REST, validazione dell'input e risposte HTTP. | Application e mapper necessari. |
| `exception` | Conversione centralizzata delle eccezioni in errori HTTP coerenti. | Spring Web e eccezioni applicative. |

I contesti funzionali principali sono `agente`, `prodotto`, `ordine` (vendita),
`acquisto` e `reporting`.

## Flusso di una richiesta REST

```text
Frontend -> Controller -> Caso d'uso -> Dominio -> Repository (porta)
                                             -> Adapter JPA/JDBC -> SQL Server
```

Il controller valida il contratto HTTP. Il caso d'uso carica le entità
necessarie e definisce il confine transazionale. Il dominio applica le regole e
l'adapter converte i modelli per la persistenza.

## Ordini e giacenza

### Vendita

Il rilascio dell'ordine di vendita è un'unica transazione:

1. verifica che l'ordine sia pendente e contenga almeno una riga;
2. carica e blocca i prodotti in ordine deterministico;
3. controlla la disponibilità di tutti gli articoli prima di modificare dati;
4. se un articolo non è disponibile, annulla l'intera operazione;
5. decrementa tutte le giacenze e registra la data di rilascio;
6. pubblica un evento se una giacenza attraversa la soglia minima verso il basso.

### Acquisto

Il ricevimento dell'ordine di acquisto è un'unica transazione:

1. verifica che l'ordine sia pendente e contenga almeno una riga;
2. carica e blocca i prodotti;
3. incrementa le giacenze;
4. registra la data di ricevimento;
5. pubblica un evento se una giacenza prima sotto il minimo raggiunge o supera la
   soglia.

La quantità non può essere modificata dall'API di aggiornamento del prodotto.
La creazione di un prodotto inizializza la quantità a zero.

## Eventi e notifiche

Gli eventi di scorta vengono elaborati con
`@TransactionalEventListener(phase = AFTER_COMMIT)` e `@Async`. L'e-mail viene
quindi tentata soltanto dopo il commit della transazione e non prolunga il tempo
della risposta HTTP.

Un errore della Gmail API viene registrato nei log e non annulla un movimento di
magazzino già confermato. Questa scelta privilegia la consistenza operativa;
un'eventuale evoluzione può introdurre outbox e tentativi automatici.

## Rapporti dinamici

Il reporting utilizza una connessione separata e un catalogo nello schema
`reporting`. Il browser invia l'identificativo del rapporto e i valori dei
parametri, mentre il backend ricava dal catalogo la procedura autorizzata.

Le protezioni principali sono:

- procedure limitate allo schema `reporting`;
- utente SQL dedicato con solo `report_executor_role`;
- timeout e numero massimo di righe configurabili;
- parametri tipizzati e sincronizzati con `sys.parameters`;
- esposizione delle sole colonne registrate nella lista consentita.

Dettagli operativi: [reporting/README.md](reporting/README.md).

## Errori HTTP

`GlobalExceptionHandler` centralizza gli errori applicativi. Le regole devono
essere verificate nel backend anche quando il frontend disabilita o nasconde
un'azione: l'interfaccia migliora l'esperienza, ma non sostituisce la validazione
del server.

## Strategia di test

- test di dominio per invarianti e transizioni di stato;
- test unitari dei casi d'uso con dipendenze simulate;
- test web dei controller e dei contratti HTTP;
- test di mapper e servizi infrastrutturali;
- test di repository, SQL e API con SQL Server reale tramite Testcontainers;
- test manuale separato per l'invio e-mail reale.

I test di integrazione sono identificati con il tag `integration` e vengono
eseguiti dal profilo Maven omonimo.

## Regole di manutenzione

- una nuova regola deve risiedere nel dominio o nel caso d'uso appropriato, non
  soltanto nell'interfaccia;
- le operazioni che modificano ordine e giacenza devono mantenere un unico
  confine transazionale;
- un nuovo rapporto deve seguire il contratto del motore dinamico;
- nessun segreto deve essere scritto nel codice, nei test o nella documentazione;
- una modifica funzionale deve aggiornare
  [ai/base-conoscenza-sistema.md](ai/base-conoscenza-sistema.md).
