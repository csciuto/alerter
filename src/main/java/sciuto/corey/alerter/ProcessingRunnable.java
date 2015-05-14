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
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sciuto.corey.alerter.drive.DriveClient;
import sciuto.corey.alerter.drive.IDriveWrapper;
import sciuto.corey.alerter.drive.google.GoogleDriveFactory;
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
	private DriveClient googleDriveClient;
	private String rootDirectoryId;

	private static final long MINUTE = 1000L * 60l; // 1000 milliseconds x 60
													// seconds

	public ProcessingRunnable(Properties applicationProperties) {
		this.applicationProperties = applicationProperties;
		this.rootDirectoryId = configureDrive(applicationProperties);
	}

	@Override
	public void run() {
		boolean running = true;
		while (running) {
			LOGGER.info("Running...");

			List<ProcessedMessage> messages = getMessages();
			try {
				googleDriveClient.uploadMessages(messages, rootDirectoryId);
			} catch (IOException e1) {
				LOGGER.error("Couldn't upload messages!",e1);
			}

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
	 * Configures the internal instance of the Google Drive client at startup.
	 * 
	 * @param applicationProperties
	 * @return The ID of the root directory.
	 */
	private String configureDrive(Properties applicationProperties) {
	
		String secretsLocation = applicationProperties.getProperty("drive.secrets.location");
		try {
			IDriveWrapper googleDrive = GoogleDriveFactory.createDrive(secretsLocation);
			googleDriveClient = new DriveClient(googleDrive);
		} catch (IOException | GeneralSecurityException e) {
			LOGGER.error("Error creating Google Drive instance. Exiting...", e);
			System.exit(202);
		}
	
		try {
			return googleDriveClient.createRootFolder();
		} catch (IOException e) {
			LOGGER.error("Error creating Google Drive root folder. Exiting...",e);
			System.exit(203);
		}
		
		return null; // You can't actually get here...
	}

	/**
	 * Downloads all new messages and their attachments. Returns information
	 * about each message in a List.
	 * 
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
