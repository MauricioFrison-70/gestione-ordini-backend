package com.gestioneOrdini.application.ordine.usecase;

import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.ordine.exception.ScortaInsufficienteException;
import com.gestioneOrdini.domain.ordine.model.OrdineVendita;
import com.gestioneOrdini.domain.ordine.model.RigaOrdineVendita;
import com.gestioneOrdini.domain.ordine.repository.OrdineVenditaRepository;
import com.gestioneOrdini.domain.ordine.repository.RigaOrdineVenditaRepository;
import com.gestioneOrdini.domain.prodotto.event.ScortaSottoMinimoEvent;
import com.gestioneOrdini.domain.prodotto.model.Prodotto;
import com.gestioneOrdini.domain.prodotto.repository.ProdottoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RilasciaOrdineVenditaUseCaseTest {
    @Mock private OrdineVenditaRepository ordineRepository;
    @Mock private RigaOrdineVenditaRepository rigaRepository;
    @Mock private ProdottoRepository prodottoRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    private RilasciaOrdineVenditaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RilasciaOrdineVenditaUseCase(
                ordineRepository, rigaRepository, prodottoRepository, eventPublisher);
    }

    @Test
    void dovrebbeVerificareIlSaldoEDecrementareLaGiacenzaPrimaDelRilascio() {
        OrdineVendita ordine = ordine();
        Prodotto prodotto = prodotto(20L, "P001", 10, 5);
        prepara(ordine, List.of(riga(30L, prodotto, 4)));
        when(prodottoRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(prodotto));
        when(ordineRepository.save(ordine)).thenReturn(ordine);

        OrdineVendita risultato = useCase.eseguire(10L);

        assertThat(prodotto.getQuantita()).isEqualTo(6);
        assertThat(risultato.getDataRilascio()).isEqualTo(LocalDate.now());
        verify(prodottoRepository).save(prodotto);
        verify(ordineRepository).save(ordine);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void dovrebbeAnnullareTuttaLOperazioneQuandoUnProdottoNonHaSaldo() {
        OrdineVendita ordine = ordine();
        Prodotto sufficiente = prodotto(20L, "P001", 10, 2);
        Prodotto insufficiente = prodotto(21L, "P002", 1, 1);
        prepara(ordine, List.of(
                riga(30L, sufficiente, 2),
                riga(31L, insufficiente, 3)));
        when(prodottoRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(sufficiente));
        when(prodottoRepository.findByIdForUpdate(21L)).thenReturn(Optional.of(insufficiente));

        assertThatThrownBy(() -> useCase.eseguire(10L))
                .isInstanceOf(ScortaInsufficienteException.class)
                .hasMessageContaining("P002")
                .hasMessageContaining("disponibile: 1")
                .hasMessageContaining("richiesta: 3");

        assertThat(sufficiente.getQuantita()).isEqualTo(10);
        assertThat(insufficiente.getQuantita()).isEqualTo(1);
        assertThat(ordine.getDataRilascio()).isNull();
        verify(prodottoRepository, never()).save(any());
        verify(ordineRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void dovrebbePubblicareEventoQuandoLaGiacenzaScendeSottoLaScortaMinima() {
        OrdineVendita ordine = ordine();
        Prodotto prodotto = prodotto(20L, "P001", 6, 5);
        prepara(ordine, List.of(riga(30L, prodotto, 2)));
        when(prodottoRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(prodotto));
        when(ordineRepository.save(ordine)).thenReturn(ordine);

        useCase.eseguire(10L);

        ArgumentCaptor<ScortaSottoMinimoEvent> captor =
                ArgumentCaptor.forClass(ScortaSottoMinimoEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().codiceProdotto()).isEqualTo("P001");
        assertThat(captor.getValue().quantitaPrecedente()).isEqualTo(6);
        assertThat(captor.getValue().quantitaAttuale()).isEqualTo(4);
        assertThat(captor.getValue().scortaMinima()).isEqualTo(5);
        assertThat(captor.getValue().numeroOrdineVendita()).isEqualTo("OV-2026-000010");
    }

    @Test
    void dovrebbeRifiutareRilascioSenzaRighe() {
        OrdineVendita ordine = ordine();
        prepara(ordine, List.of());

        assertThatThrownBy(() -> useCase.eseguire(10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("almeno una riga");
        verifyNoInteractions(prodottoRepository, eventPublisher);
        verify(ordineRepository, never()).save(ordine);
    }

    private void prepara(OrdineVendita ordine, List<RigaOrdineVendita> righe) {
        when(ordineRepository.findById(10L)).thenReturn(Optional.of(ordine));
        when(rigaRepository.findAllByOrdineVenditaId(10L)).thenReturn(righe);
    }

    private RigaOrdineVendita riga(Long id, Prodotto prodotto, int quantita) {
        return new RigaOrdineVendita(
                id, 10L, prodotto, quantita, new BigDecimal("10.00"));
    }

    private Prodotto prodotto(Long id, String codice, int quantita, int scortaMinima) {
        return new Prodotto(id, codice, "Prodotto " + codice,
                new BigDecimal("5.00"), new BigDecimal("10.00"),
                quantita, scortaMinima, false);
    }

    private OrdineVendita ordine() {
        return new OrdineVendita(10L, "OV-2026-000010",
                agente(1L, TipoAgente.CLIENTE), agente(2L, TipoAgente.VENDITORE),
                agente(3L, TipoAgente.TRASPORTATORE), LocalDateTime.now(), null, null);
    }

    private Agente agente(Long id, TipoAgente tipo) {
        return new Agente(id, tipo.name(), tipo.name().toLowerCase() + "@example.com", tipo, false);
    }
}
