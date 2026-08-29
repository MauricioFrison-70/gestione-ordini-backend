IF SCHEMA_ID(N'reporting') IS NULL
    EXEC(N'CREATE SCHEMA reporting AUTHORIZATION dbo');
GO

IF OBJECT_ID(N'reporting.rapporti', N'U') IS NULL
BEGIN
    CREATE TABLE reporting.rapporti (
        id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT pk_reporting_rapporti PRIMARY KEY,
        codice NVARCHAR(50) NOT NULL,
        titolo NVARCHAR(120) NOT NULL,
        descrizione NVARCHAR(500) NOT NULL
            CONSTRAINT df_reporting_rapporti_descrizione DEFAULT N'',
        nome_procedura NVARCHAR(257) NOT NULL,
        attivo BIT NOT NULL
            CONSTRAINT df_reporting_rapporti_attivo DEFAULT 1,
        ordine INT NOT NULL
            CONSTRAINT df_reporting_rapporti_ordine DEFAULT 0,
        data_modifica DATETIME2 NOT NULL
            CONSTRAINT df_reporting_rapporti_data_modifica DEFAULT SYSUTCDATETIME(),
        CONSTRAINT uk_reporting_rapporti_codice UNIQUE (codice),
        CONSTRAINT uk_reporting_rapporti_procedura UNIQUE (nome_procedura)
    );
END;
GO

IF OBJECT_ID(N'reporting.parametri_rapporti', N'U') IS NULL
BEGIN
    CREATE TABLE reporting.parametri_rapporti (
        id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT pk_reporting_parametri PRIMARY KEY,
        rapporto_id BIGINT NOT NULL,
        nome_parametro NVARCHAR(128) NOT NULL,
        tipo_sql NVARCHAR(128) NOT NULL,
        lunghezza_massima SMALLINT NOT NULL DEFAULT 0,
        precisione TINYINT NOT NULL DEFAULT 0,
        scala TINYINT NOT NULL DEFAULT 0,
        etichetta NVARCHAR(100) NOT NULL,
        tipo_campo NVARCHAR(20) NOT NULL,
        obbligatorio BIT NOT NULL DEFAULT 0,
        ordine INT NOT NULL,
        valore_predefinito NVARCHAR(500) NULL,
        procedura_opzioni NVARCHAR(257) NULL,
        attivo BIT NOT NULL DEFAULT 1,
        CONSTRAINT fk_reporting_parametri_rapporto
            FOREIGN KEY (rapporto_id) REFERENCES reporting.rapporti(id),
        CONSTRAINT uk_reporting_parametri_nome
            UNIQUE (rapporto_id, nome_parametro),
        CONSTRAINT ck_reporting_parametri_tipo_campo CHECK (
            tipo_campo IN (N'TESTO', N'INTERO', N'DECIMALE', N'DATA',
                           N'BOOLEANO', N'SELEZIONE'))
    );
END;
GO

IF OBJECT_ID(N'reporting.colonne_rapporti', N'U') IS NULL
BEGIN
    CREATE TABLE reporting.colonne_rapporti (
        id BIGINT IDENTITY(1,1) NOT NULL
            CONSTRAINT pk_reporting_colonne PRIMARY KEY,
        rapporto_id BIGINT NOT NULL,
        nome_colonna NVARCHAR(128) NOT NULL,
        etichetta NVARCHAR(100) NOT NULL,
        formato NVARCHAR(30) NULL,
        ordine INT NOT NULL,
        visibile BIT NOT NULL DEFAULT 1,
        totalizzare BIT NOT NULL
            CONSTRAINT df_reporting_colonne_totalizzare DEFAULT 0,
        CONSTRAINT fk_reporting_colonne_rapporto
            FOREIGN KEY (rapporto_id) REFERENCES reporting.rapporti(id),
        CONSTRAINT uk_reporting_colonne_nome
            UNIQUE (rapporto_id, nome_colonna)
    );
END;
GO

IF COL_LENGTH(N'reporting.colonne_rapporti', N'totalizzare') IS NULL
BEGIN
    ALTER TABLE reporting.colonne_rapporti
        ADD totalizzare BIT NOT NULL
            CONSTRAINT df_reporting_colonne_totalizzare DEFAULT 0;
END;
GO

CREATE OR ALTER PROCEDURE reporting.usp_registra_rapporto
    @Codice NVARCHAR(50),
    @Titolo NVARCHAR(120),
    @Descrizione NVARCHAR(500) = N'',
    @NomeProcedura NVARCHAR(257),
    @Attivo BIT = 1,
    @Ordine INT = 0
WITH EXECUTE AS OWNER
AS
BEGIN
    SET NOCOUNT ON;

    IF NULLIF(LTRIM(RTRIM(@Codice)), N'') IS NULL
    BEGIN
        ;THROW 51001, N'Il codice del rapporto è obbligatorio.', 1;
    END;

    IF PARSENAME(@NomeProcedura, 2) <> N'reporting'
       OR PARSENAME(@NomeProcedura, 1) IS NULL
       OR PARSENAME(@NomeProcedura, 3) IS NOT NULL
       OR OBJECT_ID(@NomeProcedura, N'P') IS NULL
    BEGIN
        ;THROW 51002, N'La stored procedure deve esistere nello schema reporting.', 1;
    END;

    IF EXISTS (
        SELECT 1 FROM sys.parameters
        WHERE object_id = OBJECT_ID(@NomeProcedura) AND is_output = 1
    )
    BEGIN
        ;THROW 51003, N'La stored procedure del rapporto non può avere parametri OUTPUT.', 1;
    END;

    IF EXISTS (SELECT 1 FROM reporting.rapporti WHERE codice = @Codice)
    BEGIN
        UPDATE reporting.rapporti
        SET titolo = @Titolo,
            descrizione = COALESCE(@Descrizione, N''),
            nome_procedura = @NomeProcedura,
            attivo = @Attivo,
            ordine = @Ordine,
            data_modifica = SYSUTCDATETIME()
        WHERE codice = @Codice;
    END
    ELSE
    BEGIN
        INSERT INTO reporting.rapporti
            (codice, titolo, descrizione, nome_procedura, attivo, ordine)
        VALUES
            (@Codice, @Titolo, COALESCE(@Descrizione, N''),
             @NomeProcedura, @Attivo, @Ordine);
    END;

    DECLARE @GrantSql NVARCHAR(700) =
        N'GRANT EXECUTE ON OBJECT::'
        + QUOTENAME(PARSENAME(@NomeProcedura, 2)) + N'.'
        + QUOTENAME(PARSENAME(@NomeProcedura, 1))
        + N' TO report_executor_role;';
    EXEC sp_executesql @GrantSql;
END;
GO

CREATE OR ALTER PROCEDURE reporting.usp_sincronizza_parametri
    @CodiceRapporto NVARCHAR(50)
WITH EXECUTE AS OWNER
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @RapportoId BIGINT;
    DECLARE @NomeProcedura NVARCHAR(257);
    SELECT @RapportoId = id, @NomeProcedura = nome_procedura
    FROM reporting.rapporti
    WHERE codice = @CodiceRapporto;

    IF @RapportoId IS NULL
    BEGIN
        ;THROW 51004, N'Rapporto non registrato.', 1;
    END;

    IF OBJECT_ID(@NomeProcedura, N'P') IS NULL
    BEGIN
        ;THROW 51005, N'La stored procedure registrata non esiste.', 1;
    END;

    IF EXISTS (
        SELECT 1 FROM sys.parameters
        WHERE object_id = OBJECT_ID(@NomeProcedura) AND is_output = 1
    )
    BEGIN
        ;THROW 51003, N'La stored procedure del rapporto non può avere parametri OUTPUT.', 1;
    END;

    BEGIN TRANSACTION;

    UPDATE destinazione
    SET tipo_sql = origine.tipo_sql,
        lunghezza_massima = origine.max_length,
        precisione = origine.precisione,
        scala = origine.scala,
        ordine = origine.ordine,
        attivo = 1
    FROM reporting.parametri_rapporti destinazione
    JOIN (
        SELECT
            SUBSTRING(p.name, 2, 128) AS nome_parametro,
            TYPE_NAME(p.user_type_id) AS tipo_sql,
            p.max_length,
            p.precision AS precisione,
            p.scale AS scala,
            p.parameter_id AS ordine
        FROM sys.parameters p
        WHERE p.object_id = OBJECT_ID(@NomeProcedura)
          AND p.parameter_id > 0
    ) origine
      ON origine.nome_parametro = destinazione.nome_parametro
    WHERE destinazione.rapporto_id = @RapportoId;

    INSERT INTO reporting.parametri_rapporti
        (rapporto_id, nome_parametro, tipo_sql, lunghezza_massima,
         precisione, scala, etichetta, tipo_campo, obbligatorio, ordine, attivo)
    SELECT
        @RapportoId,
        SUBSTRING(p.name, 2, 128),
        TYPE_NAME(p.user_type_id),
        p.max_length,
        p.precision,
        p.scale,
        SUBSTRING(p.name, 2, 128),
        CASE
            WHEN TYPE_NAME(p.user_type_id) IN (N'date', N'datetime', N'datetime2', N'smalldatetime')
                THEN N'DATA'
            WHEN TYPE_NAME(p.user_type_id) IN (N'tinyint', N'smallint', N'int', N'bigint')
                THEN N'INTERO'
            WHEN TYPE_NAME(p.user_type_id) IN (N'decimal', N'numeric', N'money', N'smallmoney', N'float', N'real')
                THEN N'DECIMALE'
            WHEN TYPE_NAME(p.user_type_id) = N'bit'
                THEN N'BOOLEANO'
            ELSE N'TESTO'
        END,
        0,
        p.parameter_id,
        1
    FROM sys.parameters p
    WHERE p.object_id = OBJECT_ID(@NomeProcedura)
      AND p.parameter_id > 0
      AND NOT EXISTS (
          SELECT 1
          FROM reporting.parametri_rapporti existente
          WHERE existente.rapporto_id = @RapportoId
            AND existente.nome_parametro = SUBSTRING(p.name, 2, 128)
      );

    UPDATE parametro
    SET attivo = 0
    FROM reporting.parametri_rapporti parametro
    WHERE parametro.rapporto_id = @RapportoId
      AND NOT EXISTS (
          SELECT 1
          FROM sys.parameters p
          WHERE p.object_id = OBJECT_ID(@NomeProcedura)
            AND p.parameter_id > 0
            AND SUBSTRING(p.name, 2, 128) = parametro.nome_parametro
      );

    COMMIT TRANSACTION;
END;
GO

CREATE OR ALTER PROCEDURE reporting.usp_configura_parametro
    @CodiceRapporto NVARCHAR(50),
    @NomeParametro NVARCHAR(128),
    @Etichetta NVARCHAR(100),
    @TipoCampo NVARCHAR(20),
    @Obbligatorio BIT = 0,
    @Ordine INT,
    @ValorePredefinito NVARCHAR(500) = NULL,
    @ProceduraOpzioni NVARCHAR(257) = NULL
WITH EXECUTE AS OWNER
AS
BEGIN
    SET NOCOUNT ON;

    IF LEFT(@NomeParametro, 1) = N'@'
        SET @NomeParametro = SUBSTRING(@NomeParametro, 2, 128);

    IF @TipoCampo NOT IN (N'TESTO', N'INTERO', N'DECIMALE', N'DATA',
                          N'BOOLEANO', N'SELEZIONE')
    BEGIN
        ;THROW 51006, N'Tipo di campo non supportato.', 1;
    END;

    IF @ProceduraOpzioni IS NOT NULL
       AND (
            @TipoCampo <> N'SELEZIONE'
            OR PARSENAME(@ProceduraOpzioni, 2) <> N'reporting'
            OR OBJECT_ID(@ProceduraOpzioni, N'P') IS NULL
       )
    BEGIN
        ;THROW 51007, N'La procedura delle opzioni non è valida.', 1;
    END;

    UPDATE parametro
    SET etichetta = @Etichetta,
        tipo_campo = @TipoCampo,
        obbligatorio = @Obbligatorio,
        ordine = @Ordine,
        valore_predefinito = @ValorePredefinito,
        procedura_opzioni = @ProceduraOpzioni
    FROM reporting.parametri_rapporti parametro
    JOIN reporting.rapporti rapporto ON rapporto.id = parametro.rapporto_id
    WHERE rapporto.codice = @CodiceRapporto
      AND parametro.nome_parametro = @NomeParametro
      AND parametro.attivo = 1;

    IF @@ROWCOUNT = 0
    BEGIN
        ;THROW 51008, N'Parametro non trovato. Eseguire prima la sincronizzazione.', 1;
    END;

    IF @ProceduraOpzioni IS NOT NULL
    BEGIN
        DECLARE @GrantSql NVARCHAR(700) =
            N'GRANT EXECUTE ON OBJECT::'
            + QUOTENAME(PARSENAME(@ProceduraOpzioni, 2)) + N'.'
            + QUOTENAME(PARSENAME(@ProceduraOpzioni, 1))
            + N' TO report_executor_role;';
        EXEC sp_executesql @GrantSql;
    END;
END;
GO

CREATE OR ALTER PROCEDURE reporting.usp_configura_colonna
    @CodiceRapporto NVARCHAR(50),
    @NomeColonna NVARCHAR(128),
    @Etichetta NVARCHAR(100),
    @Formato NVARCHAR(30) = NULL,
    @Ordine INT,
    @Visibile BIT = 1,
    @Totalizzare BIT = 0
WITH EXECUTE AS OWNER
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @RapportoId BIGINT = (
        SELECT id FROM reporting.rapporti WHERE codice = @CodiceRapporto
    );
    IF @RapportoId IS NULL
    BEGIN
        ;THROW 51004, N'Rapporto non registrato.', 1;
    END;

    IF EXISTS (
        SELECT 1 FROM reporting.colonne_rapporti
        WHERE rapporto_id = @RapportoId AND nome_colonna = @NomeColonna
    )
    BEGIN
        UPDATE reporting.colonne_rapporti
        SET etichetta = @Etichetta,
            formato = @Formato,
            ordine = @Ordine,
            visibile = @Visibile,
            totalizzare = @Totalizzare
        WHERE rapporto_id = @RapportoId AND nome_colonna = @NomeColonna;
    END
    ELSE
    BEGIN
        INSERT INTO reporting.colonne_rapporti
            (rapporto_id, nome_colonna, etichetta, formato, ordine, visibile, totalizzare)
        VALUES
            (@RapportoId, @NomeColonna, @Etichetta, @Formato, @Ordine, @Visibile,
             @Totalizzare);
    END;
END;
GO

CREATE OR ALTER PROCEDURE reporting.usp_opzioni_clienti
AS
BEGIN
    SET NOCOUNT ON;
    SELECT id AS valore, name AS etichetta
    FROM dbo.agenti
    WHERE agent_type = N'CLIENTE' AND is_archived = 0
    ORDER BY name;
END;
GO

CREATE OR ALTER PROCEDURE reporting.usp_opzioni_stati_ordini_vendita
AS
BEGIN
    SET NOCOUNT ON;

    SELECT valore, etichetta
    FROM (VALUES
        (N'NON_RILASCIATO', N'Non rilasciato', 1),
        (N'RILASCIATO', N'Rilasciato', 2),
        (N'ANNULLATO', N'Annullato', 3)
    ) AS stati(valore, etichetta, ordine)
    ORDER BY ordine;
END;
GO

IF DATABASE_PRINCIPAL_ID(N'report_executor_role') IS NULL
    CREATE ROLE report_executor_role AUTHORIZATION dbo;
GO

IF DATABASE_PRINCIPAL_ID(N'report_designer_role') IS NULL
    CREATE ROLE report_designer_role AUTHORIZATION dbo;
GO

GRANT SELECT ON OBJECT::reporting.rapporti TO report_executor_role;
GRANT SELECT ON OBJECT::reporting.parametri_rapporti TO report_executor_role;
GRANT SELECT ON OBJECT::reporting.colonne_rapporti TO report_executor_role;
GRANT EXECUTE ON OBJECT::reporting.usp_opzioni_clienti TO report_executor_role;
GRANT EXECUTE ON OBJECT::reporting.usp_opzioni_stati_ordini_vendita TO report_executor_role;

GRANT EXECUTE ON OBJECT::reporting.usp_registra_rapporto TO report_designer_role;
GRANT EXECUTE ON OBJECT::reporting.usp_sincronizza_parametri TO report_designer_role;
GRANT EXECUTE ON OBJECT::reporting.usp_configura_parametro TO report_designer_role;
GRANT EXECUTE ON OBJECT::reporting.usp_configura_colonna TO report_designer_role;
GRANT VIEW DEFINITION ON SCHEMA::reporting TO report_designer_role;
GO

EXEC reporting.usp_registra_rapporto
    @Codice = N'ORDINI_VENDITA_PER_PERIODO',
    @Titolo = N'Ordini di vendita per periodo',
    @Descrizione = N'Elenco degli ordini di vendita filtrato per periodo, cliente e stato.',
    @NomeProcedura = N'reporting.usp_report_ordini_vendita_per_periodo',
    @Attivo = 1,
    @Ordine = 1;

EXEC reporting.usp_sincronizza_parametri
    @CodiceRapporto = N'ORDINI_VENDITA_PER_PERIODO';

EXEC reporting.usp_configura_parametro
    @CodiceRapporto = N'ORDINI_VENDITA_PER_PERIODO',
    @NomeParametro = N'DataInizio',
    @Etichetta = N'Data iniziale',
    @TipoCampo = N'DATA',
    @Obbligatorio = 1,
    @Ordine = 1;

EXEC reporting.usp_configura_parametro
    @CodiceRapporto = N'ORDINI_VENDITA_PER_PERIODO',
    @NomeParametro = N'DataFine',
    @Etichetta = N'Data finale',
    @TipoCampo = N'DATA',
    @Obbligatorio = 1,
    @Ordine = 2;

EXEC reporting.usp_configura_parametro
    @CodiceRapporto = N'ORDINI_VENDITA_PER_PERIODO',
    @NomeParametro = N'ClienteId',
    @Etichetta = N'Cliente',
    @TipoCampo = N'SELEZIONE',
    @Obbligatorio = 0,
    @Ordine = 3,
    @ProceduraOpzioni = N'reporting.usp_opzioni_clienti';

EXEC reporting.usp_configura_parametro
    @CodiceRapporto = N'ORDINI_VENDITA_PER_PERIODO',
    @NomeParametro = N'Stato',
    @Etichetta = N'Stato',
    @TipoCampo = N'SELEZIONE',
    @Obbligatorio = 0,
    @Ordine = 4,
    @ProceduraOpzioni = N'reporting.usp_opzioni_stati_ordini_vendita';

EXEC reporting.usp_configura_colonna N'ORDINI_VENDITA_PER_PERIODO', N'clienteId',
    N'Cliente ID', NULL, 1, 0;
EXEC reporting.usp_configura_colonna N'ORDINI_VENDITA_PER_PERIODO', N'numeroOrdine',
    N'Numero ordine', NULL, 2, 1;
EXEC reporting.usp_configura_colonna N'ORDINI_VENDITA_PER_PERIODO', N'dataRegistrazione',
    N'Data registrazione', N'DATA_ORA', 3, 1;
EXEC reporting.usp_configura_colonna N'ORDINI_VENDITA_PER_PERIODO', N'cliente',
    N'Cliente', NULL, 4, 1;
EXEC reporting.usp_configura_colonna N'ORDINI_VENDITA_PER_PERIODO', N'venditore',
    N'Venditore', NULL, 5, 1;
EXEC reporting.usp_configura_colonna N'ORDINI_VENDITA_PER_PERIODO', N'trasportatore',
    N'Trasportatore', NULL, 6, 1;
EXEC reporting.usp_configura_colonna N'ORDINI_VENDITA_PER_PERIODO', N'stato',
    N'Stato', NULL, 7, 1;
EXEC reporting.usp_configura_colonna N'ORDINI_VENDITA_PER_PERIODO', N'valoreTotale',
    N'Valore totale', N'VALUTA', 8, 1, 1;
GO
