package main.java.de.c4.model.messages.file;

import main.java.de.c4.controller.shared.listener.FileTransferStateListener;

public class FileTransferState {
	public FileTransferRequest request;
	public FileTransferStateListener listener;
	public long bytesDone = 0;
	
	public FileTransferState(FileTransferRequest request, FileTransferStateListener l) {
		this.request = request;
		this.listener = l;
	}
}
