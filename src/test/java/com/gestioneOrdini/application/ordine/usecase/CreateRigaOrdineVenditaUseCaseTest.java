package com.gestioneOrdini.application.ordine.usecase;

import com.gestioneOrdini.application.ordine.dto.RigaOrdineVenditaRequest;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.ordine.exception.OrdineVenditaNonModificabileException;
import com.gestioneOrdini.domain.ordine.exception.ProdottoArchiviatoNellOrdineException;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateRigaOrdineVenditaUseCaseTest {
    @Mock private OrdineVenditaRepository ordineRepository;
    @Mock private ProdottoRepository prodottoRepository;
    @Mock private RigaOrdineVenditaRepository rigaRepository;
    private CreateRigaOrdineVenditaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateRigaOrdineVenditaUseCase(
                ordineRepository, prodottoRepository, rigaRepository);
    }

    @Test
    void dovrebbeCreareRigaConProdottoEsistente() {
        OrdineVendita ordine = ordine(null, null);
        Prodotto prodotto = prodotto(false);
        when(ordineRepository.findById(10L)).thenReturn(Optional.of(ordine));
        when(prodottoRepository.findByCodice("P001")).thenReturn(Optional.of(prodotto));
        when(rigaRepository.existsByOrdineVenditaIdAndProdottoId(10L, 20L))
                .thenReturn(false);
        when(rigaRepository.save(any())).thenAnswer(invocation -> {
            RigaOrdineVendita riga = invocation.getArgument(0);
            return new RigaOrdineVendita(30L, riga.getOrdineVenditaId(),
                    riga.getProdotto(), riga.getQuantita(), riga.getValoreUnitario());
        });

        RigaOrdineVendita creata = useCase.eseguire(10L, richiesta());

        assertThat(creata.getId()).isEqualTo(30L);
        assertThat(creata.getProdotto().getCodice()).isEqualTo("P001");
        verify(rigaRepository).save(any(RigaOrdineVendita.class));
    }

    @Test
    void dovrebbeRifiutareProdottoDuplicato() {
        when(ordineRepository.findById(10L)).thenReturn(Optional.of(ordine(null, null)));
        when(prodottoRepository.findByCodice("P001")).thenReturn(Optional.of(prodotto(false)));
        when(rigaRepository.existsByOrdineVenditaIdAndProdottoId(10L, 20L))
                .thenReturn(true);

        assertThatThrownBy(() -> useCase.eseguire(10L, richiesta()))
                .isInstanceOf(ProdottoGiaPresenteNellOrdineException.class);
        verify(rigaRepository, never()).save(any());
    }

    @Test
    void dovrebbeRifiutareProdottoArchiviato() {
        when(ordineRepository.findById(10L)).thenReturn(Optional.of(ordine(null, null)));
        when(prodottoRepository.findByCodice("P001")).thenReturn(Optional.of(prodotto(true)));

        assertThatThrownBy(() -> useCase.eseguire(10L, richiesta()))
                .isInstanceOf(ProdottoArchiviatoNellOrdineException.class);
        verify(rigaRepository, never()).save(any());
    }

    @Test
    void dovrebbeRifiutareOrdineRilasciato() {
        when(ordineRepository.findById(10L)).thenReturn(Optional.of(
                ordine(LocalDate.of(2026, 8, 22), null)));

        assertThatThrownBy(() -> useCase.eseguire(10L, richiesta()))
                .isInstanceOf(OrdineVenditaNonModificabileException.class);
        verify(prodottoRepository, never()).findByCodice(any());
    }

    private RigaOrdineVenditaRequest richiesta() {
        return new RigaOrdineVenditaRequest(
                "P001", 2, new BigDecimal("10.20"));
    }

    private OrdineVendita ordine(LocalDate rilascio, LocalDate annullamento) {
        return new OrdineVendita(10L, "OV-2026-000010",
                agente(1L, TipoAgente.CLIENTE), agente(2L, TipoAgente.VENDITORE),
                agente(3L, TipoAgente.TRASPORTATORE), LocalDateTime.now(),
                rilascio, annullamento);
    }

    private Agente agente(Long id, TipoAgente tipo) {
        return new Agente(id, tipo.name(), tipo.name().toLowerCase() + "@example.com", tipo, false);
    }

    private Prodotto prodotto(boolean archiviato) {
        return new Prodotto(20L, "P001", "Prodotto",
                new BigDecimal("5.00"), new BigDecimal("10.20"),
                10, 1, archiviato);
    }
}
