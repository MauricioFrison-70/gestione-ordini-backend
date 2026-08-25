package com.gestioneOrdini.application.acquisto.usecase;

import com.gestioneOrdini.application.acquisto.dto.RigaOrdineAcquistoRequest;
import com.gestioneOrdini.domain.acquisto.exception.ProdottoArchiviatoNellOrdineAcquistoException;
import com.gestioneOrdini.domain.acquisto.exception.ProdottoGiaPresenteNellOrdineAcquistoException;
import com.gestioneOrdini.domain.acquisto.model.OrdineAcquisto;
import com.gestioneOrdini.domain.acquisto.model.RigaOrdineAcquisto;
import com.gestioneOrdini.domain.acquisto.repository.OrdineAcquistoRepository;
import com.gestioneOrdini.domain.acquisto.repository.RigaOrdineAcquistoRepository;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateRigaOrdineAcquistoUseCase {
    private final OrdineAcquistoRepository ordineRepository;
    private final ProdottoRepository prodottoRepository;
    private final RigaOrdineAcquistoRepository rigaRepository;

    public CreateRigaOrdineAcquistoUseCase(
            OrdineAcquistoRepository ordineRepository,
            ProdottoRepository prodottoRepository,
            RigaOrdineAcquistoRepository rigaRepository) {
        this.ordineRepository = ordineRepository;
        this.prodottoRepository = prodottoRepository;
        this.rigaRepository = rigaRepository;
    }

    @Transactional
    public RigaOrdineAcquisto eseguire(
            Long ordineId, RigaOrdineAcquistoRequest request) {
        OrdineAcquisto ordine = ordineRepository.findById(ordineId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Ordine di acquisto", ordineId));
        ordine.verificaModificabile();

        String codice = request.codiceProdotto().trim();
        Prodotto prodotto = prodottoRepository.findByCodice(codice)
                .orElseThrow(() -> new EntityNotFoundException("Prodotto", codice));
        if (Boolean.TRUE.equals(prodotto.getArchiviato())) {
            throw new ProdottoArchiviatoNellOrdineAcquistoException(codice);
        }
        if (rigaRepository.existsByOrdineAcquistoIdAndProdottoId(
                ordineId, prodotto.getId())) {
            throw new ProdottoGiaPresenteNellOrdineAcquistoException(
                    codice, ordineId);
        }
        return rigaRepository.save(new RigaOrdineAcquisto(
                ordineId, prodotto, request.quantita(), request.valoreUnitario()));
    }
}
