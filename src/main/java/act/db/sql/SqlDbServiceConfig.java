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

import act.Act;
import act.db.sql.datasource.SharedDataSourceProvider;
import act.db.sql.util.NamingConvention;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.S;

import java.util.Map;

/**
 * Encapsulate common configuration for a SQL DbService
 */
public final class SqlDbServiceConfig {

    public DataSourceConfig dataSourceConfig;

    public DdlGeneratorConfig ddlGeneratorConfig;

    public Map<String, String> rawConf;

    public NamingConvention tableNamingConvention = NamingConvention.Default.MATCHING;

    public NamingConvention fieldNamingConvention = NamingConvention.Default.MATCHING;

    public SharedDataSourceProvider sharedDataSourceProvider;

    private DataSourceProvider loadedDataSourceProvider;

    public SqlDbServiceConfig(String dbId, Map<String, String> conf) {
        rawConf = processAliases(conf, aliases);
        sharedDataSourceProvider = checkSharedDatasource(conf);
        if (null == sharedDataSourceProvider) {
            dataSourceConfig = new DataSourceConfig(dbId, rawConf);
            ddlGeneratorConfig = new DdlGeneratorConfig(rawConf);
        } else {
            dataSourceConfig = sharedDataSourceProvider.dataSourceConfig();
        }
        loadNamingConvention(rawConf);
    }

    public boolean isSharedDatasource() {
        return sharedDataSourceProvider != null;
    }

    public boolean createDdl() {
        return ddlGeneratorConfig.create;
    }

    public DataSourceProvider dataSourceProvider() {
        if (null != sharedDataSourceProvider) {
            return sharedDataSourceProvider;
        }
        if (null != loadedDataSourceProvider) {
            return loadedDataSourceProvider;
        }
        String dsProvider = rawConf.get("datasource");
        if (S.notBlank(dsProvider)) {
            String s = dsProvider.toLowerCase();
            if (s.contains("hikari")) {
                dsProvider = HIKARI_PROVIDER;
            } else if (s.contains("druid")) {
                dsProvider = DRUID_PROVDER;
            }
        }
        DataSourceProvider provider;
        if (S.notBlank(dsProvider)) {
            provider = Act.getInstance(dsProvider);
        } else {
            // try HikariCP first
            try {
                Class.forName("com.zaxxer.hikari.HikariConfig");
                provider = Act.getInstance(HIKARI_PROVIDER);
            } catch (Exception e) {
                try {
                    Class.forName("com.alibaba.druid.pool.DruidDataSource");
                    provider = Act.getInstance(DRUID_PROVDER);
                } catch (Exception e1) {
                    // ignore
                    return null;
                }
            }
        }
        loadedDataSourceProvider = provider;
        return provider;
    }

    private SharedDataSourceProvider checkSharedDatasource(Map<String, String> conf) {
        String dsProvider = rawConf.get("datasource");
        if (S.notBlank(dsProvider)) {
            String s = dsProvider.toLowerCase();
            if (s.contains("shared:")) {
                String dbId = S.afterFirst(s, ":");
                E.invalidConfigurationIf(S.blank(dbId), "Invalid datasource provider: %s", dsProvider);
                return new SharedDataSourceProvider(dbId, Act.app().dbServiceManager());
            }
        }
        return null;
    }

    private void loadNamingConvention(Map<String, String> conf) {
        if (conf.containsKey("naming.convention")) {
            String s = conf.get("naming.convention");
            this.tableNamingConvention = namingConventionOf(s);
            this.fieldNamingConvention = this.tableNamingConvention;
        }
        if (conf.containsKey("naming.table")) {
            String s = conf.get("naming.table");
            this.tableNamingConvention = namingConventionOf(s);
        }
        if (conf.containsKey("naming.field")) {
            String s = conf.get("naming.field");
            this.fieldNamingConvention = namingConventionOf(s);
        }
    }

    private NamingConvention namingConventionOf(String s) {
        if (NamingConvention.Default.MATCHING.name().equalsIgnoreCase(s)) {
            return NamingConvention.Default.MATCHING;
        } else if (NamingConvention.Default.UNDERSCORE.name().equalsIgnoreCase(s)) {
            return NamingConvention.Default.UNDERSCORE;
        } else {
            return Act.getInstance(s);
        }
    }


    static Map<String, String> processAliases(Map<String, String> conf, Map<String, String> confMapping) {
        Map<String, String> newConf = C.newMap(conf);
        for (Map.Entry<String, String> entry : confMapping.entrySet()) {
            String alias = entry.getKey();
            String key = entry.getValue();
            if (newConf.containsKey(alias)) {
                newConf.put(key, newConf.get(alias));
            }
        }
        return newConf;
    }

    private static final Map<String, String> aliases = C.Map("jdbcDriver", "driver",
            "jdbcUrl", "url",
            "databaseDriver", "driver",
            "databaseUrl", "url",
            "datasource.provider", "datasource"
    );

    private static final String DRUID_PROVDER = "act.db.sql.datasource.DruidDataSourceProvider";
    private static final String HIKARI_PROVIDER = "act.db.sql.datasource.HikariDataSourceProvider";

}
