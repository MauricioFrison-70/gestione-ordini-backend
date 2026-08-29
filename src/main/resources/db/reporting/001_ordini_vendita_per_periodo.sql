IF SCHEMA_ID(N'reporting') IS NULL
BEGIN
    EXEC(N'CREATE SCHEMA reporting AUTHORIZATION dbo');
END;
GO

CREATE OR ALTER VIEW reporting.vw_ordini_vendita
AS
SELECT
    ov.id AS idOrdine,
    ov.order_number AS numeroOrdine,
    ov.created_at AS dataRegistrazione,
    ov.release_date AS dataRilascio,
    ov.cancellation_date AS dataAnnullamento,
    cliente.id AS clienteId,
    cliente.name AS cliente,
    venditore.id AS venditoreId,
    venditore.name AS venditore,
    trasportatore.name AS trasportatore,
    CASE
        WHEN ov.cancellation_date IS NOT NULL THEN N'ANNULLATO'
        WHEN ov.release_date IS NOT NULL THEN N'RILASCIATO'
        ELSE N'NON_RILASCIATO'
    END AS stato,
    CAST(
        COALESCE(
            SUM(CAST(riga.quantity AS DECIMAL(19, 2)) * riga.unit_price),
            0
        ) AS DECIMAL(19, 2)
    ) AS valoreTotale
FROM dbo.ordini_vendita ov
INNER JOIN dbo.agenti cliente
    ON cliente.id = ov.customer_id
INNER JOIN dbo.agenti venditore
    ON venditore.id = ov.seller_id
INNER JOIN dbo.agenti trasportatore
    ON trasportatore.id = ov.carrier_id
LEFT JOIN dbo.righe_ordini_vendita riga
    ON riga.order_id = ov.id
GROUP BY
    ov.id,
    ov.order_number,
    ov.created_at,
    ov.release_date,
    ov.cancellation_date,
    cliente.id,
    cliente.name,
    venditore.id,
    venditore.name,
    trasportatore.name;
GO

CREATE OR ALTER PROCEDURE reporting.usp_report_ordini_vendita_per_periodo
    @DataInizio DATE,
    @DataFine DATE,
    @ClienteId BIGINT = NULL,
    @Stato NVARCHAR(20) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF @DataInizio IS NULL OR @DataFine IS NULL
    BEGIN
        ;THROW 50001, N'La data iniziale e la data finale sono obbligatorie.', 1;
    END;

    IF @DataInizio > @DataFine
    BEGIN
        ;THROW 50002, N'La data iniziale non può essere successiva alla data finale.', 1;
    END;

    IF @Stato IS NOT NULL
       AND @Stato NOT IN (N'NON_RILASCIATO', N'RILASCIATO', N'ANNULLATO')
    BEGIN
        ;THROW 50003, N'Lo stato dell''ordine non è valido.', 1;
    END;

    SELECT
        idOrdine,
        numeroOrdine,
        dataRegistrazione,
        dataRilascio,
        dataAnnullamento,
        clienteId,
        cliente,
        venditore,
        trasportatore,
        stato,
        valoreTotale
    FROM reporting.vw_ordini_vendita
    WHERE dataRegistrazione >= CAST(@DataInizio AS DATETIME2)
      AND dataRegistrazione < DATEADD(DAY, 1, CAST(@DataFine AS DATETIME2))
      AND (@ClienteId IS NULL OR clienteId = @ClienteId)
      AND (@Stato IS NULL OR stato = @Stato)
    ORDER BY dataRegistrazione DESC, idOrdine DESC;
END;
GO

IF DATABASE_PRINCIPAL_ID(N'report_executor_role') IS NULL
BEGIN
    CREATE ROLE report_executor_role AUTHORIZATION dbo;
END;
GO

GRANT EXECUTE
ON OBJECT::reporting.usp_report_ordini_vendita_per_periodo
TO report_executor_role;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = N'ix_ordini_vendita_reporting_periodo_cliente'
      AND object_id = OBJECT_ID(N'dbo.ordini_vendita')
)
BEGIN
    CREATE INDEX ix_ordini_vendita_reporting_periodo_cliente
        ON dbo.ordini_vendita (created_at, customer_id)
        INCLUDE (order_number, seller_id, carrier_id, release_date, cancellation_date);
END;
GO
