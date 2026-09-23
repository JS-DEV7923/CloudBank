FROM eclipse-temurin:21-jre

WORKDIR /app
COPY target/cloudbank-api-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
