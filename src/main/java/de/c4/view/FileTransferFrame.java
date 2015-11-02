package main.java.de.c4.view;

import javax.swing.JFrame;

import main.java.de.c4.controller.shared.listener.FileTransferStateListener;
import main.java.de.c4.model.messages.file.FileTransferState;

import com.esotericsoftware.minlog.Log;

public class FileTransferFrame extends JFrame implements
		FileTransferStateListener {
	
	
	private static final long serialVersionUID = 1L;
	
	public static final FileTransferFrame INSTANCE = new FileTransferFrame();

	public void setTransferState(FileTransferState state) {
		Log.debug("Status of FileTransfer \""+state.request.filenName+
				"\" updated: "+state.bytesDone+" of total "+
				state.request.fileSize+"bytes.");
	}

}
