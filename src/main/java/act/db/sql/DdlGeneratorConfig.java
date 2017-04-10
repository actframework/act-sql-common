package act.db.sql;

import act.Act;

import java.io.File;
import java.util.Map;

/**
 * Specify DDL generating logic
 */
public class DdlGeneratorConfig {

    /**
     * Should it generate and run create database DDL
     */
    public boolean create;

    /**
     * Should it generate and run drop database DDL
     */
    public boolean drop;

    public DdlGeneratorConfig(Map<String, String> conf) {
        if (conf.containsKey("ddl.create")) {
            create = Boolean.parseBoolean(conf.get("ddl.create"));
        } else if (Act.isDev()) {
            String url = conf.get("url");
            create = !h2DbFileExists(url);
        }
        if (create && conf.containsKey("ddl.drop")) {
            drop = Boolean.parseBoolean(conf.get("ddl.drop"));
        }
    }

    /**
     * Used to check if an h2 database file exists. This method is supposed to
     * be called in dev mode when database auto generation is needed
     *
     * @param jdbcUrl the h2 jdbc URL
     * @return `true` if the h2 database file exists; `false` for all other cases including
     *         the jdbc URL is not for h2
     */
    protected final boolean h2DbFileExists(String jdbcUrl) {
        if (Act.isProd()) {
            return true;
        }
        if (jdbcUrl.startsWith("jdbc:h2:")) {
            String file = jdbcUrl.substring("jdbc:h2:".length()) + ".mv.db";
            File _file = new File(file);
            return (_file.exists());
        }
        return false;
    }

}
