package applisem.dao;

import static applisem.log.Applisem_Log.ApplisemLog.*;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * THis Class is a cache of opened connection to the SQL data base.<br>
 * <br>
 * This version only support one database.<br>
 * <br>
 * The cache is made by a LinkedList (better performance for adding to the end
 * and removing from the begining.<br>
 * The List is synchronized for thread safety.
 * <br>
 *
 * @author LBO
 */
public class ConnectionsManager {

    /**
     * the default time out
     */
    public static final long TIMEOUT = 16000;

    /**
     * the defined timeout
     */
    private long timeout = TIMEOUT;

    /**
     * the singleton instance
     */
    private static ConnectionsManager instance = null;

    /**
     * the cannonical name of the driver class
     */
    private String driver;
    /**
     * the jdbc url of the database
     */
    private String url;
    /**
     * the user for the database
     */
    private String user;
    /**
     * the password for the database
     */
    private String password;

    /**
     * the maximum number of cached connection
     */
    private int maxConnections;
    /**
     * the container for cached free connection this is a synchronised
     * LinkedList to better performance and thread safety
     */
    private final List<Connection> connections = Collections.synchronizedList(new LinkedList<Connection>());

    /**
     * the database config
     */
    DatabaseConfig config = DatabaseConfig.getDatabaseConfig();

    /**
     * Singleton method
     *
     * @return this singleton Connection manager
     * @throws Exception
     */
    public static ConnectionsManager getConnectManager() throws Exception {
        Log_Applisem_Entree();
        if (instance == null) {
            instance = new ConnectionsManager();
        }
        Log_Applisem_Sortie();
        return instance;
    }

    /**
     * The constructor parameters a extracted from the configuration file
     *
     * @throws Exception
     */
    public ConnectionsManager() throws Exception {
        Log_Applisem_Entree();
        this.driver = config.driver;
        this.url = config.url;
        this.user = config.username;
        this.password = config.password;
        this.maxConnections = config.maxConnections;
        this.timeout = TIMEOUT;
        if (Log_Applisem_Actif()) {
            Log_Applisem(" Driver = " + driver);
            Log_Applisem(" url = " + url);
            Log_Applisem(" user = " + user);
            Log_Applisem(" password = " + password);
            Log_Applisem(" maxConnextions = " + maxConnections);
            Log_Applisem(" timeout = " + timeout);
        }
        try {
            registerDriver(driver);
        } catch (Exception e) {
            Log_Applisem_Error("", e);
            throw e;
        }
        Log_Applisem_Sortie();
    }

    /**
     * set the maximum number of connection cached
     *
     * @param maxConnections the new number of the maximum
     */
    public void setMaxConnections(int maxConnections) {
        Log_Applisem_Entree();
        if (maxConnections < 0) {
            this.maxConnections = DatabaseConfig.MAX_CONNECTIONS;
        } else {
            this.maxConnections = maxConnections;
        }
        Log_Applisem(" maxConnextions = " + maxConnections);
        Log_Applisem_Sortie();
    }

    /**
     * get the maximum number of connection cached
     *
     * @return the maximum number of connection cached
     */
    public int getMaxConnections() {
        Log_Applisem_Entree();
        Log_Applisem_Sortie();
        return this.maxConnections;
    }

    /**
     * Getter of the connection timeout
     *
     * @return the timeout in ms
     */
    public long getTimeout() {
        Log_Applisem_Entree();
        Log_Applisem_Sortie();
        return this.timeout;
    }

    /**
     * Setter of the timout to establish a connection
     *
     * @param timeout the timeout in ms
     */
    public void setTimeout(long timeout) {
        Log_Applisem_Entree();
        if (timeout < 0) {
            this.timeout = TIMEOUT;
        } else {
            this.timeout = timeout;
        }
        Log_Applisem_Sortie();
    }

    /**
     * Getter of the cannonical name of the SQL driver class
     *
     * @return the cannonical name of the SQL driver class
     */
    public String getDriver() {
        Log_Applisem_Entree();
        Log_Applisem_Sortie();
        return this.driver;
    }

    /**
     * Getter of the Database adresse
     *
     * @return the adresse of the database
     */
    public String getURL() {
        Log_Applisem_Entree();
        Log_Applisem_Sortie();
        return this.url;
    }

    /**
     * Getter of the user for the database
     *
     * @return the username for the database
     */
    public String getUser() {
        Log_Applisem_Entree();
        Log_Applisem_Sortie();
        return this.user;
    }

    /**
     * Getter for the password for the database
     *
     * @return the password for the database
     */
    public String getPassword() {
        Log_Applisem_Entree();
        Log_Applisem_Sortie();
        return this.password;
    }

    /**
     * Internal Method witch create a new Connection to the database
     *
     * @return the created connection
     * @throws SQLException thrown if the connection fail
     */
    private Connection createConnection() throws SQLException {
        Log_Applisem_Entree();
        Connection connection = DriverManager.getConnection(url, user, password);
        connection.setAutoCommit(false);
        Log_Applisem_Sortie();
        return connection;
    }

    /**
     * the normal method to get a connection to the database. the connection are
     * first searched in the cache of free connection
     *
     * @return a free connection to the database
     */
    public Connection getConnection() throws SQLException {
        Log_Applisem_Entree();
        Log_Applisem_Sortie();
        return getConnection(timeout);
    }

    /**
     * method to get a connection to the database with a spécifique timeout. the
     * connection are first searched in the cache of free connection
     *
     * @return a free connection to the database
     */
    public Connection getConnection(long timeout) throws SQLException {
        Log_Applisem_Entree();
        while (true) {
            Log_Applisem_Sortie();
            return tryConnection(timeout);
        }
    }

    /**
     * Method to free a connection.<br>
     * Perform the commit and store to the cache is maximum is not reach
     *
     * @param connection the connection to free
     * @throws SQLException thrown if error append during the process
     */
    public void disconnect(Connection connection) throws SQLException {
        Log_Applisem_Entree();
        if (connection == null) {
            Log_Applisem_Sortie();
            return;
        }
        if (isClosed(connection)) {
            Log_Applisem_Sortie();
            return;
        }
        try {
            connection.commit();
        } catch (SQLException e) {
            Log_Applisem_Error("", e);
            return;
        }
        if (connections.size() < maxConnections) {
            connections.add(connection);
        }
        Log_Applisem_Sortie();
    }

    /**
     * Internal method to get a free connection, perform the search in the cache
     *
     * @param timeout the time out to get a connection
     * @return a free connection
     * @throws SQLException thrown if error occur
     */
    private synchronized Connection tryConnection(long timeout) throws SQLException {
        Log_Applisem_Entree();
        Connection connection;
        while (!connections.isEmpty()) {
            connection = connections.remove(0);
            if (!isClosed(connection)) {
                return connection;
            }
        }
        connection = createConnection();
        Log_Applisem_Sortie();
        return connection;
    }

    /**
     * internal method to get a free connection with the default timeout
     *
     * @return a free connection
     * @throws SQLException thrown if error append
     */
    private Connection tryConnection() throws SQLException {
        Log_Applisem_Entree();
        Log_Applisem_Sortie();
        return tryConnection(timeout);
    }

    /**
     * internal method witch verify if the connection is closed
     *
     * @param connection the connection to verify
     * @return true if the connection is closed
     */
    protected boolean isClosed(Connection connection) {
        Log_Applisem_Entree();
        try {
            if (connection.isClosed()) {
                return true;
            }
        } catch (SQLException e) {
            Log_Applisem_Error("", e);
        }
        Log_Applisem_Sortie();
        return false;
    }

    /**
     * internal method to allow acces to the driver of the database
     *
     * @param driver
     * @throws Exception
     */
    private void registerDriver(String driver) throws Exception {
        Log_Applisem_Entree();
        try {
            Class clazz = Class.forName(driver);
            Driver mydriver = (Driver) clazz.newInstance();
            Properties props = System.getProperties();
            props.put("jdbc.drivers", driver);
            System.setProperties(props);
        } catch (ClassNotFoundException e) {
            Log_Applisem_Error(e.getLocalizedMessage(), e);
            throw e;
        } catch (InstantiationException e) {
            Log_Applisem_Error(e.getLocalizedMessage(), e);
            throw e;
        } catch (IllegalAccessException e) {
            Log_Applisem_Error(e.getLocalizedMessage(), e);
            throw e;
        }
        Log_Applisem_Sortie();
    }
}
