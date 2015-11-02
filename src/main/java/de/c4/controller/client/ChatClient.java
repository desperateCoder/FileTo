package main.java.de.c4.controller.client;
import java.io.IOException;
import java.net.InetAddress;

import main.java.de.c4.controller.shared.MessageHandler;
import main.java.de.c4.controller.shared.Network;

import com.esotericsoftware.kryonet.Client;

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

	public static String discoverRandomServer() throws NullPointerException{
		Client client = new Client();
		client.start();
		InetAddress addr = client.discoverHost(Network.UDP_PORT, 2000);
		client.stop();
		if (addr == null) {
			throw new NullPointerException("There is no other server online.");
		}
		String host = addr.getHostAddress();
		return host;
	}
	
//	public static void main (String[] args) {
//		System.setProperty("java.net.preferIPv4Stack" , "true");
//		Log.set(Log.LEVEL_DEBUG);
//		new ChatClient(discoverRandomServer()).connect();
//	}
}