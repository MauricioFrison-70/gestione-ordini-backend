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
`acquisto`, `reporting` e `assistente`.

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

## Assistente di intelligenza artificiale

L'assistente della dashboard risponde a domande sull'utilizzo, sulle regole e
sui dati correnti degli ordini di vendita. Non è un modello addestrato con i
dati dell'applicazione: il backend fornisce a ogni richiesta istruzioni di
ambito, la base di conoscenza autorizzata e un contesto dinamico controllato.

### Flusso della richiesta

```text
Frontend
   -> POST /api/assistente/domande
   -> AssistenteController
   -> RispondiDomandaAssistenteUseCase
      -> BaseConoscenzaAssistente (porta)
         -> ClasspathBaseConoscenzaAssistente
      -> ContestoDatiOrdiniVendita (porta)
         -> JdbcContestoDatiOrdiniVendita
            -> datasource gestione_ordini_ai (sola lettura)
               -> reporting.vw_ordini_vendita
      -> ModelloLinguisticoClient (porta)
         -> GroqModelloLinguisticoClient
            -> AiHttpTransport
               -> Groq Chat Completions API
```

Il contratto HTTP riceve una domanda e, facoltativamente, una cronologia breve.
La domanda e ogni messaggio sono limitati a 1.000 caratteri; la cronologia
accetta al massimo otto messaggi con ruolo `UTENTE` o `ASSISTENTE`.

Il caso d'uso:

1. carica la base `docs/ai/base-conoscenza-sistema.md`, inserita nel classpath
   dal processo Maven;
2. acquisisce dalla view autorizzata il riepilogo e le righe correnti;
3. prepara le istruzioni che delimitano il dominio consentito;
4. converte la cronologia nel formato neutro della porta applicativa;
5. richiede al modello una classificazione di ambito e una risposta;
6. ignora il testo generato quando `inAmbito` è falso e restituisce la frase di
   rifiuto definita dall'applicazione.

Questa verifica nel caso d'uso impedisce che il testo proposto dal provider per
una domanda classificata fuori ambito venga inoltrato al browser. Le richieste
di SQL libero, credenziali, prompt interni e argomenti estranei devono essere
rifiutate. Le regole di business continuano a essere applicate dal dominio e
dai casi d'uso normali: una risposta della IA non autorizza né esegue
un'operazione.

### Adapter Groq

`GroqModelloLinguisticoClient` usa `java.net.http.HttpClient` attraverso
`AiHttpTransport`, mantenendo il trasporto sostituibile nei test. La chiamata
utilizza l'endpoint chat completions compatibile con OpenAI e richiede una
risposta JSON strutturata con i campi obbligatori:

```json
{
  "inAmbito": true,
  "risposta": "Testo destinato all'utente"
}
```

Lo schema è configurato in modalità stretta e non accetta proprietà aggiuntive.
Il backend valida inoltre la presenza dei campi e rifiuta risposte vuote o non
conformi. Il modello, il timeout, il limite della risposta e l'endpoint sono
configurabili; il valore predefinito del modello è `openai/gpt-oss-20b`.

### Configurazione e sicurezza

L'integrazione è disabilitata per impostazione predefinita. Le proprietà sono
mappate da `AssistenteAiProperties` e provengono dalle seguenti variabili:

| Variabile | Funzione |
| --- | --- |
| `AI_ENABLED` | Abilita esplicitamente le chiamate al provider. |
| `GROQ_API_KEY` | Credenziale privata inviata soltanto dal backend. |
| `AI_MODEL` | Identificativo del modello. |
| `AI_BASE_URL` | Endpoint del provider compatibile. |
| `AI_TIMEOUT_SECONDS` | Timeout della richiesta HTTP. |
| `AI_MAX_COMPLETION_TOKENS` | Limite massimo della risposta. |
| `AI_DB_ENABLED` | Abilita il contesto dinamico; usa `AI_ENABLED` come valore predefinito. |
| `AI_DB_URL` | URL JDBC della connessione dedicata. |
| `AI_DB_USERNAME` | Login SQL di sola lettura. |
| `AI_DB_PASSWORD` | Password del login SQL dedicato. |
| `AI_DB_MAX_ROWS_PER_SECTION` | Limite di classifiche e ordini recenti. |
| `AI_DB_QUERY_TIMEOUT_SECONDS` | Timeout delle query controllate. |

La chiave non deve essere inviata al frontend, registrata nei log o versionata.
Domande e risposte transitano attraverso un servizio esterno e non devono
contenere credenziali o dati personali e commerciali non necessari.

Il progetto non dispone ancora di autenticazione applicativa. Di conseguenza,
l'endpoint IA non deve essere pubblicato su Internet senza autenticazione,
autorizzazione, limitazione delle richieste e monitoraggio dell'utilizzo. Il
CORS limita i browser autorizzati, ma non sostituisce questi controlli.

### Limiti attuali

- i dati dinamici sono limitati agli ordini di vendita esposti dalla view
  `reporting.vw_ordini_vendita`;
- sono disponibili riepilogo generale, stato, venditore, cliente, ultimi dodici
  mesi e ordini recenti; non sono eseguiti SQL liberi o operazioni di scrittura;
- classifiche e righe recenti sono troncate al limite configurato;
- la conversazione è mantenuta dal frontend soltanto durante la sessione della
  pagina e non viene persistita dal backend;
- le risposte possono contenere inesattezze e non sostituiscono le validazioni
  deterministiche dell'applicazione.

Lo script di provisioning crea l'identità SQL `gestione_ordini_ai`, limitata
alla lettura di `reporting.vw_ordini_vendita`. Il datasource dedicato è separato
da quello applicativo e dal motore dei rapporti. Le query sono costanti definite
nel backend: il modello non può scegliere nomi di stored procedure né produrre
SQL da eseguire.

## Errori HTTP

`GlobalExceptionHandler` centralizza gli errori applicativi. Le regole devono
essere verificate nel backend anche quando il frontend disabilita o nasconde
un'azione: l'interfaccia migliora l'esperienza, ma non sostituisce la validazione
del server.

Per l'assistente, una configurazione assente, un limite del provider o
un'indisponibilità temporanea producono HTTP `503`. Una risposta del provider
non valida produce HTTP `502`. In entrambi i casi il client riceve un messaggio
italiano sicuro, senza corpo della risposta esterna, stack trace o credenziali.

## Strategia di test

- test di dominio per invarianti e transizioni di stato;
- test unitari dei casi d'uso con dipendenze simulate;
- test web dei controller e dei contratti HTTP;
- test di mapper e servizi infrastrutturali;
- test del caso d'uso dell'assistente per risposta valida e rifiuto fuori
  ambito;
- test web del contratto e dei limiti della domanda e della cronologia;
- test dell'adapter Groq con trasporto simulato, senza effettuare chiamate reali
  né esporre la chiave nel corpo HTTP;
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
