package main.java.de.c4.controller.shared;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import main.java.de.c4.controller.Messenger;
import main.java.de.c4.controller.shared.listener.ContactListReceivedListener;
import main.java.de.c4.controller.shared.listener.OnlineStateChangeListener;
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
	

	private Set<ContactListReceivedListener> contactListReceivedListener = new HashSet<ContactListReceivedListener>();
	private Set<OnlineStateChangeListener> onlineStateChangeListener = new HashSet<OnlineStateChangeListener>();

	public static final ContactList INSTANCE = new ContactList(true);
	
	private ArrayList<ContactDto> knownOnlineContacts = new ArrayList<ContactDto>();
	
	private static String LOCAL_IP = null;
	
	private ContactDto me = null;
	
	public ContactList() {
		this(false);
	}
	private ContactList(boolean isInternalCall) {
		if (!isInternalCall) {
			throw new RuntimeException("This Class is a Singleton and " +
					"should be accessed by its Instance-Field");
		}
		LOCAL_IP = getLocalIP();
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
					Log.debug("Host-Address: " + hostAddress);
					if (hostAddress.matches("[0-9]{1,3}(.)[0-9]{1,3}(.)[0-9]{1,3}(.)[0-9]{1,3}")
							&& !hostAddress.equals("127.0.0.1")) {
						Log.debug("Found local Host-Address: " + hostAddress);
						return hostAddress;
					}
				}
				Log.error("No local Host-Address Found! (No interfaces detected)");
			} 
		} catch(Exception e) {
			Log.error("No local Host-Address Found! Cause:");
			Log.error(ExceptionUtil.getStacktrace(e));
//	        System.exit(1);
		}
	    try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			Log.error("No local Host-Address Found! Cause:");
			Log.error(ExceptionUtil.getStacktrace(e));
		}
	    return "";
	    
	} 
	
	public ContactListDto getContactListForContactsRequest(InetAddress endPointAdress){
		ArrayList<ContactDto> contacts = new ArrayList<ContactDto>(knownOnlineContacts);
		boolean includedMe = false;
		for (int i = 0; i < contacts.size(); i++) {
			if (endPointAdress.getHostAddress().equals(contacts.get(i).ip)) {
				contacts.set(i, me);
				includedMe=true;
			}
		}
		if (!includedMe) {
			contacts.add(me);
		}
		return new ContactListDto(contacts.toArray(new ContactDto[]{}));
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
			if (!knownOnlineContacts.contains(contact)) {
				knownOnlineContacts.add(contact);
			}
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
		for (OnlineStateChangeListener l : onlineStateChangeListener) {
			l.onlineStateChanged(onlineState);
		}
	}
	
	public void addOnlineStateChangeListener(OnlineStateChangeListener l){
		onlineStateChangeListener.add(l);
	}
	
	public void setOnlineContacts(ContactDto[] contacts){
		knownOnlineContacts.clear();
		for (ContactDto c : contacts) {
			knownOnlineContacts.add(c);
		}
		for (ContactListReceivedListener l : contactListReceivedListener) {
			l.receivedContactList(contacts);
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
	 * Sets the OnlineState and notifies all known other clients and updates the settings, 
	 * if, and only if, it has changed.
	 * @param onlineState new online-state
	 */
	public void setOnlineState(final EOnlineState onlineState) {
		setOnlineState(onlineState, true);
	}
	
	/**
	 * Sets the OnlineState and notifies all known other clients and updates the settings, 
	 * if, and only if, it has changed.
	 * @param setInSettings if true, settings-file will be updated
	 * @param onlineState new online-state
	 */
	public void setOnlineState(final EOnlineState onlineState, boolean setInSettings) {
		boolean isFromOffToOn = false;
		if (me.state==EOnlineState.OFFLINE && onlineState != EOnlineState.OFFLINE) {
			isFromOffToOn = true;
		} else if (onlineState == EOnlineState.OFFLINE) {
			Messenger.goOffline();
		}
		if (me.state != onlineState) {
			me.state = onlineState;
			OnlineStateChange change = new OnlineStateChange();
			change.contact = me;
			change.newState = me.state;
			broadcast(change);
			if (setInSettings && onlineState != EOnlineState.OFFLINE) {
				Settings.INSTANCE.set(Settings.CONTACT_ONLINE_STATE, onlineState.getNr()+"");
				Settings.INSTANCE.save();
			}
		}

		if (isFromOffToOn) {
			Messenger.requestContacts();
			Messenger.goOnline();
		}
	}
	public void broadcast(final Object o) {
		new Thread(new Runnable() {
			
			public void run() {
				for (ContactDto c : knownOnlineContacts) {
					Connection connection = ConnectionManager.createConnectionTo(c);
					connection.sendTCP(o);
					ConnectionManager.closeAndRemoveConnection(connection);
				}
			}
			
		}).start();
	}
	
	public static ContactDto getMe(){
		return INSTANCE.me;
	}

	public ArrayList<ContactDto> getContacts() {
		return knownOnlineContacts;
	}
	public void addReceivedContactListListener(ContactListReceivedListener l) {
		contactListReceivedListener.add(l);
		if (knownOnlineContacts.size()>0) {
			l.receivedContactList(knownOnlineContacts.toArray(new ContactDto[]{}));
		}
	}
}
