:ON ERROR EXIT

USE master;
GO

IF DB_ID(N'ProjectJava') IS NULL
BEGIN
    CREATE DATABASE ProjectJava;
END;
GO

DECLARE @PasswordApp NVARCHAR(128) = N'$(APP_DB_PASSWORD)';
DECLARE @PasswordReporting NVARCHAR(128) = N'$(REPORTING_DB_PASSWORD)';

IF @PasswordApp = N'$' + N'(APP_DB_PASSWORD)' OR LEN(@PasswordApp) < 16
BEGIN
    ;THROW 55001, N'Password applicativa non valida.', 1;
END;

IF @PasswordReporting = N'$' + N'(REPORTING_DB_PASSWORD)'
   OR LEN(@PasswordReporting) < 16
BEGIN
    ;THROW 55002, N'Password reporting non valida.', 1;
END;

DECLARE @SqlLoginApp NVARCHAR(1200);
IF SUSER_ID(N'gestione_ordini_app') IS NULL
BEGIN
    SET @SqlLoginApp =
        N'CREATE LOGIN gestione_ordini_app WITH PASSWORD = '
        + QUOTENAME(@PasswordApp, N'''')
        + N', CHECK_POLICY = ON, CHECK_EXPIRATION = OFF, '
        + N'DEFAULT_DATABASE = ProjectJava;';
END
ELSE
BEGIN
    SET @SqlLoginApp =
        N'ALTER LOGIN gestione_ordini_app WITH PASSWORD = '
        + QUOTENAME(@PasswordApp, N'''')
        + N', CHECK_POLICY = ON, CHECK_EXPIRATION = OFF, '
        + N'DEFAULT_DATABASE = ProjectJava;';
END;
EXEC sys.sp_executesql @SqlLoginApp;

DECLARE @SqlLoginReporting NVARCHAR(1200);
IF SUSER_ID(N'gestione_ordini_report') IS NULL
BEGIN
    SET @SqlLoginReporting =
        N'CREATE LOGIN gestione_ordini_report WITH PASSWORD = '
        + QUOTENAME(@PasswordReporting, N'''')
        + N', CHECK_POLICY = ON, CHECK_EXPIRATION = OFF, '
        + N'DEFAULT_DATABASE = ProjectJava;';
END
ELSE
BEGIN
    SET @SqlLoginReporting =
        N'ALTER LOGIN gestione_ordini_report WITH PASSWORD = '
        + QUOTENAME(@PasswordReporting, N'''')
        + N', CHECK_POLICY = ON, CHECK_EXPIRATION = OFF, '
        + N'DEFAULT_DATABASE = ProjectJava;';
END;
EXEC sys.sp_executesql @SqlLoginReporting;
GO

USE ProjectJava;
GO

IF DATABASE_PRINCIPAL_ID(N'gestione_ordini_app') IS NULL
BEGIN
    CREATE USER gestione_ordini_app FOR LOGIN gestione_ordini_app
        WITH DEFAULT_SCHEMA = dbo;
END;

IF IS_ROLEMEMBER(N'db_datareader', N'gestione_ordini_app') <> 1
    ALTER ROLE db_datareader ADD MEMBER gestione_ordini_app;

IF IS_ROLEMEMBER(N'db_datawriter', N'gestione_ordini_app') <> 1
    ALTER ROLE db_datawriter ADD MEMBER gestione_ordini_app;

IF IS_ROLEMEMBER(N'db_ddladmin', N'gestione_ordini_app') <> 1
    ALTER ROLE db_ddladmin ADD MEMBER gestione_ordini_app;

GRANT VIEW DEFINITION TO gestione_ordini_app;

IF DATABASE_PRINCIPAL_ID(N'gestione_ordini_report') IS NULL
BEGIN
    CREATE USER gestione_ordini_report FOR LOGIN gestione_ordini_report
        WITH DEFAULT_SCHEMA = reporting;
END;
GO
