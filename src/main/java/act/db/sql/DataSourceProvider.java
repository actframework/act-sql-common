package act.db.sql;

import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;

import javax.sql.DataSource;
import java.util.Map;

public abstract class DataSourceProvider {

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

}
