package act.db.sql.inject;

/*-
 * #%L
 * ACT SQL Common Module
 * %%
 * Copyright (C) 2015 - 2019 ActFramework
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

import act.Act;
import act.app.App;
import act.db.DB;
import act.db.sql.DataSourceProvider;
import act.db.sql.SqlDbService;
import org.osgl.inject.Genie;
import org.osgl.inject.NamedProvider;

import javax.inject.Provider;
import javax.sql.DataSource;

public class SqlDbProviders {

    private static Provider<SqlDbService> SQL_DB_SVC_PROVIDER = new Provider<SqlDbService>() {
        @Override
        public SqlDbService get() {
            return NAMED_SQL_DB_SVC_PROVIDER.get(DB.DEFAULT);
        }
    };

    private static NamedProvider<SqlDbService> NAMED_SQL_DB_SVC_PROVIDER = new NamedProvider<SqlDbService>() {
        @Override
        public SqlDbService get(String name) {
            return Act.app().dbServiceManager().dbService(name);
        }
    };

    private static Provider<DataSource> DATA_SRC_PROVIDER = new Provider<DataSource>() {
        @Override
        public DataSource get() {
            return NAMED_DATA_SRC_PROVIDER.get(DB.DEFAULT);
        }
    };

    private static NamedProvider<DataSource> NAMED_DATA_SRC_PROVIDER = new NamedProvider<DataSource>() {
        @Override
        public DataSource get(String name) {
            SqlDbService sqlDbService = NAMED_SQL_DB_SVC_PROVIDER.get(name);
            return sqlDbService.dataSource();
        }
    };

    private static Provider<DataSourceProvider> DATA_SRC_PROVIDER_PROVIDER = new Provider<DataSourceProvider>() {
        @Override
        public DataSourceProvider get() {
            return NAMED_DATA_SRC_PROVIDER_PROVIDER.get(DB.DEFAULT);
        }
    };

    private static NamedProvider<DataSourceProvider> NAMED_DATA_SRC_PROVIDER_PROVIDER = new NamedProvider<DataSourceProvider>() {
        @Override
        public DataSourceProvider get(String name) {
            SqlDbService sqlDbService = NAMED_SQL_DB_SVC_PROVIDER.get(name);
            return sqlDbService.dataSourceProvider();
        }
    };

    public static void classInit(App app) {
        Genie genie = app.getInstance(Genie.class);
        genie.registerProvider(SqlDbService.class, SQL_DB_SVC_PROVIDER);
        genie.registerNamedProvider(SqlDbService.class, NAMED_SQL_DB_SVC_PROVIDER);
        genie.registerProvider(DataSource.class, DATA_SRC_PROVIDER);
        genie.registerNamedProvider(DataSource.class, NAMED_DATA_SRC_PROVIDER);
        genie.registerProvider(DataSourceProvider.class, DATA_SRC_PROVIDER_PROVIDER);
        genie.registerNamedProvider(DataSourceProvider.class, NAMED_DATA_SRC_PROVIDER_PROVIDER);
    }

}
