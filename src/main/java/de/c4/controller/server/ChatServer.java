package main.java.de.c4.controller.server;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import main.java.de.c4.controller.shared.ConnectionPeer;
import main.java.de.c4.controller.shared.MessageHandler;
import main.java.de.c4.controller.shared.Network;
import main.java.de.c4.model.connections.ChatConnection;

public class ChatServer extends ConnectionPeer{
	
	private Server server;
	
	
	public ChatServer() throws IOException {
		new PingServer().start();
		server = new Server() {
			protected Connection newConnection() {
				return new ChatConnection();
			}
		
		};

		// For consistency, the classes to be sent over the network are
		// registered by the same method for both the client and server.
		Network.register(server);

		MessageHandler messageHandler = new MessageHandler();
		server.addListener(messageHandler);
		server.bind(Network.TCP_PORT);//

		// Open a window to provide an easy way to stop the server.
		JFrame frame = new JFrame("Chat Server");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent evt) {
				server.stop();
			}
		});
		frame.getContentPane().add(new JLabel("Close to stop the chat server."));
		frame.setSize(320, 200);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
//	private ContactDto buildByAddr(InetAddress ad){
//		ContactDto dto = new ContactDto();
//		dto.ip = ad;
//		return dto;
//	}
	
	
	
//	void updateNames() {
//		// Collect the names for each connection.
//		Connection[] connections = server.getConnections();
//		ArrayList names = new ArrayList(connections.length);
//		for (int i = connections.length - 1; i >= 0; i--) {
//			ChatConnection connection = (ChatConnection) connections[i];
////			names.add(connection.contact.name);
//		}
//		// Send the names to everyone.
//		UpdateNames updateNames = new UpdateNames();
//		updateNames.names = (String[]) names.toArray(new String[names.size()]);
//		server.sendToAllTCP(updateNames);
//	}

	public static void main(String[] args) throws IOException {
		Log.set(Log.LEVEL_DEBUG);
		
		new ChatServer().start();
	}

	@Override
	public void sendData(Object o) {
		
	}

	@Override
	public void run() {

		server.start();
	}
}