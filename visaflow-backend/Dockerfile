# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and source code for standalone backend build
COPY pom.xml ./
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests --no-transfer-progress

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN useradd -r -s /bin/false visaflow

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /app/uploads && chown -R visaflow:visaflow /app
VOLUME ["/app/uploads"]

USER visaflow

EXPOSE 10000

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Djava.awt.headless=true", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:TieredStopAtLevel=1", \
  "-jar", "app.jar"]


