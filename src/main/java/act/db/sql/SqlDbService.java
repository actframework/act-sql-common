package act.db.sql;

import act.Act;
import act.app.App;
import act.app.event.AppEventId;
import act.conf.AppConfigKey;
import act.db.DbService;
import act.db.sql.ddl.DDL;
import act.db.sql.util.EbeanAgentLoader;
import act.event.AppEventListenerBase;
import org.osgl.util.S;

import javax.sql.DataSource;
import java.util.EventObject;
import java.util.Map;
import java.util.Set;

import static act.app.event.AppEventId.PRE_LOAD_CLASSES;

/**
 * `SqlDbService` is the base class for DbService solution that dealing with SQL database
 */
public abstract class SqlDbService extends DbService {

    protected SqlDbServiceConfig config;
    private DataSource ds;

    public SqlDbService(final String dbId, final App app, final Map<String, String> config) {
        super(dbId, app);
        final SqlDbService me = this;
        app.jobManager().on(AppEventId.SINGLETON_PROVISIONED, new Runnable() {
            @Override
            public void run() {
                me.config = new SqlDbServiceConfig(dbId, config);
                me.configured();
                me.initDataSource();
                if (!me.config.isSharedDatasource() && !supportDdl() && me.config.createDdl()) {
                    // the plugin doesn't support ddl generating and executing
                    // we have to run our logic to get it done
                    app.jobManager().on(AppEventId.START, new Runnable() {
                        @Override
                        public void run() {
                            me.executeDdl();
                        }
                    });
                }
            }
        });
        if (Act.isDev() && !supportDdl()) {
            // ensure ebean agent is load for automatically generating tables
            app.eventBus().bind(PRE_LOAD_CLASSES, new AppEventListenerBase(S.builder(dbId).append("-ebean-pre-cl")) {
                @Override
                public void on(EventObject event) {
                    Object o = config.get("agentPackage");
                    final String agentPackage = null == o ? S.string(app().config().get(AppConfigKey.SCAN_PACKAGE)) : S.string(o).trim();
                    String s = S.builder("debug=").append(Act.isDev() ? "1" : "0")
                            .append(";packages=")
                            //.append("act.db.ebean.*,")
                            .append(agentPackage)
                            .toString();
                    if (!EbeanAgentLoader.loadAgentFromClasspath("ebean-agent", s)) {
                        Act.LOGGER.warn("ebean-agent not found in classpath - not dynamically loaded");
                    }
                }
            });
        }
    }

    /**
     * Returns a {@link DataSource} instance loaded by this DbService
     * @return the datasource of this service
     */
    public DataSource dataSource() {
        return ds;
    }

    public DataSourceConfig dataSourceConfig() {
        return config.dataSourceConfig;
    }

    @Override
    protected void releaseResources() {
        ds = null;
        config = null;
    }

    /**
     * Called when {@link SqlDbServiceConfig} is loaded
     *
     * Sub class can overwrite this method to provide logic to do further configuration
     */
    protected void configured() {}

    private void initDataSource() {
        DataSourceProvider dsProvider = config.dataSourceProvider();
        if (null != dsProvider) {
            ds = dsProvider.createDataSource(config.dataSourceConfig);
            dataSourceProvided(ds);
        } else {
            ds = createDataSource();
        }
    }

    /**
     * Called after datasource is initialized with a configured dataSourceProvider.
     * Subclass can use this method to do relevant logic that require a
     * datasource to be initialized.
     *
     * Note this method is mutually exclusive with {@link #createDataSource()}
     */
    protected void dataSourceProvided(DataSource dataSource) {}

    /**
     * If no data source provider is specified then it relies on the sub
     * class to provide the new datasource instance.
     *
     * Note this method is mutually exclusive with
     * {@link #dataSourceProvided(DataSource)}
     *
     * @return an new datasource instance
     */
    protected abstract DataSource createDataSource();

    /**
     * Sub class shall implement this method and report if it support
     * generating and running DDL
     *
     * @return `true` if the db service support DDL generating and running; or `false` otherwise
     */
    protected abstract boolean supportDdl();

    public String tableNameFromClassName(String className) {
        return config.tableNamingConvention.toDb(className);
    }

    public String fieldNameFromPropertyName(String propertyName) {
        return config.fieldNamingConvention.toDb(propertyName);
    }

    private void executeDdl() {
        Set<Class> models = modelClasses();
        if (models.isEmpty()) {
            return;
        }
        DDL.execute(this, this.config);
    }

}
