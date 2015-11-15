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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((request == null) ? 0 : request.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileTransferState other = (FileTransferState) obj;
		if (request == null) {
			if (other.request != null)
				return false;
		} else if (!request.equals(other.request))
			return false;
		return true;
	}

}
