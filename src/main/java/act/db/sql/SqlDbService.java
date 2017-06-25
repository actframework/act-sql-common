package act.db.sql;

import act.Act;
import act.app.App;
import act.app.event.AppEventId;
import act.conf.AppConfigKey;
import act.db.DbService;
import act.db.DbServiceInitialized;
import act.db.sql.datasource.SharedDataSourceProvider;
import act.db.sql.ddl.DDL;
import act.db.sql.monitor.DataSourceStatus;
import act.db.sql.util.EbeanAgentLoader;
import act.event.AppEventListenerBase;
import org.osgl.$;
import org.osgl.Osgl;
import org.osgl.logging.LogManager;
import org.osgl.logging.Logger;
import org.osgl.util.E;
import org.osgl.util.S;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.util.EventObject;
import java.util.Map;
import java.util.Set;

import static act.app.event.AppEventId.PRE_LOAD_CLASSES;

/**
 * `SqlDbService` is the base class for DbService solution that dealing with SQL database
 */
public abstract class SqlDbService extends DbService {

    private static final Logger _logger = LogManager.get(SqlDbService.class);

    public static final DataSourceStatus DUMB_STATUS = new DataSourceStatus();

    static {
        // we need load DriverManager proactively by calling any one of
        // it's static method, e.g. `println`.
        // This is to avoid issues like https://github.com/actframework/actframework/issues/249
        DriverManager.println("loading DriverManager proactively");
    }

    protected SqlDbServiceConfig config;
    protected DataSource ds;
    private boolean initialized;

    public SqlDbService(final String dbId, final App app, final Map<String, String> config) {
        super(dbId, app);
        final SqlDbService me = this;
        app.eventBus().bindAsync(AppEventId.SINGLETON_PROVISIONED, new AppEventListenerBase() {
            @Override
            public void on(EventObject event) throws Exception {
                if (_logger.isTraceEnabled()) {
                    _logger.trace("trigger on SINGLETON_PROVISIONED event: %s", dbId);
                }
                run();
            }
            private void run() {
                try {
                    final boolean traceEnabled = _logger.isTraceEnabled();
                    if (traceEnabled) {
                        _logger.trace("initializing %s", dbId);
                    }
                    me.config = new SqlDbServiceConfig(dbId, config);
                    me.configured();
                    if (traceEnabled) {
                        _logger.trace("configured: %s", dbId);
                    }
                    me.initDataSource();
                    if (traceEnabled) {
                        _logger.trace("data source initialized: %s", dbId);
                    }
                    if (!me.config.isSharedDatasource() && !supportDdl() && me.config.createDdl()) {
                        // the plugin doesn't support ddl generating and executing
                        // we have to run our logic to get it done
                        app.jobManager().on(AppEventId.START, new Runnable() {
                            @Override
                            public void run() {
                                if (traceEnabled) {
                                    _logger.trace("executing DDL: %s", dbId);
                                }
                                me.executeDdl();
                            }
                        });
                    }
                    me.initialized = true;
                    if (traceEnabled) {
                        _logger.trace("emitting db-svc-init event: %s", dbId);
                    }
                    app.eventBus().emit(new DbServiceInitialized(me));
                    if (traceEnabled) {
                        _logger.trace("db-svc-init event triggered: %s", dbId);
                    }
                } catch (RuntimeException e) {
                    throw E.invalidConfiguration(e, "Error init SQL db service");
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

    @Override
    public String toString() {
        S.Buffer buffer = S.buffer(getClass().getSimpleName());
        String id = id();
        if (S.notBlank(id)) {
            buffer.append("[").append(id).append("]");
        }
        return buffer.toString();
    }

    @Override
    public boolean initAsynchronously() {
        return true;
    }

    @Override
    public boolean initialized() {
        return initialized;
    }

    /**
     * Returns a {@link DataSource} instance loaded by this DbService
     * @return the datasource of this service
     */
    public DataSource dataSource() {
        return ds;
    }

    public DataSourceProvider dataSourceProvider() {
        return config.dataSourceProvider();
    }

    public DataSourceConfig dataSourceConfig() {
        return config.dataSourceConfig;
    }

    public DataSourceStatus dataSourceStatus() {
        DataSourceProvider dsp = dataSourceProvider();
        return null == dsp ? DUMB_STATUS : dsp.getStatus(ds);
    }

    @Override
    protected void releaseResources() {
        DataSourceProvider dsp = null == config ? null : dataSourceProvider();
        if (null != dsp) {
            dsp.destroy();
        }
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
        if (_logger.isTraceEnabled()) {
            _logger.trace("init data source: %s", id());
        }
        final DataSourceProvider dsProvider = config.dataSourceProvider();
        if (null != dsProvider) {
            if (dsProvider.initialized()) {
                DataSourceConfig dsConfig = this.config.dataSourceConfig;
                if (dsProvider instanceof SharedDataSourceProvider) {
                    dsConfig = ((SharedDataSourceProvider) dsProvider).dataSourceConfig();
                }
                ds = dsProvider.createDataSource(dsConfig);
                dataSourceProvided(ds, dsConfig);
            } else {
                dsProvider.setInitializationCallback(new $.Visitor<DataSourceProvider>() {
                    @Override
                    public void visit(DataSourceProvider dataSourceProvider) throws Osgl.Break {
                        DataSourceConfig dsConfig = SqlDbService.this.config.dataSourceConfig;
                        if (dsProvider instanceof SharedDataSourceProvider) {
                            dsConfig = ((SharedDataSourceProvider) dsProvider).dataSourceConfig();
                        }
                        ds = dataSourceProvider.createDataSource(dsConfig);
                        dataSourceProvided(ds, dsConfig);
                    }
                });
            }
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
     *
     * @param dataSource the datasource
     * @param dataSourceConfig the datasource config used to load the data source
     */
    protected void dataSourceProvided(DataSource dataSource, DataSourceConfig dataSourceConfig) {}

    /**
     * If no data source provider is specified then it relies on the sub
     * class to provide the new datasource instance.
     *
     * Note this method is mutually exclusive with
     * {@link #dataSourceProvided(DataSource, DataSourceConfig)}
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
