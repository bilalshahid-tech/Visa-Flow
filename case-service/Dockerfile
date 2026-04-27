FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

COPY src src

RUN ./mvnw clean package -DskipTests

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "target/case-service-0.0.1-SNAPSHOT.jar"]
