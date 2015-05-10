package sciuto.corey.alerter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sciuto.corey.alerter.mail.PropertiesFileAuthenticator;
import sciuto.corey.alerter.util.MessageProcessor;
import sciuto.corey.alerter.util.PropertiesReader;

/**
 * Hello world!
 * 
 */
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
		String versionInformationProp = PropertiesReader.read("version.properties").getProperty("alerter.version");
		LOGGER.info("Build information: " + versionInformationProp);
		LOGGER.debug("Properties:" + System.getProperties());

		String alerterProps = args[0];

		InputStream propertiesFile = null;
		Properties mailProperties = null;
		try {
			propertiesFile = new FileInputStream(new File(alerterProps));
			mailProperties = new Properties();
			mailProperties.load(propertiesFile);

			if (mailProperties.size() == 0) {
				LOGGER.error(alerterProps + " did not contain any properties. Exiting...");
				System.exit(1);
			}
		} catch (IOException e) {
			LOGGER.error(alerterProps + " could not be opened. Exiting...");
			System.exit(2);
		}

		PropertiesFileAuthenticator authenticator = new PropertiesFileAuthenticator(mailProperties);
		Session mailSession = Session.getInstance(mailProperties, authenticator);
		
		Store store = null;
		try {
			store = mailSession.getStore();
		} catch (NoSuchProviderException e) {
			LOGGER.error("Could not find default Store. Check your properties file! Exiting...");
			System.exit(3);
		}
		
		try {
			store.connect();
		} catch (MessagingException e) {
			LOGGER.error("Could not connect to the mail server!");
			System.exit(4);
		}
		
		Folder inboxFolder=null;
		try {
			inboxFolder = store.getFolder("INBOX");
			inboxFolder.open(Folder.READ_WRITE);
		} catch (MessagingException e) {
			LOGGER.error("Error opening folder. Exiting...");
			System.exit(5);
		}

		MessageProcessor messageProcessor = new MessageProcessor();
		try {
			messageProcessor.processMessages(inboxFolder);
		} catch (MessagingException e) {
			LOGGER.error("Error processing messages", e);
		}
		
		LOGGER.info("Shutdown.");
	}
}
