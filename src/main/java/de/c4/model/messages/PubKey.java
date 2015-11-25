package main.java.de.c4.model.messages;

import java.io.Serializable;

public class PubKey implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public byte[] key;
	
	public PubKey() {
		// nix
	}
	
	public PubKey(byte[] key) {
		this.key = key;
	}
	
	
}
