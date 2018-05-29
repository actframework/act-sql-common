package act.db.sql;

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

import static act.db.sql.SqlDbService.DUMB_STATUS;

import act.db.sql.monitor.DataSourceStatus;
import act.util.DestroyableBase;
import org.osgl.$;
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;
import org.osgl.util.C;

import java.util.Map;
import javax.sql.DataSource;

public abstract class DataSourceProvider extends DestroyableBase {

    public static final DataSourceProvider NULL_PROVIDER = new DataSourceProvider() {
        @Override
        public DataSource createDataSource(DataSourceConfig conf) {
            return null;
        }

        @Override
        public Map<String, String> confKeyMapping() {
            return C.Map();
        }

        @Override
        public DataSourceStatus getStatus(DataSource ds) {
            return DUMB_STATUS;
        }
    };

    protected $.Function<DataSourceProvider, ?> initializationCallback;

    protected final Logger logger = LogManager.get(getClass());

    /**
     * Create datasource from configuration map
     * @param conf the data source configuration
     * @return a DataSource instance
     */
    public abstract DataSource createDataSource(DataSourceConfig conf);

    /**
     * Provide a mapping to bridge the default configuration into solution specific
     * configuration. E.g. HikariCP use `jdbcUrl` instead of `url`
     * @return the conf key mapping
     */
    public abstract Map<String, String> confKeyMapping();

    /**
     * Returns the current status
     * @param ds the datasource (created by this datasource provider)
     * @return the current status of the datasource
     */
    public abstract DataSourceStatus getStatus(DataSource ds);

    /**
     * Report if this data source provider has been initialized
     * @return `true` if the data source provider has been initialized
     */
    public boolean initialized() {
        return true;
    }

    public void setInitializationCallback($.Function<DataSourceProvider, ?> callback) {
        this.initializationCallback = $.requireNotNull(callback);
        if (initialized()) {
            callback.apply(this);
        }
    }

}
