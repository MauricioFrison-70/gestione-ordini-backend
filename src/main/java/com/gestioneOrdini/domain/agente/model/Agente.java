package com.gestioneOrdini.domain.agente.model;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Rappresenta un agente all'interno del dominio dell'applicazione.
 * <p>
 * Questa classe modella l'entità principale utilizzata per gestire
 * informazioni relative agli agenti, come identificazione, dati personali
 * e tipologia. È una semplice struttura di dominio (POJO) utilizzata
 * dalle varie componenti dell'applicazione.
 * </p>
 *
 * <p><strong>Campi principali:</strong></p>
 * <ul>
 *     <li><strong>id</strong>: identificatore univoco dell'agente.</li>
 *     <li><strong>nome</strong>: nome completo dell'agente.</li>
 *     <li><strong>email</strong>: indirizzo email associato.</li>
 *     <li><strong>tipoAgente</strong>: tipologia dell'agente, rappresentata da {@link TipoAgente}.</li>
 *     <li><strong>archiviato</strong>: indica se l'agente è archiviato.</li>
 * </ul>
 *
 * <p>
 * La classe fornisce costruttori per creare nuove istanze sia con ID
 * predefinito (per oggetti già persistiti), sia senza ID (per nuove entità
 * da salvare). Include inoltre i metodi getter e setter standard.
 * </p>
 */

public class Agente {

    private static final int LUNGHEZZA_MASSIMA_NOME = 60;
    private static final int LUNGHEZZA_MASSIMA_EMAIL = 80;
    private static final Pattern FORMATO_EMAIL =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    //@JsonIgnore
    private final Long id;
    private String nome;
    private String email;
    private TipoAgente tipoAgente;
    private Boolean archiviato = false;
    private final LocalDateTime dataRegistrazione;

    public Agente(Long id, String nome, String email, TipoAgente tipoAgente, Boolean archiviato) {
        this(id, nome, email, tipoAgente, archiviato, null);
    }

    /**
     * Ricostruisce un agente già persistito, inclusa la data di registrazione
     * assegnata dall'infrastruttura di persistenza.
     */
    public Agente(Long id, String nome, String email, TipoAgente tipoAgente,
                  Boolean archiviato, LocalDateTime dataRegistrazione) {
        this.id = id;
        setNome(nome);
        setEmail(email);
        setTipoAgente(tipoAgente);
        this.archiviato = archiviato;
        this.dataRegistrazione = dataRegistrazione;
    }

    public Agente(String nome, String email, TipoAgente tipoAgente, Boolean archiviato) {
        this(null, nome, email, tipoAgente, archiviato);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        validaNome(nome);
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        validaEmail(email);
        this.email = email;
    }

    public TipoAgente getTipoAgente() {
        return tipoAgente;
    }

    public void setTipoAgente(TipoAgente tipoAgente) {
        if (tipoAgente == null) {
            throw new IllegalArgumentException("Il tipo di agente è obbligatorio");
        }
        this.tipoAgente = tipoAgente;
    }

    public Boolean getArchiviato() {
        return archiviato;
    }

    public void setArchiviato(Boolean archiviato) {
        this.archiviato = archiviato;
    }

    public LocalDateTime getDataRegistrazione() {
        return dataRegistrazione;
    }

    private static void validaNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Il nome è obbligatorio");
        }

        if (nome.length() > LUNGHEZZA_MASSIMA_NOME) {
            throw new IllegalArgumentException(
                    "Il nome può contenere al massimo " + LUNGHEZZA_MASSIMA_NOME + " caratteri");
        }
    }

    private static void validaEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("L'email è obbligatoria");
        }

        if (email.length() > LUNGHEZZA_MASSIMA_EMAIL) {
            throw new IllegalArgumentException(
                    "L'email può contenere al massimo " + LUNGHEZZA_MASSIMA_EMAIL + " caratteri");
        }

        if (!FORMATO_EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("Email non valida");
        }
    }
}
