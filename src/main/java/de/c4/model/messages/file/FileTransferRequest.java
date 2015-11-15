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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		FileTransferRequest other = (FileTransferRequest) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	

}
