package main.java.de.c4.controller.shared.listener;

import main.java.de.c4.model.messages.OnlineStateChange;

public interface OnlineStateChangeListener {

	public void onlineStateChanged(OnlineStateChange change);
}
