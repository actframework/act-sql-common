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

import static act.controller.Controller.Util.badRequestIf;
import static act.controller.Controller.Util.notFoundIfNull;

import act.app.DbServiceManager;
import act.cli.Command;
import act.cli.Required;
import act.db.DbService;
import act.db.sql.monitor.DataSourceStatus;
import act.util.JsonView;

/**
 * Provides CLI admin command
 */
public class SqlDbAdminConsole {

    @Command(name = "act.db.ds.status", help = "report datasource status for specified SQL db service")
    @JsonView
    public DataSourceStatus dataSourceStatus(@Required("specify the db service") String id, DbServiceManager dsm) {
        DbService dbs = dsm.dbService(id);
        notFoundIfNull(dbs, "Cannot find db service with id specified: %s", id);
        badRequestIf(!(dbs instanceof SqlDbService), "Specified db service is not a SqlDbService: %s", id);
        return ((SqlDbService) dbs).dataSourceStatus();
    }

}
