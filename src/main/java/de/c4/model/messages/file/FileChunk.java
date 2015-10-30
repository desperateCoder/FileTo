package main.java.de.c4.model.messages.file;

public class FileChunk {

	public long id;
	public byte[] data;
	
	public FileChunk() {
	}
	
	public FileChunk(long id, byte[] data) {
		this.id = id;
		this.data = data;
	}

}
