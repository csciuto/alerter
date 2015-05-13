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
package sciuto.corey.alerter.googledrive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sciuto.corey.alerter.mail.Attachment;
import sciuto.corey.alerter.mail.ProcessedMessage;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ParentReference;

public class GoogleDriveClient {

	private final static Logger LOGGER = LogManager.getLogger();
	
	private Drive googleDrive;
	private List<ParentReference> rootDirectoryReferenceList;
	
	public GoogleDriveClient(Drive googleDrive) {
		this.googleDrive = googleDrive; 
	}
	
	/**
	 * Creates a directory called "alerts" at the root of the hierarchy.
	 * @throws IOException
	 */
	public void createRootFolder() throws IOException {
		com.google.api.services.drive.Drive.Files.List listRequest = googleDrive.files().list();
		listRequest.setQ("trashed=false");
		List<com.google.api.services.drive.model.File> files = listRequest.execute().getItems();

		com.google.api.services.drive.model.File rootDirectory = null;
		
		boolean found = false;
		for (com.google.api.services.drive.model.File file : files) {
			if (file.getTitle().equals("alerts")) {
				found = true;
				rootDirectory = file;
				break;
			}
		}

		if (!found) {
			LOGGER.debug("Creating root Drive folder...");

			com.google.api.services.drive.model.File rootDirectoryCreate = new com.google.api.services.drive.model.File();
			rootDirectoryCreate.setTitle("alerts");
			rootDirectoryCreate.setMimeType("application/vnd.google-apps.folder");
			
			rootDirectory = googleDrive.files().insert(rootDirectoryCreate).execute();
			LOGGER.debug("...done");
			
			LOGGER.debug("Setting permissions...");
			com.google.api.services.drive.model.Permission allRead = new com.google.api.services.drive.model.Permission();
			allRead.setType("anyone");
			allRead.setRole("reader");
			googleDrive.permissions().insert(rootDirectory.getId(), allRead).execute();
			LOGGER.debug("...done");
		}
		
		rootDirectoryReferenceList = new ArrayList<ParentReference>();
		ParentReference rootDirectoryReference = new ParentReference();
		rootDirectoryReference.setId(rootDirectory.getId());
		rootDirectoryReferenceList.add(rootDirectoryReference);
		
		LOGGER.debug("Root Drive Folder ID: " + rootDirectory.getId());
		
	}

	/**
	 * Uploads the ProcessedMessages to Google Drive. Creates a directory with the message's subject as a name, and uploads all files to it.
	 * @param messages
	 * @throws IOException
	 */
	public void uploadMessages(List<ProcessedMessage> messages) throws IOException {
		
		for (ProcessedMessage message : messages) {
			
			LOGGER.debug("Creating message folder " + message.getSubject());

			com.google.api.services.drive.model.File directoryCreate = new com.google.api.services.drive.model.File();
			directoryCreate.setTitle(message.getSubject());
			directoryCreate.setMimeType("application/vnd.google-apps.folder");
			directoryCreate.setParents(rootDirectoryReferenceList);
			
			com.google.api.services.drive.model.File messageDirectory = googleDrive.files().insert(directoryCreate).execute();
			LOGGER.debug("...done");
			
			if (message.getMessageBodyFileName() != null) {
				LOGGER.debug("Creating message body file " + message.getMessageBodyFileName());
				createFile(messageDirectory, message.getMessageBodyFileName(), "text/plain");
			    LOGGER.debug("...done");
			}
		    
		    List<Attachment> attachments = message.getAttachments();
		    if (attachments != null){
		    	for (Attachment attachment : attachments) {
		    		LOGGER.debug("Creating attachment " + attachment.getFileLocation());
		    		createFile(messageDirectory, attachment.getFileLocation(), attachment.getMimeType());
		    		LOGGER.debug("...done");
		    	}
		    }
			
		}
		
	}
	
	/**
	 * Uploads the file identified by fileLocation of type mimeType to the directory parentDirectory
	 * @param parentDirectory
	 * @param fileLocation
	 * @param mimeType
	 * @throws IOException 
	 */
	private void createFile(com.google.api.services.drive.model.File parentDirectory, String fileLocation, String mimeType) throws IOException {
		ParentReference messageDirectoryReference = new ParentReference();
		messageDirectoryReference.setId(parentDirectory.getId());
		List<ParentReference> messageDirectoryReferenceList = Collections.singletonList(messageDirectoryReference);
		
		com.google.api.services.drive.model.File descriptionFile = new com.google.api.services.drive.model.File();
		descriptionFile.setParents(messageDirectoryReferenceList);
		String fileName = fileLocation.substring(fileLocation.lastIndexOf("/")+1, fileLocation.length());
		descriptionFile.setTitle(fileName);
		
		java.io.File messageBodyFile = new java.io.File(fileLocation);
		
		com.google.api.client.http.FileContent mediaContent = new com.google.api.client.http.FileContent(mimeType, messageBodyFile);

	    googleDrive.files().insert(descriptionFile, mediaContent).execute();
	}
	
}
