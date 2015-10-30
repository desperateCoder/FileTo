package main.java.de.c4.model.messages.file;

public class FileTransferRequest {

	public long id;
	public String filenName;
	public long fileSize;
	
	public FileTransferRequest() {
	}
	
	public FileTransferRequest(String filenName, long fileSize) {
		this.id = System.currentTimeMillis();
		this.filenName = filenName;
		this.fileSize = fileSize;
	}
	
	

}
