package com.gestioneOrdini.infrastructure.persistence.repository;

import com.gestioneOrdini.domain.model.Agente;
import com.gestioneOrdini.domain.repository.AgenteRepository;
import com.gestioneOrdini.infrastructure.persistence.entity.AgenteEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AgenteRepositoryImpl implements AgenteRepository {

    private final AgenteJpaRepository jpa;

    public AgenteRepositoryImpl(AgenteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Agente> findAll() {
        return jpa.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Agente> findById(Long id) {
        return jpa.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Agente salva(Agente agente) {
        AgenteEntity entity;

        if (agente.getId() != null) {
            // Atualização: carregar a entidade existente
            entity = jpa.findById(agente.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Agente non trovato"));
        } else {
            // Criação
            entity = new AgenteEntity();
        }

        // Atualizar campos
        entity.setNome(agente.getNome());
        entity.setEmail(agente.getEmail());
        entity.setTipoAgente(agente.getTipoAgente());

        AgenteEntity salvato = jpa.save(entity);
        return toDomain(salvato);
    }


    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    // Conversão domínio → entidade
    private AgenteEntity toEntity(Agente agente) {
        AgenteEntity entity = new AgenteEntity();
        entity.setId(agente.getId());
        entity.setNome(agente.getNome());
        entity.setEmail(agente.getEmail());
        entity.setTipoAgente(agente.getTipoAgente());
        return entity;
    }

    // Conversão entidade → domínio
    private Agente toDomain(AgenteEntity entity) {
        return new Agente(
                entity.getId(),
                entity.getNome(),
                entity.getEmail(),
                entity.getTipoAgente()
        );
    }
}
