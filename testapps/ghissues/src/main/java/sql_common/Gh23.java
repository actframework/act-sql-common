package sql_common;

import act.controller.annotation.UrlContext;
import act.db.beetlsql.BeetlSqlService;
import act.db.eclipselink.EclipseLinkService;
import act.db.hibernate.HibernateService;
import act.db.jpa.JPAService;
import act.db.sql.SqlDbService;
import org.osgl.mvc.annotation.GetAction;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

@UrlContext("23")
public class Gh23 {

    @Inject
    @Named("el")
    private JPAService elService;

    @Inject
    @Named("hi")
    private JPAService hiService;

    @Inject
    @Named("be")
    private SqlDbService beService;

    @Inject
    @Named("el")
    private DataSource elDs;

    @Inject
    @Named("hi")
    private DataSource hiDs;

    @Inject
    @Named("be")
    private DataSource beDs;

    @GetAction("~elService~")
    public boolean testElService() {
        if (null == elService) {
            return false;
        }
        return elService instanceof EclipseLinkService;
    }

    @GetAction("~hiService~")
    public boolean testHiService() {
        if (null == hiService) {
            return false;
        }
        return hiService instanceof HibernateService;
    }

    @GetAction("~beService~")
    public boolean testBeService() {
        if (null == beService) {
            return false;
        }
        return beService instanceof BeetlSqlService;
    }

    @GetAction("~elDataSource~")
    public boolean elDataSource() {
        if (null == elDs) {
            return false;
        }
        return elDs == elService.dataSource();
    }

    @GetAction("~hiDataSource~")
    public boolean hiDataSource() {
        if (null == hiDs) {
            return false;
        }
        return hiDs == hiService.dataSource();
    }

    @GetAction("~beDataSource~")
    public boolean beDataSource() {
        if (null == beDs) {
            return false;
        }
        return beDs == beService.dataSource();
    }

}
