package com.gestioneOrdini.infrastructure.persistence.mapper.acquisto;

import com.gestioneOrdini.application.acquisto.dto.FornitoreRiferimentoResponse;
import com.gestioneOrdini.application.acquisto.dto.OrdineAcquistoResponse;
import com.gestioneOrdini.domain.acquisto.model.OrdineAcquisto;
import com.gestioneOrdini.infrastructure.persistence.entity.OrdineAcquistoEntity;
import com.gestioneOrdini.infrastructure.persistence.mapper.agente.AgenteMapper;
import org.springframework.stereotype.Component;

@Component
public class OrdineAcquistoMapper {
    private final AgenteMapper agenteMapper;

    public OrdineAcquistoMapper(AgenteMapper agenteMapper) {
        this.agenteMapper = agenteMapper;
    }

    public OrdineAcquistoEntity toEntity(OrdineAcquisto ordine) {
        return new OrdineAcquistoEntity(
                ordine.getId(), ordine.getNumeroOrdine(),
                agenteMapper.toEntity(ordine.getFornitore()),
                ordine.getDataRegistrazione(), ordine.getDataRicevimento(),
                ordine.getDataAnnullamento());
    }

    public OrdineAcquisto toDomain(OrdineAcquistoEntity entity) {
        return new OrdineAcquisto(
                entity.getId(), entity.getNumeroOrdine(),
                agenteMapper.toDomain(entity.getFornitore()),
                entity.getDataRegistrazione(), entity.getDataRicevimento(),
                entity.getDataAnnullamento());
    }

    public OrdineAcquistoResponse toResponse(OrdineAcquisto ordine) {
        return new OrdineAcquistoResponse(
                ordine.getId(), ordine.getNumeroOrdine(),
                new FornitoreRiferimentoResponse(
                        ordine.getFornitore().getId(), ordine.getFornitore().getNome()),
                ordine.getDataRegistrazione(), ordine.getDataRicevimento(),
                ordine.getDataAnnullamento());
    }
}
