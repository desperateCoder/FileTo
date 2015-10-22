package main.java.de.c4.controller;

import main.java.de.c4.controller.client.ChatClient;



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
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}

}
