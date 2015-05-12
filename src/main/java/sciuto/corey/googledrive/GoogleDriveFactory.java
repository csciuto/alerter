/*
 * Borrows liberally from a sample at 
 * https://github.com/google/google-api-java-client-samples/blob/master/
 * drive-cmdline-sample/src/main/java/com/google/api/services/samples/drive/cmdline/DriveSample.java
 * Copyright (c) 2012 Google Inc.
 * 
 * Original author rmistry@google.com (Ravi Mistry)
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
package sciuto.corey.googledrive;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

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
 * Creates connections to Google Drive
 * 
 * @author rmistry@google.com (Ravi Mistry)
 * @author Corey
 * 
 */
public class GoogleDriveFactory {

	private static FileDataStoreFactory dataStoreFactory;
	private static HttpTransport httpTransport;
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/**
	 * Authorizes the installed application to access user's protected data.
	 * 
	 * @throws IOException
	 * */
	private static Credential authorize(String secretsLocation) throws IOException {

		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new FileReader(secretsLocation));
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY,
				clientSecrets, Collections.singleton(DriveScopes.DRIVE_FILE)).setDataStoreFactory(dataStoreFactory)
				.build();

		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
	}

	/**
	 * Creates the Google Drive client. Pass in the pointer to
	 * clients_secret.json.
	 * 
	 * @see https://developers.google.com/identity/protocols/OAuth2
	 * 
	 * @param secretsLocation
	 * @return
	 * @throws IOException
	 */
	public Drive createDrive(String secretsLocation) throws IOException {
		Credential credential = authorize(secretsLocation);
		Drive drive = new Drive.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(
				"CoreySciuto-Alerter/0.1").build();
		return drive;
	}

	public GoogleDriveFactory() throws GeneralSecurityException, IOException {
		httpTransport = GoogleNetHttpTransport.newTrustedTransport();

		File dataStoreDirectory = new File("dataStore/");
		dataStoreDirectory.mkdir();
		dataStoreFactory = new FileDataStoreFactory(dataStoreDirectory);
	}
}
