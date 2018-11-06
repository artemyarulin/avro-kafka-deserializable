FROM maven:3.6.0-jdk-8
# Custom fork for now. PR to master is in progress https://github.com/apache/avro/pull/366
RUN git clone --single-branch -b avro-tools-template-dir https://github.com/artemyarulin/avro
RUN cd avro && mvn clean install -DskipTests

FROM openjdk:8u181-jdk-alpine3.8
WORKDIR /app
COPY --from=0 /avro/lang/java/tools/target/avro-tools-1.9.0-SNAPSHOT.jar app.jar
COPY templates /templates/
CMD java -jar app.jar compile -templateDir /templates/ schema /srv/*.json /srv
