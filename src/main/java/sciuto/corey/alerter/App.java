package sciuto.corey.alerter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sciuto.corey.alerter.util.PropertiesReader;

/**
 * Hello world!
 *
 */
public class App 
{
	private final static Logger LOGGER = LogManager.getLogger();
	
    public static void main( String[] args )
    {
    	LOGGER.info( "Starting application...");
    	String versionInformationProp = PropertiesReader.read("version.properties").getProperty("alerter.version");
        LOGGER.info( "Build information: " + versionInformationProp );
        LOGGER.info( "Shutdown." );
    }
}
