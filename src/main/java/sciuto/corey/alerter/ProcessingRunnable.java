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

/**
 * This is the main execution thread.
 * 
 * @author Corey
 * 
 */
public class ProcessingRunnable implements Runnable {

	private final static Logger LOGGER = LogManager.getLogger();

	private Properties applicationProperties;

	private static final long MINUTE = 1000L * 60l; // 1000 milliseconds x 60 seconds
	
	public ProcessingRunnable(Properties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}

	@Override
	public void run() {
		boolean running = true;
		while (running) {
			LOGGER.info("Running...");
			
			List<ProcessedMessage> messages = getMessages();
			
			LOGGER.info("...Sleeping...");
			try {
				Thread.sleep(5 * MINUTE);
			} catch (InterruptedException e) {
				LOGGER.warn("Thread interrupted. Ending execution", e);
				running = false;
			}
		}
	}

	/**
	 * Downloads all new messages and their attachments. Returns information about each message in a List.
	 * @return
	 */
	private List<ProcessedMessage> getMessages() {

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
