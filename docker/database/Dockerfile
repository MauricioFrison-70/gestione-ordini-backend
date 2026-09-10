FROM mcr.microsoft.com/mssql/server:2022-latest

LABEL org.opencontainers.image.source="https://github.com/MauricioFrison-70/gestione-ordini-backend" \
      org.opencontainers.image.description="SQL Server con struttura e dati dimostrativi di Gestione Ordini"

USER root

RUN mkdir -p /opt/gestione-ordini/scripts/reporting \
    /opt/gestione-ordini/scripts/ai

COPY --chmod=755 docker/database/sqlserver-entrypoint.sh /opt/gestione-ordini/sqlserver-entrypoint.sh
COPY --chmod=755 docker/database/inizializza-database.sh /opt/gestione-ordini/inizializza-database.sh
COPY --chmod=755 docker/database/finalizza-database.sh /opt/gestione-ordini/finalizza-database.sh
COPY docker/database/sql/000_crea_database_utenti.sql /opt/gestione-ordini/scripts/000_crea_database_utenti.sql
COPY docker/database/sql/010_configura_utente_rapporti.sql /opt/gestione-ordini/scripts/010_configura_utente_rapporti.sql
COPY docker/database/sql/020_dati_demo.sql /opt/gestione-ordini/scripts/020_dati_demo.sql
COPY src/main/resources/db/reporting/ /opt/gestione-ordini/scripts/reporting/
COPY src/main/resources/db/ai/ /opt/gestione-ordini/scripts/ai/

RUN chown -R mssql:root /opt/gestione-ordini \
    && chmod -R g=u /opt/gestione-ordini

USER mssql

ENTRYPOINT ["/opt/gestione-ordini/sqlserver-entrypoint.sh"]
CMD ["/opt/mssql/bin/sqlservr"]
