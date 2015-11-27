package main.java.de.c4.controller.shared.listener;

import main.java.de.c4.model.messages.ChatMessage;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.file.FileTransferRequest;

public interface MessageRecievedListener {
	public void messageRecieved(ContactDto contact, ChatMessage message);

	public void fileTransferRequestRecieved(ContactDto contact,
			FileTransferRequest request);
	
	public void alert(ContactDto contact);
	
	/**
	 * Is called, when the user tries to start a second instance of this application.
	 */
	public void secondClientStarted();
}
