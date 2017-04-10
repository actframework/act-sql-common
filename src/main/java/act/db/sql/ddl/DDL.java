package act.db.sql.ddl;

import act.db.sql.SqlDbService;
import act.db.sql.SqlDbServiceConfig;
import act.util.DestroyableBase;
import org.osgl.$;

public abstract class DDL extends DestroyableBase {

    protected SqlDbService svc;
    protected SqlDbServiceConfig config;

    public DDL(SqlDbService svc, SqlDbServiceConfig config) {
        this.svc = $.notNull(svc);
        this.config = $.notNull(config);
    }

    public abstract void create();

    public abstract void drop();

    @Override
    protected abstract void releaseResources();

    public static void execute(SqlDbService svc, SqlDbServiceConfig config) {
        new EbeanDDL(svc, config).destroy();
    }

}
