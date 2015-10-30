package main.java.de.c4.model;

import java.io.File;

import main.java.de.c4.controller.shared.listener.FileTransferInfoListener;
import main.java.de.c4.model.messages.ContactDto;

public class PendingFileTransfer {
	public File file;
	public ContactDto contact;
	public FileTransferInfoListener listener;
	
	public PendingFileTransfer(ContactDto contact, File f, FileTransferInfoListener l) {
		this.file = f;
		this.contact = contact;
		this.listener = l;
	}

	
	
}
