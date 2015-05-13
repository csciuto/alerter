/* 
 * Copyright (c) 2015 Corey Sciuto <corey.sciuto@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package sciuto.corey.alerter.mail;

import java.util.ArrayList;
import java.util.List;

public class ProcessedMessage {

	private List<String> fileNames = new ArrayList<String>();
	private String messageBodyFileName;
	private String subject;
	
	public void addFileName(String fileName) {
		fileNames.add(fileName);
	}
	public List<String> getFileNames() {
		return fileNames;
	}
	public String getMessageBodyFileName() {
		return messageBodyFileName;
	}
	public void setMessageBodyFileName(String messageBodyFileName) {
		this.messageBodyFileName = messageBodyFileName;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
}
