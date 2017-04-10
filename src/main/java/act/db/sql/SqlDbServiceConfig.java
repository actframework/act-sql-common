package act.db.sql;

import act.Act;
import act.db.sql.util.NamingConvention;
import org.osgl.util.C;

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

    public SqlDbServiceConfig(String dbId, Map<String, String> conf) {
        rawConf = processAliases(conf, aliases);
        dataSourceConfig = new DataSourceConfig(dbId, rawConf);
        ddlGeneratorConfig = new DdlGeneratorConfig(rawConf);
        loadNamingConvention(rawConf);
    }

    public boolean createDdl() {
        return ddlGeneratorConfig.create;
    }

    public DataSourceProvider dataSourceProvider() {
        String dsProvider = rawConf.get("datasource.provider");

        if (null != dsProvider) {
            if (dsProvider.toLowerCase().contains("hikari")) {
                dsProvider = HIKARI_PROVIDER;
            } else if (dsProvider.toLowerCase().contains("druid")) {
                dsProvider = DRUID_PROVDER;
            }
        }
        DataSourceProvider provider;
        if (null != dsProvider) {
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
        return provider;
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

    private static final Map<String, String> aliases = C.map("jdbcDriver", "driver",
            "jdbcUrl", "url",
            "databaseDriver", "driver",
            "databaseUrl", "url"
    );

    private static final String DRUID_PROVDER = "act.db.sql.datasource.DruidDataSourceProvider";
    private static final String HIKARI_PROVIDER = "act.db.sql.datasource.HikariDataSourceProvider";

}
