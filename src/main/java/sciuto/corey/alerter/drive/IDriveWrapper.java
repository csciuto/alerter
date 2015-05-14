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

import sciuto.corey.alerter.mail.ProcessedMessage;

/**
 * An interface specifying operations on a cloud drive service.
 * @author Corey
 *
 */
public interface IDriveWrapper {

	/**
	 * Locates the main directory that Drive will be writing to. Returns an ID
	 * if it is found, otherwise, null.
	 * 
	 * @param name The name of the directory to locate.
	 * @return An ID to identify the directory.
	 * @throws IOException
	 */
	public String locateRootDirectory(String name) throws IOException;

	/**
	 * Creates a root directory with permissions for all viewers to read.
	 * 
	 * @param name The name of the directory to create.
	 * @return An ID to identify the directory.
	 * @throws IOException
	 */
	public String createRootDirectory(String name) throws IOException;

	/**
	 * Creates the directory to put messages in
	 * @param message
	 * @param rootDirectoryId
	 * @return The ID of the created directory
	 * @throws IOException
	 */
	public String createSubdirectory(ProcessedMessage message, String rootDirectoryId) throws IOException;

	/**
	 * Uploads the file identified by fileLocation of type mimeType to the directory specified by directoryId
	 * @param directoryId
	 * @param fileLocation
	 * @param mimeType
	 * @throws IOException 
	 * @return the ID of the created file.
	 */
	String createFile(String directoryId, String fileLocation, String mimeType) throws IOException;
}
