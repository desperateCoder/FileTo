package main.java.de.c4.controller.server;

import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

import main.java.de.c4.controller.shared.MessageHandler;
import main.java.de.c4.controller.shared.Network;
import main.java.de.c4.model.connections.ChatConnection;

public class ChatServer extends Thread{
	
	private Server server;
	private PingServer pingServer;
	
	public ChatServer() throws IOException {
		startUDP();
		server = new Server() {
			protected Connection newConnection() {
				return new ChatConnection();
			}
		
		};

		Network.register(server);

		MessageHandler messageHandler = new MessageHandler();
		server.addListener(messageHandler);
		server.bind(Network.TCP_PORT);//

	}

	public void startUDP() throws IOException {
		pingServer = new PingServer();
		pingServer.start();
	}
	
	public static void main(String[] args) throws IOException {
		Log.set(Log.LEVEL_DEBUG);
		
		new ChatServer().start();
	}

	@Override
	public void run() {
		server.start();
	}
	
	public void killUDP() {
		pingServer.kill();
	}
}