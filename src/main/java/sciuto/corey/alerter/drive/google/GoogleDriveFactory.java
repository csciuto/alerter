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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import sciuto.corey.alerter.drive.IDriveWrapper;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

/**
 * 
 * Creates a Google Drive client
 * 
 * @author Corey
 * 
 */
public class GoogleDriveFactory {

	/**
	 * Creates the Google Drive client. Pass in the pointer to
	 * clients_secret.json.
	 * 
	 * @see https://developers.google.com/identity/protocols/OAuth2
	 * 
	 * @param secretsLocation
	 * @return
	 * @throws IOException
	 * @throws GeneralSecurityException 
	 */
	public static IDriveWrapper createDrive(String secretsLocation) throws IOException, GeneralSecurityException {
		
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		File dataStoreDirectory = new File("dataStore/");
		dataStoreDirectory.mkdir();
		FileDataStoreFactory dataStoreFactory = new FileDataStoreFactory(dataStoreDirectory);
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new FileReader(secretsLocation));
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory,
				clientSecrets, Collections.singleton(DriveScopes.DRIVE_FILE)).setDataStoreFactory(dataStoreFactory)
				.build();
	
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
		
		Drive drive = new Drive.Builder(httpTransport, jsonFactory, credential).setApplicationName(
				"CoreySciuto-Alerter/0.1").build();
		return new GoogleDriveWrapper(drive);
	}

}
