package main.java.de.c4.controller.shared;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;

import main.java.de.c4.controller.client.ChatClient;
import main.java.de.c4.model.messages.ContactDto;

/**
 * Singleton providing all open Connections to Contacts
 * @author artur
 *
 */
public class ConnectionManager extends HashMap<ContactDto, Set<Connection>> {
	private static final long serialVersionUID = 1L;
	
	private static final ConnectionManager INSTANCE = new ConnectionManager();
	
	/**
	 * returns the Connections to a specified Contact
	 * @param connections to which contact
	 * @return List of open connections
	 */
	public static Set<Connection> getConnectionsTo(ContactDto contact){
		return getConnectionsTo(contact, false);
	}

	public static Set<Connection> getConnectionsTo(ContactDto contact,
			boolean createIfNone) {
		if (INSTANCE.containsKey(contact)) {
			return INSTANCE.get(contact);
		}
		Set<Connection> list = new HashSet<Connection>();
		if (createIfNone) {
			ChatClient client = new ChatClient(contact.ip);
			client.connect();
			list.add(client.getClient());
		}
		INSTANCE.put(contact, list);
		
		return list;
	}
	
	public static Connection createConnectionTo(ContactDto contact){
		ChatClient client = new ChatClient(contact.ip);
		client.connect();
		Set<Connection> list = null;
		if (INSTANCE.containsKey(contact)) {
			list = INSTANCE.get(contact);
		} else {
			list = new HashSet<Connection>();
			INSTANCE.put(contact, list);
		}
		Client c = client.getClient();
		list.add(c);
		return c;
	}
	
	/**
	 * Removes connection if registered.
	 * @param connection Connection to remove
	 */
	public static void removeConnection(Connection connection){
		Set<ContactDto> keys = INSTANCE.keySet();
		for (ContactDto key : keys) {
			Set<Connection> connections = INSTANCE.get(key);
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
			Set<Connection> connections = INSTANCE.get(key);
			for (Connection c : connections) {
				c.close();
			}
			INSTANCE.remove(key);
		}
	}

	public static boolean isConnectionRegistered(Connection c) {
		Collection<Set<Connection>> all = INSTANCE.values();
		for (Set<Connection> set : all) {
			for (Connection connection : set) {
				if (connection.getID() == c.getID()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean isConnectionRegistered(ContactDto contact, Connection c) {
		Set<Connection> set =INSTANCE.get(contact);
		if (set==null) {
			return false;
		}
		for (Connection connection : set) {
			if (connection.getID() == c.getID()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Registers a Connection, if, and only if, it isn't already registered
	 * the IP will be overwritten by the RemoteAdress of the Connection
	 * @param contact Contact-Info
	 * @param c Connection
	 */
	public static void registerConnection(ContactDto contact, Connection c) {
		contact.ip = c.getRemoteAddressTCP().getAddress().getHostAddress();
		if (!isConnectionRegistered(contact, c)) {
			Set<Connection> list = null;
			if (INSTANCE.containsKey(contact)) {
				list = INSTANCE.get(contact);
			} else {
				list = new HashSet<Connection>();
				INSTANCE.put(contact, list);
			}
			list.add(c);
		}
	}
}
