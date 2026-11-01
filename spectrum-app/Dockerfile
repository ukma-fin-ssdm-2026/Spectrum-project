FROM quay.io/quarkus/ubi10-quarkus-mandrel-builder-image:jdk-25 AS build
WORKDIR /project
COPY --chown=quarkus:quarkus . .
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B package -Dnative -DskipTests

FROM quay.io/quarkus/ubi10-quarkus-micro-image:2.0
WORKDIR /work
COPY --from=build --chown=1001:root --chmod=755 /project/target/*-runner /work/application
EXPOSE 10000
USER 1001
ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0"]
