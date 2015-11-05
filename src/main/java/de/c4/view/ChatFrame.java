package main.java.de.c4.view;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import main.java.de.c4.controller.FileTransferManager;
import main.java.de.c4.controller.Messenger;
import main.java.de.c4.controller.shared.ChatMessage;
import main.java.de.c4.controller.shared.ConnectionManager;
import main.java.de.c4.controller.shared.listener.FileTransferInfoListener;
import main.java.de.c4.controller.shared.listener.MessageRecievedListener;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.file.FileTransferAnswer;
import main.java.de.c4.model.messages.file.FileTransferRequest;
import main.java.de.c4.view.components.ChatPanel;
import main.java.de.c4.view.components.ChatTabPane;
import main.java.de.c4.view.resources.EIcons;
import main.java.de.c4.view.resources.IconProvider;

import com.esotericsoftware.minlog.Log;


public class ChatFrame extends JFrame implements ActionListener, MessageRecievedListener, FileTransferInfoListener{
	
	private static final long serialVersionUID = 1L;
	
	private ChatTabPane tabbedPane = new ChatTabPane(this);
														
	private final static int SHAKE_LENGTH = 20;
	private final static int SHAKE_VELOCITY = 5;

	
	public ChatFrame() {
		setIconImage(IconProvider.getImage(EIcons.BUBBLES));
		Messenger.addMessageReceivedListener(this);
		FileTransferManager.INSTANCE.addFileTransferListener(this);
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
				infoMessage(panel, "Eingehende Dateiübertragung von "
						+contact.name+" (\""+request.filenName+"\", "+request.fileSize+
						" bytes)");
				int i = JOptionPane.showConfirmDialog(null, "Angebot zur Dateiübertragung von "
						+contact.name+" (\""+request.filenName+"\", "+request.fileSize+
						" bytes) annehmen?", "Datei übertragen?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				boolean accepted = i == JOptionPane.YES_OPTION;
				int port = 0;
				if (accepted) {
					port = FileTransferManager.INSTANCE.startFileServer(request, contact);
					if (port == 0) {
						JOptionPane.showMessageDialog(null, "Dateiübertragung von "
						+contact.name+" (\""+request.filenName+"\", "+request.fileSize+
						" bytes) Fehlgeschlagen", "Dateiübertragung fehlgeschlagen! (Kein freier Port?)", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					infoMessage(panel, "Dateiübertragung von "
							+contact.name+" (\""+request.filenName+"\", "+request.fileSize+
							" bytes) abgelehnt!");
				}
				FileTransferAnswer answer = new FileTransferAnswer();
				answer.accepted = accepted;
				answer.id = request.id;
				answer.serverPort = port;
				try {
					ConnectionManager.getConnectionsTo(contact, true).iterator().next().sendTCP(answer);
					infoMessage(panel, "Dateiübertragung von "
							+contact.name+" (\""+request.filenName+"\", "+request.fileSize+
							" bytes) gestartet!");
				} catch (Exception e) {
					Log.debug("Could not establish connection");
					infoMessage(panel, "Dateiübertragung von "
							+contact.name+" (\""+request.filenName+"\", "+request.fileSize+
							" bytes) fehlgeschlagen!<br/>Fehler:"+e.getMessage());
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


	@Override
	public void started(File f, ContactDto c) {
		//TODO: implement
		Log.debug("Started transfer of File "+f.getName()+" (Contact: "+c.name+")");
	}


	@Override
	public void abroted(File f, ContactDto c) {
		//TODO: implement
		Log.debug("transfer of File "+f.getName()+" (Contact: "+c.name+") abroted!");
	}


	@Override
	public void finnished(File f, ContactDto c) {
		//TODO: implement
		Log.debug("transfer of File "+f.getName()+" (Contact: "+c.name+") finnished!!");
	}


	@Override
	public void declined(ContactDto contact, File file) {
		//TODO: implement
		Log.debug("transfer of File "+file.getName()+" (Contact: "+contact.name+") declined!");
	}

}
