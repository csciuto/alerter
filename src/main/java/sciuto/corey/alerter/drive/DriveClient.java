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
package sciuto.corey.alerter.drive;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sciuto.corey.alerter.model.Attachment;
import sciuto.corey.alerter.model.ProcessedMessage;

/**
 * A client around an abstract Drive class.
 * 
 * @author Corey
 * 
 */
public class DriveClient {

	private final static Logger LOGGER = LogManager.getLogger();

	private IDriveWrapper drive;

	public DriveClient(IDriveWrapper googleDrive) {
		this.drive = googleDrive;
	}

	/**
	 * Creates a directory called at the root of the hierarchy.
	 * 
	 * @param name
	 * @throws IOException
	 * @return An ID for the created directory.
	 */
	public String createRootFolder(String name) throws IOException {

		LOGGER.debug("Looking for Root Directory " + name);
		String id = drive.locateRootDirectory(name);

		if (id == null) {
			LOGGER.debug("Not found. Creating " + name);
			id = drive.createRootDirectory(name);
		}

		LOGGER.debug("Root Drive Folder ID: " + id);
		return id;

	}

	/**
	 * Uploads the ProcessedMessages to Google Drive. Creates a directory with
	 * the message's subject as a name, and uploads all files to it.
	 * 
	 * @param messages
	 * @param rootDirectoryId
	 * @throws IOException
	 */
	public void uploadMessages(List<ProcessedMessage> messages, String rootDirectoryId) throws IOException {

		for (ProcessedMessage message : messages) {

			LOGGER.info("Uploading message " + message.getSubject());
			LOGGER.debug("Creating message folder " + message.getSubject());
			String messageDirectoryId = drive.createSubdirectory(message, rootDirectoryId);
			LOGGER.debug("...done");

			if (message.getMessageBodyFileName() != null) {
				LOGGER.debug("Creating message body file " + message.getMessageBodyFileName());
				drive.createFile(messageDirectoryId, message.getMessageBodyFileName(), "text/plain");
				LOGGER.debug("...done");
			}

			List<Attachment> attachments = message.getAttachments();
			for (Attachment attachment : attachments) {
				LOGGER.debug("Creating attachment " + attachment.getFileLocation());
				drive.createFile(messageDirectoryId, attachment.getFileLocation(), attachment.getMimeType());
				LOGGER.debug("...done");
			}
			LOGGER.info("Uploaded.");
		}

	}

}
