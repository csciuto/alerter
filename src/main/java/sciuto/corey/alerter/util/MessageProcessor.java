package sciuto.corey.alerter.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.*;
import javax.mail.Flags.Flag;
import javax.mail.internet.MimeBodyPart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageProcessor {

	Logger LOGGER = LogManager.getLogger();

	public void processMessages(Folder folder) throws MessagingException {

		List<Message> unreadMessages = retrieveUnreadMessages(folder);
		LOGGER.info("There are " + unreadMessages.size() + " unread messages.");
		
		for (Message message : unreadMessages) {
			String messageSubject = message.getSubject();
			LOGGER.info("Found message " + messageSubject + ". Processing...");

			String contentType = message.getContentType();
			LOGGER.debug("Content type: " + contentType);

			if (contentType.contains("multipart/mixed")) {
				try {
					Multipart mp = (Multipart) message.getContent();
					
					int numBodyParts = mp.getCount();
					LOGGER.debug("There are " + numBodyParts + " to this message.");
					for (int i=0;i<numBodyParts;i++) {
						BodyPart bp = mp.getBodyPart(i);
						String bodyPartContentType = bp.getContentType();
						String bodyPartFileName = bp.getFileName();
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Body part is of type " + bodyPartContentType + " file name:" + bodyPartFileName);
						}
						
						if (bodyPartContentType.contains("application/pdf")) {
							LOGGER.debug("Extracting PDF...");
							
							MimeBodyPart mbp = (MimeBodyPart)bp;
							mbp.saveFile(bodyPartFileName);
							
							LOGGER.debug("...extracted.");
						}
					}
					
				} catch (IOException e) {
					LOGGER.error( "Error getting message content:", e);
				}
			} else {
				LOGGER.debug("Not handling contentType of " + contentType);
			}
			
			message.setFlag(Flag.SEEN, true);
			
			LOGGER.info("...processed.");
		}
		
		LOGGER.info("Processed messages.");

	}
	
	/**
	 * Retrieve the unread messages in this folder
	 * @param folder
	 * @return
	 * @throws MessagingException
	 */
	private List<Message> retrieveUnreadMessages(Folder folder) throws MessagingException {
		List<Message> unreadMessages = new ArrayList<Message>();
		int messageCount = folder.getMessageCount();

		// getMessage starts at one.
		for (int i = 1; i <= messageCount; i++) {
			Message message = folder.getMessage(i);

			if (!message.getFlags().contains(Flag.SEEN)) {
				unreadMessages.add(message);
			}
		}
		
		return unreadMessages;
	}
}
