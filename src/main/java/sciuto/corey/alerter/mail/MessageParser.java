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
package sciuto.corey.alerter.mail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.mail.*;
import javax.mail.Flags.Flag;
import javax.mail.internet.MimeBodyPart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sciuto.corey.alerter.model.ProcessedMessage;

/**
 * Extracts the Message Subject, Body, and attachments (PDF only) from Messages and stores them locally.
 * @author Corey
 *
 */
public class MessageParser {

	private static final Logger LOGGER = LogManager.getLogger();

	public List<ProcessedMessage> extractMessages(List<Message> unreadMessages) throws MessagingException {

		List<ProcessedMessage> processedMessages = new ArrayList<ProcessedMessage>();

		for (Message message : unreadMessages) {

			String messageSubject = message.getSubject();
			LOGGER.info("Processing " + messageSubject + "...");

			String contentType = message.getContentType();
			LOGGER.debug("Content type: " + contentType);

			if (contentType.contains("multipart/mixed")) {
				ProcessedMessage processedMessage = processMultiPartMixedMessage(message);
				processedMessages.add(processedMessage);
			}  else if (contentType.contains("multipart/alternative")) {
				ProcessedMessage processedMessage = processMultiPartAlternativeMessage(message);
				processedMessages.add(processedMessage);
			} else {
				LOGGER.debug("Not handling contentType of " + contentType);
			}

			message.setFlag(Flag.SEEN, true);

			LOGGER.info("...processed.");
		}

		LOGGER.info("Processed messages.");

		return processedMessages;
	}
	
	/**
	 * Returns a message subject and body from a message.
	 * @param message
	 * @return
	 */
	private ProcessedMessage processMultiPartAlternativeMessage(Message message) {
		ProcessedMessage processedMessage = new ProcessedMessage();

		File directory = createDirectory();
		
		try {
			Multipart mp = (Multipart) message.getContent();
			String text = processMultiPartAlternativeBodyPart( mp );
			
			String fileName = directory.getAbsolutePath() + "/" + "messageBody.txt";
			FileOutputStream fos = new FileOutputStream(fileName);
			fos.write(text.getBytes());
			fos.close();
			
			processedMessage.setMessageBodyFileName(fileName);
			
			processedMessage.setSubject(message.getSubject());
			return processedMessage;
		} catch (IOException e) {
			LOGGER.error("Error getting message content:", e);
			return null;
		} catch (MessagingException e) {
			LOGGER.error("Couldn't process message", e);
			return null;
		}
	}

	/**
	 * Extracts any attachments (so far, only of type PDF). <br />
	 * Returns a list of PDF file names and the subject and body.
	 * @param message
	 * @return
	 */
	private ProcessedMessage processMultiPartMixedMessage(Message message) {

		File directory = createDirectory();
		
		ProcessedMessage processedMessage = new ProcessedMessage();

		try {
			Multipart mp = (Multipart) message.getContent();

			int numBodyParts = mp.getCount();
			LOGGER.debug("There are " + numBodyParts + " body parts to this message.");

			for (int i = 0; i < numBodyParts; i++) {
				BodyPart bp = mp.getBodyPart(i);
				String bodyPartContentType = bp.getContentType();
				String bodyPartFileName = bp.getFileName();
				
				LOGGER.debug("Body part " + i + " is of type " + bodyPartContentType);

				if (bodyPartContentType.contains("application/pdf")) {

					LOGGER.debug("Extracting PDF...");
					MimeBodyPart mbp = (MimeBodyPart) bp;
					String fileName = directory.getAbsolutePath() + "/" + bodyPartFileName;
					mbp.saveFile(fileName);
					LOGGER.debug("...extracted.");

					processedMessage.addAttachment(fileName, "application/pdf");

				} else if (bodyPartContentType.contains("application/msword")) {

					LOGGER.debug("Extracting Word Document...");
					MimeBodyPart mbp = (MimeBodyPart) bp;
					String fileName = directory.getAbsolutePath() + "/" + bodyPartFileName;
					mbp.saveFile(fileName);
					LOGGER.debug("...extracted.");

					processedMessage.addAttachment(fileName, "application/msword");

				} else if (bodyPartContentType.contains("multipart/alternative")) {

					LOGGER.debug("Extracting Body...");

					MimeBodyPart mbp = (MimeBodyPart) bp;
					String text = processMultiPartAlternativeBodyPart( (Multipart) mbp.getContent() );
					
					String fileName = directory.getAbsolutePath() + "/" + "messageBody.txt";
					FileOutputStream fos = new FileOutputStream(fileName);
					fos.write(text.getBytes());
					fos.close();
					
					processedMessage.setMessageBodyFileName(fileName);
					LOGGER.debug("...extracted.");

				}
			}

			processedMessage.setSubject(message.getSubject());
			return processedMessage;

		} catch (IOException e) {
			LOGGER.error("Error getting message content:", e);
			return null;
		} catch (MessagingException e) {
			LOGGER.error("Couldn't process message", e);
			return null;
		}

	}

	/**
	 * Return the text/plain value found in this MimeBodyPart
	 * @param mp The body part to extract from.
	 * @return
	 * @throws IOException
	 * @throws MessagingException
	 */
	private String processMultiPartAlternativeBodyPart(Multipart mp) throws IOException, MessagingException {

		int numBodyParts = mp.getCount();

		for (int i = 0; i < numBodyParts; i++) {
			BodyPart bp = mp.getBodyPart(i);
			String bodyPartContentType = bp.getContentType();
			LOGGER.debug("Alternative Body part is of type " + bodyPartContentType);

			if (bodyPartContentType.contains("text/plain")) {
				LOGGER.debug("Extracting Text...");

				MimeBodyPart mbp2 = (MimeBodyPart) bp;
				String text = (String)mbp2.getContent();
				LOGGER.debug("...extracted.");

				return text;
			} 
		}
		return "<No Body>";

	}

	private File createDirectory() {
		UUID uuid = UUID.randomUUID();
		File directory = new File("alerts/" + uuid.toString());
		directory.mkdirs();
		
		return directory;
	}
}
