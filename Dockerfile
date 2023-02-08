FROM openjdk:17
ARG RELEASE_VERSION

WORKDIR /app

ADD target/modbus-server-2.4.1-trace.jar ./modbus-server.jar

ENTRYPOINT ["java", "-jar", "modbus-server.jar"]
EXPOSE 6651
