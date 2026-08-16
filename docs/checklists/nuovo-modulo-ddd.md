📄 CHECKLIST — Questo documento definisce lo standard architetturale per la creazione di nuovi moduli nel progetto, seguendo DDD e Architettura Esagonale.

🧱 1. DOMAIN — Nucleo del business
1.1. Creare l’entità di dominio
domain/<contexto>/model/NovaEntidade.java

Includere:

attributi essenziali

invarianti

regole di business

metodi di comportamento

stati validi

1.2. Creare eventi di dominio (se presenti)
domain/<contexto>/event/NovaEntidadeCriadaEvent.java

1.3. Creare il repository di dominio (interfaccia)
domain/<contexto>/repository/NovaEntidadeRepository.java

Senza JPA.
Senza database.
Senza tecnologia.

1.4. Creare Domain Services (se necessario)
Solo se la logica non può essere contenuta nell’entità.

⚙️ 2. APPLICATION — Casi d’uso
2.1. Creare i DTO
application/<contexto>/dto/NovaEntidadeRequest.java  
application/<contexto>/dto/NovaEntidadeResponse.java

2.2. Creare i Use Case
application/<contexto>/usecase/CriarNovaEntidadeUseCase.java  
application/<contexto>/usecase/AtualizarNovaEntidadeUseCase.java  
application/<contexto>/usecase/BuscarNovaEntidadeUseCase.java

Il use case:

riceve il DTO

chiama il dominio

utilizza il repository (interfaccia)

restituisce il DTO

Senza JPA.
Senza database.

🗄️ 3. INFRASTRUCTURE — Adapters
3.1. Creare la Entity JPA
infrastructure/persistence/entity/NovaEntidadeEntity.java

3.2. Creare il Mapper
infrastructure/persistence/mapper/<contexto>/NovaEntidadeMapper.java

Converte:

Domain ↔ Entity

Domain ↔ DTO

3.3. Creare il JpaRepository
infrastructure/persistence/repository/NovaEntidadeJpaRepository.java

3.4. Creare il RepositoryImpl (adapter)
infrastructure/persistence/repository/NovaEntidadeRepositoryImpl.java

Implementa il repository di dominio usando:

JpaRepository

Mapper

🌐 4. PRESENTATION — API
4.1. Creare il Controller
presentation/<contexto>/NovaEntidadeController.java

Esso:

riceve le richieste

chiama il use case

restituisce il DTO

🎯 Riassunto dell’ordine corretto
Domain Model

Domain Event (se presente)

Domain Repository (interfaccia)

DTOs

Use Cases

Entity JPA

Mapper

JpaRepository

RepositoryImpl

Controller