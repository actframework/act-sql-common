package act.db.sql.ddl;

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
