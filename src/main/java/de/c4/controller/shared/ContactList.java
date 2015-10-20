package main.java.de.c4.controller.shared;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.ContactListDto;
import main.java.de.c4.model.messages.EOnlineState;
import main.java.de.c4.model.messages.OnlineStateChange;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.minlog.Log;
/**
 * Singleton, which provides the ContactList and States of the Contacts
 * @author Artur Dawtjan
 *
 */
public class ContactList {

	public static final ContactList INSTANCE = new ContactList(true);
	
	private Set<ContactDto> knownOnlineContacts = new HashSet<ContactDto>();
	
	public static final String LOCAL_IP = getLocalIP();
	
	private ContactDto me = null;
	
	public ContactList() {
		this(false);
	}
	private ContactList(boolean isInternalCall) {
		if (!isInternalCall) {
			throw new RuntimeException("This Class is a Singleton and " +
					"should be accessed by its Instance-Field");
		}
		setOwnContact(Settings.INSTANCE.get(Settings.CONTACT_NAME), 
				EOnlineState.getByNr(Integer.parseInt(Settings.INSTANCE.get(Settings.CONTACT_ONLINE_STATE))));
	}
	
	public ContactDto findByAddr(InetAddress ad){
		String ip = ad.getHostAddress();
		for (ContactDto contactDto : knownOnlineContacts) {
			if (contactDto.ip.equals(ip)) {
				return contactDto;
			}
		}
		return null;
	}
	
	private static String getLocalIP(){
	    try {
			Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
			while (n.hasMoreElements()) {
				NetworkInterface e = n.nextElement();

				Enumeration<InetAddress> a = e.getInetAddresses();
				while (a.hasMoreElements()) {
					InetAddress addr = a.nextElement();
					String hostAddress = addr.getHostAddress();
					if (hostAddress.matches("[0-9]{1,3}(.)[0-9]{1,3}(.)[0-9]{1,3}(.)[0-9]{1,3}")
							&& !hostAddress.equals("127.0.0.1")) {
						Log.debug("Found local Host-Address: " + hostAddress);
						return hostAddress;
					}
				}
				Log.error("No local Host-Address Found!");
				System.exit(1);
			} 
		} catch(Exception e) {
			Log.error("No local Host-Address Found! Cause:");
			Log.error(ExceptionUtil.getStacktrace(e));
	        System.exit(1);
		}
	    return null;
	} 
	
	public ContactListDto getContactListForContactsRequest(InetAddress endPointAdress){
		ContactDto[] contacts = knownOnlineContacts.toArray(new ContactDto[]{});
		for (int i = 0; i < contacts.length; i++) {
			if (endPointAdress.getHostAddress().equals(contacts[i].ip)) {
				contacts[i] = me;
			}
		}
		return new ContactListDto(contacts);
	}
	
	

	public void contactStateChanged(OnlineStateChange onlineState, InetAddress ip){
		if (onlineState.contact == null || onlineState.contact.name == null)
			return;
		String name = onlineState.contact.name;
		name = name.trim();
		if (name.length() == 0)
			return;
		ContactDto contact = findByAddr(ip);
		if (contact == null) {
			contact = new ContactDto(name);
		}
		contact.ip = ip.getHostAddress();
		contact.state = onlineState.newState;
		
		
		switch (onlineState.newState) {
		case ONLINE:
			knownOnlineContacts.add(contact);
			break;
		case OFFLINE:
			knownOnlineContacts.remove(contact);
			break;
		case AFK:
			knownOnlineContacts.remove(contact);//status aktualisieren
			knownOnlineContacts.add(contact);
			break;
		case DND:
			knownOnlineContacts.remove(contact);//status aktualisieren
			knownOnlineContacts.add(contact);

		}
	}
	
	public void setOnlineContacts(ContactDto[] contacts){
		knownOnlineContacts.clear();
		for (ContactDto c : contacts) {
			knownOnlineContacts.add(c);
		}
	}
	
	private void setOwnContact(String name, EOnlineState onlineState){
		if (me==null) {
			me = new ContactDto();
		}
		me.ip = LOCAL_IP;
		me.name = name;
		me.state = onlineState;
	}

	/**
	 * Sets the OnlineState and notifies all known other clients, 
	 * if, and only if, it has changed.
	 * @param onlineState new online-state
	 */
	public void setOnlineState(final EOnlineState onlineState) {
		new Thread(new Runnable() {
			
			public void run() {
				if (me.state != onlineState) {
					me.state = onlineState;
					OnlineStateChange change = new OnlineStateChange();
					change.contact = me;
					change.newState = me.state;
					for (ContactDto c : knownOnlineContacts) {
						Connection connection = ConnectionManager.createConnectionTo(c);
						connection.sendTCP(change);
						ConnectionManager.closeAndRemoveConnection(connection);
					}
				} else me.state = onlineState;
			}
		}).start();
		
	}
	
	public static ContactDto getMe(){
		return INSTANCE.me;
	}

	public Set<ContactDto> getContacts() {
		return knownOnlineContacts;
	}
}
