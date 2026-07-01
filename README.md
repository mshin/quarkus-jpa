# quarkus-jpa

This service is an example of providing a one-at-a-time flow of transactions through the service.

A `ReentrantLock` is used to provide this, locking before the Quarkus transaction starts.

## Before Running
In order to get tests to run, must execute these commands 1st to get testcontainers to work with podman:
```
podman stop --all
podman rm --all --volumes
podman system prune -a -f

podman machine stop
podman system reset -f
podman machine start
```
```
podman machine init
podman machine set --rootful
podman machine set --memory 4096  # 4GB
podman machine start
podman machine ssh -- sudo systemctl enable --now podman.socket
export DOCKER_HOST=unix://$(podman machine inspect --format '{{.ConnectionInfo.PodmanSocket.Path}}')
export TESTCONTAINERS_RYUK_DISABLED=true
```

## Demos

### 01 Database Access and Transactions

### 02 Healthchecks, testing, and container building

#### Healthchecks

Quarkus provides (through `quarkus-smallrye-health`) the ability to introduce a standardized healthcheck.

Steps:

1. Start Quarkus (in dev mode): `./mvnw quarkus:dev`
2. In browser, goto `http://localhost:8080/q/health` (or `curl http://localhost:8080/q/health | jq`)
3. See the healthceck
4. Also note liveness, readiness;
    - `http://localhost:8080/q/health/live`
    - `http://localhost:8080/q/health/ready`
5. See "Custom Health Check", defined by [LivenessHealthCheck](src/main/java/org/acme/health/LivenessHealthCheck.java)

#### Testing

Quarkus comes out of the box with a wide array of robust testing tools and supported methodologies

Steps:

1. Observe [EntityMutexTest](src/test/java/org/acme/EntityMutexTest.java); fairly standard testing setup. Things to point out:
    - `@Quarkustest`
    - `@TestHTTPEndpoint(EntityCrud.class)`
    - `testList()`, specifically the way we call / test the endpoint
2. Run the tests `./mvnw clean verify`
   - During, watch output and `watch docker ps`
3. Run the integration tests `./mvnw clean verify -Dquarkus.container-image.build=true -DskipITs=false`
   - Also keep watching `watch docker ps`


# Quarkus Stuff

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

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

You can then execute your native executable with: `./target/mutex-test-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- Observability ([guide](https://quarkus.io/guides/observability-devservices-lgtm)): Serve and consume Observability Dev
  Services
- Hibernate ORM with Panache ([guide](https://quarkus.io/guides/hibernate-orm-panache)): Simplified JPA/Hibernate data
  access layer with active record and repository patterns
- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC
- Micrometer metrics ([guide](https://quarkus.io/guides/micrometer)): Instrument the runtime and your application with
  dimensional metrics using Micrometer.

## Provided Code

### Hibernate ORM

Create your first JPA entity

[Related guide section...](https://quarkus.io/guides/hibernate-orm)

[Related Hibernate with Panache section...](https://quarkus.io/guides/hibernate-orm-panache)

### LGTM Observability

Create your first LGTM Observability application

[Related guide section...](https://quarkus.io/guides/observability-devservices-lgtm)
