# spectrum-app

Go back to the [Main README](../README.md#how-to-run-locally).

This project uses Quarkus, the Supersonic Subatomic Java Framework.

## Application configuration

[AppConfig](src/main/java/edu/ukma/ssdm/teamspectrum/config/AppConfig.java) is an immutable record created and validated at startup by
[AppConfigProducer](src/main/java/edu/ukma/ssdm/teamspectrum/config/AppConfigProducer.java). It contains `JwtConfig` and `DbConfig`; the latter contains
`RetryConfig`. All three nested records are also available for constructor injection:

```java
import edu.ukma.ssdm.teamspectrum.config.AppConfig.JwtConfig;

public TokenFactoryImpl(JwtConfig config) {
    this.config = config;
}
```

JWT lifetime and database timeouts use `PositiveDuration`. Retry delay, jitter and
maximum delay use `NonNegativeDuration`, allowing zero. Unwrap with `.value()` when
calling APIs that accept `Duration`; `DbConfig.queryTimeoutSeconds()` converts the
query timeout for jOOQ. JWT lifetime and query timeout must be at least one second;
the transaction timeout must fit PostgreSQL's integer millisecond range.

Configure values in [application.properties](src/main/resources/application.properties) under `app.jwt.*`
and `app.db.*`, or override them through Quarkus configuration sources. For example,
`APP_DB_QUERY_TIMEOUT_SECONDS=8` and `APP_DB_RETRY_MAX_RETRIES=2` override their
defaults. The JWT secret still comes from `JWT_SECRET`.

Defaults are a 15-minute JWT lifetime, a 5-second query timeout, a 10-second
transaction timeout, and three retries with a 200 ms initial delay, 100 ms jitter,
and exponential backoff (factor 2, capped at 1000 ms).


## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/spectrum-app-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

