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
package sciuto.corey.alerter.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertiesReader {

	public static Logger LOGGER = LogManager.getLogger();

	/**
	 * Reads in the specified file from the classpath. If there's a problem
	 * retrieving it, it returns an empty Properties list.
	 * 
	 * @param fileName
	 * @return
	 */
	public static Properties readFromClasspath(String fileName) {
		Properties props = null;
		try {
			props = new Properties();
			URL url = PropertiesReader.class.getClassLoader().getResource(fileName);
			InputStream propertiesFile = url.openStream();
			props.load(propertiesFile);
		} catch (Exception e) {
			LOGGER.error("Cannot read file " + fileName, e);
		}
		return props;
	}

	/**
	 * Reads in the specified file from the file system. If there's a problem
	 * retrieving it, it returns an empty Properties list.
	 * 
	 * @param fileName
	 * @return
	 */
	public static Properties readFromFile(String fileName) {

		Properties props = new Properties();
		try {
			InputStream propertiesFile = new FileInputStream(new File(fileName));
			props.load(propertiesFile);

		} catch (IOException e) {
			LOGGER.error(fileName + " could not be opened. Exiting...");
		}
		
		return props;
	}
}
