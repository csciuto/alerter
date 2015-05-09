package sciuto.corey.alerter.util;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertiesReader {

	public static Logger LOGGER = LogManager.getLogger();

	/**
	 * Reads in the specified file. If there's a problem retrieving it, returns null.
	 * @param fileName
	 * @return
	 */
	public static Properties read(String fileName) {
		Properties props = null;
		try {
			props = new Properties();
			URL url = PropertiesReader.class.getClassLoader().getResource(fileName);
			InputStream propertiesFile = url.openStream();
			props.load(propertiesFile);
		} catch (Exception e) {
			LOGGER.error("Cannot read file " + fileName, e);
		}
		return props;
	}
}
