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

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {
	private final static Logger LOGGER;
	static {
		// Redirect JUL to Log4J. This must happen before the first call to
		// LogManager.
		System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
		LOGGER = LogManager.getLogger();
	}

	private static final long MINUTE = 1000L * 60l; // 1000 milliseconds x 60
													// seconds

	public static void main(String[] args) {
		LOGGER.info("Starting application...");
		String versionInformationProp = PropertiesReader.readFromClasspath("version.properties").getProperty(
				"alerter.version");
		LOGGER.info("Build information: " + versionInformationProp);
		LOGGER.debug("Properties:" + System.getProperties());

		String alerterProps = args[0];
		Properties applicationProperties = PropertiesReader.readFromFile(alerterProps);
		if (applicationProperties.size() == 0) {
			LOGGER.error("Could not load properties from " + applicationProperties + ". exiting...");
			System.exit(2);
		}

		MessageProcessor messageProcessor = new MessageProcessor(applicationProperties);
		boolean running = true;
		while (running) {
			try {
				Thread processingThread = new Thread(messageProcessor, "ProcessingThread");
				processingThread.start();
				processingThread.join();
				Thread.sleep(5 * MINUTE);
			} catch (InterruptedException e) {
				LOGGER.warn("Thread interrupted.", e);
				running = false;
			}
		}

		LOGGER.info("Shutdown.");
	}
}
