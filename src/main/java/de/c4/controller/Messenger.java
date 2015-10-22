package main.java.de.c4.controller;

import main.java.de.c4.controller.client.ChatClient;
import main.java.de.c4.model.messages.RequestKnownOnlineClients;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.minlog.Log;



public class Messenger {

	public static final Messenger INSTANCE = new Messenger(true);
	
	public Messenger() {
		this(false);
	}
	private Messenger(boolean isInternalCall) {
		if (!isInternalCall) {
			throw new RuntimeException("This Class is a Singleton and " +
					"should be accessed by its Instance-Field");
		}
		ChatClient chatClient = new ChatClient(ChatClient.discoverRandomServer());
		chatClient.connect();
		Client client = chatClient.getClient();
		Log.debug("TCP Connected to Server: "+client.getRemoteAddressTCP());
		
		RequestKnownOnlineClients req = new RequestKnownOnlineClients();
		client.sendTCP(req);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}

}
