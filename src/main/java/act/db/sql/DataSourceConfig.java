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

import static act.Act.LOGGER;

import act.data.annotation.Data;
import act.util.SimpleBean;
import org.osgl.exception.ConfigurationException;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.Keyword;
import org.osgl.util.N;

import java.sql.Connection;
import java.util.Map;

/**
 * The simple class encapsulate configurations for an SQL DataSource.
 *
 * The class is originated from `org.avaje.datasource.DataSourceConfig`
 */
@Data
public class DataSourceConfig implements SimpleBean {

    public static final int DEF_MIN_CONN = 2;
    public static final int DEF_MAX_CONN = 10;

    public String id;

    public String url;

    public String username;

    public String password;

    public String driver;

    public int minConnections = DEF_MIN_CONN;

    public int maxConnections = DEF_MAX_CONN;

    public int isolationLevel = Connection.TRANSACTION_READ_COMMITTED;

    public boolean autoCommit;

    public String heartbeatSql;

    public int heartbeatFreqSecs = 30;

    public int heartbeatTimeoutSeconds = 3;

    public boolean captureStackTrace;

    public int maxStackTraceSize = 5;

    public int leakTimeMinutes = 30;

    public int maxInactiveTimeSecs = 720;

    public int maxAgeMinutes = 0;

    public int trimPoolFreqSecs = 59;

    public int pstmtCacheSize = 20;

    public int cstmtCacheSize = 20;

    public int waitTimeoutMillis = 1000;

    public String poolListener;

    public boolean offline;

    public Map<String, String> customProperties;

    public DataSourceConfig(String dbId, Map<String, String> conf) {
        id = dbId;

        username = get(conf, "username");
        password = get(conf, "password");
        driver = get(conf, "driver");
        url = get(conf, "url");

        autoCommit = getBoolean(conf, "autoCommit", autoCommit);
        captureStackTrace = getBoolean(conf, "captureStackTrace", captureStackTrace);
        maxStackTraceSize = getInt(conf, "maxStackTraceSize", maxStackTraceSize);
        leakTimeMinutes = getInt(conf, "leakTimeMinutes", leakTimeMinutes);
        maxInactiveTimeSecs = getInt(conf, "maxInactiveTimeSecs", maxInactiveTimeSecs);
        trimPoolFreqSecs = getInt(conf, "trimPoolFreqSecs", trimPoolFreqSecs);
        maxAgeMinutes = getInt(conf, "maxAgeMinutes", maxAgeMinutes);

        minConnections = getInt(conf, "minConnections", minConnections);
        maxConnections = getInt(conf, "maxConnections", maxConnections);
        pstmtCacheSize = getInt(conf, "0", pstmtCacheSize);
        cstmtCacheSize = getInt(conf, "cstmtCacheSize", cstmtCacheSize);

        waitTimeoutMillis = getInt(conf, "waitTimeout", waitTimeoutMillis);

        heartbeatSql = get(conf, "heartbeatSql");
        heartbeatTimeoutSeconds =  getInt(conf, "heartbeatTimeoutSeconds", heartbeatTimeoutSeconds);
        poolListener = get(conf, "poolListener");
        offline = getBoolean(conf, "offline", offline);

        String isoLevel = get(conf, "isolationLevel");
        if (null != isoLevel) {
            if (N.isInt(isoLevel)) {
                this.isolationLevel = Integer.parseInt(isoLevel);
            } else {
                isoLevel = Keyword.of(isoLevel.toUpperCase()).constantName();
                if (isoLevel.startsWith("TRANSACTION_")) {
                    isoLevel = isoLevel.substring("TRANSACTION_".length());
                }
                E.invalidConfigurationIf(!isolationLevels.containsKey(isoLevel), "Unknown isolationLevel: %s", isoLevel);
                this.isolationLevel = isolationLevels.get(isoLevel);
            }
        }

        ensureDefaultDatasourceConfig();

        customProperties = conf;
    }



    private static String get(Map<String, String> conf, String key) {
        return conf.get(key);
    }

    private static int getInt(Map<String, String> conf, String key, int def) {
        return conf.containsKey(key) ? Integer.parseInt(get(conf, key)) : def;
    }

    private static boolean getBoolean(Map<String, String> conf, String key, boolean def) {
        return conf.containsKey(key) ? Boolean.parseBoolean(get(conf, key)) : def;
    }

    protected void ensureDefaultDatasourceConfig() {
        if (null == username) {
            LOGGER.warn("No data source user configuration specified. Will use the default 'sa' user");
            username = "sa";
        }

        if (null == password) {
            password = "";
        }

        if (null == url) {
            try {
                Class.forName("org.h2.Driver");
                LOGGER.warn("No database URL configuration specified. Will use the default h2 inmemory test database");
                url = "jdbc:h2:./test";
            } catch (ClassNotFoundException e) {
                throw new ConfigurationException("No database URL configuration specified to db service: " + id);
            }
        }

        if (null == driver) {
            if (url.contains("mysql")) {
                try {
                    driver = "com.mysql.cj.jdbc.Driver";
                    Class.forName(driver);
                } catch (ClassNotFoundException e) {
                    // we are using mysql jdbc driver 5.x
                    driver = "com.mysql.jdbc.Driver";
                }
            } else if (url.contains("postgresql")) {
                driver = "org.postgresql.Driver";
            } else if (url.contains("jdbc:h2:")) {
                driver = "org.h2.Driver";
            } else if (url.contains("jdbc:oracle")) {
                driver = "oracle.jdbc.OracleDriver";
            } else if (url.contains("sqlserver")) {
                driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            } else if (url.contains("jdbc:db2")) {
                driver = "com.ibm.db2.jcc.DB2Driver";
            } else {
                throw E.invalidConfiguration("JDBC driver needs to be configured for datasource: %s", id);
            }
            LOGGER.warn("JDBC driver not configured, system automatically set to: " + driver);
        }
    }


    private static final Map<String, Integer> isolationLevels = C.Map(
            "NONE", Connection.TRANSACTION_NONE,
            "READ_UNCOMMITTED", Connection.TRANSACTION_READ_UNCOMMITTED,
            "READ_COMMITTED", Connection.TRANSACTION_READ_COMMITTED,
            "REPEATABLE_READ", Connection.TRANSACTION_REPEATABLE_READ,
            "SERIALIZABLE", Connection.TRANSACTION_SERIALIZABLE
    );
}
