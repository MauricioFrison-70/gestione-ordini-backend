package com.gestioneOrdini.application.acquisto.usecase;

import com.gestioneOrdini.application.acquisto.dto.RigaOrdineAcquistoRequest;
import com.gestioneOrdini.domain.acquisto.exception.OrdineAcquistoNonModificabileException;
import com.gestioneOrdini.domain.acquisto.model.OrdineAcquisto;
import com.gestioneOrdini.domain.acquisto.model.RigaOrdineAcquisto;
import com.gestioneOrdini.domain.acquisto.repository.OrdineAcquistoRepository;
import com.gestioneOrdini.domain.acquisto.repository.RigaOrdineAcquistoRepository;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateRigaOrdineAcquistoUseCaseTest {
    @Mock private OrdineAcquistoRepository ordineRepository;
    @Mock private ProdottoRepository prodottoRepository;
    @Mock private RigaOrdineAcquistoRepository rigaRepository;

    private UpdateRigaOrdineAcquistoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateRigaOrdineAcquistoUseCase(
                ordineRepository, prodottoRepository, rigaRepository);
    }

    @Test
    void dovrebbeAggiornareUnItemDiUnOrdinePendente() {
        RigaOrdineAcquisto riga = new RigaOrdineAcquisto(
                30L, 10L, prodotto(20L, "P001"),
                1, new BigDecimal("2.50"));
        Prodotto nuovoProdotto = prodotto(21L, "P002");
        when(ordineRepository.findById(10L))
                .thenReturn(Optional.of(ordinePendente()));
        when(rigaRepository.findByIdAndOrdineAcquistoId(30L, 10L))
                .thenReturn(Optional.of(riga));
        when(prodottoRepository.findByCodice("P002"))
                .thenReturn(Optional.of(nuovoProdotto));
        when(rigaRepository
                .existsByOrdineAcquistoIdAndProdottoIdAndIdNot(
                        10L, 21L, 30L))
                .thenReturn(false);
        when(rigaRepository.save(riga)).thenReturn(riga);

        RigaOrdineAcquisto aggiornata = useCase.eseguire(
                10L,
                30L,
                new RigaOrdineAcquistoRequest(
                        "P002", 4, new BigDecimal("3.75")));

        assertThat(aggiornata.getProdotto().getCodice()).isEqualTo("P002");
        assertThat(aggiornata.getQuantita()).isEqualTo(4);
        assertThat(aggiornata.getValoreUnitario())
                .isEqualByComparingTo("3.75");
        verify(rigaRepository).save(riga);
    }

    @Test
    void nonDovrebbeAggiornareUnItemDiUnOrdineRicevuto() {
        when(ordineRepository.findById(10L))
                .thenReturn(Optional.of(ordineRicevuto()));

        assertThatThrownBy(() -> useCase.eseguire(
                10L,
                30L,
                new RigaOrdineAcquistoRequest(
                        "P001", 4, new BigDecimal("3.75"))))
                .isInstanceOf(OrdineAcquistoNonModificabileException.class);

        verify(rigaRepository, never())
                .findByIdAndOrdineAcquistoId(30L, 10L);
    }

    private OrdineAcquisto ordinePendente() {
        return ordine(null);
    }

    private OrdineAcquisto ordineRicevuto() {
        return ordine(LocalDate.of(2026, 8, 25));
    }

    private OrdineAcquisto ordine(LocalDate dataRicevimento) {
        return new OrdineAcquisto(
                10L,
                "OA-2026-000010",
                new Agente(
                        2L,
                        "Fornitore",
                        "fornitore@example.com",
                        TipoAgente.FORNITORE,
                        false),
                LocalDateTime.of(2026, 8, 25, 10, 0),
                dataRicevimento,
                null);
    }

    private Prodotto prodotto(Long id, String codice) {
        return new Prodotto(
                id,
                codice,
                "Prodotto " + codice,
                new BigDecimal("2.50"),
                new BigDecimal("5.00"),
                10,
                2,
                false);
    }
}
