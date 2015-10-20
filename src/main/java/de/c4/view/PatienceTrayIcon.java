package main.java.de.c4.view;

import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class PatienceTrayIcon extends TrayIcon {
	private final PopupMenu popup = new PopupMenu();
	private MenuItem openItem;
	private MenuItem quitItem;
	private MenuItem timeItem;
	private static ActionEvent evt = null;

	public PatienceTrayIcon(Image image) {
		super(image);
		if (evt == null) {
			evt = new ActionEvent(this, 1, "ope");
		}
		// setToolTipMin(Patience.INSTANCE.getRemTimeString());
		this.openItem = new MenuItem("Öffnen");
		this.quitItem = new MenuItem("Beenden");
		this.timeItem = new MenuItem("");
		this.timeItem.setEnabled(false);
		this.popup.add(this.openItem);
		this.popup.add(this.timeItem);
		this.popup.add(this.quitItem);
//		this.openItem.addActionListener(Patience.INSTANCE);
		this.openItem.setActionCommand("ope");
//		this.quitItem.addActionListener(Patience.INSTANCE);
		this.quitItem.setActionCommand("qui");

		addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1){
//					Patience.INSTANCE.actionPerformed(PatienceTrayIcon.evt);
				}
			}
		});
	}

	public PopupMenu getPopupMenu() {
		this.timeItem.setLabel(getToolTip());
		return this.popup;
	}

	public void setToolTipMin(String min) {
		super.setToolTip(min);
	}
}