package main.java.de.c4.controller.client;
import java.awt.EventQueue;
import java.io.IOException;
import java.net.InetAddress;

import main.java.de.c4.controller.shared.Network;
import main.java.de.c4.controller.shared.Network.ChatMessage;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.ContactListDto;
import main.java.de.c4.model.messages.EOnlineState;
import main.java.de.c4.model.messages.OnlineStateChange;
import main.java.de.c4.model.messages.RequestKnownOnlineClients;
import main.java.de.c4.view.ChatFrame;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

public class ChatClient extends Thread{
	ChatFrame chatFrame;
	private Client client;
	private String host;
//	String name;

	public ChatClient (String hostAdress) {
		client = new Client();
		client.start();

		Network.register(client);

		client.addListener(new Listener() {
			public void connected (Connection connection) {
				Log.debug("TCP Connected to Server: "+connection.getRemoteAddressTCP());
				OnlineStateChange state = new OnlineStateChange();
				state.contact = new ContactDto("Test "+System.currentTimeMillis());
				state.newState = EOnlineState.ONLINE;
				client.sendTCP(state);
				
				RequestKnownOnlineClients req = new RequestKnownOnlineClients();
				client.sendTCP(req);
			}

			public void received (Connection connection, Object object) {

				if (object instanceof ContactListDto){
					ContactListDto list = (ContactListDto)object;
					Log.info("Recieved ContactList!");
					for (ContactDto dto : list.contacts) {
						System.out.println(dto.name + " ("+dto.ip+"): "+dto.state);
					}
				} else if (object instanceof ChatMessage) {
					ChatMessage chatMessage = (ChatMessage)object;
					chatFrame.addMessage(chatMessage.text);
					return;
				}
			}

			public void disconnected (Connection connection) {
				EventQueue.invokeLater(new Runnable() {
					public void run () {
						// Closing the frame calls the close listener which will stop the client's update thread.
						chatFrame.dispose();
					}
				});
			}
		});


		// Request the user's name.
//		String input = (String)JOptionPane.showInputDialog(null, "Name:", "Connect to chat server", JOptionPane.QUESTION_MESSAGE, null,
//			null, "Test");
//		if (input == null || input.trim().length() == 0) System.exit(1);
//		name = "TEST";

		// All the ugly Swing stuff is hidden in ChatFrame so it doesn't clutter the KryoNet example code.
		
//		final String host = "localhost";
		chatFrame = new ChatFrame(host);
		// This listener is called when the send button is clicked.
		chatFrame.setSendListener(new Runnable() {
			public void run () {
				ChatMessage chatMessage = new ChatMessage();
				chatMessage.text = chatFrame.getSendText();
				client.sendTCP(chatMessage);
			}
		});
		// This listener is called when the chat window is closed.
		chatFrame.setCloseListener(new Runnable() {
			public void run () {
				client.stop();
			}
		});
		chatFrame.setVisible(true);
		
		
	}
	
	public Client getClient() {
		return client;
	}
	
	public void run () {
		try {
			client.connect(Network.DEFAULT_TIMEOUT, host, Network.TCP_PORT);
			// Server communication after connection can go here, or in Listener#connected().
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	public static String discoverRandomServer(){
		Client client = new Client();
		client.start();
		InetAddress addr = client.discoverHost(Network.UDP_PORT, 10000);
		String host = addr.getHostAddress();
		client.stop();
		return host;
	}
	
	public static void main (String[] args) {
		System.setProperty("java.net.preferIPv4Stack" , "true");
		Log.set(Log.LEVEL_DEBUG);
		new ChatClient(discoverRandomServer());
	}
}