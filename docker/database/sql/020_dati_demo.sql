:ON ERROR EXIT

USE ProjectJava;
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.agenti WHERE email = N'cliente.milano@demo.local')
    INSERT dbo.agenti (name, email, agent_type, is_archived, created_at)
    VALUES (N'Cliente Milano S.r.l.', N'cliente.milano@demo.local', N'CLIENTE', 0, SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM dbo.agenti WHERE email = N'cliente.verona@demo.local')
    INSERT dbo.agenti (name, email, agent_type, is_archived, created_at)
    VALUES (N'Cliente Verona S.p.A.', N'cliente.verona@demo.local', N'CLIENTE', 0, SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM dbo.agenti WHERE email = N'venditore.rossi@demo.local')
    INSERT dbo.agenti (name, email, agent_type, is_archived, created_at)
    VALUES (N'Alessandro Rossi', N'venditore.rossi@demo.local', N'VENDITORE', 0, SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM dbo.agenti WHERE email = N'venditore.bianchi@demo.local')
    INSERT dbo.agenti (name, email, agent_type, is_archived, created_at)
    VALUES (N'Giulia Bianchi', N'venditore.bianchi@demo.local', N'VENDITORE', 0, SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM dbo.agenti WHERE email = N'venditore.verdi@demo.local')
    INSERT dbo.agenti (name, email, agent_type, is_archived, created_at)
    VALUES (N'Marco Verdi', N'venditore.verdi@demo.local', N'VENDITORE', 0, SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM dbo.agenti WHERE email = N'trasportatore@demo.local')
    INSERT dbo.agenti (name, email, agent_type, is_archived, created_at)
    VALUES (N'Trasporti Lombardia', N'trasportatore@demo.local', N'TRASPORTATORE', 0, SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM dbo.agenti WHERE email = N'fornitore@demo.local')
    INSERT dbo.agenti (name, email, agent_type, is_archived, created_at)
    VALUES (N'Forniture Italia', N'fornitore@demo.local', N'FORNITORE', 0, SYSUTCDATETIME());
GO

IF NOT EXISTS (SELECT 1 FROM dbo.prodotti WHERE code = N'PR0001')
    INSERT dbo.prodotti
        (code, description, purchase_value, sale_value, quantity, minimum_stock, archived, created_at)
    VALUES (N'PR0001', N'Notebook professionale', 720.00, 1090.00, 30, 8, 0, SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM dbo.prodotti WHERE code = N'PR0002')
    INSERT dbo.prodotti
        (code, description, purchase_value, sale_value, quantity, minimum_stock, archived, created_at)
    VALUES (N'PR0002', N'Monitor 27 pollici', 180.00, 299.90, 45, 10, 0, SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM dbo.prodotti WHERE code = N'PR0003')
    INSERT dbo.prodotti
        (code, description, purchase_value, sale_value, quantity, minimum_stock, archived, created_at)
    VALUES (N'PR0003', N'Tastiera meccanica', 55.00, 99.90, 60, 12, 0, SYSUTCDATETIME());

IF NOT EXISTS (SELECT 1 FROM dbo.prodotti WHERE code = N'PR0004')
    INSERT dbo.prodotti
        (code, description, purchase_value, sale_value, quantity, minimum_stock, archived, created_at)
    VALUES (N'PR0004', N'Mouse ergonomico', 28.00, 54.90, 75, 15, 0, SYSUTCDATETIME());
GO

DECLARE @ClienteMilano BIGINT = (
    SELECT id FROM dbo.agenti WHERE email = N'cliente.milano@demo.local');
DECLARE @ClienteVerona BIGINT = (
    SELECT id FROM dbo.agenti WHERE email = N'cliente.verona@demo.local');
DECLARE @VenditoreRossi BIGINT = (
    SELECT id FROM dbo.agenti WHERE email = N'venditore.rossi@demo.local');
DECLARE @VenditoreBianchi BIGINT = (
    SELECT id FROM dbo.agenti WHERE email = N'venditore.bianchi@demo.local');
DECLARE @VenditoreVerdi BIGINT = (
    SELECT id FROM dbo.agenti WHERE email = N'venditore.verdi@demo.local');
DECLARE @Trasportatore BIGINT = (
    SELECT id FROM dbo.agenti WHERE email = N'trasportatore@demo.local');

;WITH mesi AS (
    SELECT numero,
           DATEADD(MONTH, -numero,
               DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)) AS inizio_mese
    FROM (VALUES (0), (1), (2), (3), (4), (5),
                 (6), (7), (8), (9), (10), (11)) elenco(numero)
), ordini AS (
    SELECT
        CONCAT(N'OV-DEMO-', CONVERT(CHAR(6), inizio_mese, 112), N'-',
               RIGHT(N'0' + CONVERT(NVARCHAR(2), indice), 2)) AS numero_ordine,
        CASE WHEN indice IN (1, 3) THEN @ClienteMilano ELSE @ClienteVerona END AS cliente_id,
        CASE indice
            WHEN 1 THEN @VenditoreRossi
            WHEN 2 THEN @VenditoreBianchi
            ELSE @VenditoreVerdi
        END AS venditore_id,
        @Trasportatore AS trasportatore_id,
        DATEADD(HOUR, 9 + indice,
            CAST(DATEADD(DAY,
                CASE
                    WHEN numero = 0 AND indice = 1 THEN 0
                    WHEN numero = 0 AND indice = 2 THEN
                        CASE WHEN DAY(GETDATE()) > 2 THEN DAY(GETDATE()) - 2 ELSE 0 END
                    WHEN numero = 0 THEN
                        CASE WHEN DAY(GETDATE()) > 1 THEN DAY(GETDATE()) - 1 ELSE 0 END
                    WHEN indice = 1 THEN 3
                    WHEN indice = 2 THEN 12
                    ELSE 21
                END,
                inizio_mese) AS DATETIME2)) AS data_registrazione
    FROM mesi
    CROSS JOIN (VALUES (1), (2), (3)) posizioni(indice)
)
MERGE dbo.ordini_vendita AS destinazione
USING ordini AS origine
ON destinazione.order_number = origine.numero_ordine
WHEN MATCHED THEN
    UPDATE SET
        customer_id = origine.cliente_id,
        seller_id = origine.venditore_id,
        carrier_id = origine.trasportatore_id,
        created_at = origine.data_registrazione,
        release_date = CAST(origine.data_registrazione AS DATE),
        cancellation_date = NULL
WHEN NOT MATCHED THEN
    INSERT (order_number, customer_id, seller_id, carrier_id,
            created_at, release_date, cancellation_date)
    VALUES (origine.numero_ordine, origine.cliente_id, origine.venditore_id,
            origine.trasportatore_id, origine.data_registrazione,
            CAST(origine.data_registrazione AS DATE), NULL);
GO

DECLARE @ProdottoNotebook BIGINT = (
    SELECT id FROM dbo.prodotti WHERE code = N'PR0001');
DECLARE @ProdottoMonitor BIGINT = (
    SELECT id FROM dbo.prodotti WHERE code = N'PR0002');

;WITH dati_ordini AS (
    SELECT
        ordine.id AS ordine_id,
        MONTH(ordine.created_at) AS mese,
        CASE venditore.email
            WHEN N'venditore.rossi@demo.local' THEN 1
            WHEN N'venditore.bianchi@demo.local' THEN 2
            ELSE 3
        END AS indice_venditore
    FROM dbo.ordini_vendita ordine
    INNER JOIN dbo.agenti venditore ON venditore.id = ordine.seller_id
    WHERE ordine.order_number LIKE N'OV-DEMO-[0-9]%'
), righe_demo AS (
    SELECT
        dati.ordine_id,
        prodotto.id AS prodotto_id,
        prodotto.quantita,
        CAST(prodotto.prezzo AS DECIMAL(15, 2)) AS prezzo
    FROM dati_ordini dati
    CROSS APPLY (VALUES
        (
            @ProdottoNotebook,
            1 + ((dati.mese + dati.indice_venditore) % 3),
            900.00 + (dati.mese * 23.00) + (dati.indice_venditore * 67.00)
        ),
        (
            @ProdottoMonitor,
            1 + (((dati.mese * 2) + dati.indice_venditore) % 4),
            210.00 + (dati.mese * 11.00) + (dati.indice_venditore * 29.00)
        )
    ) prodotto(id, quantita, prezzo)
)
MERGE dbo.righe_ordini_vendita AS destinazione
USING righe_demo AS origine
ON destinazione.order_id = origine.ordine_id
   AND destinazione.product_id = origine.prodotto_id
WHEN MATCHED THEN
    UPDATE SET
        quantity = origine.quantita,
        unit_price = origine.prezzo
WHEN NOT MATCHED THEN
    INSERT (order_id, product_id, quantity, unit_price)
    VALUES (origine.ordine_id, origine.prodotto_id,
            origine.quantita, origine.prezzo);
GO

DECLARE @Cliente BIGINT = (
    SELECT id FROM dbo.agenti WHERE email = N'cliente.milano@demo.local');
DECLARE @Venditore BIGINT = (
    SELECT id FROM dbo.agenti WHERE email = N'venditore.rossi@demo.local');
DECLARE @Trasportatore BIGINT = (
    SELECT id FROM dbo.agenti WHERE email = N'trasportatore@demo.local');
DECLARE @Prodotto BIGINT = (
    SELECT id FROM dbo.prodotti WHERE code = N'PR0003');

IF NOT EXISTS (SELECT 1 FROM dbo.ordini_vendita WHERE order_number = N'OV-DEMO-PENDENTE')
    INSERT dbo.ordini_vendita
        (order_number, customer_id, seller_id, carrier_id,
         created_at, release_date, cancellation_date)
    VALUES (N'OV-DEMO-PENDENTE', @Cliente, @Venditore, @Trasportatore,
            SYSUTCDATETIME(), NULL, NULL);

IF NOT EXISTS (SELECT 1 FROM dbo.righe_ordini_vendita r
               JOIN dbo.ordini_vendita o ON o.id = r.order_id
               WHERE o.order_number = N'OV-DEMO-PENDENTE' AND r.product_id = @Prodotto)
    INSERT dbo.righe_ordini_vendita (order_id, product_id, quantity, unit_price)
    SELECT id, @Prodotto, 3, 99.90
    FROM dbo.ordini_vendita WHERE order_number = N'OV-DEMO-PENDENTE';
GO

DECLARE @Fornitore BIGINT = (
    SELECT id FROM dbo.agenti WHERE email = N'fornitore@demo.local');
DECLARE @ProdottoAcquisto BIGINT = (
    SELECT id FROM dbo.prodotti WHERE code = N'PR0004');
DECLARE @Anno CHAR(4) = CONVERT(CHAR(4), YEAR(GETDATE()));
DECLARE @NumeroAcquisto NVARCHAR(30) = CONCAT(N'OA-DEMO-', @Anno, N'-001');

IF NOT EXISTS (SELECT 1 FROM dbo.ordini_acquisto WHERE order_number = @NumeroAcquisto)
    INSERT dbo.ordini_acquisto
        (order_number, supplier_id, created_at, receipt_date, cancellation_date)
    VALUES (@NumeroAcquisto, @Fornitore, SYSUTCDATETIME(), NULL, NULL);

IF NOT EXISTS (SELECT 1 FROM dbo.righe_ordini_acquisto r
               JOIN dbo.ordini_acquisto o ON o.id = r.order_id
               WHERE o.order_number = @NumeroAcquisto
                 AND r.product_id = @ProdottoAcquisto)
    INSERT dbo.righe_ordini_acquisto (order_id, product_id, quantity, unit_price)
    SELECT id, @ProdottoAcquisto, 20, 28.00
    FROM dbo.ordini_acquisto WHERE order_number = @NumeroAcquisto;
GO

PRINT N'Dati dimostrativi disponibili.';
GO
