package com.gestioneOrdini.infrastructure.persistence.repository;

import com.gestioneOrdini.domain.acquisto.exception.ProdottoGiaPresenteNellOrdineAcquistoException;
import com.gestioneOrdini.domain.acquisto.model.RigaOrdineAcquisto;
import com.gestioneOrdini.domain.acquisto.repository.RigaOrdineAcquistoRepository;
import com.gestioneOrdini.infrastructure.persistence.entity.OrdineAcquistoEntity;
import com.gestioneOrdini.infrastructure.persistence.entity.ProdottoEntity;
import com.gestioneOrdini.infrastructure.persistence.entity.RigaOrdineAcquistoEntity;
import com.gestioneOrdini.infrastructure.persistence.mapper.acquisto.RigaOrdineAcquistoMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class RigaOrdineAcquistoRepositoryImpl
        implements RigaOrdineAcquistoRepository {
    private final RigaOrdineAcquistoJpaRepository jpa;
    private final OrdineAcquistoJpaRepository ordineJpa;
    private final ProdottoJpaRepository prodottoJpa;
    private final RigaOrdineAcquistoMapper mapper;

    public RigaOrdineAcquistoRepositoryImpl(
            RigaOrdineAcquistoJpaRepository jpa,
            OrdineAcquistoJpaRepository ordineJpa,
            ProdottoJpaRepository prodottoJpa,
            RigaOrdineAcquistoMapper mapper) {
        this.jpa = jpa;
        this.ordineJpa = ordineJpa;
        this.prodottoJpa = prodottoJpa;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public RigaOrdineAcquisto save(RigaOrdineAcquisto riga) {
        try {
            OrdineAcquistoEntity ordine = ordineJpa.getReferenceById(
                    riga.getOrdineAcquistoId());
            ProdottoEntity prodotto = prodottoJpa.getReferenceById(
                    riga.getProdotto().getId());
            RigaOrdineAcquistoEntity salvata = jpa.saveAndFlush(
                    mapper.toEntity(riga, ordine, prodotto));
            return mapper.toDomain(salvata);
        } catch (DataIntegrityViolationException ex) {
            throw new ProdottoGiaPresenteNellOrdineAcquistoException(
                    riga.getProdotto().getCodice(),
                    riga.getOrdineAcquistoId(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RigaOrdineAcquisto> findByIdAndOrdineAcquistoId(
            Long id, Long ordineId) {
        return jpa.findByIdAndOrdineAcquistoId(id, ordineId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RigaOrdineAcquisto> findAllByOrdineAcquistoId(Long ordineId) {
        return jpa.findAllByOrdineAcquistoIdOrderById(ordineId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByOrdineAcquistoIdAndProdottoId(
            Long ordineId, Long prodottoId) {
        return jpa.existsByOrdineAcquistoIdAndProdottoId(ordineId, prodottoId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByOrdineAcquistoIdAndProdottoIdAndIdNot(
            Long ordineId, Long prodottoId, Long idEscluso) {
        return jpa.existsByOrdineAcquistoIdAndProdottoIdAndIdNot(
                ordineId, prodottoId, idEscluso);
    }

    @Override
    @Transactional
    public void deleteByIdAndOrdineAcquistoId(Long id, Long ordineId) {
        jpa.deleteByIdAndOrdineAcquistoId(id, ordineId);
        jpa.flush();
    }
}
