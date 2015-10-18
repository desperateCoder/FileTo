package main.java.de.c4.controller.server;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;

import javax.swing.JFrame;
import javax.swing.JLabel;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import main.java.de.c4.controller.shared.ContactList;
import main.java.de.c4.controller.shared.Network;
import main.java.de.c4.controller.shared.Network.ChatMessage;
import main.java.de.c4.controller.shared.listener.MessageRecievedListener;
import main.java.de.c4.model.connections.ChatConnection;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.ContactListDto;
import main.java.de.c4.model.messages.OnlineStateChange;
import main.java.de.c4.model.messages.RequestKnownOnlineClients;

public class ChatServer {
	
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

		server.addListener(new Listener() {
			public void received(Connection c, Object object) {
				// We know all connections for this server are actually
				// ChatConnections.

				if (object instanceof ChatMessage) {
					
					// Ignore the object if a client tries to chat before it is registered.
					ChatMessage chatMessage = (ChatMessage) object;
					ContactDto contact = ContactList.INSTANCE.findByAddr(c.getRemoteAddressTCP().getAddress());
					if (contact == null)
						return;
					
					// Ignore the object if the chat message is invalid.
					String message = chatMessage.text;
					if (message == null || message.length()==0)
						return;
					Log.debug("Nachricht von "+contact.name+" ("+contact.ip+") bekommen: "+message);
					server.sendToAllTCP(object);
					ContactList.INSTANCE.messageRecieved(contact, chatMessage);
					
				} else if (object instanceof OnlineStateChange) {
					
					InetAddress ip = c.getRemoteAddressTCP().getAddress();
					OnlineStateChange onlineState = (OnlineStateChange) object;
					ContactList.INSTANCE.contactStateChanged(onlineState, ip);
					
				} else if (object instanceof RequestKnownOnlineClients) {
					InetAddress address = c.getRemoteAddressTCP().getAddress();
					ContactListDto contacts = ContactList.INSTANCE.getContactListForContactsRequest(address);
					c.sendTCP(contacts);
				}
			}

			public void disconnected(Connection c) {
				// listener?? eigentlich nicht interessant...
			}
		});
		server.bind(Network.TCP_PORT);//
		server.start();

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
		
		new ChatServer();
	}
}