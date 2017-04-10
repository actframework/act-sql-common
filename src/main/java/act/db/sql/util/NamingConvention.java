package act.db.sql.util;

import org.osgl.util.Keyword;

/**
 * Define the logic of converting name between database and Java object
 */
public interface NamingConvention {

    /**
     * Convert database name to Java name
     * @param name the database name
     * @return the java name corresponding to the database name specified
     */
    String fromDb(String name);

    /**
     * Convert java name to database name
     * @param name the java name
     * @return the database from the java name specified
     */
    String toDb(String name);

    enum Default implements NamingConvention {

        MATCHING() {
            @Override
            public String fromDb(String name) {
                return name;
            }

            @Override
            public String toDb(String name) {
                return name;
            }
        },

        UNDERSCORE() {
            @Override
            public String fromDb(String name) {
                return Keyword.of(name).camelCase();
            }

            @Override
            public String toDb(String name) {
                return Keyword.of(name).underscore();
            }
        }
    }

}
