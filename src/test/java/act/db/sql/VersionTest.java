package act.db.sql;

import org.junit.Test;
import org.osgl.ut.TestBase;

public class VersionTest extends TestBase {

    @Test
    public void versionShallContainsSqlCommon() {
        yes(SqlDbService.VERSION.toString().contains("sql-common"));
    }

}
