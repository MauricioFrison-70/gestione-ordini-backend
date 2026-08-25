package com.gestioneOrdini.application.ordine.usecase;

import com.gestioneOrdini.application.ordine.dto.RigaOrdineVenditaRequest;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.ordine.exception.ProdottoGiaPresenteNellOrdineException;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.model.RigaOrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.ordine.repository.RigaOrdineVenditaRepository;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateRigaOrdineVenditaUseCaseTest {
    @Mock private OrdineVenditaRepository ordineRepository;
    @Mock private ProdottoRepository prodottoRepository;
    @Mock private RigaOrdineVenditaRepository rigaRepository;
    private UpdateRigaOrdineVenditaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateRigaOrdineVenditaUseCase(
                ordineRepository, prodottoRepository, rigaRepository);
    }

    @Test
    void dovrebbeAggiornareProdottoQuantitaEValore() {
        Prodotto originale = prodotto(20L, "P001");
        Prodotto sostitutivo = prodotto(21L, "P002");
        RigaOrdineVendita riga = new RigaOrdineVendita(
                30L, 10L, originale, 1, new BigDecimal("10.00"));
        when(ordineRepository.findById(10L)).thenReturn(Optional.of(ordine()));
        when(rigaRepository.findByIdAndOrdineVenditaId(30L, 10L))
                .thenReturn(Optional.of(riga));
        when(prodottoRepository.findByCodice("P002")).thenReturn(Optional.of(sostitutivo));
        when(rigaRepository.existsByOrdineVenditaIdAndProdottoIdAndIdNot(10L, 21L, 30L))
                .thenReturn(false);
        when(rigaRepository.save(riga)).thenReturn(riga);

        RigaOrdineVendita aggiornata = useCase.eseguire(
                10L, 30L,
                new RigaOrdineVenditaRequest(
                        "P002", 3, new BigDecimal("12.50")));

        assertThat(aggiornata.getProdotto().getCodice()).isEqualTo("P002");
        assertThat(aggiornata.getQuantita()).isEqualTo(3);
        assertThat(aggiornata.getValoreUnitario()).isEqualByComparingTo("12.50");
    }

    @Test
    void dovrebbeRifiutareProdottoGiaPresenteInAltraRiga() {
        RigaOrdineVendita riga = new RigaOrdineVendita(
                30L, 10L, prodotto(20L, "P001"),
                1, new BigDecimal("10.00"));
        when(ordineRepository.findById(10L)).thenReturn(Optional.of(ordine()));
        when(rigaRepository.findByIdAndOrdineVenditaId(30L, 10L))
                .thenReturn(Optional.of(riga));
        when(prodottoRepository.findByCodice("P002"))
                .thenReturn(Optional.of(prodotto(21L, "P002")));
        when(rigaRepository.existsByOrdineVenditaIdAndProdottoIdAndIdNot(10L, 21L, 30L))
                .thenReturn(true);

        assertThatThrownBy(() -> useCase.eseguire(
                10L, 30L,
                new RigaOrdineVenditaRequest(
                        "P002", 1, new BigDecimal("12.50"))))
                .isInstanceOf(ProdottoGiaPresenteNellOrdineException.class);
        verify(rigaRepository, never()).save(riga);
    }

    private OrdineVendita ordine() {
        return new OrdineVendita(10L, "OV-2026-000010",
                agente(1L, TipoAgente.CLIENTE), agente(2L, TipoAgente.VENDITORE),
                agente(3L, TipoAgente.TRASPORTATORE), LocalDateTime.now(), null, null);
    }

    private Agente agente(Long id, TipoAgente tipo) {
        return new Agente(id, tipo.name(), tipo.name().toLowerCase() + "@example.com", tipo, false);
    }

    private Prodotto prodotto(Long id, String codice) {
        return new Prodotto(id, codice, "Prodotto " + codice,
                new BigDecimal("5.00"), new BigDecimal("10.00"),
                10, 1, false);
    }
}
