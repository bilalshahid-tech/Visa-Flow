# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy root pom and visaflow-backend files
COPY pom.xml ./
COPY visaflow-backend/ ./visaflow-backend/

RUN mvn -pl visaflow-backend package -DskipTests --no-transfer-progress

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN useradd -r -s /bin/false visaflow

COPY --from=build /app/visaflow-backend/target/*.jar app.jar

RUN mkdir -p /app/uploads && chown -R visaflow:visaflow /app
VOLUME ["/app/uploads"]

USER visaflow

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
