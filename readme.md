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

## 📚 Documentazione API
Swagger UI disponibile su:
http://localhost:8080/swagger-ui.html

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
