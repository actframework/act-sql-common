package act.db.sql.util;

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
