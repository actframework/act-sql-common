package act.db.sql.datasource;

import act.app.DbServiceManager;
import act.db.DbService;
import act.db.sql.DataSourceConfig;
import act.db.sql.DataSourceProvider;
import act.db.sql.SqlDbService;
import org.osgl.$;
import org.osgl.util.E;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Allow it share the datasource from another DB service
 */
public class SharedDataSourceProvider extends DataSourceProvider {

    public DataSource ds;
    public DataSourceConfig dsConf;

    /**
     * Construct `SharedDataSourceProvider` with the ID of the db service
     * whose datasource will be shared
     * @param dbId the ID of the db service
     */
    public SharedDataSourceProvider(String dbId, DbServiceManager dbm) {
        DbService db = dbm.dbService(dbId);
        E.invalidConfigurationIf(null == db, "Cannot find db service: %s", dbId);
        E.invalidConfigurationIf(!(db instanceof SqlDbService), "DB service is not a SQL DB service: %s", dbId);
        SqlDbService sql = $.cast(db);
        ds = sql.dataSource();
        dsConf = sql.dataSourceConfig();
        E.illegalStateIf(null == ds, "Datasource is not initialized in DB service: %s", dbId);
    }

    @Override
    public DataSource createDataSource(DataSourceConfig conf) {
        return ds;
    }

    public DataSourceConfig dataSourceConfig() {
        return dsConf;
    }

    @Override
    public Map<String, String> confKeyMapping() {
        throw E.unsupport();
    }
}
