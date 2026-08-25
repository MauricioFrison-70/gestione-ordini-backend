package com.gestioneOrdini.infrastructure.persistence.mapper.acquisto;

import com.gestioneOrdini.application.acquisto.dto.RigaOrdineAcquistoResponse;
import com.gestioneOrdini.domain.acquisto.model.RigaOrdineAcquisto;
import com.gestioneOrdini.infrastructure.persistence.entity.OrdineAcquistoEntity;
import com.gestioneOrdini.infrastructure.persistence.entity.ProdottoEntity;
import com.gestioneOrdini.infrastructure.persistence.entity.RigaOrdineAcquistoEntity;
import com.gestioneOrdini.infrastructure.persistence.mapper.prodotto.ProdottoMapper;
import org.springframework.stereotype.Component;

@Component
public class RigaOrdineAcquistoMapper {
    private final ProdottoMapper prodottoMapper;

    public RigaOrdineAcquistoMapper(ProdottoMapper prodottoMapper) {
        this.prodottoMapper = prodottoMapper;
    }

    public RigaOrdineAcquistoEntity toEntity(
            RigaOrdineAcquisto riga, OrdineAcquistoEntity ordine,
            ProdottoEntity prodotto) {
        return new RigaOrdineAcquistoEntity(
                riga.getId(), ordine, prodotto,
                riga.getQuantita(), riga.getValoreUnitario());
    }

    public RigaOrdineAcquisto toDomain(RigaOrdineAcquistoEntity entity) {
        return new RigaOrdineAcquisto(
                entity.getId(), entity.getOrdineAcquisto().getId(),
                prodottoMapper.toDomain(entity.getProdotto()),
                entity.getQuantita(), entity.getValoreUnitario());
    }

    public RigaOrdineAcquistoResponse toResponse(RigaOrdineAcquisto riga) {
        return new RigaOrdineAcquistoResponse(
                riga.getId(), riga.getOrdineAcquistoId(),
                riga.getProdotto().getCodice(), riga.getProdotto().getDescrizione(),
                riga.getQuantita(), riga.getValoreUnitario(), riga.getTotaleRiga());
    }
}
