package com.gestioneOrdini.infrastructure.persistence.repository;

import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.infrastructure.persistence.entity.OrdineVenditaEntity;
import com.gestioneOrdini.infrastructure.persistence.mapper.ordine.OrdineVenditaMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class OrdineVenditaRepositoryImpl implements OrdineVenditaRepository {
    private final OrdineVenditaJpaRepository jpa;
    private final OrdineVenditaMapper mapper;

    public OrdineVenditaRepositoryImpl(OrdineVenditaJpaRepository jpa,
                                       OrdineVenditaMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public OrdineVendita save(OrdineVendita ordine) {
        OrdineVenditaEntity entity = jpa.saveAndFlush(mapper.toEntity(ordine));
        if (entity.getNumeroOrdine() == null) {
            int anno = entity.getDataRegistrazione().getYear();
            entity.setNumeroOrdine("OV-%d-%06d".formatted(anno, entity.getId()));
            entity = jpa.saveAndFlush(entity);
        }
        return mapper.toDomain(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrdineVendita> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdineVendita> findAll() {
        return jpa.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByAgenteId(Long agenteId) {
        return jpa.existsByClienteIdOrVenditoreIdOrTrasportatoreId(
                agenteId, agenteId, agenteId);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        jpa.deleteById(id);
        jpa.flush();
    }
}
