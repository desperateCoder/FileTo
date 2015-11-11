package main.java.de.c4.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.minlog.Log;

import main.java.de.c4.controller.client.FileTransferClient;
import main.java.de.c4.controller.server.FileTransferServer;
import main.java.de.c4.controller.shared.ConnectionManager;
import main.java.de.c4.controller.shared.listener.FileTransferInfoListener;
import main.java.de.c4.controller.shared.listener.FileTransferListener;
import main.java.de.c4.model.PendingFileTransfer;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.file.FileTransferAnswer;
import main.java.de.c4.model.messages.file.FileTransferRequest;
import main.java.de.c4.model.messages.file.FileTransferState;
import main.java.de.c4.view.FileTransferFrame;

public class FileTransferManager implements FileTransferListener{

	public static final FileTransferManager INSTANCE = new FileTransferManager(true);
	
	private Map<Long, FileTransferState> transfers = new HashMap<Long, FileTransferState>();
	private Map<Long, PendingFileTransfer> pendingTransfers = new HashMap<Long, PendingFileTransfer>();
	private Map<Long, List<FileTransferInfoListener>> transferListeners = new HashMap<Long, List<FileTransferInfoListener>>();
	
	public FileTransferManager() {
		this(false);
	}
	
	private FileTransferManager(boolean isInternalCall) {
		if (!isInternalCall) {
			throw new RuntimeException("Singleton should not be instanciated!");
		}
	}

	public synchronized void updateState(long id, long bytesDone){
		Long lId = Long.valueOf(id);
		if (transfers.containsKey(lId)) {
			FileTransferState state = transfers.get(lId);
			state.bytesDone = bytesDone;
			if (state.bytesDone==0 && bytesDone > 0) {
				for (FileTransferInfoListener l : transferListeners.get(lId)) {
					l.started(new File(state.request.filenName), state.contact);
				}
			} else if (state.request.fileSize == bytesDone) {
				for (FileTransferInfoListener l : transferListeners.get(lId)) {
					l.finnished(new File(state.request.filenName), state.contact);
				}
				transfers.remove(lId);
				removeFileTransferListener(lId);
			}
			state.listener.setTransferState(state);
		}
	}
	
	public synchronized void sendFileTo(File f, ContactDto contact, FileTransferInfoListener l){
		FileTransferRequest request = new FileTransferRequest(f.getName(), f.length());
		Long id = Long.valueOf(request.id);
		ConnectionManager.getConnectionsTo(contact, true).iterator().next().sendTCP(request);
		pendingTransfers.put(id, new PendingFileTransfer(contact, f, l));
	}
	
	public synchronized void requestAnswered(FileTransferAnswer answer){
		Long id = Long.valueOf(answer.id);
		if (!pendingTransfers.containsKey(id)) {
			return;
		}
		PendingFileTransfer t = pendingTransfers.get(id);
		if (!answer.accepted) {
			pendingTransfers.remove(t);
			t.listener.declined(t.contact, t.file);
			return;
		}
		FileTransferRequest request = new FileTransferRequest(t.file.getName(), t.file.length());
		request.id = answer.id;
		transfers.put(id, new FileTransferState(request, FileTransferFrame.INSTANCE, t.contact, true));
		pendingTransfers.remove(t);
		addFileTransferListener(id, t.listener);
		new FileTransferClient(t.contact.ip, answer, t.file, this).start();
	}
	
	public synchronized int startFileServer(FileTransferRequest request, ContactDto contact){
		Long id = Long.valueOf(request.id);
		if (!transfers.containsKey(id)) {
			transfers.put(id, new FileTransferState(request, FileTransferFrame.INSTANCE, contact, false));
		}
		try {
			FileTransferServer server = new FileTransferServer(request, this);
			server.start();
			return server.getPort();
		} catch (IOException e) {
			Log.debug("Fehler beim starten des File-Servers: "+e.getMessage());
		}
		return 0;
	}
	public void disconnected(long id) {
		Long fId = Long.valueOf(id);
		if (transfers.containsKey(fId)) {
			FileTransferState state = transfers.get(fId);
			if (state.bytesDone < state.request.fileSize) {
				for (FileTransferInfoListener l : transferListeners.get(id)) {
					l.abroted(new File(state.request.filenName), state.contact);
				}
			}
			transfers.remove(fId);
		}
	}
	
	public void addFileTransferListener(long Id, FileTransferInfoListener l){
		Long id = Long.valueOf(Id);
		List<FileTransferInfoListener> listener; 
		if (transferListeners.containsKey(id)) {
			listener = transferListeners.get(id);
		} else {
			listener = new ArrayList<FileTransferInfoListener>();
			transferListeners.put(id, listener);
		}
		listener.add(l);
	}
	
	public void removeFileTransferListener(long id){
		transferListeners.remove(Long.valueOf(id));
	}
	
	/**
	 * Turns the number of bytes to a human-readable format (kB, MB, GB, etc.)
	 * (StackOverflow FTW! :D)
	 * @param bytes number of bytes
	 * @param si if true, base is 1000, else 1024
	 * @return formated Byte-count as String
	 */
	public static String toReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

}
