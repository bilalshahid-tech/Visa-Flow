FROM eclipse-temurin:17-jdk-alpine as builder
WORKDIR /app
# We assume maven is used, let's copy the pom and src. It will require maven to build.
# A simpler approach for the Dockerfile is just to package outside and copy the jar, 
# or use a maven image. Let's use maven to build it.
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/api-gateway-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
