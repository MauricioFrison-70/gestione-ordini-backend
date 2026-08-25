package com.gestioneOrdini.application.ordine.usecase;

import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.ordine.exception.OrdineVenditaNonModificabileException;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.model.RigaOrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.ordine.repository.RigaOrdineVenditaRepository;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RigaOrdineVenditaQueryDeleteUseCasesTest {
    @Mock private OrdineVenditaRepository ordineRepository;
    @Mock private RigaOrdineVenditaRepository rigaRepository;
    private GetRigaOrdineVenditaUseCase getUseCase;
    private ListRigheOrdineVenditaUseCase listUseCase;
    private DeleteRigaOrdineVenditaUseCase deleteUseCase;

    @BeforeEach
    void setUp() {
        getUseCase = new GetRigaOrdineVenditaUseCase(rigaRepository);
        listUseCase = new ListRigheOrdineVenditaUseCase(ordineRepository, rigaRepository);
        deleteUseCase = new DeleteRigaOrdineVenditaUseCase(ordineRepository, rigaRepository);
    }

    @Test
    void dovrebbeCercareEdElencareLeRigheSoltantoNellOrdineIndicato() {
        RigaOrdineVendita riga = riga();
        when(rigaRepository.findByIdAndOrdineVenditaId(30L, 10L))
                .thenReturn(Optional.of(riga));
        when(ordineRepository.findById(10L)).thenReturn(Optional.of(ordine(null, null)));
        when(rigaRepository.findAllByOrdineVenditaId(10L)).thenReturn(List.of(riga));

        assertThat(getUseCase.eseguire(10L, 30L)).isSameAs(riga);
        assertThat(listUseCase.eseguire(10L)).containsExactly(riga);
    }

    @Test
    void dovrebbeEliminareRigaDaOrdineModificabile() {
        when(ordineRepository.findById(10L)).thenReturn(Optional.of(ordine(null, null)));
        when(rigaRepository.findByIdAndOrdineVenditaId(30L, 10L))
                .thenReturn(Optional.of(riga()));

        deleteUseCase.eseguire(10L, 30L);

        verify(rigaRepository).deleteByIdAndOrdineVenditaId(30L, 10L);
    }

    @Test
    void dovrebbeRifiutareEliminazioneRigaDaOrdineAnnullato() {
        when(ordineRepository.findById(10L)).thenReturn(Optional.of(
                ordine(null, LocalDate.of(2026, 8, 22))));

        assertThatThrownBy(() -> deleteUseCase.eseguire(10L, 30L))
                .isInstanceOf(OrdineVenditaNonModificabileException.class);
        verify(rigaRepository, never()).deleteByIdAndOrdineVenditaId(30L, 10L);
    }

    private RigaOrdineVendita riga() {
        return new RigaOrdineVendita(30L, 10L, prodotto(),
                2, new BigDecimal("10.00"));
    }

    private Prodotto prodotto() {
        return new Prodotto(20L, "P001", "Prodotto",
                new BigDecimal("5.00"), new BigDecimal("10.00"),
                10, 1, false);
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
}
