IF SCHEMA_ID(N'reporting') IS NULL
BEGIN
    ;THROW 52001, N'Lo schema reporting non è installato.', 1;
END;
GO

IF OBJECT_ID(N'reporting.vw_ordini_vendita', N'V') IS NULL
BEGIN
    ;THROW 52002, N'La vista degli ordini di vendita non è installata.', 1;
END;
GO

CREATE OR ALTER PROCEDURE reporting.usp_report_ranking_venditori_per_periodo
    @DataInizio DATE,
    @DataFine DATE
AS
BEGIN
    SET NOCOUNT ON;

    IF @DataInizio IS NULL OR @DataFine IS NULL
    BEGIN
        ;THROW 52003, N'La data iniziale e la data finale sono obbligatorie.', 1;
    END;

    IF @DataInizio > @DataFine
    BEGIN
        ;THROW 52004, N'La data iniziale non può essere successiva alla data finale.', 1;
    END;

    ;WITH vendite AS (
        SELECT
            venditoreId,
            venditore,
            COUNT_BIG(*) AS numeroOrdini,
            CAST(SUM(valoreTotale) AS DECIMAL(19, 2)) AS valoreTotale
        FROM reporting.vw_ordini_vendita
        WHERE dataRegistrazione >= CAST(@DataInizio AS DATETIME2)
          AND dataRegistrazione < DATEADD(DAY, 1, CAST(@DataFine AS DATETIME2))
          AND stato <> N'ANNULLATO'
        GROUP BY venditoreId, venditore
    )
    SELECT
        DENSE_RANK() OVER (
            ORDER BY valoreTotale DESC
        ) AS posizione,
        venditoreId,
        venditore,
        numeroOrdini,
        valoreTotale
    FROM vendite
    ORDER BY valoreTotale DESC, venditore;
END;
GO

EXEC reporting.usp_registra_rapporto
    @Codice = N'RANKING_VENDITORI_PER_PERIODO',
    @Titolo = N'Classifica venditori per periodo',
    @Descrizione = N'Classifica dei venditori per valore degli ordini non annullati nel periodo selezionato.',
    @NomeProcedura = N'reporting.usp_report_ranking_venditori_per_periodo',
    @Attivo = 1,
    @Ordine = 2;

EXEC reporting.usp_sincronizza_parametri
    @CodiceRapporto = N'RANKING_VENDITORI_PER_PERIODO';

EXEC reporting.usp_configura_parametro
    @CodiceRapporto = N'RANKING_VENDITORI_PER_PERIODO',
    @NomeParametro = N'DataInizio',
    @Etichetta = N'Data iniziale',
    @TipoCampo = N'DATA',
    @Obbligatorio = 1,
    @Ordine = 1;

EXEC reporting.usp_configura_parametro
    @CodiceRapporto = N'RANKING_VENDITORI_PER_PERIODO',
    @NomeParametro = N'DataFine',
    @Etichetta = N'Data finale',
    @TipoCampo = N'DATA',
    @Obbligatorio = 1,
    @Ordine = 2;

EXEC reporting.usp_configura_colonna N'RANKING_VENDITORI_PER_PERIODO', N'venditoreId',
    N'Venditore ID', NULL, 1, 0, 0;
EXEC reporting.usp_configura_colonna N'RANKING_VENDITORI_PER_PERIODO', N'posizione',
    N'Posizione', NULL, 2, 1, 0;
EXEC reporting.usp_configura_colonna N'RANKING_VENDITORI_PER_PERIODO', N'venditore',
    N'Venditore', NULL, 3, 1, 0;
EXEC reporting.usp_configura_colonna N'RANKING_VENDITORI_PER_PERIODO', N'numeroOrdini',
    N'Numero ordini', NULL, 4, 1, 1;
EXEC reporting.usp_configura_colonna N'RANKING_VENDITORI_PER_PERIODO', N'valoreTotale',
    N'Valore totale', N'VALUTA', 5, 1, 1;
GO
