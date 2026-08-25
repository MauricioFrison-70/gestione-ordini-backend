package com.gestioneOrdini.application.ordine.usecase;

import com.gestioneOrdini.domain.ordine.exception.ScortaInsufficienteException;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.model.RigaOrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.ordine.repository.RigaOrdineVenditaRepository;
import com.gestioneOrdini.domain.prodotto.event.ScortaSottoMinimoEvent;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import com.gestioneOrdini.domain.shared.EntityNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RilasciaOrdineVenditaUseCase {
    private final OrdineVenditaRepository ordineRepository;
    private final RigaOrdineVenditaRepository rigaRepository;
    private final ProdottoRepository prodottoRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RilasciaOrdineVenditaUseCase(
            OrdineVenditaRepository ordineRepository,
            RigaOrdineVenditaRepository rigaRepository,
            ProdottoRepository prodottoRepository,
            ApplicationEventPublisher eventPublisher) {
        this.ordineRepository = ordineRepository;
        this.rigaRepository = rigaRepository;
        this.prodottoRepository = prodottoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrdineVendita eseguire(Long id) {
        OrdineVendita ordine = ordineRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ordine di vendita", id));
        ordine.verificaModificabile();
        List<RigaOrdineVendita> righe = rigaRepository.findAllByOrdineVenditaId(id)
                .stream()
                .sorted(Comparator.comparing(riga -> riga.getProdotto().getId()))
                .toList();
        if (righe.isEmpty()) {
            throw new IllegalArgumentException(
                    "Inserire almeno una riga prima di rilasciare l'ordine di vendita");
        }

        List<MovimentoScorta> movimenti = new ArrayList<>();
        List<String> prodottiInsufficienti = new ArrayList<>();
        for (RigaOrdineVendita riga : righe) {
            Long prodottoId = riga.getProdotto().getId();
            Prodotto prodotto = prodottoRepository.findByIdForUpdate(prodottoId)
                    .orElseThrow(() -> new EntityNotFoundException("Prodotto", prodottoId));
            if (prodotto.getQuantita() < riga.getQuantita()) {
                prodottiInsufficienti.add(prodotto.getCodice()
                        + " (disponibile: " + prodotto.getQuantita()
                        + ", richiesta: " + riga.getQuantita() + ")");
            }
            movimenti.add(new MovimentoScorta(prodotto, riga.getQuantita()));
        }

        if (!prodottiInsufficienti.isEmpty()) {
            throw new ScortaInsufficienteException(
                    "Prodotti: " + String.join(", ", prodottiInsufficienti) + ".");
        }

        for (MovimentoScorta movimento : movimenti) {
            Prodotto prodotto = movimento.prodotto();
            int quantitaPrecedente = prodotto.getQuantita();
            prodotto.decrementaQuantita(movimento.quantita());
            prodottoRepository.save(prodotto);

            if (quantitaPrecedente >= prodotto.getScortaMinima()
                    && prodotto.getQuantita() < prodotto.getScortaMinima()) {
                eventPublisher.publishEvent(new ScortaSottoMinimoEvent(
                        prodotto.getId(),
                        prodotto.getCodice(),
                        prodotto.getDescrizione(),
                        quantitaPrecedente,
                        prodotto.getQuantita(),
                        prodotto.getScortaMinima(),
                        ordine.getNumeroOrdine()));
            }
        }

        ordine.rilascia(LocalDate.now());
        return ordineRepository.save(ordine);
    }

    private record MovimentoScorta(Prodotto prodotto, Integer quantita) {
    }
}
