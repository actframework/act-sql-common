package act.db.sql.datasource;

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

import org.osgl.util.E;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Dispatch calls to multiple data sources.
 */
public class DataSourceProxy implements DataSource {

    private List<DataSource> workers;
    private int limit;
    private int cursor;

    public DataSourceProxy(Collection<? extends DataSource> dataSources) {
        E.illegalArgumentIf(dataSources.size() < 0, "no data source found");
        workers = new ArrayList<>();
        workers.addAll(dataSources);
        limit = workers.size();
    }

    public void clear() {
        workers.clear();
        limit = 0;
        cursor = 0;
    }

    private synchronized DataSource getOne() {
        return workers.get(cursor);
    }

    private DataSource getOneAndMoveCursor() {
        DataSource ds = getOne();
        cursor = (cursor + 1) % limit;
        return ds;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getOneAndMoveCursor().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getOneAndMoveCursor().getConnection(username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return getOne().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return getOne().isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return getOne().getLogWriter();
    }

    @Override
    public synchronized void setLogWriter(PrintWriter out) throws SQLException {
        for (DataSource ds : workers) {
            ds.setLogWriter(out);
        }
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        for (DataSource ds : workers) {
            ds.setLoginTimeout(seconds);
        }
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return getOne().getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return getOne().getParentLogger();
    }

}
