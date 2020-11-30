package ua.vstup.dao.db;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ua.vstup.dao.db.manager.ConnectionManager;
import ua.vstup.dao.db.manager.DbConfig;
import ua.vstup.entity.Message;
import ua.vstup.injector.db.DbCreator;
import ua.vstup.injector.db.DbState;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.Assert.*;

public class DbCreatorTest {
    private static final String ACTUAL_DATABASE_PROPERTIES_FILENAME = "properties/db";
    private static final String DB_URL = "db.url";
    private static final String DB_USERNAME = "db.username";
    private static final String DB_PASS = "db.password";

    public static final String TABLES_COLUMN = "Tables_in_";


    private Connection connection;
    private DbConfig config = new DbConfig();
    private List<Class<?>> entities = new ArrayList<>();

    private String getDbName(String url){
        int index = url.lastIndexOf('/');
        return url.substring(index + 1, url.length());
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void init() throws SQLException {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(ACTUAL_DATABASE_PROPERTIES_FILENAME);
        config.setJdbcUrl(resourceBundle.getString(DB_URL));
        config.setUsername(resourceBundle.getString(DB_USERNAME));
        config.setPassword(resourceBundle.getString(DB_PASS));
        connection = DriverManager.getConnection(
                config.getJdbcUrl(),
                config.getUsername(),
                config.getPassword());

        entities.add(ua.vstup.entity.Test.class);
        entities.add(Message.class);
    }

    @Test
    public void testDrop() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, SQLException {
        DbCreator creator = new DbCreator(connection, DbState.CREATE);
        Method method = creator.getClass().getDeclaredMethod("deleteTables", String.class);
        method.setAccessible(true);
        method.invoke(creator, getDbName(config.getJdbcUrl()));

        Statement statement = connection.createStatement();
        ResultSet set = statement.executeQuery("SHOW TABLES;");
        assertFalse(set.next());
    }

    //TODO rewrite test
    @Test
    public void testLoadEntities() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException, InstantiationException {
        DbCreator creator = new DbCreator(connection, DbState.CREATE);
        Method method = creator.getClass().getDeclaredMethod("loadEntities", String.class);
        method.setAccessible(true);
        method.invoke(creator, "ua.vstup.entity");

        List<Class<?>> instance = new ArrayList<>();
        Field field = creator.getClass().getDeclaredField("entities");
        field.setAccessible(true);
        Object value = field.get(creator);

        assertEquals(entities, value);
    }

    @Test
    public void testCreateTables() throws SQLException {
        DbCreator creator = new DbCreator(connection, DbState.CREATE);
        creator.check(getDbName(config.getJdbcUrl()), "ua.vstup.entity");

        Statement statement = connection.createStatement();
        ResultSet set = statement.executeQuery("SHOW TABLES;");

    }

}
