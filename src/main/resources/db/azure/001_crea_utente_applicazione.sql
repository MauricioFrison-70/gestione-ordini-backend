:ON ERROR EXIT

-- Eseguire con sqlcmd collegandosi direttamente al database Azure SQL.
-- La password viene fornita con: -v APP_DB_PASSWORD="..."
SET NOCOUNT ON;
GO

DECLARE @Password NVARCHAR(128) = N'$(APP_DB_PASSWORD)';

IF @Password = N'$' + N'(APP_DB_PASSWORD)'
   OR LEN(@Password) < 16
   OR LEN(@Password) > 128
BEGIN
    ;THROW 55201,
        N'Fornire APP_DB_PASSWORD con una lunghezza compresa tra 16 e 128 caratteri.',
        1;
END;

DECLARE @Sql NVARCHAR(1000);

IF DATABASE_PRINCIPAL_ID(N'gestione_ordini_app') IS NULL
BEGIN
    SET @Sql =
        N'CREATE USER [gestione_ordini_app] WITH PASSWORD = '
        + QUOTENAME(@Password, N'''')
        + N', DEFAULT_SCHEMA = [dbo];';
END
ELSE
BEGIN
    SET @Sql =
        N'ALTER USER [gestione_ordini_app] WITH PASSWORD = '
        + QUOTENAME(@Password, N'''')
        + N';';
END;

EXEC sys.sp_executesql @Sql;
GO

IF IS_ROLEMEMBER(N'db_datareader', N'gestione_ordini_app') <> 1
    ALTER ROLE db_datareader ADD MEMBER gestione_ordini_app;

IF IS_ROLEMEMBER(N'db_datawriter', N'gestione_ordini_app') <> 1
    ALTER ROLE db_datawriter ADD MEMBER gestione_ordini_app;

-- Necessario soltanto finché il profilo dimostrativo usa Hibernate ddl-auto=update.
IF IS_ROLEMEMBER(N'db_ddladmin', N'gestione_ordini_app') <> 1
    ALTER ROLE db_ddladmin ADD MEMBER gestione_ordini_app;

GRANT VIEW DEFINITION TO gestione_ordini_app;
GO
