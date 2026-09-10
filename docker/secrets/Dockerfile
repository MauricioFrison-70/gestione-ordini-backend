FROM alpine:3.22

LABEL org.opencontainers.image.source="https://github.com/MauricioFrison-70/gestione-ordini-backend" \
      org.opencontainers.image.description="Generatore locale di credenziali per la demo Gestione Ordini"

COPY --chmod=755 genera-segreti.sh /usr/local/bin/genera-segreti

ENTRYPOINT ["/usr/local/bin/genera-segreti"]
