package act.db.sql.ddl;

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

import act.db.sql.SqlDbService;
import act.db.sql.SqlDbServiceConfig;
import act.db.sql.ebean.EbeanConfigAdaptor;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.ServerConfig;

public class EbeanDDL extends DDL {

    private EbeanServer ebean;

    public EbeanDDL(SqlDbService svc, SqlDbServiceConfig config) {
        super(svc, config);
        createEbeanServer();
    }

    @Override
    public void create() {
        // already done when creating ebean server
    }

    @Override
    public void drop() {
        // already done when creating ebean server
    }

    @Override
    protected void releaseResources() {
        ebean.shutdown(false, true);
    }

    private void createEbeanServer() {
        ServerConfig ebeanConfig = new EbeanConfigAdaptor().adaptFrom(config, svc);
        ebean = EbeanServerFactory.create(ebeanConfig);
    }
}
