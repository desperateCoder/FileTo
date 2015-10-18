package main.java.de.c4.controller.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import main.java.de.c4.controller.client.ChatClient;
import main.java.de.c4.model.messages.ContactDto;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;

/**
 * Singleton providing all open Connections to Contacts
 * @author artur
 *
 */
public class ConnectionManager extends HashMap<ContactDto, List<Connection>> {
	private static final long serialVersionUID = 1L;
	
	private static final ConnectionManager INSTANCE = new ConnectionManager();
	
	/**
	 * returns the Connections to a specified Contact
	 * @param connections to which contact
	 * @return List of open connections
	 */
	public static List<Connection> getConnectionsTo(ContactDto contact){
		return getConnectionsTo(contact, false);
	}

	public static List<Connection> getConnectionsTo(ContactDto contact,
			boolean createIfNone) {
		if (INSTANCE.containsKey(contact)) {
			return INSTANCE.get(contact);
		}
		List<Connection> list = new ArrayList<Connection>();
		if (createIfNone) {
			ChatClient client = new ChatClient(contact.ip);
			client.start();
			list.add(client.getClient());
		}
		INSTANCE.put(contact, list);
		
		return list;
	}
	
	public static Connection createConnectionTo(ContactDto contact){
		ChatClient client = new ChatClient(contact.ip);
		client.start();
		List<Connection> list = null;
		if (INSTANCE.containsKey(contact)) {
			list = INSTANCE.get(contact);
		} else {
			list = new ArrayList<Connection>();
			INSTANCE.put(contact, list);
		}
		Client c = client.getClient();
		list.add(c);
		return c;
	}
	
	public static void removeConnection(Connection connection){
		Set<ContactDto> keys = INSTANCE.keySet();
		for (ContactDto key : keys) {
			List<Connection> connections = INSTANCE.get(key);
			for (Connection c : connections) {
				if (c.getID()==connection.getID()) {
					connections.remove(c);
					return;
				}
			}
		}
	}
	
	public static void closeAndRemoveConnection(Connection connection){
		connection.close();
		removeConnection(connection);
	}
	
	public static void closeAll(){
		Set<ContactDto> keys = INSTANCE.keySet();
		for (ContactDto key : keys) {
			List<Connection> connections = INSTANCE.get(key);
			for (Connection c : connections) {
				c.close();
			}
			INSTANCE.remove(key);
		}
	}
}
