# Build context: repository root (see deploy/docker-compose.yml).
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN mvn -B -ntp -DskipTests -pl wp-server -am package

FROM eclipse-temurin:17-jre
RUN groupadd --system wp && useradd --system --gid wp --home-dir /srv/webprotege wp \
    && mkdir -p /srv/webprotege && chown wp:wp /srv/webprotege
WORKDIR /app
COPY --from=build /workspace/wp-server/target/wp-server-*.jar /app/app.jar
USER wp
VOLUME ["/srv/webprotege"]
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
