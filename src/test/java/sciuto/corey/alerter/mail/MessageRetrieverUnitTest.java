package sciuto.corey.alerter.mail;

import java.util.List;
import java.util.Properties;

import javax.mail.*;
import javax.mail.Flags.Flag;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

public class MessageRetrieverUnitTest {

	@Test
	public void testConstructor() {
		
		Properties props = new Properties();
		props.put("mail.username", "Scott");
		props.put("mail.password", "Tiger");
		props.put("mail.whitelist", "John Smith <jsmith@example.com>--delim--Bob Jones <bjones@example.com>");
		
		MessageRetriever retriever = new MessageRetriever(props);
		
		PasswordAuthentication auth = retriever.getAuthenticator().getPasswordAuthentication();
		Assert.assertEquals("Scott",auth.getUserName());
		Assert.assertEquals("Tiger",auth.getPassword());		
		
		List<String> whitelist = retriever.getWhitelist();
		Assert.assertEquals(2, whitelist.size());
		Assert.assertTrue(whitelist.contains("John Smith <jsmith@example.com>"));
		Assert.assertTrue(whitelist.contains("Bob Jones <bjones@example.com>"));		
	}
	
	@Test
	public void testGetFiles() throws MessagingException {

		Folder inboxFolder = generateInboxFolder();
		
		Properties props = new Properties();
		props.put("mail.username", "Scott");
		props.put("mail.password", "Tiger");
		props.put("mail.whitelist", "John Smith <jsmith@example.com>--delim--Bob Jones <bjones@example.com>");
		
		MessageRetriever retriever = new MessageRetriever(props);
		List<Message> messages = retriever.retrieveUnreadMessages(inboxFolder);
		
		// Two messages are good. One has an invalid address, and two are already read.
		Assert.assertEquals(2, messages.size());
	}

	private Folder generateInboxFolder() throws MessagingException {
		Flags seenFlags = new Flags();
		seenFlags.add(Flag.SEEN);
		
		Address addr1 = Mockito.mock(Address.class);
		Mockito.when(addr1.toString()).thenReturn("John Smith <jsmith@example.com>");
		Address addr2 = Mockito.mock(Address.class);
		Mockito.when(addr2.toString()).thenReturn("Bob Jones <bjones@example.com>");
		Address addr3 = Mockito.mock(Address.class);
		Mockito.when(addr3.toString()).thenReturn("Bad Guy <bad@example.com>");
		
		Message msg1 = Mockito.mock(Message.class);
		Mockito.when(msg1.getFlags()).thenReturn(new Flags());
		Mockito.when(msg1.getSubject()).thenReturn("Message one");
		Mockito.when(msg1.getFrom()).thenReturn(new Address[]{addr1});
		
		Message msg2 = Mockito.mock(Message.class);
		Mockito.when(msg2.getFlags()).thenReturn(seenFlags);
		Mockito.when(msg2.getSubject()).thenReturn("Message two");
		Mockito.when(msg2.getFrom()).thenReturn(new Address[]{addr1});
		
		Message msg3 = Mockito.mock(Message.class);
		Mockito.when(msg3.getFlags()).thenReturn(new Flags());
		Mockito.when(msg3.getSubject()).thenReturn("Message three");
		Mockito.when(msg3.getFrom()).thenReturn(new Address[]{addr3});
		
		Message msg4 = Mockito.mock(Message.class);
		Mockito.when(msg4.getFlags()).thenReturn(seenFlags);
		Mockito.when(msg4.getSubject()).thenReturn("Message four");
		Mockito.when(msg4.getFrom()).thenReturn(new Address[]{addr2});
		
		Message msg5 = Mockito.mock(Message.class);
		Mockito.when(msg5.getFlags()).thenReturn(new Flags());
		Mockito.when(msg5.getSubject()).thenReturn("Message five");
		Mockito.when(msg5.getFrom()).thenReturn(new Address[]{addr2});
		
		Folder inboxFolder = Mockito.mock(Folder.class);
		Mockito.when(inboxFolder.isOpen()).thenReturn(true);
		Mockito.when(inboxFolder.getMessageCount()).thenReturn(5);
		Mockito.when(inboxFolder.getMessage(1)).thenReturn(msg1);
		Mockito.when(inboxFolder.getMessage(2)).thenReturn(msg2);
		Mockito.when(inboxFolder.getMessage(3)).thenReturn(msg3);
		Mockito.when(inboxFolder.getMessage(4)).thenReturn(msg4);
		Mockito.when(inboxFolder.getMessage(5)).thenReturn(msg5);
		
		return inboxFolder;
	}
	
}
