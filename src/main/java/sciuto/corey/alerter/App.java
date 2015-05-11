package sciuto.corey.alerter;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sciuto.corey.alerter.util.PropertiesReader;

public class App {
	private final static Logger LOGGER;
	static {
		// Redirect JUL to Log4J. This must happen before the first call to
		// LogManager.
		System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
		LOGGER = LogManager.getLogger();
	}

	public static void main(String[] args) {
		LOGGER.info("Starting application...");
		String versionInformationProp = PropertiesReader.readFromClasspath("version.properties").getProperty(
				"alerter.version");
		LOGGER.info("Build information: " + versionInformationProp);
		LOGGER.debug("Properties:" + System.getProperties());

		String alerterProps = args[0];
		Properties applicationProperties = PropertiesReader.readFromFile(alerterProps);
		if (applicationProperties.size() == 0) {
			LOGGER.error("Could not load properties from " + applicationProperties + ". exiting...");
			System.exit(2);
		}


		Thread processingThread = new Thread(new ProcessingRunnable(applicationProperties), "ProcessingThread");
		processingThread.start();
		
		try {
			processingThread.join();
		} catch (InterruptedException e) {
			LOGGER.warn("Thread interrupted.",e);
		}
		
		LOGGER.info("Shutdown.");
	}
}
