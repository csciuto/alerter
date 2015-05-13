package sciuto.corey.alerter.googledrive;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.services.drive.Drive;

public class GoogleDriveClient {

	private final static Logger LOGGER = LogManager.getLogger();
	
	private Drive googleDrive;
	private String rootDriveFolderId;
	
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
		
		rootDriveFolderId = rootDirectory.getId();		
		LOGGER.debug("Root Drive Folder ID: " + rootDriveFolderId);
		
	}
	
}
