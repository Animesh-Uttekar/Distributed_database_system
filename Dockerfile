FROM openjdk:17-slim-buster

ARG JAR_FILE=build/libs/distributeddb-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app.jar"]
