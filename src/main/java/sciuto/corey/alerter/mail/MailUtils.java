package sciuto.corey.alerter.mail;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MailUtils {

	private final static Logger LOGGER = LogManager.getLogger();

	/**
	 * Splits a string of email addresses into an array.
	 * 
	 * @param whitelistString
	 *            String to split
	 * @param delimiter
	 * @return
	 */
	public static List<String> parseWhiteList(String whitelistString, String delimiter) {

		List<String> whitelist = new ArrayList<String>();
		String[] tokens = whitelistString.split(delimiter);

		for (int i = 0; i < tokens.length; i++) {
			whitelist.add(tokens[i]);
		}

		return whitelist;
	}

	/**
	 * XXX: Security by obscurity is NOT security. I'm not sure what else can be
	 * done...
	 * 
	 * @param fromAddresses
	 * @param whitelist
	 * @return
	 * @throws MessagingException
	 */
	public static boolean validateSender(Address[] fromAddresses, List<String> whitelist) throws MessagingException {

		for (int j = 0; j < fromAddresses.length; j++) {
			Address address = fromAddresses[j];

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Validating " + address.toString() + " against whitelist...");
			}

			if (whitelist.contains(address.toString())) {
				LOGGER.debug("Validated.");
			} else {
				LOGGER.warn("Invalid message from " + address.toString());
				return false;
			}
		}

		return true;
	}

	/**
	 * Removes the REs and FWDs from a String.
	 * 
	 * @param subject
	 * @return
	 */
	public static String cleanSubject(String subject) {
		String subjectInCaps = subject.toUpperCase();
		
		boolean keepTrimming = true;
		int idx = 0;
		
		while (keepTrimming){
			if (subjectInCaps.startsWith(" ")){
				subjectInCaps = subjectInCaps.substring(1);
				idx++;
			} else if (subjectInCaps.startsWith("FWD:")) {
				subjectInCaps = subjectInCaps.substring(4);
				idx += 4;
			} else if (subjectInCaps.startsWith("RE:") || subjectInCaps.startsWith("FW:")) {
				subjectInCaps = subjectInCaps.substring(3);
				idx += 3;
			} else {
				keepTrimming = false;
			}
		}
		if (idx >= subject.length()) {
			return "<No Subject>";
		} else {
			return subject.substring(idx);
		}
	}
}
