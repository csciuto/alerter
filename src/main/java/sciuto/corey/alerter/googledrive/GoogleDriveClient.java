package sciuto.corey.alerter.googledrive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sciuto.corey.alerter.mail.ProcessedMessage;

import com.google.api.client.googleapis.media.MediaHttpUploader;
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

	public void uploadMessages(List<ProcessedMessage> messages) throws IOException {
		
		for (ProcessedMessage message : messages) {
			
			LOGGER.debug("Creating message folder...");

			com.google.api.services.drive.model.File directoryCreate = new com.google.api.services.drive.model.File();
			directoryCreate.setTitle(message.getSubject());
			directoryCreate.setMimeType("application/vnd.google-apps.folder");
			directoryCreate.setParents(rootDirectoryReferenceList);
			
			com.google.api.services.drive.model.File messageDirectory = googleDrive.files().insert(directoryCreate).execute();
			LOGGER.debug("...done");
			
			LOGGER.debug("Creating message body file...");
			
			ParentReference messageDirectoryReference = new ParentReference();
			messageDirectoryReference.setId(messageDirectory.getId());
			List<ParentReference> messageDirectoryReferenceList = Collections.singletonList(messageDirectoryReference);
			
			com.google.api.services.drive.model.File descriptionFile = new com.google.api.services.drive.model.File();
			descriptionFile.setParents(messageDirectoryReferenceList);
			descriptionFile.setTitle("Description");
			
			java.io.File messageBodyFile = new java.io.File(message.getMessageBodyFileName());
			
			com.google.api.client.http.FileContent mediaContent = new com.google.api.client.http.FileContent("text/plain", messageBodyFile);

		    googleDrive.files().insert(descriptionFile, mediaContent).execute();
		    
		    LOGGER.debug("...done");
			
		}
		
	}
	
}
