package main.java.de.c4.model.messages;

import main.java.de.c4.view.resources.EIcons;

public enum EOnlineState {
	ONLINE(1, "Online", EIcons.STATE_ONLINE),
	AFK(2, "AFK (Abwesend)", EIcons.STATE_AFK), 
	DND(3, "DND (Bitte nicht stören)", EIcons.STATE_DND),
	OFFLINE(4, "Offline", EIcons.STATE_OFFLINE); 
	
	private int nr;
	private String title;
	private EIcons icon;

	private EOnlineState(int nr, String title, EIcons icon) {
		this.nr = nr;
		this.icon = icon;
		this.title = title;
	}

	public int getNr() {
		return nr;
	}
	public static EOnlineState getByNr(int i){
		for (EOnlineState s : values()) {
			if (s.getNr()==i) {
				return s;
			}
		}
		throw new RuntimeException("Unbekannter Online-Status: "+i);
	}
	
	@Override
	public String toString() {
		return title;
	}
	public EIcons getIcon() {
		return icon;
	}
}
