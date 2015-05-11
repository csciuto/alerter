package sciuto.corey.alerter;

import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sciuto.corey.alerter.mail.MessageParser;
import sciuto.corey.alerter.mail.MessageRetriever;
import sciuto.corey.alerter.mail.ProcessedMessage;
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
		String versionInformationProp = PropertiesReader.readFromClasspath("version.properties").getProperty(
				"alerter.version");
		LOGGER.info("Build information: " + versionInformationProp);
		LOGGER.debug("Properties:" + System.getProperties());

		String alerterProps = args[0];
		Properties applicationProperties = PropertiesReader.readFromFile(alerterProps);
		if (applicationProperties.size() == 0) {
			LOGGER.error("Could not load properties from " + applicationProperties + ". exiting...");
			System.exit(1);
		}

		List<ProcessedMessage> processedMessages = getMessages(applicationProperties);

		LOGGER.info("Shutdown.");
	}

	private static List<ProcessedMessage> getMessages(Properties applicationProperties) {

		List<ProcessedMessage> processedMessages = null;
		
		MessageRetriever messageRetriever = new MessageRetriever(applicationProperties);

		try {
			messageRetriever.openFolder();
		} catch (MessagingException e1) {
			LOGGER.error("Could not open folder exiting...", e1);
			System.exit(101);
		}

		List<Message> messages = null;
		try {
			messages = messageRetriever.retrieveUnreadMessages();
			LOGGER.info("There are " + messages.size() + " unread messages.");
		} catch (MessagingException e) {
			LOGGER.error("Could not retrieve messages from INBOX. exiting...");
			System.exit(102);
		}

		MessageParser messageParser = new MessageParser();
		try {
			processedMessages = messageParser.extractMessages(messages);
		} catch (MessagingException e1) {
			LOGGER.error("Could not extract messages! exiting...", e1);
			System.exit(103);
		}

		try {
			messageRetriever.closeFolder();
		} catch (MessagingException e) {
			LOGGER.error("Could not close folder! exiting...", e);
			System.exit(104);
		}

		return processedMessages;
	}
}
