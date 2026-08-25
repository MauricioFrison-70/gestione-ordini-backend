package com.gestioneOrdini.infrastructure.persistence.mapper.ordine;

import com.gestioneOrdini.application.ordine.dto.AgenteRiferimentoResponse;
import com.gestioneOrdini.application.ordine.dto.OrdineVenditaResponse;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.infrastructure.persistence.entity.OrdineVenditaEntity;
import com.gestioneOrdini.infrastructure.persistence.mapper.agente.AgenteMapper;
import org.springframework.stereotype.Component;

@Component
public class OrdineVenditaMapper {
    private final AgenteMapper agenteMapper;

    public OrdineVenditaMapper(AgenteMapper agenteMapper) {
        this.agenteMapper = agenteMapper;
    }

    public OrdineVenditaEntity toEntity(OrdineVendita ordine) {
        return new OrdineVenditaEntity(
                ordine.getId(), ordine.getNumeroOrdine(),
                agenteMapper.toEntity(ordine.getCliente()),
                agenteMapper.toEntity(ordine.getVenditore()),
                agenteMapper.toEntity(ordine.getTrasportatore()),
                ordine.getDataRegistrazione(), ordine.getDataRilascio(),
                ordine.getDataAnnullamento());
    }

    public OrdineVendita toDomain(OrdineVenditaEntity entity) {
        return new OrdineVendita(
                entity.getId(), entity.getNumeroOrdine(),
                agenteMapper.toDomain(entity.getCliente()),
                agenteMapper.toDomain(entity.getVenditore()),
                agenteMapper.toDomain(entity.getTrasportatore()),
                entity.getDataRegistrazione(), entity.getDataRilascio(),
                entity.getDataAnnullamento());
    }

    public OrdineVenditaResponse toResponse(OrdineVendita ordine) {
        return new OrdineVenditaResponse(
                ordine.getId(), ordine.getNumeroOrdine(),
                riferimento(ordine.getCliente()), riferimento(ordine.getVenditore()),
                riferimento(ordine.getTrasportatore()),
                ordine.getDataRegistrazione(), ordine.getDataRilascio(),
                ordine.getDataAnnullamento());
    }

    private AgenteRiferimentoResponse riferimento(com.gestioneOrdini.domain.agente.model.Agente agente) {
        return new AgenteRiferimentoResponse(agente.getId(), agente.getNome());
    }
}
