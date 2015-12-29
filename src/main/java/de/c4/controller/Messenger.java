package main.java.de.c4.controller;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.java.de.c4.controller.client.ChatClient;
import main.java.de.c4.controller.server.ChatServer;
import main.java.de.c4.controller.shared.ConnectionManager;
import main.java.de.c4.controller.shared.Diffie;
import main.java.de.c4.controller.shared.ExceptionUtil;
import main.java.de.c4.controller.shared.Network;
import main.java.de.c4.controller.shared.listener.MessageRecievedListener;
import main.java.de.c4.model.messages.ChatMessage;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.ContactList;
import main.java.de.c4.model.messages.OnlineStateChange;
import main.java.de.c4.model.messages.RequestKnownOnlineClients;
import main.java.de.c4.model.messages.SecondClientStarted;
import main.java.de.c4.model.messages.file.FileTransferRequest;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;

public class Messenger {

	public static final Messenger INSTANCE = new Messenger(true);
	private static final Set<MessageRecievedListener> LISTENER = new HashSet<MessageRecievedListener>();
	private ChatServer chatServer = null;

	public Messenger() {
		this(false);
	}

	private Messenger(boolean isInternalCall) {
		if (!isInternalCall) {
			throw new RuntimeException("This Class is a Singleton and " +
					"should be accessed by its Instance-Field");
		}
		detectRunningInstance();
		requestContacts(getRandomServer(), false);
		try {
			chatServer = new ChatServer();
			chatServer.start();
		} catch (IOException e) {
			Log.error("I/O-Error starting the server: " + ExceptionUtil.getStacktrace(e));
			
		}
	}

	private static String getRandomServer() {
		String host = null;
		try {
			host=ChatClient.discoverRandomServer();
		} catch (NullPointerException e) {
			Log.info("No UDP-Server availible.");
		}
		return host;
	}
	private void detectRunningInstance() {
		if (!isPortAvailable(Network.TCP_PORT)) {
			try {
				ChatClient chatClient = new ChatClient("localhost");
				chatClient.connect();
				Client client = chatClient.getClient();
				Diffie.wait(client);
				client.sendTCP(new SecondClientStarted());
			} catch (NullPointerException e) {
				Log.info("Server started, but: " + e.getMessage());
			} finally {
				System.exit(6);
			}
		}
	}
	private boolean isPortAvailable(int port) {
	    Socket s = null;
	    try {
	        s = new Socket("localhost", port);
	        return false;
	    } catch (IOException e) {
	        return true;
	    } finally {
	        if( s != null){
	            try {
	                s.close();
	            } catch (IOException e) {
	                throw new RuntimeException("You should handle this error." , e);
	            }
	        }
	    }
	}

	public static void requestContacts() {
		requestContacts(getRandomServer(), true);
	}
	private static void requestContacts(final String host, final boolean killUDP) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (killUDP) {
					INSTANCE.chatServer.killUDP();
				}
				if (host!=null) {
					ChatClient chatClient = new ChatClient(host);
					chatClient.connect();
					Client client = chatClient.getClient();
					Diffie.wait(client);
					Log.debug("TCP Connected to Server: "+client.getRemoteAddressTCP());
					
					RequestKnownOnlineClients req = new RequestKnownOnlineClients();
					client.sendTCP(req);
				}
				if (killUDP) {
					try {
						INSTANCE.chatServer.startUDP();
					} catch (IOException e) {
						Log.error("Cannot start UDP-Server: "+ExceptionUtil.getStacktrace(e));
					}
				}
					
			}
		}).start();
	}

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
		change.newState = change.contact.state;
		ContactList.INSTANCE.broadcast(change);
	}
	
	public static void sendMessageTo(final Object chatMessage, final Collection<ContactDto> contacts) {
		new Thread(new Runnable() {
			public void run() {
				for (ContactDto contactDto : contacts) {
					try {
						Connection c = ConnectionManager.getConnectionsTo(contactDto, true).iterator().next();
						c.sendTCP(chatMessage);
					} catch (Exception e) {
						Log.debug("Senden an "+contactDto.name+" nicht moeglich: "+e.getMessage());
					}
				}
			}
		}).start();
	}
	public static void receiveMessageFrom(ContactDto contact, ChatMessage message){
		for (MessageRecievedListener l : LISTENER) {
			l.messageRecieved(contact, message);
		}
	}
	public static void receiveAlertFrom(ContactDto contact){
		for (MessageRecievedListener l : LISTENER) {
			l.alert(contact);
		}
	}
	public static void addMessageReceivedListener(MessageRecievedListener l){
		LISTENER.add(l);
	}
	public static void removeMessageReceivedListener(MessageRecievedListener l){
		LISTENER.remove(l);
	}

	public static void goOffline() {
		INSTANCE.chatServer.killUDP();
	}

	public static void fileTransferRequestReceived(FileTransferRequest request,
			ContactDto contact) {
		for (MessageRecievedListener l : LISTENER) {
			l.fileTransferRequestRecieved(contact, request);
		}
	}

	public static void secondClientStarted() {
		for (MessageRecievedListener l : LISTENER) {
			l.secondClientStarted();
		}
	}
}
