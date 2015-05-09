package sciuto.corey.alerter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sciuto.corey.alerter.util.PropertiesReader;

/**
 * Hello world!
 *
 */
public class App 
{
	private final static Logger LOGGER;
	static {
		 // Redirect JUL to Log4J. This must happen before the first call to LogManager.
		 System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
		 LOGGER = LogManager.getLogger();
	}
	
    public static void main( String[] args )
    {
    	LOGGER.info( "Starting application...");
    	String versionInformationProp = PropertiesReader.read("version.properties").getProperty("alerter.version");
        LOGGER.info( "Build information: " + versionInformationProp );
        LOGGER.debug( "Properties:" + System.getProperties());

        String alerterProps = args[0];
        
        InputStream propertiesFile;
        Properties mailProperties;
        try {
			propertiesFile  = new FileInputStream(new File(alerterProps));
			mailProperties = new Properties();
	        mailProperties.load(propertiesFile);
	        
	        if (mailProperties.size() == 0) {
	        	LOGGER.error(alerterProps + " did not contain any properties. Exiting...");
	        	System.exit(-1);
	        }
		} catch (IOException e) {
        	LOGGER.error(alerterProps + " could not be opened. Exiting...");
        	System.exit(-1);
		}

        ////////////////////////////
        Properties props = new Properties();
        
        props.put("mail.smtp.host", "my-mail-server");
        Session session = Session.getInstance(props, null);

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("me@example.com"));
            msg.setRecipients(Message.RecipientType.TO,
                              "you@example.com");
            msg.setSubject("JavaMail hello world example");
            msg.setSentDate(new Date());
            msg.setText("Hello, world!\n");
            
            Address [] toAddresses = new InternetAddress[1];
            toAddresses[0] = new InternetAddress("me@example.com");
            
            Transport.send(msg, toAddresses);
        } catch (MessagingException mex) {
            System.out.println("send failed, exception: " + mex);
        }
        
        
        LOGGER.info( "Shutdown." );
    }
}
