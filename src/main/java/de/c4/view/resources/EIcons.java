package main.java.de.c4.view.resources;

public enum EIcons {

	APP_ICON("icon.png"),
	STATE_ONLINE("states/online.png"),
	STATE_AFK("states/afk.png"),
	STATE_DND("states/dnd.png"),
	STATE_OFFLINE("states/offline.png"),
	SMILEY_SMILE("smileys/1.png");
	
	private String path;
	
	private EIcons(String path) {
		this.setPath(path);
	}

	public String getPath() {
		return path;
	}

	private void setPath(String path) {
		this.path = path;
	}
}
