# syntax=docker/dockerfile:1

FROM openjdk:17
ARG RELEASE_VERSION

WORKDIR /app

ADD target/modbus-server-${RELEASE_VERSION}.jar ./modbus-server.jar

ENTRYPOINT ["java", "-jar", "modbus-server.jar"]
EXPOSE 6651
