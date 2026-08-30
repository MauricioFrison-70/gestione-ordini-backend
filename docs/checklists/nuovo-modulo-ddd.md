# Checklist per un nuovo modulo

Questa checklist mantiene coerente l'organizzazione a livelli del progetto. Le
cartelle vanno adattate al contesto funzionale, per esempio `agente`, `prodotto`,
`ordine`, `acquisto` o `reporting`.

## 1. Dominio

- creare il modello in `domain/<contesto>/model`;
- definire invarianti e transizioni di stato nel modello, quando pertinenti;
- creare eccezioni di dominio specifiche e con messaggi in italiano;
- creare eventi di dominio soltanto per effetti successivi al caso d'uso;
- definire l'interfaccia del repository in `domain/<contesto>/repository`;
- evitare dipendenze da JPA, HTTP o dettagli del database.

## 2. Application

- creare request e response DTO in `application/<contesto>/dto`;
- aggiungere Bean Validation ai contratti di input;
- creare un caso d'uso per ogni operazione applicativa;
- usare le interfacce di repository e le porte, non implementazioni tecniche;
- definire esplicitamente il confine `@Transactional` delle operazioni di
  scrittura;
- validare nel backend tutte le regole, anche se il frontend limita le azioni.

## 3. Infrastructure

- creare o aggiornare l'entità JPA;
- creare il mapper tra dominio, persistenza e DTO;
- aggiungere `JpaRepository` e adapter del repository di dominio;
- usare JDBC soltanto quando necessario, come nel motore dei rapporti;
- configurare servizi esterni attraverso proprietà e variabili di ambiente;
- non inserire credenziali nel codice o nei test.

## 4. Presentation

- creare il controller REST in `presentation`;
- utilizzare percorsi, status HTTP e header `Location` coerenti;
- delegare la regola al caso d'uso;
- aggiungere il trattamento dell'errore in `GlobalExceptionHandler`, se
  necessario;
- verificare il contratto nella Swagger UI.

## 5. Test

- testare invarianti e stati del dominio;
- testare il caso d'uso e i rollback logici;
- testare il controller e gli errori HTTP;
- testare mapper e adapter che contengono logica significativa;
- aggiungere test repository/API con Testcontainers quando la persistenza è
  coinvolta;
- verificare casi positivi, input non valido, entità inesistente e conflitti;
- evitare servizi esterni reali nella suite automatica.

## 6. Frontend e documentazione

- aggiungere tipi, servizio HTTP, pagina, rotta e voce di menu;
- visualizzare gli errori restituiti dal backend in modo comprensibile;
- aggiungere test del servizio e dei principali flussi utente;
- aggiornare i README e, se cambia una regola funzionale, la
  [base di conoscenza](../ai/base-conoscenza-sistema.md);
- non esporre segreti nelle variabili `VITE_`.

## Ordine suggerito

```text
Dominio -> porte/DTO -> casi d'uso -> persistenza -> controller
        -> frontend -> test integrati -> documentazione
```
