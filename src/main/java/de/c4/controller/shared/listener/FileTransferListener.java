package main.java.de.c4.controller.shared.listener;

public interface FileTransferListener {
	void updateState(long id, long bytesRecieved);

	void disconnected(long id);
}
