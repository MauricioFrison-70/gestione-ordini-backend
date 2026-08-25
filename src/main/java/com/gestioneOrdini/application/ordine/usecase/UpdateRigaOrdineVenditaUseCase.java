package com.gestioneOrdini.application.ordine.usecase;

import com.gestioneOrdini.application.ordine.dto.RigaOrdineVenditaRequest;
import com.gestioneOrdini.domain.ordine.exception.ProdottoArchiviatoNellOrdineException;
import com.gestioneOrdini.domain.ordine.exception.ProdottoGiaPresenteNellOrdineException;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.model.RigaOrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.ordine.repository.RigaOrdineVenditaRepository;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateRigaOrdineVenditaUseCase {
    private final OrdineVenditaRepository ordineRepository;
    private final ProdottoRepository prodottoRepository;
    private final RigaOrdineVenditaRepository rigaRepository;

    public UpdateRigaOrdineVenditaUseCase(
            OrdineVenditaRepository ordineRepository,
            ProdottoRepository prodottoRepository,
            RigaOrdineVenditaRepository rigaRepository) {
        this.ordineRepository = ordineRepository;
        this.prodottoRepository = prodottoRepository;
        this.rigaRepository = rigaRepository;
    }

    @Transactional
    public RigaOrdineVendita eseguire(
            Long ordineId, Long rigaId, RigaOrdineVenditaRequest request) {
        OrdineVendita ordine = ordineRepository.findById(ordineId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Ordine di vendita", ordineId));
        ordine.verificaModificabile();
        RigaOrdineVendita riga = rigaRepository
                .findByIdAndOrdineVenditaId(rigaId, ordineId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Riga ordine di vendita", rigaId));
        String codice = request.codiceProdotto().trim();
        Prodotto prodotto = prodottoRepository.findByCodice(codice)
                .orElseThrow(() -> new EntityNotFoundException("Prodotto", codice));
        if (Boolean.TRUE.equals(prodotto.getArchiviato())) {
            throw new ProdottoArchiviatoNellOrdineException(prodotto.getCodice());
        }
        if (rigaRepository.existsByOrdineVenditaIdAndProdottoIdAndIdNot(
                ordineId, prodotto.getId(), rigaId)) {
            throw new ProdottoGiaPresenteNellOrdineException(
                    prodotto.getCodice(), ordineId);
        }
        riga.aggiorna(prodotto, request.quantita(), request.valoreUnitario());
        return rigaRepository.save(riga);
    }
}
