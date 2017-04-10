package act.db.sql.datasource;

import act.db.sql.DataSourceConfig;
import act.db.sql.DataSourceProvider;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.osgl.util.C;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Provide HikariCP data source
 */
public class HikariDataSourceProvider extends DataSourceProvider {

    @Override
    public DataSource createDataSource(DataSourceConfig conf) {
        HikariConfig hc = new HikariConfig();
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

        return new HikariDataSource(hc);
    }

    @Override
    public Map<String, String> confKeyMapping() {
        return C.map("jdbcUrl", "url",
                "maximumPoolSize", "maxConnections",
                "minimumIdle", "minConnections",
                "connectionTimeout", "waitTimeout"
        );
    }
}
