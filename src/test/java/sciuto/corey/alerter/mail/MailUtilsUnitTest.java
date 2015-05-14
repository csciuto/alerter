package sciuto.corey.alerter.mail;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.MessagingException;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

public class MailUtilsUnitTest {

	@Test
	public void testWhiteListParser() {
		String whiteListString1 = "John Smith <jsmith@example.com>--delim--Bob Jones <bjones@example.com>";
		List<String> results1 = MailUtils.parseWhiteList(whiteListString1, "--delim--");
		Assert.assertEquals(2, results1.size());
		
		String whiteListString2 = "John Smith <jsmith@example.com>";
		List<String> results2 = MailUtils.parseWhiteList(whiteListString2, "--delim--");
		Assert.assertEquals(1, results2.size());
	}
	
	@Test
	public void testValidateSender() throws MessagingException {
		String name1 = "John Smith <jsmith@example.com>";
		String name2 = "Bob Jones <bjones@example.com>";
		List<String> whitelist = new ArrayList<String>();
		whitelist.add(name1);
		whitelist.add(name2);
		
		Address[] address = new Address[1];
		Address addr1 = Mockito.mock(Address.class);
		Mockito.when(addr1.toString()).thenReturn("John Smith <jsmith@example.com>");
		address[0] = addr1;
		
		Assert.assertTrue(MailUtils.validateSender(address, whitelist));
		
		Address[] address2 = new Address[1];
		Address addr2 = Mockito.mock(Address.class);
		Mockito.when(addr2.toString()).thenReturn("Jamie Sullivan <jsullivan@example.com>");
		address2[0] = addr2;
		
		Assert.assertFalse(MailUtils.validateSender(address2, whitelist));
		
		Address[] address3 = new Address[2];
		address3[0] = addr1;
		address3[1] = addr2;
		
		Assert.assertFalse(MailUtils.validateSender(address3, whitelist));
	}
	
	@Test
	public void testCleanSubject() {
		Assert.assertEquals("This is the subject.", MailUtils.cleanSubject("This is the subject."));
		Assert.assertEquals("This is the subject.", MailUtils.cleanSubject("FWD: FW: This is the subject."));
		Assert.assertEquals("This is the subject.", MailUtils.cleanSubject("FW:RE:This is the subject."));
		Assert.assertEquals("<No Subject>", MailUtils.cleanSubject("FW: "));
		Assert.assertEquals("<No Subject>", MailUtils.cleanSubject("   "));
	}
}
