package ua.vstup.dao.db.manager;

import ua.vstup.injector.db.DbState;

public class DbConfig {
    private String driverClassName;
    private String jdbcUrl;
    private String username;
    private String password;
    private DbState state;
    private int maximumPoolSize;
    private int connectionTimeout;

    public String getDriverClassName() {
        return driverClassName;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public String getPassword() {
        return password;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DbState getState() {
        return state;
    }

    public void setState(DbState state) {
        this.state = state;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
}
