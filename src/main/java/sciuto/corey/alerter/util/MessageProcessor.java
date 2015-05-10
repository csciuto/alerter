package sciuto.corey.alerter.util;

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageProcessor {

	Logger LOGGER = LogManager.getLogger();
	
	public void processMessages(Folder folder) throws MessagingException{

		int unreadMessageCount = folder.getUnreadMessageCount();
		LOGGER.info("There are " + unreadMessageCount + " unread messages.");
		
		int i=0;
		while (i<unreadMessageCount){
			try {
			Message message = folder.getMessage(++i);
			String messageSubject = message.getSubject();
			LOGGER.info("Found message " + messageSubject);
			
			message.setFlag(Flag.SEEN, true);
			}
			catch (MessagingException e) {
				LOGGER.error("Unable to process message.", e);
			}
		}
		
		LOGGER.info("Processed messages.");
	}
	
}
