package applisem.dao;

import static applisem.data.ManipFichier.fDir_Conf;
import static applisem.log.Applisem_Log.ApplisemLog.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * The class is an Helper to get the database configuration from the file on the
 * configuration folder of the server installation path.<br>
 * <br>
 * The configuration file is based on a java text property file<br>
 *
 * @author LBO
 */
public class DatabaseConfig {

    /**
     * the default max connection for the cache
     */
    public static final int MAX_CONNECTIONS = 500;

    /**
     * the user name for the database
     */
    String username;
    /**
     * the password for the database
     */
    String password;
    /**
     * the cannonical name of the database driver class
     */
    String driver;

    /**
     * the JDBC url for the database instance
     */
    String url;

    /**
     * the maximum number of free connexion stored in the cache
     */
    int maxConnections = MAX_CONNECTIONS;

    /**
     * the type of the database ie MYSQL
     */
    String type;

    /**
     * the maximum number of stored connection as a string
     */
    String max;

    /**
     * the relative path to the configuration directory
     */
    public static final String configDir = fDir_Conf.getName();

    /**
     * the singleton instance
     */
    private static DatabaseConfig instance = null;

    /**
     * the singleton factory method
     *
     * @return the singleton instance
     */
    public static DatabaseConfig getDatabaseConfig() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    /**
     * the default constructor only called by the factory method
     */
    private DatabaseConfig() {
        Log_Applisem_Entree();
        Properties props = new Properties();
        try {
            Log_Applisem_Info("fDir_Conf : " + fDir_Conf);
            Log_Applisem_Info("Conf dir : " + configDir);
            FileInputStream fis = new FileInputStream(fDir_Conf + File.separator + "database.conf");
            props.load(fis);
            fis.close();
        } catch (IOException e) {
            Log_Applisem_Error("Failed to load database properties from " + fDir_Conf + File.separator + "database.conf", e);
        }
/*        
        username = props.getProperty("username");
        password = props.getProperty("password");
        driver = props.getProperty("driver");
        url = props.getProperty("url");
        type = props.getProperty("type");
        max = props.getProperty("connections");
*/        
        username = "LEnoVA$2015$";
        password = "HOj3Ho3OkOPOWoyATA4a8OyaJox6kE";
        driver = props.getProperty("driver");
        url = props.getProperty("url");
        Log_Applisem_Info(" Driver : " + driver);
        Log_Applisem_Info(" Url : " + url);
//        url = "jdbc:mysql://localhost:3960/?characterEncoding=utf8";
        type = props.getProperty("type");
        max = props.getProperty("connections");
        if (max != null) {
            try {
                maxConnections = Integer.parseInt(max);
            } catch (NumberFormatException e) {
                Log_Applisem_Error(configDir + "database.conf: max connections must be a number " + maxConnections, e);
            }
        }
        Log_Applisem_Sortie();
    }

}
