package act.db.sql;

import act.app.DbServiceManager;
import act.cli.Command;
import act.cli.JsonView;
import act.cli.Required;
import act.db.DbService;
import act.db.sql.monitor.DataSourceStatus;

import static act.controller.Controller.Util.badRequestIf;
import static act.controller.Controller.Util.notFoundIfNull;

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
