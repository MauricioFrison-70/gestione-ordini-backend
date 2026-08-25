package com.gestioneOrdini.application.acquisto.usecase;

import com.gestioneOrdini.domain.acquisto.model.OrdineAcquisto;
import com.gestioneOrdini.domain.acquisto.model.RigaOrdineAcquisto;
import com.gestioneOrdini.domain.acquisto.repository.OrdineAcquistoRepository;
import com.gestioneOrdini.domain.acquisto.repository.RigaOrdineAcquistoRepository;
import com.gestioneOrdini.domain.agente.model.Agente;
import com.gestioneOrdini.domain.agente.model.TipoAgente;
import com.gestioneOrdini.domain.prodotto.event.ScortaRipristinataEvent;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiceviOrdineAcquistoUseCaseTest {
    @Mock private OrdineAcquistoRepository ordineRepository;
    @Mock private RigaOrdineAcquistoRepository rigaRepository;
    @Mock private ProdottoRepository prodottoRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    private RiceviOrdineAcquistoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RiceviOrdineAcquistoUseCase(
                ordineRepository, rigaRepository, prodottoRepository,
                eventPublisher);
    }

    @Test
    void dovrebbeIncrementareLaGiacenzaERegistrareIlRicevimento() {
        OrdineAcquisto ordine = ordine();
        Prodotto prodotto = prodotto(10);
        RigaOrdineAcquisto riga = new RigaOrdineAcquisto(
                30L, 1L, prodotto, 5, new BigDecimal("2.50"));
        when(ordineRepository.findById(1L)).thenReturn(Optional.of(ordine));
        when(rigaRepository.findAllByOrdineAcquistoId(1L))
                .thenReturn(List.of(riga));
        when(prodottoRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(prodotto));
        when(prodottoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(ordineRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        OrdineAcquisto ricevuto = useCase.eseguire(1L);

        ArgumentCaptor<Prodotto> captor = ArgumentCaptor.forClass(Prodotto.class);
        verify(prodottoRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantita()).isEqualTo(15);
        assertThat(ricevuto.getDataRicevimento()).isNotNull();
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void dovrebbeNotificareQuandoLaGiacenzaRaggiungeLaScortaMinima() {
        OrdineAcquisto ordine = ordine();
        Prodotto prodotto = prodotto(1, 5);
        preparaRicevimento(ordine, prodotto, 4);

        useCase.eseguire(1L);

        ArgumentCaptor<ScortaRipristinataEvent> captor =
                ArgumentCaptor.forClass(ScortaRipristinataEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        ScortaRipristinataEvent evento = captor.getValue();
        assertThat(evento.prodottoId()).isEqualTo(20L);
        assertThat(evento.codiceProdotto()).isEqualTo("P001");
        assertThat(evento.quantitaPrecedente()).isEqualTo(1);
        assertThat(evento.quantitaAttuale()).isEqualTo(5);
        assertThat(evento.scortaMinima()).isEqualTo(5);
        assertThat(evento.numeroOrdineAcquisto()).isEqualTo("OA-2026-000001");
    }

    @Test
    void dovrebbeNotificareQuandoLaGiacenzaSuperaLaScortaMinima() {
        OrdineAcquisto ordine = ordine();
        Prodotto prodotto = prodotto(1, 5);
        preparaRicevimento(ordine, prodotto, 10);

        useCase.eseguire(1L);

        verify(eventPublisher).publishEvent(any(ScortaRipristinataEvent.class));
    }

    @Test
    void nonDovrebbeNotificareQuandoLaGiacenzaRimaneSottoLaScortaMinima() {
        OrdineAcquisto ordine = ordine();
        Prodotto prodotto = prodotto(1, 10);
        preparaRicevimento(ordine, prodotto, 4);

        useCase.eseguire(1L);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void nonDovrebbeNotificareQuandoLaGiacenzaEraGiaSufficiente() {
        OrdineAcquisto ordine = ordine();
        Prodotto prodotto = prodotto(5, 5);
        preparaRicevimento(ordine, prodotto, 2);

        useCase.eseguire(1L);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void dovrebbeRifiutareUnOrdineSenzaRighe() {
        when(ordineRepository.findById(1L)).thenReturn(Optional.of(ordine()));
        when(rigaRepository.findAllByOrdineAcquistoId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.eseguire(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("almeno una riga");
        verify(prodottoRepository, never()).save(any());
    }

    private OrdineAcquisto ordine() {
        Agente fornitore = new Agente(
                2L, "Fornitore", "fornitore@example.com",
                TipoAgente.FORNITORE, false);
        return new OrdineAcquisto(
                1L, "OA-2026-000001", fornitore,
                LocalDateTime.now(), null, null);
    }

    private Prodotto prodotto(int quantita) {
        return prodotto(quantita, 2);
    }

    private Prodotto prodotto(int quantita, int scortaMinima) {
        return new Prodotto(
                20L, "P001", "Prodotto", new BigDecimal("2.50"),
                new BigDecimal("4.00"), quantita, scortaMinima, false);
    }

    private void preparaRicevimento(
            OrdineAcquisto ordine,
            Prodotto prodotto,
            int quantitaRicevuta) {
        RigaOrdineAcquisto riga = new RigaOrdineAcquisto(
                30L, 1L, prodotto, quantitaRicevuta,
                new BigDecimal("2.50"));
        when(ordineRepository.findById(1L)).thenReturn(Optional.of(ordine));
        when(rigaRepository.findAllByOrdineAcquistoId(1L))
                .thenReturn(List.of(riga));
        when(prodottoRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(prodotto));
        when(prodottoRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(ordineRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }
}
