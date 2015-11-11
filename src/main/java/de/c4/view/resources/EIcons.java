package main.java.de.c4.view.resources;

public enum EIcons {

	APP_ICON("icon.png"),
	TRAY_ICON("tray.png"),
	SETTINGS("settings.png"),
	BUBBLES("conversation.png"),
	STATE_ONLINE("states/online.png"),
	STATE_AFK("states/afk.png"),
	STATE_DND("states/dnd.png"),
	STATE_OFFLINE("states/offline.png"),
	CONTACTS("contacts.png"),
	SEND("send.png"),
	ADD("add.png"),
	ALARM("alarm.png"),
	ATTACH("attach.png"),
	ARROW_DOWN("arrow_down.png"), 
	TRANSFER("transfer.png"), 
	UPLOAD("upload.png"),
	DOWNLOAD("download.png");
	
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
