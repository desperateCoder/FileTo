package main.java.de.c4.controller.server;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;

import main.java.de.c4.controller.shared.Network;
import main.java.de.c4.controller.shared.Network.ChatMessage;
import main.java.de.c4.controller.shared.Network.UpdateNames;
import main.java.de.c4.model.connections.ChatConnection;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.OnlineStateChange;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;

public class ChatServer {
	private Server server;

	public ChatServer() throws IOException {
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
				ChatConnection connection = (ChatConnection) c;

				if (object instanceof OnlineStateChange) {
					// Ignore the object if a client has already registered a
					// name. This is
					// impossible with our client, but a hacker could send
					// messages at any time.
					if (connection.contact != null && connection.contact.name != null)
						return;
					// Ignore the object if the name is invalid.
					String name = ((OnlineStateChange) object).contact.name;
					if (name == null)
						return;
					name = name.trim();
					if (name.length() == 0)
						return;
					// Store the name on the connection.
					if (connection.contact == null) {
						connection.contact = new ContactDto();
					}
					connection.contact.name = name;
					// Send a "connected" message to everyone except the new
					// client.
					ChatMessage chatMessage = new ChatMessage();
					chatMessage.text = name + " connected.";
					server.sendToAllExceptTCP(connection.getID(), chatMessage);
					// Send everyone a new list of connection names.
					updateNames();
					return;
				}

				if (object instanceof ChatMessage) {
					// Ignore the object if a client tries to chat before
					// registering a name.
					if (connection.contact.name == null)
						return;
					ChatMessage chatMessage = (ChatMessage) object;
					// Ignore the object if the chat message is invalid.
					String message = chatMessage.text;
					if (message == null)
						return;
					message = message.trim();
					if (message.length() == 0)
						return;
					// Prepend the connection's name and send to everyone.
					chatMessage.text = connection.contact.name + ": " + message;
					server.sendToAllTCP(chatMessage);
					return;
				}
			}

			public void disconnected(Connection c) {
				ChatConnection connection = (ChatConnection) c;
				if (connection.contact.name != null) {
					// Announce to everyone that someone (with a registered
					// name) has left.
					ChatMessage chatMessage = new ChatMessage();
					chatMessage.text = connection.contact.name + " disconnected.";
					server.sendToAllTCP(chatMessage);
					updateNames();
				}
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

	void updateNames() {
		// Collect the names for each connection.
		Connection[] connections = server.getConnections();
		ArrayList names = new ArrayList(connections.length);
		for (int i = connections.length - 1; i >= 0; i--) {
			ChatConnection connection = (ChatConnection) connections[i];
			names.add(connection.contact.name);
		}
		// Send the names to everyone.
		UpdateNames updateNames = new UpdateNames();
		updateNames.names = (String[]) names.toArray(new String[names.size()]);
		server.sendToAllTCP(updateNames);
	}

	public static void main(String[] args) throws IOException {
		Log.set(Log.LEVEL_DEBUG);
		
		new ChatServer();
	}
}