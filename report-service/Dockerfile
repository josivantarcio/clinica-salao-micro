FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/report-service-1.0.0.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
