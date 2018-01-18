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
import org.osgl.util.E;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
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
        source.setMinIdle(conf.minConnections);
        source.setMaxWait(conf.waitTimeoutMillis);
        source.setValidationQuery(conf.heartbeatSql);
        source.setMaxPoolPreparedStatementPerConnectionSize(conf.pstmtCacheSize);

        Map<String, String> miscConf = conf.customProperties;
        String s = miscConf.get("initialSize");
        if (null != s) {
            source.setInitialSize(Integer.parseInt(s));
        } else {
            source.setInitialSize(source.getMinIdle());
        }

        s = miscConf.get("timeBetweenEvictionRunsMillis");
        if (null != s) {
            source.setTimeBetweenEvictionRunsMillis(Long.parseLong(s));
        }

        s = miscConf.get("minEvictableIdleTimeMillis");
        if (null != s) {
            source.setMinEvictableIdleTimeMillis(Long.parseLong(s));
        }

        s = miscConf.get("testWhileIdle");
        if (null != s) {
            source.setTestWhileIdle(Boolean.parseBoolean(s));
        }

        s = miscConf.get("testOnBorrow");
        if (null != s) {
            source.setTestOnBorrow(Boolean.parseBoolean(s));
        }

        s = miscConf.get("testOnReturn");
        if (null != s) {
            source.setTestOnReturn(Boolean.parseBoolean(s));
        }

        s = miscConf.get("filters");
        if (null != s) {
            try {
                source.setFilters(s);
            } catch (SQLException e) {
                throw E.unexpected(e);
            }
        }

        s = miscConf.get("poolPreparedStatements");
        if (null != s) {
            source.setPoolPreparedStatements(Boolean.parseBoolean(s));
        }

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
