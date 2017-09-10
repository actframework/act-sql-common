# act-sql-common

[![APL v2](https://img.shields.io/badge/license-Apache%202-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html) 
[![Maven Central](https://img.shields.io/maven-central/v/org.actframework/act-sql-common.svg)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22act-sql-common%22)
[![Build Status](https://travis-ci.org/actframework/act-sql-common.svg?branch=master)](https://travis-ci.org/actframework/act-sql-common)
[![codecov](https://codecov.io/gh/actframework/act-sql-common/branch/master/graph/badge.svg)](https://codecov.io/gh/actframework/act-sql-common)
[![Javadocs](http://www.javadoc.io/badge/org.actframework/act-sql-common.svg?color=blue)](http://www.javadoc.io/doc/org.actframework/act-sql-common)

Common module for SQL database plugin

## Versions

| *Actframework version* | *act-sql-common version* |
| ---------------------- | ------------------------ |
| 1.1.0 | 1.0.0 |
| 1.1.1 | 1.0.1 |

## Configurations

### 1. Data source configuration

#### `url` or `jdbcUrl` or `databaseUrl`

Specifies the jdbc url of the datasource. 

Default value: `jdbc:h2:./test` if h2 jdbc driver is provided, otherwise it will break the application from bootstrap.

#### `driver` or `jdbcDriver` or `databaseDriver`

Optionally. Specifies the jdbc driver class. If not specified the framework will infer the driver class from the `url`

#### `username`

Specifies the jdbc connection username

Default value: `sa`

#### `password`

Specifies the jdbc connection password

Default value: empty string

#### `datasource` or `datasource.provider` 

Specifies the `DataSourceProvider` implementation.

About value and default value: 

1. if `hikari` found in the value then it will use the `act.db.sql.datasource.HikariDataSourceProvider`
1. otherwise if `druid` found in the value then it will use the `act.db.sql.datasource.DruidDataSourceProvider`
1. otherwise if the value starts with `shared:` then it will try to extract the `dbId` from the value, e.g. if value is `shared:ds1` then the `dbId` is `ds1`, after that it will try to load the db service with id `ds1` and use the datasource of that db service and feed into the current db service being configured. This allows multiple db service share the same datasource instance. **Note** app must make sure the db service been referenced has already configured and the db service must be an implementation of `SqlDbService`. If a datasource is configured as shared datasource then it does not need to configure other datasource properties e.g. url, username etc. However the `naming.convention` still needs to be configured to make the db service able to mapping between table/column and class/field.
1. otherwise if specified then it will try to load the `DataSourceProvider` instance by using `Act.getInstance(<value>)`
1. if not specified then system will probe the class loader by try loading `com.zaxxer.hikari.HikariConfig` or `com.alibaba.druid.pool.DruidDataSource` and load the hikari CP provider or druid provider respectively

#### `autoCommit`

Specify should datasource auto commit transaction

Default value: `false`

#### `minConnections`

Specify the minimum connections should be created by data source

Default value: `2`
 
#### `maxConnections`

Specify the maximum connections can be created by data source

Default value: `10`

#### `isolationLevel`

Specifies the default transaction isolation level, could be one of the following:

1. `NONE` - no transaction
1. `READ_UNCOMMITTED`
1. `READ_COMMITTED`
1. `REPEATABLE_READ`
1. `SERIALIZABLE`

Default value: `READ_COMMITTED`

####  `naming.convention`

Specify the naming convention to map Entity class name to table name and the property name to column name
 
Supported values:

1. `matching` - map directly (could be case insensitive)
1. `underscore` - convert camelcase to underscore notation

Default value: `matching`

### 2. DDL configuration

The ddl configuration tells the framework whether or not to generate and run DDL scripts

#### `ddl.create`

Specify should framework to generate and run create table DDL
 
Default value: `false` unless `h2` is used as database

**Note** about `h2`: if using h2 anbd the database file exists then `ddl.create` will always be treated as `false` unless `ddl.drop` is specified as `true`

#### `ddl.drop`

Specify should framework to generate and run drop table DDL
 
Default value: `false`