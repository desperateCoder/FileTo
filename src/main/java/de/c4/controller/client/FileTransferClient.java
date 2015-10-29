package main.java.de.c4.controller.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import main.java.de.c4.controller.shared.Network;
import main.java.de.c4.controller.shared.listener.FileTransferListener;
import main.java.de.c4.model.messages.file.FileChunk;
import main.java.de.c4.model.messages.file.FileTransferAnswer;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.util.InputStreamSender;
import com.esotericsoftware.minlog.Log;

public class FileTransferClient extends Thread {
	private Client client;
	private String host;
	private int port;
	private long sent = 0;
	

	public FileTransferClient (String hostAdress, final FileTransferAnswer answer, final File file, final FileTransferListener listener) {
		if (file == null || !file.exists() || file.isDirectory() || !answer.accepted) {
			Log.error("File not sent, not plausible!");
			throw new IllegalArgumentException("File not sent, not plausible!");
		}
		this.port = answer.serverPort;
		client = new Client();
		client.start();

		Network.registerFileTransfer(client);

		client.addListener(new Listener(){
			public void connected (Connection connection) {
				ByteArrayOutputStream output = new ByteArrayOutputStream((int)file.length());
				for (long i = 0; i < file.length(); i++)
					output.write((int) i);
				FileInputStream input;
				try {
					input = new FileInputStream(file);
					connection.addListener(new InputStreamSender(input, 1024) {
						protected void start () {
							Log.info("starting FileUpload: "+file.getAbsolutePath());
						}
						
						protected Object next (byte[] bytes) {
							sent += bytes.length;
							long id = answer.id;
							listener.updateState(id, sent);
							return new FileChunk(id, bytes);
						}
						
					});
				} catch (FileNotFoundException e) {
					Log.error("File not found: "+e.getMessage());
				}
			}
			@Override
			public void disconnected(Connection connection) {
				
			}
		});

		host = hostAdress;

	}
	
	public Client getClient() {
		return client;
	}
	
	public void run () {
		try {
			client.connect(Network.DEFAULT_TIMEOUT, host, port);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}
}
