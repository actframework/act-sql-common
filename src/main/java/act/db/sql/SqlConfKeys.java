package act.db.sql;

/*-
 * #%L
 * ACT SQL Common Module
 * %%
 * Copyright (C) 2015 - 2018 ActFramework
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

/**
 * Common configuration keys applied to SQL database plugins
 */
public interface SqlConfKeys {

    /**
     * Database login username.
     */
    String SQL_CONF_USERNAME = "username";

    /**
     * Database login password.
     */
    String SQL_CONF_PASSWORD = "password";

    /**
     * JDBC driver class name.
     */
    String SQL_CONF_DRIVER = "driver";

    /**
     * JDBC URL string.
     */
    String SQL_CONF_URL = "url";

    /**
     * Should the JDBC connection turn on AutoCommit.
     */
    String SQL_CONF_AUTO_COMMIT = "autoCommit";

    /**
     * Is the connection in the data source readonly
     */
    String SQL_CONF_READONLY = "readonly";

    /**
     * The minimum connections in the data source pool.
     */
    String SQL_CONF_MIN_CONNECTIONS = "minConnections";

    /**
     * The max connections in the data source pool.
     */
    String SQL_CONF_MAX_CONNECTIONS = "maxConnections";

    /**
     * The maximum number of milliseconds that a client will wait for a connection from the pool.
     *
     * If this time is exceeded without a connection becoming available, a `SQLException` shall
     * be thrown out.
     */
    String SQL_CONF_CONNECTION_TIMEOUT = "connectionTimeout";

}
