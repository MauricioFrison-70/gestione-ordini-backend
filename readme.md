# Gestione ordini Service – Backend Java (Spring Boot 3.3)

## 📌 Panoramica
Il Gestione ordini Service è una piccola API backend sviluppata in Java 17 con Spring Boot 3.3, progettata per dimostrare competenze tecniche in architettura pulita, sviluppo di servizi REST e integrazione con database relazionali. Il progetto è pensato per essere semplice, chiaro e facilmente leggibile da recruiter e team tecnici.

## 🏛️ Architettura
com.projectJava
├── application          → Casi d’uso
├── domain               → Modelli e logica di dominio
├── infrastructure       → Configurazioni, JPA, handler
├── presentation         → Controller REST
├── repository           → Interfacce di accesso ai dati
└── service              → Servizi applicativi

## 📘 Documentazione
- Architettura del progetto
- Checklist per la creazione di nuovi moduli (DDD + Hexagonal):  
  [docs/checklists/nuovo-modulo-ddd.md](docs/checklists/nuovo-modulo-ddd.md)
- 
## 🚀 Tecnologie Utilizzate
- Java 17
- Spring Boot 3.3.4
- Spring Web
- Spring Data JPA
- Spring Validation
- Springdoc OpenAPI (Swagger UI)
- Spring Actuator
- SQL Server (mssql-jdbc)
- HikariCP
- Gmail API con OAuth 2.0 per le notifiche e-mail

## 📚 Documentazione API
Swagger UI disponibile su:
http://localhost:8081/swagger-ui.html

## ✉️ Notifiche e-mail con Gmail API
Il backend usa OAuth 2.0 e lo scope minimo `gmail.send`; non utilizza password
SMTP. Configurare le seguenti variabili di ambiente:

- `GMAIL_OAUTH_CLIENT_ID`
- `GMAIL_OAUTH_CLIENT_SECRET`
- `GMAIL_OAUTH_REFRESH_TOKEN`
- `GMAIL_SENDER_EMAIL`
- `EMAIL_RESPONSABILE_SCORTA`

Per ottenere il refresh token una sola volta, configurare temporaneamente
`GMAIL_OAUTH_SETUP_ENABLED=true`, avviare il backend e aprire:
`http://localhost:8081/api/setup/gmail/oauth/authorize`.

Al termine, salvare il refresh token come variabile di ambiente, impostare
`GMAIL_OAUTH_SETUP_ENABLED=false` e riavviare il backend.

## 🗄️ Configurazione del Database
spring.datasource.url=jdbc:sqlserver://<host>:<port>;databaseName=<db>
spring.datasource.username=<user>
spring.datasource.password=<password>
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

## ▶️ Avvio del Progetto
Prerequisiti:
- Java 17+
- Maven 3.8+
- SQL Server attivo

Comandi:
mvn clean install
mvn spring-boot:run

## 🔧 Miglioramenti Futuri
- Aggiunta di Flyway per migrazioni DB
- Introduzione di MapStruct
- Test di integrazione con Testcontainers
- Pipeline CI/CD (GitHub Actions)

## 👤 Autore
Mauricio  
Backend Developer – Java & Spring Boot  
Sirmione, Lombardia – Italia
