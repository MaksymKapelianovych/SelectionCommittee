package ua.vstup.injector.db;

import org.apache.log4j.Logger;
import ua.vstup.annotation.Entity;
import ua.vstup.annotation.Id;
import ua.vstup.dao.db.manager.ConnectionManager;

import javax.swing.plaf.nimbus.State;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class DbCreator {
    private static final Logger LOGGER = Logger.getLogger(DbCreator.class);

    public static final String SHOW_TABLES = "SHOW TABLES";
    public static final String TABLES_COLUMN = "Tables_in_";
    public static final String DROP_TABLE = "DROP TABLE IF EXISTS ";
    public static final String CREATE_TABLE = "CREATE TABLE ";
    public static final String NOT_NULL = "NOT NULL ";
    public static final String AUTO_INCREMENT = "AUTO_INCREMENT ";
    public static final String PRIMARY_KEY = "PRIMARY KEY ";

    public static final String LEFT_BRACKET = "(";
    public static final String RIGHT_BRACKET = ") ";
    public static final String COMMA = ", ";
    public static final String SEMICOLON = ";";
    public static final String SPACE = " ";

    private DbState state;
    private Connection connection;
    private List<Class<?>> entities = new ArrayList<>();

    public DbCreator(Connection connection, DbState state){
        this.connection = connection;
        this.state = state;
    }

    public void check(String dbname, String packagename) {
        switch (state){
            case CREATE:
                try {
                    loadEntities(packagename);
                    createTables(dbname);
                }catch (ReflectiveOperationException | IOException | SQLException e){
                    LOGGER.error("Can't load entity", e);
                    throw new IllegalStateException("Can't load entity", e); // TODO exception
                }
            case NONE:
        }
    }

    private void createTables(String dbname) throws SQLException {
        deleteTables(dbname);
        for (Class<?> clazz: entities) {
            createTable(clazz);
        }
    }

    private void deleteTables(String dbname) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet tables = statement.executeQuery(SHOW_TABLES + SEMICOLON);
        while(tables.next()){
            Statement drop = connection.createStatement();
            boolean dropped = drop.execute(DROP_TABLE + tables.getString(TABLES_COLUMN + dbname) + SEMICOLON);
            drop.close();

        }
        statement.close();
    }

    //TODO: add annotation for custom column name; add join
    private void createTable(Class<?> clazz) throws SQLException {
        StringBuilder builder = new StringBuilder();
        String name = getName(clazz.getName());

        builder.append(CREATE_TABLE)
                .append(name)
                .append(LEFT_BRACKET);

        Field id = null;
        for(Field field: clazz.getDeclaredFields()){
            builder.append(field.getName())
                    .append(SPACE)
                    .append(getType(field.getType()))
                    .append(SPACE)
                    .append(NOT_NULL);
            if(field.isAnnotationPresent(Id.class)){
                id = field;
                builder.append(AUTO_INCREMENT)
                        .append(COMMA);
                continue;
            }
            builder.append(COMMA);
        }

        if(id == null){
            throw new RuntimeException("Missing id field"); //TODO create exception
        }

        builder.append(PRIMARY_KEY)
                .append(LEFT_BRACKET)
                .append(id.getName())
                .append(RIGHT_BRACKET)
                .append(RIGHT_BRACKET)
                .append(SEMICOLON);

        String query = builder.toString();
        Statement statement = connection.createStatement();
        statement.execute(query);
        statement.close();
    }

    private String getType(Class<?> clazz){
        if (clazz == Integer.class){
            return "int";
        }else if (clazz == Long.class){
            return "int";
        }else if(clazz == Double.class){
            return "double";
        }
        else if(clazz == String.class){
            return "varchar(255)";
        }
        //TODO: join
        throw new RuntimeException();
    }

    private void loadEntities(String packageName) throws IOException, ReflectiveOperationException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<File> dirs = new ArrayList<>();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        for (File directory : dirs) {
            loadEntities(directory, packageName);
        }
    }

    private void loadEntities(File directory, String packageName) throws ReflectiveOperationException {
        if (!directory.exists()) {
            return;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                loadEntities(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                Class<?> c = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                loadEntity(c);
            }
        }
    }

    private void loadEntity(Class<?> c) {
        if(c.isAnnotationPresent(Entity.class)){
            entities.add(c);
        }
    }

    private String getName(String url){
        int index = url.lastIndexOf('.');
        return url.substring(index + 1, url.length()).toLowerCase();
    }
}
