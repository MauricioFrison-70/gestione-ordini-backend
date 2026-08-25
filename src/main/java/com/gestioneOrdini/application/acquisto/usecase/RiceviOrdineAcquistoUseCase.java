package com.gestioneOrdini.application.acquisto.usecase;

import com.gestioneOrdini.domain.acquisto.model.OrdineAcquisto;
import com.gestioneOrdini.domain.acquisto.model.RigaOrdineAcquisto;
import com.gestioneOrdini.domain.acquisto.repository.OrdineAcquistoRepository;
import com.gestioneOrdini.domain.acquisto.repository.RigaOrdineAcquistoRepository;
import com.gestioneOrdini.domain.prodotto.event.ScortaRipristinataEvent;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Comparator;

@Service
public class RiceviOrdineAcquistoUseCase {
    private final OrdineAcquistoRepository ordineRepository;
    private final RigaOrdineAcquistoRepository rigaRepository;
    private final ProdottoRepository prodottoRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RiceviOrdineAcquistoUseCase(
            OrdineAcquistoRepository ordineRepository,
            RigaOrdineAcquistoRepository rigaRepository,
            ProdottoRepository prodottoRepository,
            ApplicationEventPublisher eventPublisher) {
        this.ordineRepository = ordineRepository;
        this.rigaRepository = rigaRepository;
        this.prodottoRepository = prodottoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrdineAcquisto eseguire(Long id) {
        OrdineAcquisto ordine = ordineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Ordine di acquisto", id));
        ordine.verificaModificabile();

        List<RigaOrdineAcquisto> righe =
                rigaRepository.findAllByOrdineAcquistoId(id).stream()
                        .sorted(Comparator.comparing(riga -> riga.getProdotto().getId()))
                        .toList();
        if (righe.isEmpty()) {
            throw new IllegalArgumentException(
                    "Inserire almeno una riga prima di ricevere l'ordine di acquisto");
        }

        for (RigaOrdineAcquisto riga : righe) {
            Long prodottoId = riga.getProdotto().getId();
            Prodotto prodotto = prodottoRepository.findByIdForUpdate(prodottoId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Prodotto", prodottoId));
            int quantitaPrecedente = prodotto.getQuantita();
            prodotto.incrementaQuantita(riga.getQuantita());
            prodottoRepository.save(prodotto);

            if (quantitaPrecedente < prodotto.getScortaMinima()
                    && prodotto.getQuantita() >= prodotto.getScortaMinima()) {
                eventPublisher.publishEvent(new ScortaRipristinataEvent(
                        prodotto.getId(),
                        prodotto.getCodice(),
                        prodotto.getDescrizione(),
                        quantitaPrecedente,
                        prodotto.getQuantita(),
                        prodotto.getScortaMinima(),
                        ordine.getNumeroOrdine()));
            }
        }

        ordine.ricevi(LocalDate.now());
        return ordineRepository.save(ordine);
    }
}
