# syntax=docker/dockerfile:1

FROM maven:3.9.16-eclipse-temurin-17-noble AS build

WORKDIR /workspace

COPY pom.xml ./
COPY src ./src
COPY docs/ai ./docs/ai
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests package

FROM eclipse-temurin:17-jre-jammy AS runtime

LABEL org.opencontainers.image.source="https://github.com/MauricioFrison-70/gestione-ordini-backend" \
      org.opencontainers.image.description="Backend Spring Boot del sistema dimostrativo Gestione Ordini"

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 app \
    && useradd --uid 10001 --gid app --no-create-home --shell /usr/sbin/nologin app

WORKDIR /app

COPY --from=build --chown=app:app /workspace/target/gestioneOrdini-*.jar ./app.jar
COPY --chown=app:app --chmod=755 docker/backend/docker-entrypoint.sh ./docker-entrypoint.sh

USER app
EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl --fail --silent --show-error http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["/app/docker-entrypoint.sh"]
