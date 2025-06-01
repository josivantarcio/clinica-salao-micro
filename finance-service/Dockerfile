FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR /app
COPY ./pom.xml /app
COPY ./src /app/src
RUN apk add --no-cache maven && \
    mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/finance-service-1.0.0.jar /app/finance-service.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "/app/finance-service.jar"]
