package act.db.sql.datasource;

/*-
 * #%L
 * ACT AAA Plugin
 * %%
 * Copyright (C) 2015 - 2017 ActFramework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import act.app.DbServiceManager;
import act.app.event.AppEventId;
import act.db.DbService;
import act.db.sql.DataSourceConfig;
import act.db.sql.DataSourceProvider;
import act.db.sql.SqlDbService;
import act.db.sql.monitor.DataSourceStatus;
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
    public DataSourceProvider realProvider;
    private boolean initialized;

    /**
     * Construct `SharedDataSourceProvider` with the ID of the db service
     * whose datasource will be shared
     * @param dbId the ID of the db service
     */
    public SharedDataSourceProvider(final String dbId, DbServiceManager dbm) {
        DbService db = dbm.dbService(dbId);
        E.invalidConfigurationIf(null == db, "Cannot find db service: %s", dbId);
        E.invalidConfigurationIf(!(db instanceof SqlDbService), "DB service is not a SQL DB service: %s", dbId);
        final SqlDbService sql = $.cast(db);
        dbm.app().jobManager().on(AppEventId.DB_SVC_LOADED, new Runnable() {
            @Override
            public void run() {
                ds = sql.dataSource();
                E.illegalStateIf(null == ds, "Datasource is not initialized in DB service: %s", dbId);
                dsConf = sql.dataSourceConfig();
                realProvider = sql.dataSourceProvider();
                initialized = true;
                if (null != initializationCallback) {
                    initializationCallback.apply(SharedDataSourceProvider.this);
                }
            }
        }, true);
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

    @Override
    public DataSourceStatus getStatus(DataSource ds) {
        return null != realProvider ? realProvider.getStatus(ds) : SqlDbService.DUMB_STATUS;
    }

    @Override
    public boolean initialized() {
        return initialized;
    }

}
