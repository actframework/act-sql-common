package act.db.sql.datasource;

import act.db.sql.DataSourceConfig;
import act.db.sql.DataSourceProvider;
import act.db.sql.monitor.DataSourceStatus;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.osgl.$;
import org.osgl.util.C;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        return C.map("jdbcUrl", "url",
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
}
