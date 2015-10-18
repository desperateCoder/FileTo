package main.java.de.c4.controller.shared;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import com.esotericsoftware.minlog.Log;

import main.java.de.c4.controller.shared.Network.ChatMessage;
import main.java.de.c4.controller.shared.listener.MessageRecievedListener;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.ContactListDto;
import main.java.de.c4.model.messages.EOnlineState;
import main.java.de.c4.model.messages.OnlineStateChange;
/**
 * Singleton, which provides the ContactList and States of the Contacts
 * @author Artur Dawtjan
 *
 */
public class ContactList {

	public static final ContactList INSTANCE = new ContactList();
	
	private Set<ContactDto> knownOnlineContacts = new HashSet<ContactDto>();
	private Set<MessageRecievedListener> messageRecievedListener = new HashSet<MessageRecievedListener>();
	
	public static final String LOCAL_IP = getLocalIP();
	
	public ContactList() {
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
				ContactDto contactDto = new ContactDto("server", EOnlineState.ONLINE);
				contactDto.ip = LOCAL_IP;
				contacts[i] = contactDto;
			}
		}
		return new ContactListDto(contacts);
	}
	
	public void messageRecieved(ContactDto contact, ChatMessage chatMessage){
		for (MessageRecievedListener l : messageRecievedListener) {
			l.messageRecieved(contact, chatMessage);
		}
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
}
