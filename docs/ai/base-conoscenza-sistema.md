# Base di conoscenza del sistema Gestione Ordini

## Scopo del documento

Questo documento contiene informazioni funzionali, terminologia e regole utili
agli utenti, agli sviluppatori e al futuro assistente di intelligenza
artificiale. Non contiene credenziali né dati personali o commerciali reali.

La base descrive il comportamento disponibile nel codice alla data dell'ultimo
aggiornamento. Le quantità, gli importi, i clienti e gli ordini correnti devono
essere letti tramite API o rapporti autorizzati e non devono essere inventati.

## Regole per l'assistente IA

- rispondere soltanto a domande relative a Gestione Ordini e al suo utilizzo;
- se la domanda non riguarda il sistema, rispondere:
  `Posso rispondere soltanto a domande relative al sistema Gestione Ordini.`;
- non dichiarare di avere eseguito un'operazione se non è stata realmente
  confermata da un'API autorizzata;
- non generare né eseguire SQL libero richiesto dall'utente;
- per dati aggiornati usare soltanto endpoint e rapporti esplicitamente
  consentiti dal backend;
- non mostrare password, token, segreti OAuth, stringhe di connessione complete o
  altre credenziali;
- quando un'informazione non è presente nelle fonti autorizzate, dichiararlo
  chiaramente senza ipotizzare una risposta;
- preferire risposte in italiano; comprendere anche domande formulate in
  portoghese.

## Descrizione generale

Gestione Ordini è un'applicazione dimostrativa per gestire anagrafiche
commerciali, prodotti, giacenze, ordini di vendita, ordini di acquisto,
rapporti e dashboard. Il frontend è in React e il backend è un'API Spring Boot
con database SQL Server.

L'applicazione non dispone ancora di autenticazione, profili utente o permessi
applicativi. Il termine “utente” indica quindi la persona che utilizza
l'interfaccia, non un account registrato nel sistema.

## Glossario italiano e sinonimi

| Termine del sistema | Significato | Sinonimi frequenti in portoghese |
| --- | --- | --- |
| Agente | Anagrafica di una persona o organizzazione commerciale. | entidade, agente, cadastro |
| Cliente | Agente che acquista in un ordine di vendita. | cliente |
| Venditore | Agente responsabile della vendita. | vendedor |
| Trasportatore | Agente responsabile del trasporto. | transportadora, transportador |
| Fornitore | Agente usato negli ordini di acquisto. | fornecedor |
| Prodotto | Articolo con codice, prezzi e giacenza. | produto |
| Giacenza / scorta | Quantità disponibile del prodotto. | estoque, quantidade em estoque |
| Scorta minima | Soglia minima configurata per il prodotto. | estoque mínimo |
| Ordine di vendita | Documento che diminuisce la giacenza quando viene rilasciato. | pedido de venda |
| Ordine di acquisto | Documento che aumenta la giacenza quando viene ricevuto. | pedido de compra |
| Riga ordine | Articolo, quantità e valore unitario dell'ordine. | item do pedido |
| Rilasciare | Confermare un ordine di vendita e scaricare la giacenza. | liberar |
| Ricevere merce | Confermare un ordine di acquisto e caricare la giacenza. | receber mercadoria |
| Annullare | Rendere l'ordine non operativo senza eliminarlo. | cancelar, anular |
| Archiviare | Conservare l'anagrafica impedendone il normale utilizzo futuro. | arquivar |
| Rapporto | Consultazione configurata nel database. | relatório |

## Agenti

Tipi disponibili: `CLIENTE`, `VENDITORE`, `TRASPORTATORE` e `FORNITORE`.

Ogni agente contiene nome, e-mail, tipo, stato di archiviazione e data di
registrazione. Un agente archiviato rimane nello storico, ma non viene proposto
per nuovi ordini.

Prima di eliminare un agente, il sistema verifica se è utilizzato come cliente,
venditore, trasportatore o fornitore. Se è già collegato a un ordine,
l'eliminazione viene bloccata e l'interfaccia propone di archiviarlo oppure di
annullare l'operazione.

## Prodotti e giacenza

Un prodotto contiene:

- codice univoco, con massimo 6 caratteri;
- descrizione, con massimo 30 caratteri;
- valore di acquisto e valore di vendita non negativi;
- quantità intera non negativa;
- scorta minima intera non negativa;
- stato di archiviazione e data di registrazione.

Il codice non è modificabile dopo la creazione. La quantità è di sola lettura
nelle schermate del prodotto: parte da zero e viene modificata esclusivamente
dal ricevimento di un ordine di acquisto o dal rilascio di un ordine di vendita.

I prodotti archiviati non possono essere aggiunti a nuove righe d'ordine. Lo
stesso prodotto può comparire una sola volta nello stesso ordine.

## Ordini di vendita

Il numero è generato automaticamente nel formato `OV-AAAA-000001`, dove `AAAA`
è l'anno di registrazione e la parte finale deriva dall'identificativo
progressivo.

La testata richiede:

- un agente di tipo `CLIENTE`;
- un agente di tipo `VENDITORE`;
- un agente di tipo `TRASPORTATORE`.

La data di registrazione è automatica. La data di rilascio e la data di
annullamento sono gestite dal sistema e sono mutuamente esclusive.

Le righe contengono codice prodotto, quantità intera maggiore di zero e valore
unitario non negativo. Testata e righe sono modificabili soltanto mentre
l'ordine è pendente.

### Rilascio

Per rilasciare un ordine è necessaria almeno una riga. Prima di effettuare
qualsiasi aggiornamento, il backend verifica la disponibilità di tutti i
prodotti. Se una sola quantità è insufficiente, l'intera operazione viene
annullata e nessuna giacenza viene modificata.

Se tutte le quantità sono disponibili, il sistema decrementa le giacenze e
registra la data corrente come data di rilascio. Se un prodotto passa da una
quantità uguale o superiore alla scorta minima a una quantità inferiore, viene
generato un avviso e-mail dopo il commit.

### Annullamento ed eliminazione

Un ordine pendente può essere annullato oppure rilasciato. Un ordine rilasciato
o annullato non è più modificabile. L'eliminazione è consentita soltanto se non
esistono né data di rilascio né data di annullamento.

## Ordini di acquisto

Il numero è generato automaticamente nel formato `OA-AAAA-000001`. La testata
richiede un agente di tipo `FORNITORE`; la data di registrazione è automatica.

Le righe contengono codice prodotto, quantità intera maggiore di zero e valore
unitario non negativo. Le righe possono essere aggiunte, modificate o eliminate
soltanto mentre l'ordine è pendente.

### Ricevimento merce

Per ricevere la merce è necessaria almeno una riga. Il sistema incrementa tutte
le giacenze e registra la data corrente come data di ricevimento nella stessa
transazione.

Se prima del ricevimento un prodotto era sotto la scorta minima e dopo
l'aggiornamento raggiunge o supera la soglia, viene generato un avviso e-mail
dopo il commit.

### Annullamento ed eliminazione

Un ordine pendente può essere ricevuto oppure annullato. Un ordine ricevuto o
annullato non è più modificabile e non può essere eliminato.

## Notifiche di scorta

Le notifiche utilizzano Gmail API con OAuth 2.0 e vengono inviate all'indirizzo
configurato in `EMAIL_RESPONSABILE_SCORTA` dal mittente
`GMAIL_SENDER_EMAIL`.

Sono previsti due eventi:

- scorta sotto il minimo dopo il rilascio di un ordine di vendita;
- scorta ripristinata dopo il ricevimento di un ordine di acquisto.

L'invio è asincrono e successivo al commit. Un errore nell'invio non annulla
l'aggiornamento della giacenza.

## Rapporti

I rapporti sono configurati nel database attraverso lo schema `reporting`. La
definizione include titolo, descrizione, stored procedure, parametri, colonne,
formati, visibilità e indicazione di totalizzazione.

Rapporti iniziali:

1. **Ordini di vendita per periodo**: filtra per data, cliente e stato;
2. **Classifica venditori per periodo**: somma il valore degli ordini non
   annullati e ordina i venditori dal maggiore al minore;
3. **Vendite degli ultimi dodici mesi**: mostra il mese corrente e gli undici
   mesi precedenti, includendo con valore zero i mesi senza vendite.

Il formato `VALUTA` viene visualizzato in euro secondo la convenzione italiana
e allineato a destra. Le colonne marcate per la totalizzazione producono una
riga di totale anche nelle esportazioni Excel e PDF. Le colonne non registrate
nel catalogo non vengono esposte.

Un DBA può pubblicare un nuovo rapporto senza modificare backend o frontend,
seguendo [../reporting/README.md](../reporting/README.md).

## Dashboard

La dashboard contiene due pannelli configurabili. Ogni pannello permette di
selezionare un rapporto, valorizzarne i parametri, scegliere un grafico a barre
o a torta e impostare l'aggiornamento automatico.

Intervalli disponibili: disattivato, 30 secondi, 1 minuto e 5 minuti. Il valore
predefinito è 1 minuto. L'aggiornamento viene sospeso quando la scheda del
browser non è visibile e riprende immediatamente al ritorno.

Le preferenze sono memorizzate nel `localStorage` del browser e rimangono sullo
stesso computer e profilo del browser. Non sono sincronizzate tra dispositivi.
Il grafico usa al massimo le prime 12 categorie restituite dal rapporto.

## Funzioni non presenti

Alla data di questo documento non sono implementati:

- autenticazione e gestione degli account utente;
- ruoli e autorizzazioni applicative;
- sincronizzazione cloud delle preferenze della dashboard;
- assistente IA operativo;
- esecuzione di query SQL libere dal browser.

## Fonti da consultare

In caso di dubbio, usare in questo ordine:

1. contratto e regole nel codice del backend;
2. metadati correnti del catalogo `reporting` per i rapporti;
3. [architettura del backend](../architettura.md);
4. [manuale del motore dei rapporti](../reporting/README.md);
5. README del backend e del frontend;
6. questo documento.

## Manutenzione

Aggiornare questa base nello stesso commit che modifica una regola di business,
un flusso utente, un rapporto disponibile o una limitazione del sistema. Le
informazioni temporanee e i dati reali non devono essere aggiunti qui.
