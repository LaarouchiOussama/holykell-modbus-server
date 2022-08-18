# syntax=docker/dockerfile:1
FROM maven:3.5-jdk-8 AS build
WORKDIR /app
ADD . /app/
RUN mvn dependency:resolve
RUN mvn install -DskipTests 

FROM openjdk:8-jdk-alpine
COPY --from=build /app/target/modbus-server-1.0-SNAPSHOT.jar ./modbus-server.jar
EXPOSE 6651
ENTRYPOINT ["java", "-jar", "modbus-server.jar"]
