package main.java.de.c4.model.messages;

public enum EOnlineState {
	ONLINE(1),
	OFFLINE(2), 
	AFK(3), 
	DND(4);
	
	private int nr;

	private EOnlineState(int nr) {
		this.nr = nr;
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
}
