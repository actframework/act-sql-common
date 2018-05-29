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
import static act.db.sql.SqlConfKeys.*;

import act.data.annotation.Data;
import act.util.SimpleBean;
import org.osgl.$;
import org.osgl.exception.ConfigurationException;
import org.osgl.util.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The simple class encapsulate configurations for an SQL DataSource.
 *
 * All time configuration should be in seconds.
 */
@Data
public class DataSourceConfig implements SimpleBean {

    public static final int DEF_MIN_CONN = 2;
    public static final int DEF_MAX_CONN = 10;
    public static final int DEF_INACTIVE_TIME = 10 * 60;
    public static final int DEF_MAX_LIFE_TIME = 30 * 60;
    public static final int DEF_CONN_TIMEOUT = 30;

    public String id;

    public String url;

    public String username;

    public String password;

    public String driver;

    public int minConnections;

    public int maxConnections;

    public int isolationLevel = Connection.TRANSACTION_READ_COMMITTED;

    public boolean autoCommit;

    public boolean readOnly;

    public int connectionTimeout;

    public Map<String, String> customProperties;

    public List<DataSourceConfig> slaveDataSourceConfigurations = new ArrayList<>();

    // Constructor for slave datasource
    private DataSourceConfig(DataSourceConfig parent, Map<String, String> conf) {
        $.copy(parent).to(this);
        init(conf, true);
        readOnly = true;
    }

    public DataSourceConfig(String dbId, Map<String, String> conf) {
        id = dbId;
        init(conf, false);

        // process for readonly data source
        Map<String, String> soleSlaveConfig = new HashMap<>();
        Map<String, Map<String, String>> slaves = new HashMap<>();
        for (Map.Entry<String, String> entry : conf.entrySet()) {
            String key = entry.getKey();
            String lowercaseKey = key.toLowerCase();
            if (lowercaseKey.startsWith("readonly.") || lowercaseKey.startsWith("ro.") || lowercaseKey.startsWith("slave")) {
                key = S.cut(key).afterFirst(".");
                String probe = S.cut(key).beforeFirst(".");
                if (probe.length() == 1) {
                    key = S.cut(key).afterFirst(".");
                    Map<String, String> slaveConfig = slaves.get(probe);
                    if (null == slaveConfig) {
                        slaveConfig = new HashMap<>();
                        slaves.put(probe, slaveConfig);
                    }
                    slaveConfig.put(key, entry.getValue());
                } else {
                    soleSlaveConfig.put(key, entry.getValue());
                }
            }
        }
        if (!soleSlaveConfig.isEmpty()) {
            slaveDataSourceConfigurations.add(new DataSourceConfig(this, soleSlaveConfig));
        }
        for (Map<String, String> otherSlaveConf : slaves.values()) {
            slaveDataSourceConfigurations.add(new DataSourceConfig(this, otherSlaveConf));
        }
    }

    public boolean hasSlave() {
        return !slaveDataSourceConfigurations.isEmpty();
    }

    public DataSourceConfig soleSlave() {
        return hasSoleSlave() ? slaveDataSourceConfigurations.get(0) : null;
    }

    public boolean hasSoleSlave() {
        return 1 == slaveDataSourceConfigurations.size();
    }

    protected void ensureEssentialDatasourceConfig(Map<String, String> conf) {
        if (null == username) {
            LOGGER.warn("No data source user configuration specified. Will use the default 'sa' user");
            username = "sa";
            conf.put("username", username);
        }

        if (null == password) {
            password = "";
        }

        if (null == url) {
            try {
                Class.forName("org.h2.Driver");
                LOGGER.warn("No database URL configuration specified. Will use the default h2 inmemory test database");
                url = "jdbc:h2:./test";
                conf.put("url", url);
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
            } else if (url.contains("jdbc.mariadb")) {
                driver = "org.mariadb.jdbc.Driver";
            } else if (url.contains("jdbc:oracle")) {
                driver = "oracle.jdbc.OracleDriver";
            } else if (url.contains("sqlserver")) {
                driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            } else if (url.contains("jdbc:db2")) {
                driver = "com.ibm.db2.jcc.DB2Driver";
            } else {
                throw E.invalidConfiguration("JDBC driver needs to be configured for datasource: %s", id);
            }
            conf.put("driver", driver);
            LOGGER.warn("JDBC driver not configured, system automatically set to: " + driver);
        }
    }

    private void init(Map<String, String> conf, boolean readOnly) {
        username = get(conf, SQL_CONF_USERNAME, username);
        password = get(conf, SQL_CONF_PASSWORD, password);
        driver = get(conf, SQL_CONF_DRIVER);
        url = get(conf, SQL_CONF_URL, url);

        minConnections = getInt(conf, SQL_CONF_MIN_CONNECTIONS, DEF_MIN_CONN);
        maxConnections = getInt(conf, SQL_CONF_MAX_CONNECTIONS, DEF_MAX_CONN);

        connectionTimeout = getInt(conf, SQL_CONF_CONNECTION_TIMEOUT, DEF_CONN_TIMEOUT);

        autoCommit = getBoolean(conf, SQL_CONF_AUTO_COMMIT, readOnly);
        this.readOnly = !readOnly ? getBoolean(conf, SQL_CONF_READONLY, false) : readOnly;

        String isoLevel = get(conf, "isolationLevel", String.valueOf(this.isolationLevel));
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
        ensureEssentialDatasourceConfig(conf);
        customProperties = conf;
    }

    private static final Map<String, Integer> isolationLevels = C.Map(
            "NONE", Connection.TRANSACTION_NONE,
            "READ_UNCOMMITTED", Connection.TRANSACTION_READ_UNCOMMITTED,
            "READ_COMMITTED", Connection.TRANSACTION_READ_COMMITTED,
            "REPEATABLE_READ", Connection.TRANSACTION_REPEATABLE_READ,
            "SERIALIZABLE", Connection.TRANSACTION_SERIALIZABLE
    );

    private static String get(Map<String, String> conf, String key, String def) {
        return conf.containsKey(key) ? conf.get(key) : def;
    }

    private static String get(Map<String, String> conf, String key) {
        return conf.get(key);
    }

    private static int getInt(Map<String, String> conf, String key, int def) {
        return conf.containsKey(key) ? parseInt(get(conf, key)) : def;
    }

    private static int parseInt(String s) {
        E.illegalArgumentIf(S.blank(s));
        List<String> parts;
        N.Op op;
        int seed;
        if (s.contains("*")) {
            parts = S.fastSplit(s, "*");
            op = N.Op.MUL;
            seed = 1;
        } else if (s.contains("+")) {
            parts = S.fastSplit(s, "+");
            op = N.Op.ADD;
            seed = 0;
        } else {
            return Integer.parseInt(s);
        }
        int i = seed;
        for (String part : parts) {
            i = op.apply(i, Integer.parseInt(part)).intValue();
        }
        return i;
    }

    private static boolean getBoolean(Map<String, String> conf, String key, boolean def) {
        return conf.containsKey(key) ? Boolean.parseBoolean(get(conf, key)) : def;
    }
}
