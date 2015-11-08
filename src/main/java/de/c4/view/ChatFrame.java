package main.java.de.c4.view;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.esotericsoftware.minlog.Log;

import main.java.de.c4.controller.FileTransferManager;
import main.java.de.c4.controller.Messenger;
import main.java.de.c4.controller.shared.ChatMessage;
import main.java.de.c4.controller.shared.ConnectionManager;
import main.java.de.c4.controller.shared.listener.MessageRecievedListener;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.file.FileTransferAnswer;
import main.java.de.c4.model.messages.file.FileTransferRequest;
import main.java.de.c4.view.components.ChatPanel;
import main.java.de.c4.view.components.ChatTabPane;
import main.java.de.c4.view.resources.EIcons;
import main.java.de.c4.view.resources.IconProvider;


public class ChatFrame extends JFrame implements ActionListener, MessageRecievedListener{
	
	private static final long serialVersionUID = 1L;
	
	private ChatTabPane tabbedPane = new ChatTabPane(this);
														
	private final static int SHAKE_LENGTH = 20;
	private final static int SHAKE_VELOCITY = 5;

	
	public ChatFrame() {
		setIconImage(IconProvider.getImage(EIcons.BUBBLES));
		Messenger.addMessageReceivedListener(this);
//		List<ContactDto> contacts = ContactList.INSTANCE.getContacts();
//		
//		for (ContactDto contactDto : contacts) {
//			tabbedPane.addTab(new ChatPanel(contactDto));
//		}
		
		
		setContentPane(tabbedPane);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("Chat");
//		setVisible(true);
	}
	
	
	public void actionPerformed(ActionEvent e) {
	}
	
	
	public static void main(String[] args) {
		Messenger.init();
		Log.set(Log.LEVEL_DEBUG);
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		    Log.info("Nimbus LaF not found, proceeding with the default one!");
		}
		ChatFrame chatFrame = new ChatFrame();
		chatFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		chatFrame.setVisible(true);
	}


	public void allTabsClosed() {
		this.setVisible(false);
	}


	public void showContactTab(ContactDto c) {
		
		int indexOf = tabbedPane.indexOf(c);
		if (indexOf>=0) {
			tabbedPane.setSelectedIndex(indexOf);
		} else tabbedPane.addTab(new ChatPanel(c));
		
		if (getSize().getHeight()<10) {
			setSize(400, 500);
		}
		if (!isVisible()) {
			bringToFront();
		}
	}


	private void bringToFront() {
		EventQueue.invokeLater(new Runnable() {
		    public void run() {
		    	setVisible(true);
		        toFront();
		        repaint();
		    }
		});
	}


	public void messageRecieved(ContactDto contact, ChatMessage message) {
		//TODO: Make window blink or something to get Attention
		tabbedPane.messageReceived(contact, message);
		
		if (!isVisible()) {
			if (getSize().getHeight()<10) {
				setSize(400, 500);
			}
			bringToFront();
		}
	}


	public void fileTransferRequestRecieved(final ContactDto contact,
			final FileTransferRequest request) {
		new Thread(new Runnable() {
			
			public void run() {
				int index = tabbedPane.indexOf(contact);
				final ChatPanel panel;
				if (index > -1) {
					panel =  (ChatPanel)(tabbedPane.getComponentAt(index));
				} else {
					panel = new ChatPanel(contact);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							tabbedPane.addTab(panel);
							if (!isVisible()) {
								if (getSize().getHeight()<10) {
									setSize(400, 500);
								}
								bringToFront();
							}
						}
					});
				}
				String size = FileTransferManager.toReadableByteCount(request.fileSize, true);
				infoMessage(panel, "Eingehende Dateiübertragung von "
						+contact.name+" (\""+request.filenName+"\", "+size+
						")");
				int i = JOptionPane.showConfirmDialog(null, "Angebot zur Dateiübertragung von "
						+contact.name+" (\""+request.filenName+"\", "+size+
						") annehmen?", "Datei übertragen?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				boolean accepted = i == JOptionPane.YES_OPTION;
				int port = 0;
				if (accepted) {
					port = FileTransferManager.INSTANCE.startFileServer(request, contact);
					FileTransferManager.INSTANCE.addFileTransferListener(request.id, panel);
					if (port == 0) {
						JOptionPane.showMessageDialog(null, "Dateiübertragung von "
						+contact.name+" (\""+request.filenName+"\", "+size+
						") Fehlgeschlagen", "Dateiübertragung fehlgeschlagen! (Kein freier Port?)", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					infoMessage(panel, "Dateiübertragung von "
							+contact.name+" (\""+request.filenName+"\", "+size+
							") abgelehnt!");
				}
				FileTransferAnswer answer = new FileTransferAnswer();
				answer.accepted = accepted;
				answer.id = request.id;
				answer.serverPort = port;
				try {
					ConnectionManager.getConnectionsTo(contact, true).iterator().next().sendTCP(answer);
					if (accepted) {
						infoMessage(panel, "Dateiübertragung von "
								+contact.name+" (\""+request.filenName+"\", "+size+
								") gestartet!");
					}
				} catch (Exception e) {
					Log.debug("Could not establish connection");
					infoMessage(panel, "Dateiübertragung von "
							+contact.name+" (\""+request.filenName+"\", "+size+
							") fehlgeschlagen!<br/>Fehler:"+e.getMessage());
				}
				
			}
		}).start();
	}
	
	private void infoMessage(final ChatPanel p, final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				p.infoMessage(message);
			}
		});
	}


	@Override
	public void alert(ContactDto contact) {
		shake();//TODO: Don't shake, if Online-State is DND
	}

	/**
	 * shakes around the frame
	 */
	private void shake() { // deren.exe ;)
		try {
			final int originalX = getLocationOnScreen().x;
			final int originalY = getLocationOnScreen().y;
			for (int i = 0; i < SHAKE_LENGTH; i++) {
				Thread.sleep(10);
				setLocation(originalX, originalY + SHAKE_VELOCITY);
				Thread.sleep(10);
				setLocation(originalX, originalY - SHAKE_VELOCITY);
				Thread.sleep(10);
				setLocation(originalX + SHAKE_VELOCITY, originalY);
				Thread.sleep(10);
				setLocation(originalX, originalY);
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
	}
}
