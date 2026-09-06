:ON ERROR EXIT

USE ProjectJava;
GO

IF DATABASE_PRINCIPAL_ID(N'report_executor_role') IS NULL
BEGIN
    ;THROW 55101, N'Il ruolo report_executor_role non è installato.', 1;
END;

IF DATABASE_PRINCIPAL_ID(N'gestione_ordini_report') IS NULL
BEGIN
    ;THROW 55102, N'L''utente gestione_ordini_report non è installato.', 1;
END;

IF IS_ROLEMEMBER(N'report_executor_role', N'gestione_ordini_report') <> 1
BEGIN
    ALTER ROLE report_executor_role ADD MEMBER gestione_ordini_report;
END;

DENY INSERT, UPDATE, DELETE ON SCHEMA::reporting TO gestione_ordini_report;
DENY SELECT ON SCHEMA::dbo TO gestione_ordini_report;
GO
