FROM openjdk:17

WORKDIR /app

COPY target/IT-DLP-1.0-jar-with-dependencies.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-cp", "app.jar"]