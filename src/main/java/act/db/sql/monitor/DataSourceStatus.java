package act.db.sql.monitor;

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
