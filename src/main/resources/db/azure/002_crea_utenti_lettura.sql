:ON ERROR EXIT

-- Eseguire dopo gli script db/reporting/001..004, collegandosi direttamente
-- al database Azure SQL. Le password non devono essere salvate nel repository.
SET NOCOUNT ON;
GO

IF DATABASE_PRINCIPAL_ID(N'report_executor_role') IS NULL
BEGIN
    ;THROW 55301, N'Il ruolo report_executor_role non è installato.', 1;
END;

IF OBJECT_ID(N'reporting.vw_ordini_vendita', N'V') IS NULL
BEGIN
    ;THROW 55302, N'La view reporting.vw_ordini_vendita non è installata.', 1;
END;
GO

DECLARE @PasswordReporting NVARCHAR(128) = N'$(REPORTING_DB_PASSWORD)';
DECLARE @PasswordAi NVARCHAR(128) = N'$(AI_DB_PASSWORD)';

IF @PasswordReporting = N'$' + N'(REPORTING_DB_PASSWORD)'
   OR LEN(@PasswordReporting) < 16
   OR LEN(@PasswordReporting) > 128
BEGIN
    ;THROW 55303,
        N'Fornire REPORTING_DB_PASSWORD con una lunghezza compresa tra 16 e 128 caratteri.',
        1;
END;

IF @PasswordAi = N'$' + N'(AI_DB_PASSWORD)'
   OR LEN(@PasswordAi) < 16
   OR LEN(@PasswordAi) > 128
BEGIN
    ;THROW 55304,
        N'Fornire AI_DB_PASSWORD con una lunghezza compresa tra 16 e 128 caratteri.',
        1;
END;

DECLARE @SqlReporting NVARCHAR(1000);
IF DATABASE_PRINCIPAL_ID(N'gestione_ordini_report') IS NULL
BEGIN
    SET @SqlReporting =
        N'CREATE USER [gestione_ordini_report] WITH PASSWORD = '
        + QUOTENAME(@PasswordReporting, N'''')
        + N', DEFAULT_SCHEMA = [reporting];';
END
ELSE
BEGIN
    SET @SqlReporting =
        N'ALTER USER [gestione_ordini_report] WITH PASSWORD = '
        + QUOTENAME(@PasswordReporting, N'''')
        + N';';
END;
EXEC sys.sp_executesql @SqlReporting;

DECLARE @SqlAi NVARCHAR(1000);
IF DATABASE_PRINCIPAL_ID(N'gestione_ordini_ai') IS NULL
BEGIN
    SET @SqlAi =
        N'CREATE USER [gestione_ordini_ai] WITH PASSWORD = '
        + QUOTENAME(@PasswordAi, N'''')
        + N', DEFAULT_SCHEMA = [reporting];';
END
ELSE
BEGIN
    SET @SqlAi =
        N'ALTER USER [gestione_ordini_ai] WITH PASSWORD = '
        + QUOTENAME(@PasswordAi, N'''')
        + N';';
END;
EXEC sys.sp_executesql @SqlAi;
GO

IF IS_ROLEMEMBER(N'report_executor_role', N'gestione_ordini_report') <> 1
    ALTER ROLE report_executor_role ADD MEMBER gestione_ordini_report;

DENY INSERT, UPDATE, DELETE ON SCHEMA::reporting TO gestione_ordini_report;
DENY SELECT ON SCHEMA::dbo TO gestione_ordini_report;
GO

IF DATABASE_PRINCIPAL_ID(N'ai_ordini_vendita_reader_role') IS NULL
BEGIN
    CREATE ROLE ai_ordini_vendita_reader_role AUTHORIZATION dbo;
END;

IF IS_ROLEMEMBER(N'ai_ordini_vendita_reader_role', N'gestione_ordini_ai') <> 1
    ALTER ROLE ai_ordini_vendita_reader_role ADD MEMBER gestione_ordini_ai;

GRANT SELECT
ON OBJECT::reporting.vw_ordini_vendita
TO ai_ordini_vendita_reader_role;

DENY INSERT, UPDATE, DELETE
ON OBJECT::reporting.vw_ordini_vendita
TO ai_ordini_vendita_reader_role;

DENY SELECT ON SCHEMA::dbo TO ai_ordini_vendita_reader_role;
DENY EXECUTE TO ai_ordini_vendita_reader_role;
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
