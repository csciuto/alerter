package sciuto.corey.alerter.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.*;
import javax.mail.Flags.Flag;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Retrieves Messages from an inbox.
 * 
 * @author Corey
 * 
 */
public class MessageRetriever {

	private final static Logger LOGGER = LogManager.getLogger();

	private PropertiesFileAuthenticator authenticator;
	private Properties properties;

	private Session mailSession;
	private Store store;
	private Folder inboxFolder;

	private List<String> whitelist;

	/**
	 * Create a MessageProcessor with access to the passed-in properties.
	 * 
	 * @param properties
	 */
	public MessageRetriever(Properties properties) {
		this.authenticator = new PropertiesFileAuthenticator(properties);
		this.properties = properties;
		this.whitelist = new ArrayList<String>();

		String whitelistString = this.properties.getProperty("mail.whitelist");
		String[] tokens = whitelistString.split("--delim--");

		for (int i = 0; i < tokens.length; i++) {
			whitelist.add(tokens[i]);
		}
	}

	/**
	 * Call this to open the folder.
	 * 
	 * @throws MessagingException
	 */
	public void openFolder() throws MessagingException {
		mailSession = Session.getInstance(properties, authenticator);
		store = mailSession.getStore();
		store.connect();

		inboxFolder = store.getFolder("INBOX");
		inboxFolder.open(Folder.READ_WRITE);
	}

	/**
	 * Call to close the folder once all messages are processed.
	 * 
	 * @throws MessagingException
	 */
	public void closeFolder() throws MessagingException {
		inboxFolder.close(false);
		store.close();
	}

	/**
	 * Retrieve the unread messages in the open folder.
	 * 
	 * @param folder
	 * @return
	 * @throws MessagingException
	 */
	public List<Message> retrieveUnreadMessages() throws MessagingException {

		if (inboxFolder == null) {
			throw new IllegalStateException("Must call openFolder before retrieving messages");
		}

		List<Message> unreadMessages = new ArrayList<Message>();
		int messageCount = inboxFolder.getMessageCount();

		// getMessage starts at one.
		for (int i = 1; i <= messageCount; i++) {
			Message message = inboxFolder.getMessage(i);

			if (!message.getFlags().contains(Flag.SEEN)) {

				LOGGER.debug("Found unread message " + message.getSubject());

				boolean validSender = validateSender(message);

				if (validSender == true) {
					unreadMessages.add(message);
				} else {
					LOGGER.info("Invalid sender. Deleting message.");
					message.setFlag(Flag.SEEN, true);
					message.setFlag(Flag.DELETED, true);
				}
			}
		}

		return unreadMessages;
	}

	/**
	 * XXX: Security by obscurity is NOT security. Without requiring this
	 * application only accepts signed emails, I'm not sure what else can be
	 * done...
	 * 
	 * @param message
	 * @return
	 * @throws MessagingException
	 */
	private boolean validateSender(Message message) throws MessagingException {
		Address[] fromAddresses = message.getFrom();
		boolean validSender = false;

		for (int j = 0; j < fromAddresses.length; j++) {
			Address address = fromAddresses[j];

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Validating " + address.toString() + " against whitelist...");
			}

			if (whitelist.contains(address.toString())) {
				validSender = true;
				LOGGER.debug("Validated.");
				break;
			}
		}

		return validSender;
	}
}
