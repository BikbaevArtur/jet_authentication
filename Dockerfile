FROM maven:3.9.4-eclipse-temurin-21 as build

COPY src src
COPY pom.xml pom.xml


RUN mvn clean package -DskipTests=true

FROM bellsoft/liberica-openjdk-debian:21

RUN adduser --system authorization_service && addgroup --system authorization_service && adduser user-service authorization_service
USER authorization_service

WORKDIR /app

COPY --from=build target/authorization_service-0.0.1-SNAPSHOT.jar ./application.jar

ENTRYPOINT ["java", "-jar", "application.jar"]
