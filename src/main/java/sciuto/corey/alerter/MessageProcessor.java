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
package sciuto.corey.alerter;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sciuto.corey.alerter.drive.DriveClient;
import sciuto.corey.alerter.drive.IDriveWrapper;
import sciuto.corey.alerter.drive.google.GoogleDriveWrapper;
import sciuto.corey.alerter.mail.MessageParser;
import sciuto.corey.alerter.mail.MessageRetriever;
import sciuto.corey.alerter.model.ProcessedMessage;

/**
 * This is the main execution thread.
 * 
 * @author Corey Sciuto <corey.sciuto@gmail.com>
 * 
 */
public class MessageProcessor implements Runnable {

	private final static Logger LOGGER = LogManager.getLogger();

	private Properties applicationProperties;
	private MessageParser messageParser;
	private DriveClient googleDriveClient;
	private String rootDirectoryId;

	public MessageProcessor(Properties applicationProperties) {
		this.applicationProperties = applicationProperties;
		this.rootDirectoryId = configureDrive(applicationProperties);
		this.messageParser = new MessageParser(applicationProperties);
	}

	@Override
	public void run() {
		LOGGER.info("Running...");

		try {
			List<ProcessedMessage> messages = getMessages();
			if (messages != null && messages.size() > 0) {
				googleDriveClient.uploadMessages(messages, rootDirectoryId);
			}
		} catch (IOException e1) {
			LOGGER.error("Couldn't upload messages!", e1);
		}

		LOGGER.info("...Done.");
	}

	/**
	 * Configures the internal instance of the Google Drive client at startup.
	 * 
	 * @param applicationProperties
	 * @return The ID of the root directory.
	 */
	private String configureDrive(Properties applicationProperties) {

		String secretsLocation = applicationProperties.getProperty("drive.secrets.location");
		try {
			IDriveWrapper googleDrive = new GoogleDriveWrapper(secretsLocation);
			googleDriveClient = new DriveClient(googleDrive);
		} catch (IOException | GeneralSecurityException e) {
			LOGGER.error("Error creating Google Drive instance. Exiting...", e);
			System.exit(202);
		}

		try {
			return googleDriveClient.createRootFolder(applicationProperties.getProperty("drive.root.directory"));
		} catch (IOException e) {
			LOGGER.error("Error creating Google Drive root folder. Exiting...", e);
			System.exit(203);
		}

		return null; // You can't actually get here...
	}

	/**
	 * Downloads all new messages and their attachments. Returns information
	 * about each message in a List. Returns null if the messages can't be
	 * retrieved.
	 * 
	 * @return
	 */
	private List<ProcessedMessage> getMessages() {

		List<ProcessedMessage> processedMessages = null;

		MessageRetriever messageRetriever = new MessageRetriever(applicationProperties);

		Folder inboxFolder = null;
		try {
			inboxFolder = messageRetriever.openFolder();
		} catch (MessagingException e) {
			LOGGER.error("Could not open folder!", e);
			return null;
		}
		try {
			List<Message> messages = messageRetriever.retrieveUnreadMessages(inboxFolder);
			LOGGER.info("There are " + messages.size() + " unread messages.");

			if (messages.size() > 0) {
				processedMessages = messageParser.extractMessages(messages);
			} else {
				processedMessages = new ArrayList<ProcessedMessage>();
			}
		} catch (MessagingException e) {
			LOGGER.error("Error retrieving messages!", e);
		}
		try {
			messageRetriever.closeFolder(inboxFolder);
		} catch (MessagingException e) {
			LOGGER.error("Could not close folder!", e);
			return null;
		}

		return processedMessages;
	}

}
