IF SCHEMA_ID(N'reporting') IS NULL
BEGIN
    ;THROW 53001, N'Lo schema reporting non è installato.', 1;
END;
GO

IF OBJECT_ID(N'reporting.vw_ordini_vendita', N'V') IS NULL
BEGIN
    ;THROW 53002, N'La vista degli ordini di vendita non è installata.', 1;
END;
GO

CREATE OR ALTER PROCEDURE reporting.usp_report_vendite_ultimi_dodici_mesi
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @MeseCorrente DATE = DATEFROMPARTS(
        YEAR(GETDATE()),
        MONTH(GETDATE()),
        1
    );

    ;WITH numeri(numero) AS (
        SELECT numero
        FROM (VALUES
            (0), (1), (2), (3), (4), (5),
            (6), (7), (8), (9), (10), (11)
        ) AS elenco(numero)
    ),
    mesi AS (
        SELECT DATEADD(MONTH, -numero, @MeseCorrente) AS periodo
        FROM numeri
    )
    SELECT
        mesi.periodo,
        CONCAT(
            CHOOSE(
                MONTH(mesi.periodo),
                N'gennaio', N'febbraio', N'marzo', N'aprile',
                N'maggio', N'giugno', N'luglio', N'agosto',
                N'settembre', N'ottobre', N'novembre', N'dicembre'
            ),
            N' ',
            YEAR(mesi.periodo)
        ) AS mese,
        COUNT_BIG(vendita.idOrdine) AS numeroOrdini,
        CAST(COALESCE(SUM(vendita.valoreTotale), 0) AS DECIMAL(19, 2))
            AS valoreTotale
    FROM mesi
    LEFT JOIN reporting.vw_ordini_vendita vendita
        ON vendita.dataRegistrazione >= CAST(mesi.periodo AS DATETIME2)
       AND vendita.dataRegistrazione < DATEADD(MONTH, 1, CAST(mesi.periodo AS DATETIME2))
       AND vendita.stato = N'RILASCIATO'
    GROUP BY mesi.periodo
    ORDER BY mesi.periodo;
END;
GO

EXEC reporting.usp_registra_rapporto
    @Codice = N'VENDITE_ULTIMI_DODICI_MESI',
    @Titolo = N'Vendite degli ultimi dodici mesi',
    @Descrizione = N'Totale mensile degli ordini di vendita non annullati, dal mese corrente agli undici mesi precedenti.',
    @NomeProcedura = N'reporting.usp_report_vendite_ultimi_dodici_mesi',
    @Attivo = 1,
    @Ordine = 3;

EXEC reporting.usp_sincronizza_parametri
    @CodiceRapporto = N'VENDITE_ULTIMI_DODICI_MESI';

EXEC reporting.usp_configura_colonna N'VENDITE_ULTIMI_DODICI_MESI', N'periodo',
    N'Periodo', NULL, 1, 0, 0;
EXEC reporting.usp_configura_colonna N'VENDITE_ULTIMI_DODICI_MESI', N'mese',
    N'Mese', NULL, 2, 1, 0;
EXEC reporting.usp_configura_colonna N'VENDITE_ULTIMI_DODICI_MESI', N'numeroOrdini',
    N'Numero ordini', NULL, 3, 1, 1;
EXEC reporting.usp_configura_colonna N'VENDITE_ULTIMI_DODICI_MESI', N'valoreTotale',
    N'Valore totale', N'VALUTA', 4, 1, 1;
GO
