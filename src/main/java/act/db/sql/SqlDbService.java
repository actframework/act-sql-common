package act.db.sql;

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

import static act.app.event.SysEventId.PRE_LOAD_CLASSES;
import static act.db.sql.DataSourceProvider.NULL_PROVIDER;

import act.Act;
import act.app.App;
import act.app.event.SysEventId;
import act.conf.AppConfigKey;
import act.db.DbService;
import act.db.DbServiceInitialized;
import act.db.sql.datasource.DataSourceProxy;
import act.db.sql.datasource.SharedDataSourceProvider;
import act.db.sql.ddl.DDL;
import act.db.sql.monitor.DataSourceStatus;
import act.db.sql.tx.*;
import act.db.sql.util.EbeanAgentLoader;
import act.event.SysEventListenerBase;
import org.osgl.$;
import org.osgl.OsglConfig;
import org.osgl.util.E;
import org.osgl.util.S;
import osgl.version.Version;

import java.sql.DriverManager;
import java.util.*;
import javax.sql.DataSource;

/**
 * `SqlDbService` is the base class for DbService solution that dealing with SQL database
 */
public abstract class SqlDbService extends DbService {

    public static final Version VERSION = Version.of(SqlDbService.class);

    public static final DataSourceStatus DUMB_STATUS = new DataSourceStatus();

    static {
        // we need load DriverManager proactively by calling any one of
        // it's static method, e.g. `println`.
        // This is to avoid issues like https://github.com/actframework/actframework/issues/249
        DriverManager.println("loading DriverManager proactively");
        OsglConfig.addGlobalMappingFilter("starts:_ebean_");
    }

    protected SqlDbServiceConfig config;
    protected DataSource ds;
    protected DataSource dsReadOnly;
    private boolean initialized;

    public SqlDbService(final String dbId, final App app, final Map<String, String> config) {
        super(dbId, app);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                SqlDbService.this.init(app, dbId, config);
            }
        };
        if (app.isDev()) {
            app.jobManager().alongWith(SysEventId.DEPENDENCY_INJECTOR_LOADED, "sql_db_service[" + id() + "]:init", runnable);
        } else {
            app.jobManager().post(SysEventId.DEPENDENCY_INJECTOR_LOADED, "sql_db_service[" + id() + "]:init", runnable);
        }
        if (Act.isDev() && !supportDdl()) {
            // ensure ebean agent is load for automatically generating tables
            app.eventBus().bind(PRE_LOAD_CLASSES, new SysEventListenerBase(S.builder(dbId).append("-ebean-pre-cl")) {
                @Override
                public void on(EventObject event) {
                    Object o = config.get("agentPackage");
                    final String agentPackage = null == o ? S.string(app().config().get(AppConfigKey.SCAN_PACKAGE, null)) : S.string(o).trim();
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

    void init(final App app, final String dbId, Map<String, String> conf) {
        if (isTraceEnabled()) {
            trace("trigger on SINGLETON_PROVISIONED event: %s", dbId);
        }
        try {
            final boolean traceEnabled = isTraceEnabled();
            if (traceEnabled) {
                trace("initializing %s", dbId);
            }
            this.config = new SqlDbServiceConfig(dbId, conf);
            this.configured();
            if (traceEnabled) {
                trace("configured: %s", dbId);
            }
            this.initDataSource();
            if (traceEnabled) {
                trace("data source initialized: %s", dbId);
            }
            if (!this.config.isSharedDatasource() && !supportDdl() && this.config.createDdl()) {
                // the plugin doesn't support ddl generating and executing
                // we have to run our logic to get it done
                app.jobManager().on(SysEventId.START, new Runnable() {
                    @Override
                    public void run() {
                        if (traceEnabled) {
                            trace("executing DDL: %s", dbId);
                        }
                        SqlDbService.this.executeDdl();
                    }
                });
            }
            this.initialized = true;
            if (traceEnabled) {
                trace("emitting db-svc-init event: %s", dbId);
            }
            app.jobManager().post(SysEventId.SINGLETON_PROVISIONED, new Runnable() {
                @Override
                public void run() {
                    app.eventBus().emit(new DbServiceInitialized(SqlDbService.this));
                    if (traceEnabled) {
                        trace("db-svc-init event triggered: %s", dbId);
                    }
                }
            }, true);
        } catch (RuntimeException e) {
            throw E.invalidConfiguration(e, "Error init SQL db service");
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
     *
     * @param readonly return readonly datasource if true
     * @return the datasource of this service
     */
    public DataSource dataSource(boolean readonly) {
        return readonly ? dsReadOnly : ds;
    }

    /**
     * Returns a {@link DataSource} instance loaded by this DbService
     * @return the datasource of this service
     */
    public DataSource dataSource() {
        return ds;
    }

    /**
     * Returns a {@link DataSource} instance loaded by this DbService for readonly
     * operations.
     *
     * @return the readonly datasource of this service
     */
    public DataSource dataSourceReadOnly() {
        return dsReadOnly;
    }

    public DataSource newDataSource() {
        return dataSourceProvider().createDataSource(dataSourceConfig());
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
            try {
                dsp.destroy();
            } catch (Exception e) {
                // just ignore it
            }
        }
        ds = null;
        if (dsReadOnly instanceof DataSourceProxy) {
            ((DataSourceProxy) dsReadOnly).clear();
        }
        dsReadOnly = null;
        config = null;
    }

    protected DataSourceProvider builtInDataSourceProvider() {
        return NULL_PROVIDER;
    }

    /**
     * Called when {@link SqlDbServiceConfig} is loaded
     *
     * Sub class can overwrite this method to provide logic to do further configuration
     */
    protected void configured() {}

    private void initDataSource() {
        if (isTraceEnabled()) {
            trace("init data source: %s", id());
        }
        DataSourceProvider dsProvider = config.dataSourceProvider();
        if (null == dsProvider) {
            dsProvider = builtInDataSourceProvider();
            E.unsupportedIf(null == dsProvider, "%s does not support built-in datasource", getClass().getName());
        }
        if (dsProvider.initialized()) {
            doInitDataSource(dsProvider);
        } else {
            final DataSourceProvider finalProvider = dsProvider;
            dsProvider.setInitializationCallback(new $.Visitor<DataSourceProvider>() {
                @Override
                public void visit(DataSourceProvider dataSourceProvider) throws $.Break {
                    doInitDataSource(finalProvider);
                }
            });
        }
    }

    private void doInitDataSource(DataSourceProvider dsProvider) {
        DataSourceConfig dsConfig = this.config.dataSourceConfig;
        if (dsProvider instanceof SharedDataSourceProvider) {
            dsConfig = ((SharedDataSourceProvider) dsProvider).dataSourceConfig();
        }
        ds = dsProvider.createDataSource(dsConfig);
        dataSourceProvided(ds, dsConfig, false);
        processSlave(dsProvider, dsConfig);
    }

    private void processSlave(DataSourceProvider dsProvider, DataSourceConfig dsConfig) {
        dsReadOnly = ds;
        if (dsConfig.hasSlave()) {
            if (dsConfig.hasSoleSlave()) {
                dsReadOnly = dsProvider.createDataSource(dsConfig.soleSlave());
            } else {
                List<DataSource> slaves = new ArrayList<>();
                for (DataSourceConfig slaveConf : dsConfig.slaveDataSourceConfigurations) {
                    DataSource slave = dsProvider.createDataSource(slaveConf);
                    slaves.add(slave);
                }
                dsReadOnly = new DataSourceProxy(slaves);
            }
            dataSourceProvided(dsReadOnly, dsConfig, true);
        }
    }

    /**
     * Called after datasource is initialized with a configured dataSourceProvider.
     * Subclass can use this method to do relevant logic that require a
     * datasource to be initialized.
     *
     * @param dataSource the datasource
     * @param dataSourceConfig the datasource config used to load the data source
     * @param readonly is the datasource readonly
     */
    protected void dataSourceProvided(DataSource dataSource, DataSourceConfig dataSourceConfig, boolean readonly) {}


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

    protected void executeDdl() {
        Set<Class> models = entityClasses();
        if (models.isEmpty()) {
            return;
        }
        DDL.execute(this, this.config);
    }

    public boolean beginTxIfRequired(Object delegate) {
        TxInfo info = TxContext.info();
        if (null != info && info.withinTxScope) {
            if (null != info.listener) {
                return true;
            }
            doStartTx(delegate, info.readOnly);
            info.listener = createTxListener(delegate);
            return true;
        }
        return false;
    }

    public void forceBeginTx(Object delegate) {
        TxInfo info = TxContext.info();
        if (null != info && null != info.listener) {
            return;
        }
        if (null == info || !info.withinTxScope) {
            info = TxContext.enterTxScope(false);
        }
        E.illegalStateIf(info.readOnly, "Existing TX is read only");
        doStartTx(delegate, false);
        info.listener = createTxListener(delegate);
    }

    protected abstract void doStartTx(Object delegate, boolean readOnly);
    protected abstract void doRollbackTx(Object delegate, Throwable cause);
    protected abstract void doEndTxIfActive(Object delegate);

    protected TxScopeEventListener createTxListener(final Object delegate) {
        return new TxScopeEventListener() {

            @Override
            public void exit() {
                doEndTxIfActive(delegate);
            }

            @Override
            public void rollback(Throwable throwable) {
                doRollbackTx(delegate, throwable);
            }
        };
    }

}
