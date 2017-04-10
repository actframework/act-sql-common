package act.db.sql.datasource;

import act.db.sql.DataSourceConfig;
import act.db.sql.DataSourceProvider;
import com.alibaba.druid.pool.DruidDataSource;
import org.osgl.util.C;
import org.osgl.util.E;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

/**
 * Provide Druid datasource
 */
public class DruidDataSourceProvider extends DataSourceProvider {

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

        return source;
    }

    @Override
    public Map<String, String> confKeyMapping() {
        return C.map("minIdle", "minConnections",
                "maxActive", "maxConnections",
                "maxWait", "waitTimeout",
                "validationQuery", "heartbeatSql",
                "maxPoolPreparedStatementPerConnectionSize", "pstmtCacheSize"
        );
    }
}
