package main.java.de.c4.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import main.java.de.c4.controller.server.FileTransferServer;
import main.java.de.c4.controller.shared.listener.FileTransferListener;
import main.java.de.c4.controller.shared.listener.FileTransferStateListener;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.file.FileTransferRequest;
import main.java.de.c4.model.messages.file.FileTransferState;

import com.esotericsoftware.minlog.Log;

public class FileTransferManager implements FileTransferListener{

	public Map<Long, FileTransferState> transfers = new HashMap<Long, FileTransferState>();
	
	public synchronized void updateState(long id, long bytesDone){
		Long lId = Long.valueOf(id);
		if (transfers.containsKey(lId)) {
			FileTransferState state = transfers.get(lId);
			state.bytesDone = bytesDone;
			state.listener.setTransferState(state);
		}
	}
	
	public void sendFileTo(File f, ContactDto contact){
		
	}
	
	public synchronized void startFileServer(FileTransferStateListener l, FileTransferRequest request){
		Long id = Long.valueOf(request.id);
		if (!transfers.containsKey(id)) {
			transfers.put(id, new FileTransferState(request, l));
		}
		try {
			new FileTransferServer(request, this).start();
		} catch (IOException e) {
			Log.debug("Fehler beim starten des File-Servers: "+e.getMessage());
		}
	}
	public void disconnected(long id) {
		transfers.remove(Long.valueOf(id));
	}

}
