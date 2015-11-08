package main.java.de.c4.controller.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import main.java.de.c4.controller.shared.Network;
import main.java.de.c4.controller.shared.listener.FileTransferListener;
import main.java.de.c4.model.connections.ChatConnection;
import main.java.de.c4.model.messages.file.FileChunk;
import main.java.de.c4.model.messages.file.FileTransferRequest;

public class FileTransferServer  extends Thread{
	
	private Server server;
	private int port;
	private long bytesRecieved = 0;
	private final FileTransferListener listener;
	private final FileTransferRequest request;
	private File file;
	private OutputStream out;
	
	public FileTransferServer(FileTransferRequest req, FileTransferListener l) throws IOException {
		server = new Server() {
			protected Connection newConnection() {
				return new ChatConnection();
			}
		
		};
		this.request =  req;
		this.listener = l;
		this.file = new File(request.filenName);
		this.out = new FileOutputStream(file);
		// For consistency, the classes to be sent over the network are
		// registered by the same method for both the client and server.
		Network.registerFileTransfer(server);

		server.addListener(new Listener(){

			@Override
			public void disconnected(Connection connection) {
				server.close();
				Network.freePort(port);
				listener.disconnected(request.id);
				
			}
			@Override
			public void received(Connection connection, Object object) {
				if (object instanceof FileChunk) {
					FileChunk c = (FileChunk) object;
					try {
						out.write(c.data);
						bytesRecieved += c.data.length;
						listener.updateState(request.id, bytesRecieved);
						if (request.fileSize==bytesRecieved) {
							out.flush();
							out.close();
						}
					} catch (IOException e) {
						Log.error("Failed Writing to file "+file+": "+e.getMessage());
					}
				}
			}
		});
		port = Network.getFreePort();
		server.bind(port);//
	}
	public int getPort() {
		return port;
	}

	@Override
	public void run() {
		server.start();
	}
}