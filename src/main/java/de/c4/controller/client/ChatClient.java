package main.java.de.c4.controller.client;
import java.awt.EventQueue;
import java.io.IOException;
import java.net.InetAddress;

import main.java.de.c4.controller.shared.MessageHandler;
import main.java.de.c4.controller.shared.Network;
import main.java.de.c4.controller.shared.Network.ChatMessage;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.ContactListDto;
import main.java.de.c4.model.messages.EOnlineState;
import main.java.de.c4.model.messages.OnlineStateChange;
import main.java.de.c4.model.messages.RequestKnownOnlineClients;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

public class ChatClient{
	private Client client;
	private String host;
//	String name;

	public ChatClient (String hostAdress) {
		client = new Client();
		client.start();

		Network.register(client);

		client.addListener(new MessageHandler());

		host = hostAdress;

		// Request the user's name.
//		String input = (String)JOptionPane.showInputDialog(null, "Name:", "Connect to chat server", JOptionPane.QUESTION_MESSAGE, null,
//			null, "Test");
//		if (input == null || input.trim().length() == 0) System.exit(1);
//		name = "TEST";

	}
	
	public Client getClient() {
		return client;
	}
	
	public void connect () {
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
		new ChatClient(discoverRandomServer()).connect();
		
	}
}