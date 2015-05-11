package sciuto.corey.alerter.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags.Flag;
import javax.mail.*;

/**
 * Retrieves Messages from an inbox.
 * @author Corey
 *
 */
public class MessageRetriever {

	private PropertiesFileAuthenticator authenticator;
	private Properties properties;
	
	private Session mailSession;
	private Store store;
	private Folder inboxFolder;
	
	/**
	 * Create a MessageProcessor with access to the passed-in properties.
	 * 
	 * @param properties
	 */
	public MessageRetriever(Properties properties) {
		this.authenticator = new PropertiesFileAuthenticator(properties);
		this.properties = properties;
	}

	/**
	 * Call this to open the folder.
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
				unreadMessages.add(message);
			}
		}

		return unreadMessages;
	}
}
