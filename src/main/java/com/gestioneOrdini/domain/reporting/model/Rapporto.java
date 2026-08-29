package com.gestioneOrdini.domain.reporting.model;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Rapporto {

    private final Long id;
    private final String codice;
    private final String titolo;
    private final String descrizione;
    private final String nomeProcedura;
    private final boolean attivo;
    private final List<ParametroRapporto> parametri;
    private final List<ColonnaRapporto> colonne;

    public Rapporto(Long id, String codice, String titolo, String descrizione,
                    String nomeProcedura, boolean attivo, List<ParametroRapporto> parametri,
                    List<ColonnaRapporto> colonne) {
        this.id = id;
        this.codice = testoObbligatorio(codice, "Il codice del rapporto è obbligatorio");
        this.titolo = testoObbligatorio(titolo, "Il titolo del rapporto è obbligatorio");
        this.descrizione = descrizione == null ? "" : descrizione.trim();
        this.nomeProcedura = validaProcedura(nomeProcedura);
        this.attivo = attivo;
        this.parametri = ordinaEValidaParametri(parametri);
        this.colonne = colonne == null ? List.of() : colonne.stream()
                .sorted(Comparator.comparingInt(ColonnaRapporto::getOrdine))
                .toList();
    }

    public Rapporto(Long id, String codice, String titolo, String descrizione,
                    String nomeProcedura, boolean attivo, List<ParametroRapporto> parametri) {
        this(id, codice, titolo, descrizione, nomeProcedura, attivo, parametri, List.of());
    }

    public Rapporto(String codice, String titolo, String descrizione,
                    String nomeProcedura, boolean attivo, List<ParametroRapporto> parametri) {
        this(null, codice, titolo, descrizione, nomeProcedura, attivo, parametri, List.of());
    }

    public Long getId() { return id; }
    public String getCodice() { return codice; }
    public String getTitolo() { return titolo; }
    public String getDescrizione() { return descrizione; }
    public String getNomeProcedura() { return nomeProcedura; }
    public boolean isAttivo() { return attivo; }
    public List<ParametroRapporto> getParametri() { return parametri; }
    public List<ColonnaRapporto> getColonne() { return colonne; }

    private static List<ParametroRapporto> ordinaEValidaParametri(List<ParametroRapporto> parametri) {
        if (parametri == null) {
            throw new IllegalArgumentException("I parametri del rapporto sono obbligatori");
        }
        Set<String> nomi = new HashSet<>();
        for (ParametroRapporto parametro : parametri) {
            if (parametro == null || !nomi.add(parametro.getNome().toLowerCase())) {
                throw new IllegalArgumentException("I nomi dei parametri devono essere univoci");
            }
        }
        return parametri.stream()
                .sorted(Comparator.comparingInt(ParametroRapporto::getOrdine))
                .toList();
    }

    private static String validaProcedura(String valore) {
        String procedura = testoObbligatorio(valore, "Il nome della procedura è obbligatorio");
        if (!procedura.matches("reporting\\.[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("La procedura deve appartenere allo schema reporting");
        }
        return procedura;
    }

    private static String testoObbligatorio(String valore, String messaggio) {
        if (valore == null || valore.isBlank()) {
            throw new IllegalArgumentException(messaggio);
        }
        return valore.trim();
    }
}
