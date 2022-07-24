# syntax=docker/dockerfile:1

FROM openjdk:8

WORKDIR /app

ADD target/modbus-server-1.0-SNAPSHOT.jar ./modbus-server.jar

ENTRYPOINT ["java", "-jar", "modbus-server.jar"]
EXPOSE 6651
