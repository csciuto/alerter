package sciuto.corey.alerter.mail;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * Yeah I know this is bad practice...override if you'd like.
 * @author Corey Sciuto
 *
 */
public class PropertiesFileAuthenticator extends Authenticator {

	private PasswordAuthentication auth;
	
	/**
	 * Constructs the Authenticator <br />
	 * Reads the username and password properties out of the passed-in properties list.
	 * @param properties
	 */
	public PropertiesFileAuthenticator(Properties properties) {
		auth = new PasswordAuthentication(properties.getProperty("mail.username"), properties.getProperty("mail.password"));
	}
	
	@Override
	public PasswordAuthentication getPasswordAuthentication() {
		 return auth;
	}
	
}
