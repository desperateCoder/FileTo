package main.java.de.c4.controller.shared.listener;

import main.java.de.c4.controller.shared.ChatMessage;
import main.java.de.c4.model.messages.ContactDto;

public interface MessageRecievedListener {
	public void messageRecieved(ContactDto contact, ChatMessage message);
}
