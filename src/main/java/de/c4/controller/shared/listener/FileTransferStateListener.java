package main.java.de.c4.controller.shared.listener;

import main.java.de.c4.model.messages.file.FileTransferState;

public interface FileTransferStateListener {

	void setTransferState(FileTransferState state);

}
