package com.gestioneOrdini.domain.reporting.model;

public class ParametroRapporto {

    private final Long id;
    private final String nome;
    private final String etichetta;
    private final String tipoSql;
    private final TipoParametroRapporto tipo;
    private final boolean obbligatorio;
    private final int ordine;
    private final String valorePredefinito;
    private final String proceduraOpzioni;

    public ParametroRapporto(Long id, String nome, String etichetta, String tipoSql,
                             TipoParametroRapporto tipo, boolean obbligatorio, int ordine,
                             String valorePredefinito, String proceduraOpzioni) {
        this.id = id;
        this.nome = testoObbligatorio(nome, "Il nome del parametro è obbligatorio");
        this.etichetta = testoObbligatorio(etichetta, "L'etichetta del parametro è obbligatoria");
        this.tipoSql = testoObbligatorio(tipoSql, "Il tipo SQL del parametro è obbligatorio")
                .toLowerCase();
        if (tipo == null) {
            throw new IllegalArgumentException("Il tipo del parametro è obbligatorio");
        }
        if (ordine < 0) {
            throw new IllegalArgumentException("L'ordine del parametro non può essere negativo");
        }
        this.tipo = tipo;
        this.obbligatorio = obbligatorio;
        this.ordine = ordine;
        this.valorePredefinito = valorePredefinito;
        this.proceduraOpzioni = validaProceduraOpzioni(tipo, proceduraOpzioni);
    }

    public ParametroRapporto(String nome, String etichetta, TipoParametroRapporto tipo,
                             boolean obbligatorio, int ordine) {
        this(null, nome, etichetta, tipoSqlPredefinito(tipo), tipo,
                obbligatorio, ordine, null, null);
    }

    public ParametroRapporto(Long id, String nome, String etichetta,
                             TipoParametroRapporto tipo, boolean obbligatorio, int ordine) {
        this(id, nome, etichetta, tipoSqlPredefinito(tipo), tipo,
                obbligatorio, ordine, null, null);
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEtichetta() { return etichetta; }
    public String getTipoSql() { return tipoSql; }
    public TipoParametroRapporto getTipo() { return tipo; }
    public boolean isObbligatorio() { return obbligatorio; }
    public int getOrdine() { return ordine; }
    public String getValorePredefinito() { return valorePredefinito; }
    public String getProceduraOpzioni() { return proceduraOpzioni; }

    private static String validaProceduraOpzioni(
            TipoParametroRapporto tipo, String proceduraOpzioni) {
        if (proceduraOpzioni == null || proceduraOpzioni.isBlank()) {
            return null;
        }
        String procedura = proceduraOpzioni.trim();
        if (tipo != TipoParametroRapporto.SELEZIONE) {
            throw new IllegalArgumentException(
                    "La procedura delle opzioni è consentita solo per un campo di selezione");
        }
        if (!procedura.matches("reporting\\.[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException(
                    "La procedura delle opzioni deve appartenere allo schema reporting");
        }
        return procedura;
    }

    private static String testoObbligatorio(String valore, String messaggio) {
        if (valore == null || valore.isBlank()) {
            throw new IllegalArgumentException(messaggio);
        }
        return valore.trim();
    }

    private static String tipoSqlPredefinito(TipoParametroRapporto tipo) {
        if (tipo == null) return "nvarchar";
        return switch (tipo) {
            case DATA -> "date";
            case INTERO -> "int";
            case DECIMALE -> "decimal";
            case BOOLEANO -> "bit";
            case TESTO -> "nvarchar";
            case SELEZIONE -> "bigint";
        };
    }
}
