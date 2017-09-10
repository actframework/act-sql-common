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

    /**
     * Construct DdlGeneratorConfig with user specified configuration.
     *
     * Rules:
     *
     * 1. if user specified `ddl.create` then use the value specified
     * 2. - otherwise `create` is `false` unless using h2 database
     * 3. if user specified `ddl.drop` then use the value specified
     * 4. - otherwise `drop` is `false`
     * 5. if `drop` is false and h2 db file exists then `create` set to false
     *
     * @param conf the user specified configuration
     */
    public DdlGeneratorConfig(Map<String, String> conf) {
        String jdbcUrl = conf.get("url");
        boolean h2DbFileExists = h2DbFileExists(jdbcUrl);
        boolean createDefined = conf.containsKey("ddl.create");
        boolean dropDefined = conf.containsKey("ddl.drop");

        // default create - true if it is using h2 database
        create = jdbcUrl.startsWith("jdbc:h2:");

        // Rule 1 - if create defined then use it
        create = create || createDefined && Boolean.parseBoolean(conf.get("ddl.create"));

        // Rule 2 - if drop defined then use it otherwise default to `false`
        drop = dropDefined && Boolean.parseBoolean(conf.get("ddl.drop"));

        // Rule 3 - if h2 db file exists then we reset create to `false` unless `drop` is `true`
        create = create && (drop || !h2DbFileExists);
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
        if (jdbcUrl.startsWith("jdbc:h2:")) {
            if (jdbcUrl.contains("mem")) {
                return false;
            }
            String file = jdbcUrl.substring("jdbc:h2:".length()) + ".mv.db";
            File _file = new File(file);
            return (_file.exists());
        }
        return false;
    }

}
