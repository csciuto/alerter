package sciuto.corey.alerter;

import java.util.Properties;

import junit.framework.Assert;

import org.junit.Test;

import sciuto.corey.alerter.PropertiesReader;

public class PropertiesReaderUnitTest {

	@Test
	public void testReadFromClasspath() {
		Properties props = PropertiesReader.readFromClasspath("alerter-test.properties");
		Assert.assertTrue(props.containsKey("mail.imap.host"));
		Assert.assertTrue(props.getProperty("mail.whitelist").equals("User <user@example.com>"));
	}
	
	@Test
	public void testReadFromFile() {
		Properties props = PropertiesReader.readFromFile("buildNumber.properties");
		Assert.assertTrue(props.containsKey("buildNumber0"));
	}
}
