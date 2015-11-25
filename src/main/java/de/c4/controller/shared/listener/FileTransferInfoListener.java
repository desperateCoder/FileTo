package main.java.de.c4.controller.shared.listener;

import java.io.File;

import main.java.de.c4.model.messages.ContactDto;

public interface FileTransferInfoListener {
	void started(File f, ContactDto c, boolean isUpload);
	void abroted(File f, ContactDto c, boolean isUpload);
	void finnished(File f, ContactDto c, boolean isUpload);
	void declined(ContactDto contact, File file);
}
