FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/ledgerview-0.0.1.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx384m", "-jar", "app.jar"]
