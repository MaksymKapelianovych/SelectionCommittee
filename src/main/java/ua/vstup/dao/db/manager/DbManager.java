package ua.vstup.dao.db.manager;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.ResultTreeType;
import org.apache.log4j.Logger;
import ua.vstup.injector.db.DbCreator;
import ua.vstup.injector.db.DbState;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DbManager implements ConnectionManager {
    private static final Logger LOGGER = Logger.getLogger(DbManager.class);

    private static final String DB_URL = "db.url";
    private static final String DB_USERNAME = "db.username";
    private static final String DB_PASS = "db.password";
    private static final String DB_DRIVER = "db.driver";
    private static final String DB_POOL_SIZE = "db.pool.size";
    private static final String DB_TIMEOUT = "db.timeout";
    private static final String DB_STATE = "db.state";
    private static final String ERROR_MESSAGE = "Connection wasn't set %s";

    private DbConfig config = new DbConfig();
    private Connection connection;


    public DbManager(String filename) throws SQLException {
        ResourceBundle resource = ResourceBundle.getBundle(filename);
        config.setDriverClassName(resource.getString(DB_DRIVER));
        config.setJdbcUrl(resource.getString(DB_URL));
        config.setUsername(resource.getString(DB_USERNAME));
        config.setPassword(resource.getString(DB_PASS));
        config.setMaximumPoolSize(getIntProperty(resource, DB_POOL_SIZE));
        config.setConnectionTimeout(getIntProperty(resource, DB_TIMEOUT));
        config.setState(getDbState(resource.getString(DB_STATE)));

        connection = DriverManager.getConnection(config.getJdbcUrl(), config.getUsername(), config.getPassword());

        DbCreator creator = new DbCreator(getConnection(), config.getState());
        creator.check(getDbName(config.getJdbcUrl()), "ua.vstup.entity");

        System.out.println("Connection to Store DB succesfull!");
    }

    @Override
    public Connection getConnection() {
        try{
            if (connection.isClosed()) {
                throw new SQLException("Connection" + connection + "has been closed");
            }else{
                return connection;
            }
        }catch (SQLException exception){
            LOGGER.error(ERROR_MESSAGE, exception);
            throw new IllegalStateException(String.format(ERROR_MESSAGE, ""), exception);
        }
    }

    @Override
    public void shutdown() throws SQLException {
        connection.close();

    }

    private int getIntProperty(ResourceBundle resource, String dbPoolSize) {
        return Integer.parseInt(resource.getString(dbPoolSize));
    }

    private DbState getDbState(String dbState){
        if(dbState == null){
            return DbState.NONE;
        }
        for(DbState state: DbState.values()){
            if(state.name().toLowerCase().equals(dbState)){
                return state;
            }
        }
        return DbState.NONE;
    }

    private String getDbName(String url){
        int index = url.lastIndexOf('/');
        return url.substring(index + 1, url.length());
    }
}
