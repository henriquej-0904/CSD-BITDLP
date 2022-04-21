FROM openjdk:17

WORKDIR /app

COPY target/IT-DLP-1.0-jar-with-dependencies.jar /app/app.jar
COPY bft-smart /app/bft-smart

EXPOSE 8080

ENTRYPOINT ["java", "--class-path", "app.jar:bft-smart/lib/*"]