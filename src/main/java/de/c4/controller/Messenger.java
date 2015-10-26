package main.java.de.c4.controller;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.java.de.c4.controller.client.ChatClient;
import main.java.de.c4.controller.server.ChatServer;
import main.java.de.c4.controller.shared.ConnectionManager;
import main.java.de.c4.controller.shared.ContactList;
import main.java.de.c4.controller.shared.ExceptionUtil;
import main.java.de.c4.controller.shared.Network.ChatMessage;
import main.java.de.c4.controller.shared.listener.MessageRecievedListener;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.OnlineStateChange;
import main.java.de.c4.model.messages.RequestKnownOnlineClients;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;

public class Messenger {

	public static final Messenger INSTANCE = new Messenger(true);
	private static final Map<Long, Set<MessageRecievedListener>> LISTENER = new HashMap<Long, Set<MessageRecievedListener>>();
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
		} catch (NullPointerException e) {
			Log.info("Server started, but: " + e.getMessage());
		}
		try {
			chatServer = new ChatServer();
			chatServer.start();
		} catch (IOException e) {
			Log.error("I/O-Error starting the server: " + ExceptionUtil.getStacktrace(e));
			
		}
	}

	public static void init() {/* nichts! laedt die klasse, das reicht! */
	};

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		Log.set(Log.LEVEL_DEBUG);
		Thread.sleep(100);
		List<ContactDto> dtos = ContactList.INSTANCE.getContacts();
		for (ContactDto c : dtos) {
			Log.debug("Contact: " + c.name + " (" + c.ip + "): " + c.state);
		}
	}

	public static void goOnline(){
		OnlineStateChange change = new OnlineStateChange();
		change.contact = ContactList.getMe();
		change.newState = ContactList.getMe().state;
		ContactList.INSTANCE.broadcast(change);
	}
	
	public static void sendMessageTo(final Object chatMessage, final Collection<ContactDto> contacts) {
		new Thread(new Runnable() {
			public void run() {
				for (ContactDto contactDto : contacts) {
					Connection c = ConnectionManager.getConnectionsTo(contactDto, true).iterator().next();
					c.sendTCP(chatMessage);
				}
			}
		}).start();
	}
	public static void receiveMessageFrom(ContactDto contact, ChatMessage message){
		for (MessageRecievedListener l : LISTENER.get(Long.valueOf(message.id))) {
			l.messageRecieved(contact, message);
		}
	}
	public static void addMessageReceivedListener(long chatId, MessageRecievedListener l){
		Long id = Long.valueOf(chatId);
		Set<MessageRecievedListener> listener = LISTENER.containsKey(id)?LISTENER.get(id):new HashSet<MessageRecievedListener>();
		listener.add(l);
		LISTENER.put(id, listener);
	}
	public static void removeMessageReceivedListener(long chatId, MessageRecievedListener l){
		Long id = Long.valueOf(chatId);
		if (LISTENER.containsKey(id)) {
			Set<MessageRecievedListener> set = LISTENER.get(id);
			if (set.contains(l)) {
				if (set.size()==1) {
					LISTENER.remove(id);
				} else set.remove(l);
			}
		}
	}
}
