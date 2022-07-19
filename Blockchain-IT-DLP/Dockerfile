FROM openjdk:17

WORKDIR /app

RUN echo "security.provider.13=org.bouncycastle.jce.provider.BouncyCastleProvider" >> /usr/java/openjdk-17/conf/security/java.security

COPY target/Blockchain-IT-DLP-1.0-jar-with-dependencies.jar /app/app.jar
COPY bft-smart/lib/ /app/lib

ENTRYPOINT ["java", "--class-path", "app.jar:lib/*"]