:ON ERROR EXIT

USE master;
GO

DECLARE @Password NVARCHAR(128) = N'$(AI_DB_PASSWORD)';

IF @Password = N'$' + N'(AI_DB_PASSWORD)'
   OR LEN(@Password) < 16
BEGIN
    ;THROW 54001,
        N'Fornire AI_DB_PASSWORD con almeno 16 caratteri tramite la variabile SQLCMD.',
        1;
END;

IF SUSER_ID(N'gestione_ordini_ai') IS NULL
BEGIN
    DECLARE @CreateLoginSql NVARCHAR(1000) =
        N'CREATE LOGIN [gestione_ordini_ai] WITH PASSWORD = '
        + QUOTENAME(@Password, N'''')
        + N', CHECK_POLICY = ON, CHECK_EXPIRATION = OFF, '
        + N'DEFAULT_DATABASE = [ProjectJava];';

    EXEC sys.sp_executesql @CreateLoginSql;
END;
GO

USE ProjectJava;
GO

IF OBJECT_ID(N'reporting.vw_ordini_vendita', N'V') IS NULL
BEGIN
    ;THROW 54002, N'La view reporting.vw_ordini_vendita non esiste.', 1;
END;
GO

IF DATABASE_PRINCIPAL_ID(N'ai_ordini_vendita_reader_role') IS NULL
BEGIN
    CREATE ROLE ai_ordini_vendita_reader_role AUTHORIZATION dbo;
END;
GO

IF DATABASE_PRINCIPAL_ID(N'gestione_ordini_ai') IS NULL
BEGIN
    CREATE USER gestione_ordini_ai FOR LOGIN gestione_ordini_ai
        WITH DEFAULT_SCHEMA = reporting;
END;
GO

IF SUSER_SID(N'gestione_ordini_ai') <>
   (SELECT sid FROM sys.database_principals WHERE name = N'gestione_ordini_ai')
BEGIN
    ;THROW 54003,
        N'L''utente gestione_ordini_ai è associato a un login differente.',
        1;
END;
GO

IF IS_ROLEMEMBER(N'ai_ordini_vendita_reader_role', N'gestione_ordini_ai') <> 1
BEGIN
    ALTER ROLE ai_ordini_vendita_reader_role ADD MEMBER gestione_ordini_ai;
END;
GO

GRANT SELECT
ON OBJECT::reporting.vw_ordini_vendita
TO ai_ordini_vendita_reader_role;

DENY INSERT, UPDATE, DELETE
ON OBJECT::reporting.vw_ordini_vendita
TO ai_ordini_vendita_reader_role;

DENY SELECT ON SCHEMA::dbo TO ai_ordini_vendita_reader_role;
DENY EXECUTE TO ai_ordini_vendita_reader_role;
REVOKE CONTROL
ON SCHEMA::reporting
TO ai_ordini_vendita_reader_role;

DENY ALTER, TAKE OWNERSHIP
ON SCHEMA::reporting
TO ai_ordini_vendita_reader_role;
GO

EXECUTE AS USER = N'gestione_ordini_ai';

SELECT
    N'gestione_ordini_ai' AS utente,
    HAS_PERMS_BY_NAME(
        N'reporting.vw_ordini_vendita', N'OBJECT', N'SELECT') AS puo_leggere_view,
    HAS_PERMS_BY_NAME(
        N'dbo.ordini_vendita', N'OBJECT', N'SELECT') AS puo_leggere_tabella;

REVERT;
GO
