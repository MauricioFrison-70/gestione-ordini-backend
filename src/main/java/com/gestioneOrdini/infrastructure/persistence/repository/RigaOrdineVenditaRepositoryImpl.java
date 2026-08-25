package com.gestioneOrdini.infrastructure.persistence.repository;

import com.gestioneOrdini.domain.ordine.exception.ProdottoGiaPresenteNellOrdineException;
import com.gestioneOrdini.domain.ordine.model.RigaOrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.RigaOrdineVenditaRepository;
import com.gestioneOrdini.infrastructure.persistence.entity.OrdineVenditaEntity;
import com.gestioneOrdini.infrastructure.persistence.entity.ProdottoEntity;
import com.gestioneOrdini.infrastructure.persistence.entity.RigaOrdineVenditaEntity;
import com.gestioneOrdini.infrastructure.persistence.mapper.ordine.RigaOrdineVenditaMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class RigaOrdineVenditaRepositoryImpl implements RigaOrdineVenditaRepository {
    private final RigaOrdineVenditaJpaRepository jpa;
    private final OrdineVenditaJpaRepository ordineJpa;
    private final ProdottoJpaRepository prodottoJpa;
    private final RigaOrdineVenditaMapper mapper;

    public RigaOrdineVenditaRepositoryImpl(
            RigaOrdineVenditaJpaRepository jpa,
            OrdineVenditaJpaRepository ordineJpa,
            ProdottoJpaRepository prodottoJpa,
            RigaOrdineVenditaMapper mapper) {
        this.jpa = jpa;
        this.ordineJpa = ordineJpa;
        this.prodottoJpa = prodottoJpa;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public RigaOrdineVendita save(RigaOrdineVendita riga) {
        try {
            OrdineVenditaEntity ordine = ordineJpa.getReferenceById(riga.getOrdineVenditaId());
            ProdottoEntity prodotto = prodottoJpa.getReferenceById(riga.getProdotto().getId());
            RigaOrdineVenditaEntity salvata = jpa.saveAndFlush(
                    mapper.toEntity(riga, ordine, prodotto));
            return mapper.toDomain(salvata);
        } catch (DataIntegrityViolationException ex) {
            throw new ProdottoGiaPresenteNellOrdineException(
                    riga.getProdotto().getCodice(), riga.getOrdineVenditaId(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RigaOrdineVendita> findByIdAndOrdineVenditaId(
            Long id, Long ordineVenditaId) {
        return jpa.findByIdAndOrdineVenditaId(id, ordineVenditaId)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RigaOrdineVendita> findAllByOrdineVenditaId(Long ordineVenditaId) {
        return jpa.findAllByOrdineVenditaIdOrderById(ordineVenditaId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByOrdineVenditaIdAndProdottoId(
            Long ordineVenditaId, Long prodottoId) {
        return jpa.existsByOrdineVenditaIdAndProdottoId(ordineVenditaId, prodottoId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByOrdineVenditaIdAndProdottoIdAndIdNot(
            Long ordineVenditaId, Long prodottoId, Long idEscluso) {
        return jpa.existsByOrdineVenditaIdAndProdottoIdAndIdNot(
                ordineVenditaId, prodottoId, idEscluso);
    }

    @Override
    @Transactional
    public void deleteByIdAndOrdineVenditaId(Long id, Long ordineVenditaId) {
        jpa.deleteByIdAndOrdineVenditaId(id, ordineVenditaId);
        jpa.flush();
    }
}
