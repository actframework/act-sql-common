# act-sql-common CHANGE LOG

1.2.1
* Support `HikariCP.setConnectionInitSql` configuration #8

1.2.0
* update act to 1.6.0

1.1.3
* update act to 1.5.1
* update druid to 1.1.5
* update HikariCP to 2.7.3

1.1.2
* catch up to act-1.4.11
* improve maven build process
* update hikaricp and druid version

1.1.1
* fix for ebean2 issue: NPE raised starting app in prod mode when no third part datasource defined #7

1.1.0
* catch up to act-1.4.0

1.0.2
- Allow specific implementation to initialize in different logic when dataSourceProvider available or not #3 

1.0.1
- The default jdbc driver doesn't work with mysql jdbc driver 5.x #2 
- When it uses h2 with db on filesystem, it shall ignore the `ddl.create` if that file exists #1 

1.0.0 - baseline version
