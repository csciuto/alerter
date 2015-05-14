/*
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
package sciuto.corey.alerter.drive.google;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sciuto.corey.alerter.drive.IDriveWrapper;
import sciuto.corey.alerter.model.ProcessedMessage;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ParentReference;

/**
 * A class to de-couple Google Drive from the classes that use it to simplify
 * unit testing.
 * 
 * @author Corey Sciuto <corey.sciuto@gmail.com>
 * 
 */
public class GoogleDriveWrapper implements IDriveWrapper {

	private Drive drive;

	public GoogleDriveWrapper(Drive drive) {
		this.drive = drive;
	}

	@Override
	public String locateRootDirectory(String name) throws IOException {
		com.google.api.services.drive.Drive.Files.List listRequest = drive.files().list();
		listRequest.setQ("trashed=false");
		List<com.google.api.services.drive.model.File> files = listRequest.execute().getItems();

		for (com.google.api.services.drive.model.File file : files) {
			if (file.getTitle().equals(name)) {
				String id = file.getId();
				return id;
			}
		}

		return null;
	}

	@Override
	public String createRootDirectory(String name) throws IOException {

		com.google.api.services.drive.model.File rootDirectoryCreate = new com.google.api.services.drive.model.File();
		rootDirectoryCreate.setTitle(name);
		rootDirectoryCreate.setMimeType("application/vnd.google-apps.folder");

		com.google.api.services.drive.model.File rootDirectory = drive.files().insert(rootDirectoryCreate).execute();
		String id = rootDirectory.getId();

		com.google.api.services.drive.model.Permission allRead = new com.google.api.services.drive.model.Permission();
		allRead.setType("anyone");
		allRead.setRole("reader");
		drive.permissions().insert(id, allRead).execute();

		return id;
	}

	@Override
	public String createSubdirectory(ProcessedMessage message, String rootDirectoryId) throws IOException {
		com.google.api.services.drive.model.File directoryCreate = new com.google.api.services.drive.model.File();
		directoryCreate.setTitle(message.getSubject());
		directoryCreate.setMimeType("application/vnd.google-apps.folder");
		directoryCreate.setParents(getRootDirectoryReferenceList(rootDirectoryId));

		com.google.api.services.drive.model.File messageDirectory = drive.files().insert(directoryCreate).execute();

		return messageDirectory.getId();
	}

	@Override
	public String createFile(String messageDirectoryId, String fileLocation, String mimeType) throws IOException {
		ParentReference messageDirectoryReference = new ParentReference();
		messageDirectoryReference.setId(messageDirectoryId);
		List<ParentReference> messageDirectoryReferenceList = Collections.singletonList(messageDirectoryReference);

		com.google.api.services.drive.model.File descriptionFile = new com.google.api.services.drive.model.File();
		descriptionFile.setParents(messageDirectoryReferenceList);
		String fileName = fileLocation.substring(fileLocation.lastIndexOf("/") + 1, fileLocation.length());
		descriptionFile.setTitle(fileName);

		java.io.File messageBodyFile = new java.io.File(fileLocation);

		com.google.api.client.http.FileContent mediaContent = new com.google.api.client.http.FileContent(mimeType,
				messageBodyFile);

		com.google.api.services.drive.model.File createdFile = drive.files().insert(descriptionFile, mediaContent)
				.execute();
		return createdFile.getId();
	}

	/**
	 * Google Drive has a complex way of storing links to parent directories.
	 * Return the wrapper object.
	 * 
	 * @param id
	 *            The ID of the directory at the root.
	 */
	private List<ParentReference> getRootDirectoryReferenceList(String id) {
		List<ParentReference> rootDirectoryReferenceList = new ArrayList<ParentReference>();
		ParentReference rootDirectoryReference = new ParentReference();
		rootDirectoryReference.setId(id);
		rootDirectoryReferenceList.add(rootDirectoryReference);
		return rootDirectoryReferenceList;
	}
}
