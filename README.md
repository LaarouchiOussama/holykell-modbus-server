# Modbus RTU Server

A server application capable of communicating with a serial port that
converts RS485 (such as [Serial Server H305](https://www.holykell.com/products/Serial_Server_H305.html))

- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installing](#installing)
- [Configuring the environment](#configuring-the-environment)
- [Database migration](#database-migration)
- [Dashboard](#dashboard)
- [Using Docker](#using-docker)
- [Dependencies](#dependencies)
- [License](#license)

## Getting Started

These instructions will give you a copy of the project up and running on
your local machine for development and testing purposes. See deployment
for notes on deploying the project on a live system.

### Prerequisites

This project is written in Java, uses Maven as a build tool and MySQL/MariaDB as a database
- [Setup JDK for development](https://www.geeksforgeeks.org/download-and-install-java-development-kit-jdk-on-windows-mac-and-linux/)
- [Setup Maven](https://maven.apache.org/guides/getting-started/windows-prerequisites.html)
- [Setup MySQL](https://overiq.com/installing-mysql-windows-linux-and-mac/)

### Installing

If you're only interested in docker, please scroll to [Using Docker](#using-docker)

Clone this repository

    git clone https://github.com/Hathoute/modbus-server

Switch to project directory

    cd modbus-server

Resolve and download dependencies 

    mvn dependency:resolve

Create server jar (skipping tests for now)

    mvn install -DskipTests

Before continuing, please configure your environment (port, database, ...) by following
[Configuring the environment](#configuring-the-environment).

Start the server

    java -jar target/modbus-server-2.2.2.jar

Upon launching the server, if the database does not exist, the server creates one.

## Configuring the environment

You can easily configure the server without having to alter any java file.
The `properties` file is located under `./src/main/resources/modbus-server.properties`

| Property                          |                           Documentation                           |
|:----------------------------------|:-----------------------------------------------------------------:|
| server.port                       |               Server port for devices to connect to               |
| server.use_rtu_over_tcp           |          Use RTU over TCP when communicating with modbus          |
| database.hostname                 |                         Database hostname                         |
| database.port                     |                           Database port                           |
| database.user                     |                         Database username                         |
| database.password                 |                         Database password                         |
| database.database_name            |                           Database name                           |
| database.synchronization_interval | Time between checks for new/modified devices/metrics (in seconds) |
| debug.show_value                  | Display metric value when reading registers, useful for debugging |

You can also use `environment variables` to set these properties, which the program
prioritizes.

| Property                          | Environment variable              |
|:----------------------------------|:----------------------------------|
| server.port                       | SERVER_PORT                       |
| server.use_rtu_over_tcp           | SERVER_USE_RTU_OVER_TCP           |
| database.hostname                 | DATABASE_HOSTNAME                 |
| database.port                     | DATABASE_PORT                     |
| database.user                     | DATABASE_USER                     |
| database.password                 | DATABASE_PASSWORD                 |
| database.database_name            | DATABASE_DATABASE_NAME            |
| database.synchronization_interval | DATABASE_SYNCHRONIZATION_INTERVAL |
| debug.show_value                  | DEBUG_SHOW_VALUE                  |

## Database migration

When updating to a newer version of this project, you must ensure that the database structure is the 
is also updated. Therefore, you have to run database migrations to update your database.

Please make sure to **back up your database** before running any migration, so that no data will be lost.

    java -jar modbus-server.jar run_migrations current_version target_version

For example, if one would want to update from **1.1.0** to **2.0.0** :

    java -jar modbus-server.jar run_migrations 1.1.0 2.0.0

## Dashboard

This project comes with a [Node-RED](https://nodered.org/) dashboard that you can import to
easily manage devices and view metrics: [Modbus Node-RED flow](node-red-flow.json)

## Using Docker

You can find a [Dockerfile](Dockerfile) in the root directory 
of this project to build a docker image, and an example of a [docker-compose.yml](docker-compose.yml)
to run this project.

## Dependencies

This project is using a modified version of [j2mod modbus library](https://github.com/steveohara/j2mod)
that enables acting as a server on top of being able to query data as a client/master.

## License

This project is licensed under [Apache License 2.0](LICENSE) - see the [LICENSE.md](LICENSE) file for
details