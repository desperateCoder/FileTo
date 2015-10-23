package main.java.de.c4.view.resources;

public enum EIcons {

	APP_ICON("1.png");
	
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
