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

import act.db.sql.DataSourceConfig;
import act.db.sql.DataSourceProvider;
import act.db.sql.monitor.DataSourceStatus;
import com.alibaba.druid.pool.DruidDataSource;
import org.osgl.$;
import org.osgl.util.C;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.sql.DataSource;

/**
 * Provide Druid datasource
 */
public class DruidDataSourceProvider extends DataSourceProvider {

    private Set<DruidDataSource> created = new HashSet<>();

    @Override
    public DataSource createDataSource(DataSourceConfig conf) {
        DruidDataSource source = new DruidDataSource();
        source.setUrl(conf.url);
        source.setUsername(conf.username);
        source.setPassword(conf.password);
        source.setDriverClassName(conf.driver);
        source.setDefaultTransactionIsolation(conf.isolationLevel);
        source.setInitialSize(conf.minConnections);
        source.setMaxActive(conf.maxConnections);
        source.setMaxWait(conf.connectionTimeout * 1000);
        source.setDefaultAutoCommit(conf.autoCommit);
        source.setDefaultReadOnly(conf.readOnly);

        Properties prop = new Properties();
        prop.putAll(conf.customProperties);
        source.configFromPropety(prop);
        created.add(source);
        return source;
    }

    @Override
    public Map<String, String> confKeyMapping() {
        return C.Map("minIdle", "minConnections",
                "maxActive", "maxConnections",
                "maxWait", "waitTimeout",
                "validationQuery", "heartbeatSql",
                "maxPoolPreparedStatementPerConnectionSize", "pstmtCacheSize"
        );
    }

    @Override
    public DataSourceStatus getStatus(DataSource ds) {
        DruidDataSource dds = $.cast(ds);
        return DataSourceStatus.create()
                .activeConnections(dds.getActiveCount())
                .idleConnections(dds.getPoolingCount())
                .waitingThreads(dds.getWaitThreadCount());
    }

    @Override
    protected void releaseResources() {
        for (DruidDataSource ds : created) {
            release(ds);
        }
        created.clear();
        super.releaseResources();
    }

    private void release(DruidDataSource ds) {
        ds.close();
    }
}
