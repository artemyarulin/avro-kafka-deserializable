### Build
FROM maven:3.6.0-jdk-8
# Specific commit with merged PR for CLI tools extension https://github.com/apache/avro/pull/366
# Once new version is released - switch to that
RUN git clone https://github.com/apache/avro && \
    cd avro && \
    git reset --hard aa5860fc355d7d44c3d567fdb1fb2d20ad474cc0 && \
    mvn clean install -DskipTests

### Test
FROM gradle:4.10.2-jre11-slim
USER root
COPY --from=0 /avro/lang/java/tools/target/avro-tools-*-SNAPSHOT.jar app.jar
COPY test test/
COPY templates /templates/
RUN java -jar app.jar compile -templateDir /templates/ schema test/schemas/*.json test/src/main/java && \
    cd test && \
    gradle test --no-daemon --console plain

### Bundle
FROM openjdk:8u181-jdk-alpine3.8
WORKDIR /app
COPY --from=0 /avro/lang/java/tools/target/avro-tools-*-SNAPSHOT.jar app.jar
COPY templates /templates/
CMD java -jar app.jar compile -string -templateDir /templates/  schema /srv/*.json /srv
