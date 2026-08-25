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
public class CreateRigaOrdineVenditaUseCase {
    private final OrdineVenditaRepository ordineRepository;
    private final ProdottoRepository prodottoRepository;
    private final RigaOrdineVenditaRepository rigaRepository;

    public CreateRigaOrdineVenditaUseCase(
            OrdineVenditaRepository ordineRepository,
            ProdottoRepository prodottoRepository,
            RigaOrdineVenditaRepository rigaRepository) {
        this.ordineRepository = ordineRepository;
        this.prodottoRepository = prodottoRepository;
        this.rigaRepository = rigaRepository;
    }

    @Transactional
    public RigaOrdineVendita eseguire(Long ordineId, RigaOrdineVenditaRequest request) {
        OrdineVendita ordine = cercaOrdine(ordineId);
        ordine.verificaModificabile();
        Prodotto prodotto = cercaProdotto(request.codiceProdotto());
        verificaProdottoAttivo(prodotto);
        if (rigaRepository.existsByOrdineVenditaIdAndProdottoId(
                ordineId, prodotto.getId())) {
            throw new ProdottoGiaPresenteNellOrdineException(
                    prodotto.getCodice(), ordineId);
        }
        return rigaRepository.save(new RigaOrdineVendita(
                ordineId, prodotto, request.quantita(), request.valoreUnitario()));
    }

    private OrdineVendita cercaOrdine(Long ordineId) {
        return ordineRepository.findById(ordineId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Ordine di vendita", ordineId));
    }

    private Prodotto cercaProdotto(String codice) {
        String codiceNormalizzato = codice.trim();
        return prodottoRepository.findByCodice(codiceNormalizzato)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Prodotto", codiceNormalizzato));
    }

    private void verificaProdottoAttivo(Prodotto prodotto) {
        if (Boolean.TRUE.equals(prodotto.getArchiviato())) {
            throw new ProdottoArchiviatoNellOrdineException(prodotto.getCodice());
        }
    }
}
