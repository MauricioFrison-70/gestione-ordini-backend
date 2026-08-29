package com.gestioneOrdini.domain.reporting.model;

public class ColonnaRapporto {

    private final String nome;
    private final String etichetta;
    private final String formato;
    private final int ordine;
    private final boolean visibile;
    private final boolean totalizzare;

    public ColonnaRapporto(String nome, String etichetta, String formato,
                           int ordine, boolean visibile) {
        this(nome, etichetta, formato, ordine, visibile, false);
    }

    public ColonnaRapporto(String nome, String etichetta, String formato,
                           int ordine, boolean visibile, boolean totalizzare) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Il nome della colonna è obbligatorio");
        }
        if (ordine < 0) {
            throw new IllegalArgumentException("L'ordine della colonna non può essere negativo");
        }
        this.nome = nome.trim();
        this.etichetta = etichetta == null || etichetta.isBlank()
                ? this.nome : etichetta.trim();
        this.formato = formato == null || formato.isBlank() ? null : formato.trim();
        this.ordine = ordine;
        this.visibile = visibile;
        this.totalizzare = totalizzare;
    }

    public String getNome() { return nome; }
    public String getEtichetta() { return etichetta; }
    public String getFormato() { return formato; }
    public int getOrdine() { return ordine; }
    public boolean isVisibile() { return visibile; }
    public boolean isTotalizzare() { return totalizzare; }
}
