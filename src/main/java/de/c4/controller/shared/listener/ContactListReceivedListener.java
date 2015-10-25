package main.java.de.c4.controller.shared.listener;

import main.java.de.c4.model.messages.ContactDto;

public interface ContactListReceivedListener {

	public void receivedContactList(ContactDto[] list);

}
