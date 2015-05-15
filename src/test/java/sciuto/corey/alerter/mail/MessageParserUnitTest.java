package sciuto.corey.alerter.mail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import sciuto.corey.alerter.model.Attachment;
import sciuto.corey.alerter.model.ProcessedMessage;

public class MessageParserUnitTest {

	@Test
	public void testConstructor() {
		Properties applicationProperties = new Properties();
		applicationProperties.put("mail.stop.code", "HALT");
		MessageParser parser = new MessageParser(applicationProperties);
		Assert.assertEquals("HALT", parser.getStopCode());
	}

	@Test
	public void testMessages() throws MessagingException, IOException {

		Message message1 = mockMultipartMixed();
		Message message2 = mockMultipartAlternative();
		Message message3 = mockMultipartJunk();

		List<Message> messages = new ArrayList<Message>();
		messages.add(message1);
		messages.add(message2);
		messages.add(message3);

		Properties applicationProperties = new Properties();
		applicationProperties.put("mail.stop.code", "HALT");
		MessageParser parser = new MessageParser(applicationProperties);

		List<ProcessedMessage> processedMessages = parser.extractMessages(messages);

		Assert.assertEquals(2, processedMessages.size());

		for (ProcessedMessage msg : processedMessages) {
			if (msg.getSubject().equals("Message Subject")) {
				Assert.assertTrue(msg.getMessageBodyFileName().contains("messageBody.txt"));

				List<Attachment> attachments = msg.getAttachments();
				Assert.assertEquals(2, attachments.size());

				for (Attachment att : attachments) {
					if (att.getMimeType().equals("application/pdf")) {
						Assert.assertTrue(att.getFileLocation().contains("foo.pdf"));
					} else if (att.getMimeType().equals("application/msword")) {
						Assert.assertTrue(att.getFileLocation().contains("bar.docx"));
					} else {
						Assert.fail("Unrecognized attachment type.");
					}
				}

			} else if (msg.getSubject().equals("Message Subject 2")) {
				Assert.assertTrue(msg.getMessageBodyFileName().contains("messageBody.txt"));

				List<Attachment> attachments = msg.getAttachments();
				Assert.assertEquals(0, attachments.size());

			} else {
				Assert.fail("Unrecognized message subject.");
			}
		}
		
		// Make sure they were all marked SEEN.
		Mockito.verify(message1, Mockito.times(1)).setFlag(Flag.SEEN, true);
		Mockito.verify(message2, Mockito.times(1)).setFlag(Flag.SEEN, true);
		Mockito.verify(message3, Mockito.times(1)).setFlag(Flag.SEEN, true);
		
	}

	private Message mockMultipartJunk() throws MessagingException {
		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getSubject()).thenReturn("Message Subject 3");
		Mockito.when(message.getContentType()).thenReturn("application/virus");
		
		return message;
	}

	private Message mockMultipartAlternative() throws MessagingException, IOException {
		// /// BODY
		MimeBodyPart bp10 = Mockito.mock(MimeBodyPart.class);
		Mockito.when(bp10.getContentType()).thenReturn("text/html");

		MimeBodyPart bp20 = Mockito.mock(MimeBodyPart.class);
		Mockito.when(bp20.getContentType()).thenReturn("text/plain");
		Mockito.when(bp20.getContent()).thenReturn("This would be the body...");

		Multipart mpSub = Mockito.mock(Multipart.class);
		Mockito.when(mpSub.getCount()).thenReturn(2);
		Mockito.when(mpSub.getBodyPart(0)).thenReturn(bp10);
		Mockito.when(mpSub.getBodyPart(1)).thenReturn(bp20);

		// // ROLL UP
		Multipart mp = Mockito.mock(Multipart.class);
		Mockito.when(mp.getCount()).thenReturn(2);
		Mockito.when(mp.getBodyPart(0)).thenReturn(bp10);
		Mockito.when(mp.getBodyPart(1)).thenReturn(bp20);

		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getSubject()).thenReturn("Message Subject 2");
		Mockito.when(message.getContentType()).thenReturn("multipart/alternative");
		Mockito.when(message.getContent()).thenReturn(mpSub);
		
		return message;
	}

	private Message mockMultipartMixed() throws MessagingException, IOException {

		// // ATTACHMENT 1
		MimeBodyPart bp1 = Mockito.mock(MimeBodyPart.class);
		Mockito.when(bp1.getContentType()).thenReturn("application/pdf");
		Mockito.when(bp1.getFileName()).thenReturn("foo.pdf");

		// // ATTACHMENT 2
		MimeBodyPart bp2 = Mockito.mock(MimeBodyPart.class);
		Mockito.when(bp2.getContentType()).thenReturn("application/msword");
		Mockito.when(bp2.getFileName()).thenReturn("bar.docx");

		// /// BODY
		MimeBodyPart bp10 = Mockito.mock(MimeBodyPart.class);
		Mockito.when(bp10.getContentType()).thenReturn("text/html");

		MimeBodyPart bp20 = Mockito.mock(MimeBodyPart.class);
		Mockito.when(bp20.getContentType()).thenReturn("text/plain");
		Mockito.when(bp20.getContent()).thenReturn("This would be the body...");

		Multipart mpSub = Mockito.mock(Multipart.class);
		Mockito.when(mpSub.getCount()).thenReturn(2);
		Mockito.when(mpSub.getBodyPart(0)).thenReturn(bp10);
		Mockito.when(mpSub.getBodyPart(1)).thenReturn(bp20);

		MimeBodyPart bp3 = Mockito.mock(MimeBodyPart.class);
		Mockito.when(bp3.getContentType()).thenReturn("multipart/alternative");
		Mockito.when(bp3.getContent()).thenReturn(mpSub);

		// // ROLL UP
		Multipart mp = Mockito.mock(Multipart.class);
		Mockito.when(mp.getCount()).thenReturn(3);
		Mockito.when(mp.getBodyPart(0)).thenReturn(bp1);
		Mockito.when(mp.getBodyPart(1)).thenReturn(bp2);
		Mockito.when(mp.getBodyPart(2)).thenReturn(bp3);

		Message message = Mockito.mock(Message.class);
		Mockito.when(message.getSubject()).thenReturn("Message Subject");
		Mockito.when(message.getContentType()).thenReturn("multipart/mixed");
		Mockito.when(message.getContent()).thenReturn(mp);

		return message;
	}

}
