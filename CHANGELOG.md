# act-sql-common CHANGE LOG

1.6.1
* Default URL conflict with multiple data sources #28

1.6.0 - 03/Nov/2019
* update to act-1.8.29

1.5.1 - 30/Sep/2018
* update to act-1.8.28
* Tune jobId for all jobs

1.5.0 - 2/Jul/2018
* update to act-1.8.25
* Allow inject SqlDbService and DataSource #23

1.4.6 - 16/Jun/2019
* Error starting with db.default.datasource.provider configuration #22

1.4.5 - 20/Apr/2019
* update to act-1.8.20

1.4.4
* update to act-1.8.9
* Update SqlDbService init logic #18
* update SqlDbService for
* Make TX follow JPA `TxType.REQUIRED` semantic #17
* Apply new scope for implicit transaction #16

1.4.3 - 30/Oct/2018
* update to act-1.8.8

1.4.2 - 19/Jun/2018
* `TxInfo` - exitTxScope logic issue #15
* update to act-1.8.8-RC10
* update druid to 1.1.10

1.4.1 - 7/Jun/2018

* update to act-1.8.8-RC9
* `SqlDbPlugin.applyTo(App)` issue #14

1.4.0 - 29/May/2018

* It shall ignore the Exception raised by data source during shutdown process. #12

1.3.4 - 15/May/2018
* Support auto setup driver for MariaDB #10
* update act to 1.8.8-RC5

1.3.3 - 02/Apr/2018
* update act to 1.8.5

1.3.2 - 25/Mar/2018
* Update act to 1.8.2
* Update druid to 1.1.9

1.3.1 - 11/Mar/2018
* merge from 1.2.1 for `HikariCP.setConnectionInitSql`

1.3.0
* Update to act-1.7.x
* Update to JPA 2.2
* Add JTA into dependency

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
