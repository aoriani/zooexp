package br.unicamp.ic.zooexp.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;

import br.unicamp.ic.zooexp.core.server.Data;

/**
 * Class to retrieve configurations for servers and clients
 *
 */
public class Configuration {
    /** Logger */
    private static final Logger log = Logger.getLogger(Data.class);

    private static final String CONF_FILE = "prototype.properties";
    private static Properties properties;

    // ======================================================================
    // Config Keys
    // ======================================================================
    /** The port the server listens to */
    private static final String SERVER_PORT_KEY = "server_port";

    /** The number of connection the server can hold */
    private static final String SERVER_MAXCONN_KEY = "server_max_conn";

    /** The timeout for client socket */
    private static final String SERVER_CLIENT_TIMEOUT_KEY = "server_timeout";

    /** The server address */
    private static final String SERVER_ADDRESS_KEY = "server_address";

    /** The timeout for zookeeper connections */
    private static final String ZOOKEEPER_TIMEOUT_KEY = "zookeeper_timeout";

    /** The list of server in a zookeeper ensemble */
    private static final String ZOOSERVER_LIST_KEY = "zooserver_list";

    /** The znode used for server group membership */
    private static final String SERVERS_ZNODE_KEY = "servers_group_znode";

    /** The znode used for logging operations */
    private static final String OPLOG_ZNODE_KEY = "servers_group_znode";

    /** Tells if user must retrieve the server configuration from Zookeeper **/
    private static final String SERVER_CONF_ZOOKEEPER = "server_conf_zookeeper";
    // ======================================================================

    // LoadConfiguration file
    static {
        properties = new Properties();
        URL url = ClassLoader.getSystemResource(CONF_FILE);
        if (url != null) {
            FileInputStream propertiesFile = null;
            try {
                propertiesFile = new FileInputStream(new File(url.getFile()));
                properties.load(propertiesFile);
            } catch (FileNotFoundException e) {
                log.warn("Could not open properties file", e);
            } catch (IOException e) {
                log.error("Problem while loading property file", e);
            } finally {
                try {
                    propertiesFile.close();
                } catch (IOException e) {
                    log.error("Problem while closing property file", e);
                }
            }
        } else {
            log.info("Could not get configuation file. Using default values");
        }
    }

    private static int getIntProperty(String propertyKey, int defaultValue) {
        return Integer.parseInt(properties.getProperty(propertyKey, Integer
                .toString(defaultValue)));
    }

    private static boolean getBooleanProperty(String propertyKey, boolean defaultValue) {
        return "true".equalsIgnoreCase(properties.getProperty(propertyKey, Boolean
                .toString(defaultValue)));
    }

    public static int getServerPort() {
        return getIntProperty(SERVER_PORT_KEY, 4040);
    }

    public static int getServerMaxConn() {
        return getIntProperty(SERVER_MAXCONN_KEY, 100);
    }

    public static int getServerClientTimeout() {
        return getIntProperty(SERVER_CLIENT_TIMEOUT_KEY, 5000 * 60);
    }

    public static String getServerAddress() {
        return properties.getProperty(SERVER_ADDRESS_KEY, "127.0.0.1");
    }

    public static int getZooTimeout() {
        return getIntProperty(ZOOKEEPER_TIMEOUT_KEY, 5000);
    }

    public static String getZooKeeperServerList() {
        return properties.getProperty(ZOOSERVER_LIST_KEY, "127.0.0.1:2181");
    }

    public static String getServerZnodeGroup() {
        return properties.getProperty(SERVERS_ZNODE_KEY, "/servers");
    }

    public static String getOpLogZnode() {
        return properties.getProperty(OPLOG_ZNODE_KEY, "/operations");
    }

    public static boolean shallRetrieveServerFromZooKeeper() {
        return getBooleanProperty(SERVER_CONF_ZOOKEEPER,false);
    }

}
