# Migration script from v1.0-SNAPSHOT to v1.1
# Must be executed if willing to update from v1.0 to any newer version

alter table metrics
    add unit varchar(255) not null;