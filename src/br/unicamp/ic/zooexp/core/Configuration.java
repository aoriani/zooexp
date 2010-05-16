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
    
    //======================================================================
    // Config Keys
    //======================================================================
    /** The port the server listens to */
    private static final String SERVER_PORT_KEY = "server_port";
    /** The number of connection the server can hold */
    private static final String SERVER_MAXCONN_KEY = "server_max_conn";
   //======================================================================

    //LoadConfiguration  file
    static {
	properties = new Properties() ;
	URL url =  ClassLoader.getSystemResource(CONF_FILE);
	if (url != null) {
	    FileInputStream propertiesFile = null  ;
	    try {
		propertiesFile = new FileInputStream(new File(url.getFile()));
		properties.load(propertiesFile);
	    } catch (FileNotFoundException e) {
		log.warn("Could not open properties file", e);
	    } catch (IOException e) {
		log.error("Problem while loading property file", e);
	    } finally{
		try {
		    propertiesFile.close();
		} catch (IOException e) {
		    log.error("Problem while closing property file", e);
		}
	    }
	} else{
	    log.info("Could not get configuation file. Using default values");
	}
    }
    
    private static int getIntProperty(String propertyKey,int defaultValue){
	return Integer.parseInt(properties.getProperty(propertyKey, Integer.toString(defaultValue)));
    }
    
    public static int getServerPort(){
	return getIntProperty(SERVER_PORT_KEY,4040);
    }
    
    public static int getServerMaxConn(){
	return getIntProperty(SERVER_MAXCONN_KEY, 100);
    }
    
    


}
