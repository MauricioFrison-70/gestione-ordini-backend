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
    public List<AgenteEntity> findAll() {
        return jpa.findAll();
    }

    @Override
    public Optional<AgenteEntity> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public AgenteEntity salva(AgenteEntity agente) {
        AgenteEntity entity;

        if (agente.getId() != null) {
            // Atualização: carregar a entidade existente
            entity = jpa.findById(agente.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Agente non trovato"));
        } else {
            // Criação
            entity = new AgenteEntity();
        }

        // Atualizar campos da entidade com dados do domínio
        entity.setNome(agente.getNome());
        entity.setEmail(agente.getEmail());
        entity.setTipoAgente(agente.getTipoAgente());
        entity.setArchiviato(agente.getArchiviato());

        // Persistir e retornar a entidade
        return jpa.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

}
