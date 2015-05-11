package sciuto.corey.alerter.mail;

import java.util.ArrayList;
import java.util.List;

public class ProcessedMessage {

	private List<String> fileNames = new ArrayList<String>();
	private String messageBody;
	private String subject;
	
	public void addFileName(String fileName) {
		fileNames.add(fileName);
	}
	public List<String> getFileNames() {
		return fileNames;
	}
	public String getMessageBody() {
		return messageBody;
	}
	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
}
