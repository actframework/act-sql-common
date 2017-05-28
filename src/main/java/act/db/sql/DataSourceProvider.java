package act.db.sql;

import act.db.sql.monitor.DataSourceStatus;
import act.util.DestroyableBase;
import org.osgl.$;
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;

import javax.sql.DataSource;
import java.util.Map;

public abstract class DataSourceProvider extends DestroyableBase {

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
        this.initializationCallback = $.notNull(callback);
        if (initialized()) {
            callback.apply(this);
        }
    }

}
