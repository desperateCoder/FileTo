package main.java.de.c4.controller.server;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JLabel;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import main.java.de.c4.controller.shared.Network;
import main.java.de.c4.controller.shared.Network.ChatMessage;
import main.java.de.c4.controller.shared.listener.MessageRecievedListener;
import main.java.de.c4.model.connections.ChatConnection;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.ContactList;
import main.java.de.c4.model.messages.EOnlineState;
import main.java.de.c4.model.messages.OnlineStateChange;
import main.java.de.c4.model.messages.RequestKnownOnlineClients;

public class ChatServer {
	private Server server;
	private Set<ContactDto> knownOnlineContacts = new HashSet<ContactDto>();
	private Set<MessageRecievedListener> messageRecievedListener = new HashSet<MessageRecievedListener>();
	
	private static String LOCAL_IP;
	
	public ChatServer() throws IOException {
		getLocalIP();
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
					ContactDto contact = findByAddr(c.getRemoteAddressTCP().getAddress());
					if (contact == null)
						return;
					
					// Ignore the object if the chat message is invalid.
					String message = chatMessage.text;
					if (message == null || message.length()==0)
						return;
					Log.debug("Nachricht von "+contact.name+" ("+contact.ip+") bekommen: "+message);
					server.sendToAllTCP(object);
					for (MessageRecievedListener l : messageRecievedListener) {
						l.messageRecieved(contact, chatMessage);
					}
					
				} else if (object instanceof OnlineStateChange) {
					
					InetAddress ip = c.getRemoteAddressTCP().getAddress();
					OnlineStateChange onlineState = (OnlineStateChange) object;
					if (onlineState.contact == null || onlineState.contact.name == null)
						return;
					String name = onlineState.contact.name;
					name = name.trim();
					if (name.length() == 0)
						return;
					ContactDto contact = findByAddr(ip);
					if (contact == null) {
						contact = new ContactDto(name);
					}
					contact.ip = ip.getHostAddress();
					contact.state = onlineState.newState;
					
					
					switch (onlineState.newState) {
					case ONLINE:
						knownOnlineContacts.add(contact);
						break;
					case OFFLINE:
						knownOnlineContacts.remove(contact);
						break;
					case AFK:
						knownOnlineContacts.remove(contact);//status aktualisieren
						knownOnlineContacts.add(contact);
						break;
					case DND:
						knownOnlineContacts.remove(contact);//status aktualisieren
						knownOnlineContacts.add(contact);

					}
				} else if (object instanceof RequestKnownOnlineClients) {
					ContactDto[] contacts = knownOnlineContacts.toArray(new ContactDto[]{});
					for (int i = 0; i < contacts.length; i++) {
						if (c.getRemoteAddressTCP().getAddress().getHostAddress().equals(contacts[i].ip)) {
							ContactDto contactDto = new ContactDto("server", EOnlineState.ONLINE);
							contactDto.ip = LOCAL_IP;
							contacts[i] = contactDto;
						}
					}
					c.sendTCP(new ContactList(contacts));
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
	
	private void getLocalIP() throws UnknownHostException, SocketException{
//	    System.out.println("Your Host addr: " + InetAddress.getLocalHost().getHostAddress());  // often returns "127.0.0.1"
	    Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
	    while (n.hasMoreElements()){
	        NetworkInterface e = n.nextElement();

	        Enumeration<InetAddress> a = e.getInetAddresses();
	        while(a.hasMoreElements()){
	            InetAddress addr = a.nextElement();
	            String hostAddress = addr.getHostAddress();
	            if (hostAddress.matches("[0-9]{1,3}(.)[0-9]{1,3}(.)[0-9]{1,3}(.)[0-9]{1,3}") 
	            		&& !hostAddress.equals("127.0.0.1")) {
					LOCAL_IP = hostAddress;
					Log.debug("Found local Host-Address: " + hostAddress);
					return;
				}
	        }
	        Log.error("No local Host-Address Found!");
	        System.exit(1);
	    }
	} 
	
	private ContactDto findByAddr(InetAddress ad){
		String ip = ad.getHostAddress();
		for (ContactDto contactDto : knownOnlineContacts) {
			if (contactDto.ip.equals(ip)) {
				return contactDto;
			}
		}
		return null;
	}
	
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