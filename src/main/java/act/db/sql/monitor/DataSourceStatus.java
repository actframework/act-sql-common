package act.db.sql.monitor;

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

/**
 * Used to capture {@link javax.sql.DataSource} status
 */
public class DataSourceStatus {

    private int activeConnections;
    private int idleConnections;
    private int totalConnections;
    private int waitingThreads;

    public DataSourceStatus() {}

    public int getActiveConnections() {
        return activeConnections;
    }

    public DataSourceStatus activeConnections(int count) {
        this.activeConnections = count;
        return this;
    }

    public int getIdleConnections() {
        return idleConnections;
    }

    public DataSourceStatus idleConnections(int count) {
        this.idleConnections = count;
        return this;
    }

    public int getTotalConnections() {
        return totalConnections;
    }

    public DataSourceStatus totalConnections(int count) {
        this.totalConnections = count;
        return this;
    }

    public int getWaitingThreads() {
        return waitingThreads;
    }

    public DataSourceStatus waitingThreads(int count) {
        this.waitingThreads = count;
        return this;
    }

    public static DataSourceStatus create() {
        return new DataSourceStatus();
    }

}
