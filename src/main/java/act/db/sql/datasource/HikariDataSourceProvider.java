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

import static java.sql.Connection.*;

import act.db.sql.DataSourceConfig;
import act.db.sql.DataSourceProvider;
import act.db.sql.monitor.DataSourceStatus;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.osgl.$;
import org.osgl.util.C;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;

/**
 * Provide HikariCP data source
 */
public class HikariDataSourceProvider extends DataSourceProvider {

    private Set<HikariDataSource> created = new HashSet<>();

    @Override
    public DataSource createDataSource(DataSourceConfig conf) {
        HikariConfig hc = new HikariConfig();
        if (logger.isTraceEnabled()) {
            logger.trace("creating HikariCP data source ...");
            logger.trace("url: %s", conf.url);
            logger.trace("driver: %s", conf.driver);
            logger.trace("max conn: %s", conf.maxConnections);
            logger.trace("min conn: %s", conf.minConnections);
            logger.trace("autoCommit: %s", conf.autoCommit);
        }
        hc.setTransactionIsolation(isolationLevelLookup.get(conf.isolationLevel));
        hc.setJdbcUrl(conf.url);
        hc.setUsername(conf.username);
        hc.setPassword(conf.password);
        hc.setDriverClassName(conf.driver);
        hc.setMaximumPoolSize(conf.maxConnections);
        int minConn = conf.minConnections;
        if (minConn != DataSourceConfig.DEF_MIN_CONN) {
            // Only set min connection when it is not the default value
            // because HikariCP recommend not to set this value by default
            hc.setMinimumIdle(minConn);
        }
        hc.setConnectionTimeout(conf.waitTimeoutMillis);
        hc.setAutoCommit(conf.autoCommit);
        hc.setConnectionTestQuery(conf.heartbeatSql);

        Map<String, String> miscConf = conf.customProperties;
        String s = miscConf.get("idleTimeout");
        if (null != s) {
            int n = Integer.parseInt(s);
            hc.setIdleTimeout(n);
        } else {
            hc.setIdleTimeout(conf.maxInactiveTimeSecs * 1000);
        }

        s = miscConf.get("maxLifetime");
        if (null != s) {
            long n = Long.parseLong(s);
            hc.setMaxLifetime(n);
        } else {
            hc.setMaxLifetime(conf.maxAgeMinutes * 60 * 1000L);
        }

        s = miscConf.get("poolName");
        if (null != s) {
            hc.setPoolName(s);
        }

        HikariDataSource ds = new HikariDataSource(hc);
        created.add(ds);
        return ds;
    }

    @Override
    public Map<String, String> confKeyMapping() {
        return C.Map("jdbcUrl", "url",
                "maximumPoolSize", "maxConnections",
                "minimumIdle", "minConnections",
                "connectionTimeout", "waitTimeout"
        );
    }

    @Override
    public DataSourceStatus getStatus(DataSource ds) {
        HikariDataSource hds = $.cast(ds);
        HikariPoolMXBean mbean = hds.getHikariPoolMXBean();
        return DataSourceStatus.create()
                .activeConnections(mbean.getActiveConnections())
                .idleConnections(mbean.getIdleConnections())
                .totalConnections(mbean.getTotalConnections())
                .waitingThreads(mbean.getThreadsAwaitingConnection());
    }

    @Override
    protected void releaseResources() {
        for (HikariDataSource ds : created) {
            release(ds);
        }
        created.clear();
        super.releaseResources();
    }

    private void release(HikariDataSource ds) {
        ds.close();
    }

    private Map<Integer, String> isolationLevelLookup = C.Map(
            TRANSACTION_NONE, "TRANSACTION_NONE",
            TRANSACTION_READ_UNCOMMITTED, "TRANSACTION_READ_UNCOMMITTED",
            TRANSACTION_READ_COMMITTED, "TRANSACTION_READ_COMMITTED",
            TRANSACTION_REPEATABLE_READ, "TRANSACTION_REPEATABLE_READ",
            TRANSACTION_SERIALIZABLE, "TRANSACTION_SERIALIZABLE"
    );
}
