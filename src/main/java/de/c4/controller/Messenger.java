package main.java.de.c4.controller;

import java.io.IOException;
import java.util.Set;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.minlog.Log;

import main.java.de.c4.controller.client.ChatClient;
import main.java.de.c4.controller.server.ChatServer;
import main.java.de.c4.controller.shared.ContactList;
import main.java.de.c4.controller.shared.ExceptionUtil;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.RequestKnownOnlineClients;



public class Messenger {

	public static final Messenger INSTANCE = new Messenger(true);
	private ChatServer chatServer;
	
	public Messenger() {
		this(false);
	}
	private Messenger(boolean isInternalCall) {
		if (!isInternalCall) {
			throw new RuntimeException("This Class is a Singleton and " +
					"should be accessed by its Instance-Field");
		}
		
		try {
			ChatClient chatClient = new ChatClient(ChatClient.discoverRandomServer());
			chatClient.connect();
			Client client = chatClient.getClient();
			Log.debug("TCP Connected to Server: "+client.getRemoteAddressTCP());
			
			RequestKnownOnlineClients req = new RequestKnownOnlineClients();
			client.sendTCP(req);
			chatServer = new ChatServer();
			chatServer.start();
		} catch (IOException e) {
			Log.error("I/O-Error starting the server: " + ExceptionUtil.getStacktrace(e));
		} catch (NullPointerException e) {
			Log.info("Server started, but: " + e.getMessage());
		}
		
	}
	
	public static void init(){/*nichts! laedt die klasse, das reicht!*/};
	
	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		Log.set(Log.LEVEL_DEBUG);
		Thread.sleep(100);
		Set<ContactDto> dtos = ContactList.INSTANCE.getContacts();
		for (ContactDto c : dtos) {
			Log.debug("Contact: "+c.name+" ("+c.ip+"): "+c.state);
		}
	}
	
	

}
