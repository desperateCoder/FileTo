package main.java.de.c4.model.messages.file;

import main.java.de.c4.controller.shared.listener.FileTransferStateListener;
import main.java.de.c4.model.messages.ContactDto;

public class FileTransferState {
	public FileTransferRequest request;
	public FileTransferStateListener listener;
	public ContactDto contact;
	public long bytesDone = 0;
	public boolean isUpload = false;
	
	public FileTransferState(FileTransferRequest request, FileTransferStateListener l, ContactDto contact, boolean isUpload) {
		this.request = request;
		this.listener = l;
		this.isUpload = isUpload;
		this.contact = contact;
	}
}
