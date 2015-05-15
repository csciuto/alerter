/* 
 * Copyright (c) 2015 Corey Sciuto <corey.sciuto@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package sciuto.corey.alerter.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags.Flag;
import javax.mail.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Retrieves Messages from an inbox.
 * 
 * @author Corey Sciuto <corey.sciuto@gmail.com>
 * 
 */
public class MessageRetriever {

	private final static Logger LOGGER = LogManager.getLogger();

	private PropertiesFileAuthenticator authenticator;
	private Properties properties;

	private List<String> whitelist;

	/**
	 * Create a MessageProcessor with access to the passed-in properties.
	 * 
	 * @param properties
	 */
	public MessageRetriever(Properties properties) {
		this.authenticator = new PropertiesFileAuthenticator(properties);
		this.properties = properties;

		String whiteListString = this.properties.getProperty("mail.whitelist");
		this.whitelist = MailUtils.parseWhiteList(whiteListString, "--delim--");
	}

	/**
	 * Call this to open the folder.
	 * 
	 * @return The open folder
	 * @throws MessagingException
	 */
	public Folder openFolder() throws MessagingException {
		Session mailSession = Session.getInstance(properties, authenticator);
		Store store = mailSession.getStore();
		store.connect();
	
		Folder inboxFolder = store.getFolder("INBOX");
		inboxFolder.open(Folder.READ_WRITE);
	
		return inboxFolder;
	}

	/**
	 * Call to close the folder once all messages are processed.
	 * 
	 * @throws MessagingException
	 */
	public void closeFolder(Folder inboxFolder) throws MessagingException {
		inboxFolder.close(false);
	
		Store store = inboxFolder.getStore();
		store.close();
	}

	/**
	 * Retrieve the unread messages in the open folder.
	 * 
	 * @param folder
	 * @return
	 * @throws MessagingException
	 */
	public List<Message> retrieveUnreadMessages(Folder inboxFolder) throws MessagingException {

		if (!inboxFolder.isOpen()) {
			inboxFolder.open(Folder.READ_WRITE);
		}
		
		List<Message> unreadMessages = new ArrayList<Message>();
		int messageCount = inboxFolder.getMessageCount();

		// getMessage starts at one.
		for (int i = 1; i <= messageCount; i++) {
			Message message = inboxFolder.getMessage(i);

			if (!message.getFlags().contains(Flag.SEEN)) {

				LOGGER.debug("Found unread message " + message.getSubject());

				boolean validSender = MailUtils.validateSender(message.getFrom(), whitelist);

				if (validSender == true) {
					unreadMessages.add(message);
				} else {
					LOGGER.debug("Invalid sender. Marking message as read.");
					message.setFlag(Flag.SEEN, true);
					// message.setFlag(Flag.DELETED, true); Overkill.
				}
			}
		}

		return unreadMessages;
	}
}
