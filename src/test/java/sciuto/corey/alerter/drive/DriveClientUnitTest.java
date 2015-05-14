package sciuto.corey.alerter.drive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import sciuto.corey.alerter.model.Attachment;
import sciuto.corey.alerter.model.ProcessedMessage;

public class DriveClientUnitTest {
	
	@Test
	public void testCreateRootFolderExists() throws IOException {
		
		IDriveWrapper wrapper = Mockito.mock(IDriveWrapper.class);
		Mockito.when(wrapper.locateRootDirectory("foobar")).thenReturn("rootId");

		DriveClient client = new DriveClient(wrapper);
		String folderId = client.createRootFolder("foobar");
		Assert.assertEquals("rootId", folderId);
		
		Mockito.verify(wrapper,Mockito.times(1)).locateRootDirectory("foobar");
		Mockito.verify(wrapper,Mockito.never()).createRootDirectory("foobar");	
	}
	
	@Test
	public void testCreateRootFolderNotExists() throws IOException {
		
		IDriveWrapper wrapper = Mockito.mock(IDriveWrapper.class);
		Mockito.when(wrapper.locateRootDirectory("foobar")).thenReturn(null);
		Mockito.when(wrapper.createRootDirectory("foobar")).thenReturn("rootId");

		DriveClient client = new DriveClient(wrapper);
		String folderId = client.createRootFolder("foobar");
		Assert.assertEquals("rootId", folderId);
		
		Mockito.verify(wrapper,Mockito.times(1)).locateRootDirectory("foobar");
		Mockito.verify(wrapper,Mockito.times(1)).createRootDirectory("foobar");	
	}
	
	@Test
	public void testUploadMessages() throws IOException {
		
		IDriveWrapper wrapper = Mockito.mock(IDriveWrapper.class);
		Mockito.when(wrapper.createSubdirectory(Mockito.any(ProcessedMessage.class), Mockito.anyString())).thenReturn("subdirId");
		Mockito.when(wrapper.createFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn("fileId");

		ProcessedMessage message1 = new ProcessedMessage();
		message1.setMessageBodyFileName("bodyFileName1");
		message1.setSubject("Subject1");
		Attachment a1 = new Attachment();
		a1.setFileLocation("directory/foo.txt");
		a1.setMimeType("text/plain");
		message1.addAttachment(a1);
		Attachment a2 = new Attachment();
		a2.setFileLocation("directory/bar.pdf");
		a2.setMimeType("application/pdf");
		message1.addAttachment(a2);
		
		ProcessedMessage message2 = new ProcessedMessage();
		message2.setMessageBodyFileName(null);
		message2.setSubject("Subject2");
		Attachment a3 = new Attachment();
		a3.setFileLocation("directory/baz.doc");
		a3.setMimeType("application/msword");
		message2.addAttachment(a3);

		ProcessedMessage message3 = new ProcessedMessage();
		message3.setSubject("Subject 3");
		message3.setMessageBodyFileName("bodyFileName3");
		
		List<ProcessedMessage> processedMessages = new ArrayList<ProcessedMessage>();
		processedMessages.add(message1);
		processedMessages.add(message2);
		processedMessages.add(message3);		
		
		DriveClient client = new DriveClient(wrapper);
		client.uploadMessages(processedMessages, "rootId");
		
		Mockito.verify(wrapper,Mockito.times(3)).createSubdirectory(Mockito.any(ProcessedMessage.class), Mockito.anyString());
		Mockito.verify(wrapper,Mockito.times(5)).createFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}
}
