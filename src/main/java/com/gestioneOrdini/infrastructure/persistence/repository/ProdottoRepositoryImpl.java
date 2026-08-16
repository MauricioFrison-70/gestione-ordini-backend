package com.gestioneOrdini.infrastructure.persistence.repository;

import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import com.gestioneOrdini.infrastructure.persistence.entity.ProdottoEntity;
import com.gestioneOrdini.infrastructure.persistence.mapper.prodotto.ProdottoMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProdottoRepositoryImpl implements ProdottoRepository {

    private final ProdottoJpaRepository jpa;
    private final ProdottoMapper mapper;

    public ProdottoRepositoryImpl(ProdottoJpaRepository jpa, ProdottoMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public List<Prodotto> findAll() {
        return jpa.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Prodotto> findById(Long id) {
        return jpa.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Prodotto save(Prodotto prodotto) {
        ProdottoEntity entity = mapper.toEntity(prodotto);
        ProdottoEntity salvato = jpa.save(entity);
        return mapper.toDomain(salvato);
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpa.existsById(id);
    }
}
