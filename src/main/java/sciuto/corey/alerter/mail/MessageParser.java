package sciuto.corey.alerter.mail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.mail.*;
import javax.mail.Flags.Flag;
import javax.mail.internet.MimeBodyPart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
				ProcessedMessage processedMessage = processMultiPartMessage(message);
				processedMessage.setSubject(messageSubject);
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
	 * Extracts any attachments (so far, only of type PDF). <br />
	 * Returns a list of PDF file names, the message subject, and the body.
	 * @param message
	 * @return
	 */
	private ProcessedMessage processMultiPartMessage(Message message) {

		UUID uuid = UUID.randomUUID();
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
					File directory = new File("alerts/" + uuid.toString());
					directory.mkdirs();
					MimeBodyPart mbp = (MimeBodyPart) bp;
					mbp.saveFile(directory.getAbsolutePath() + "/" +  bodyPartFileName);
					LOGGER.debug("...extracted.");

					processedMessage.addFileName(bodyPartFileName);

				} else if (bodyPartContentType.contains("multipart/alternative")) {

					LOGGER.debug("Extracting Body...");
					MimeBodyPart mbp = (MimeBodyPart) bp;
					String text = processSubMultipartAlternative(mbp);
					processedMessage.setMessageBody(text);
					LOGGER.debug("...extracted.");

				}
			}

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
	 * @param mbp The body part to extract from.
	 * @return
	 * @throws IOException
	 * @throws MessagingException
	 */
	private String processSubMultipartAlternative(MimeBodyPart mbp) throws IOException, MessagingException {

		Multipart mp = (Multipart) mbp.getContent();
		int numBodyParts = mp.getCount();

		for (int i = 0; i < numBodyParts; i++) {
			BodyPart bp = mp.getBodyPart(i);
			String bodyPartContentType = bp.getContentType();
			LOGGER.debug("-- Alternative Body part is of type " + bodyPartContentType);

			if (bodyPartContentType.contains("text/plain")) {
				LOGGER.debug("-- Extracting Text...");

				MimeBodyPart mbp2 = (MimeBodyPart) bp;
				String text = (String)mbp2.getContent();
				LOGGER.debug("-- ...extracted.");

				return text;
			} 
		}
		return "<No Body>";

	}
}
