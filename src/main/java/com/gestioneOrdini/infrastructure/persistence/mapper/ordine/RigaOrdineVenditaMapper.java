package com.gestioneOrdini.infrastructure.persistence.mapper.ordine;

import com.gestioneOrdini.application.ordine.dto.RigaOrdineVenditaResponse;
import com.gestioneOrdini.domain.ordine.model.RigaOrdineVendita;
import com.gestioneOrdini.infrastructure.persistence.entity.OrdineVenditaEntity;
import com.gestioneOrdini.infrastructure.persistence.entity.ProdottoEntity;
import com.gestioneOrdini.infrastructure.persistence.entity.RigaOrdineVenditaEntity;
import com.gestioneOrdini.infrastructure.persistence.mapper.prodotto.ProdottoMapper;
import org.springframework.stereotype.Component;

@Component
public class RigaOrdineVenditaMapper {
    private final ProdottoMapper prodottoMapper;

    public RigaOrdineVenditaMapper(ProdottoMapper prodottoMapper) {
        this.prodottoMapper = prodottoMapper;
    }

    public RigaOrdineVenditaEntity toEntity(
            RigaOrdineVendita riga,
            OrdineVenditaEntity ordine,
            ProdottoEntity prodotto) {
        return new RigaOrdineVenditaEntity(
                riga.getId(), ordine, prodotto,
                riga.getQuantita(), riga.getValoreUnitario());
    }

    public RigaOrdineVendita toDomain(RigaOrdineVenditaEntity entity) {
        return new RigaOrdineVendita(
                entity.getId(),
                entity.getOrdineVendita().getId(),
                prodottoMapper.toDomain(entity.getProdotto()),
                entity.getQuantita(),
                entity.getValoreUnitario());
    }

    public RigaOrdineVenditaResponse toResponse(RigaOrdineVendita riga) {
        return new RigaOrdineVenditaResponse(
                riga.getId(),
                riga.getOrdineVenditaId(),
                riga.getProdotto().getCodice(),
                riga.getProdotto().getDescrizione(),
                riga.getQuantita(),
                riga.getValoreUnitario(),
                riga.getTotaleRiga());
    }
}
