FROM maven:3.8.4-openjdk-11-slim AS builder

WORKDIR /app

COPY ./pom.xml .
COPY ./src ./src

RUN mvn package -DskipTests

FROM openjdk:11-jre-slim

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]