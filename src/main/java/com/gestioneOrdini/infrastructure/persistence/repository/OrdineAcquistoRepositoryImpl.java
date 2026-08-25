package com.gestioneOrdini.infrastructure.persistence.repository;

import com.gestioneOrdini.domain.acquisto.model.OrdineAcquisto;
import com.gestioneOrdini.domain.acquisto.repository.OrdineAcquistoRepository;
import com.gestioneOrdini.infrastructure.persistence.entity.OrdineAcquistoEntity;
import com.gestioneOrdini.infrastructure.persistence.mapper.acquisto.OrdineAcquistoMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class OrdineAcquistoRepositoryImpl implements OrdineAcquistoRepository {
    private final OrdineAcquistoJpaRepository jpa;
    private final OrdineAcquistoMapper mapper;

    public OrdineAcquistoRepositoryImpl(
            OrdineAcquistoJpaRepository jpa, OrdineAcquistoMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public OrdineAcquisto save(OrdineAcquisto ordine) {
        OrdineAcquistoEntity entity = jpa.saveAndFlush(mapper.toEntity(ordine));
        if (entity.getNumeroOrdine() == null) {
            int anno = entity.getDataRegistrazione().getYear();
            entity.setNumeroOrdine("OA-%d-%06d".formatted(anno, entity.getId()));
            entity = jpa.saveAndFlush(entity);
        }
        return mapper.toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrdineAcquisto> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdineAcquisto> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByFornitoreId(Long fornitoreId) {
        return jpa.existsByFornitoreId(fornitoreId);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        jpa.deleteById(id);
        jpa.flush();
    }
}
