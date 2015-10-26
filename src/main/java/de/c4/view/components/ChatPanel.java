package main.java.de.c4.view.components;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import main.java.de.c4.controller.Messenger;
import main.java.de.c4.controller.TimestampUtil;
import main.java.de.c4.controller.shared.ContactList;
import main.java.de.c4.controller.shared.Network.ChatMessage;
import main.java.de.c4.controller.shared.listener.MessageRecievedListener;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.view.resources.EIcons;
import main.java.de.c4.view.resources.IconProvider;


public class ChatPanel extends JSplitPane implements DropTargetListener, MessageRecievedListener {

	private static final long serialVersionUID = 1L;
	
	private Set<ContactDto> contacts = new HashSet<ContactDto>();
	
	private JEditorPane messageBox = new JEditorPane();
	private JScrollPane messageScrollPane;
	
	private long chatID;
	private StringBuffer sb = new StringBuffer();
	private JTextArea inputArea = new JTextArea();

	public ChatPanel(ContactDto contact) {
		this(contact, System.currentTimeMillis());
	}
	
	public ChatPanel(ContactDto contact, long chatID) {
		super(JSplitPane.HORIZONTAL_SPLIT);
		this.setChatID(chatID);
		
		inputArea.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent arg0) {
				
			}
			public void keyReleased(KeyEvent arg0) {}
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode()==KeyEvent.VK_ENTER) {
					arg0.consume();
					String input = inputArea.getText().trim();
					if (input.isEmpty()) {
						return;
					}
					if (arg0.isControlDown()) {
						inputArea.append("\n");
						inputArea.setCaretPosition(inputArea.getText().length()-1);
					} else {
						sendMessage(input);
						inputArea.setText("");
					}
				}
			}
		});
		
		getContacts().add(contact);
		messageBox.setEditable(false);

		HTMLEditorKit kit = new HTMLEditorKit();
		messageBox.setEditorKit(kit);
		messageScrollPane = new JScrollPane(messageBox);
		
		StyleSheet styleSheet = kit.getStyleSheet();
		styleSheet.addRule("div {padding: 3px; margin-bottom: 3px; border: 2px solid;}");
		styleSheet.addRule(".oMessage {background-color : #3399FF; text-align: left; border-color: #1F5C99; margin-right: 20px;}");
		styleSheet.addRule(".nMessage {background-color : #E6E6E6; text-align: center; border-color: #666666;}");
		styleSheet.addRule(".myMessage {background-color : #99FF99; text-align: left; border-color: #4C804C; margin-left: 20px;}");
		styleSheet.addRule(".from {text-decoration: underline; padding: 0; margin-bottom: 0; text-align: left; border: none; font-size: 11pt; color: #666666;}");
		styleSheet.addRule(".emote {margin-bottom: -3px;}");
		
		Document doc = kit.createDefaultDocument();
		messageBox.setDocument(doc);
		infoMessage("Chat mit "+contact.name+" gestartet");
//		String imgsrc = 
//	            IconProvider.getImageAsURL(EIcons.SMILEY_SMILE).toString();
//		
//		messageBox.setText("<div class=\"oMessage\">hallo</div>"
//				+ "<div class=\"myMessage\"><a href=\"file:///home/artur\">Ordner</a></div>"
//				+ "<div class=\"myMessage\"><a href=\"file:///home/artur/arbeit/spielwiese/FileTo/src/ChatPanel.java\">Datei</a></div>"
//				+ "<div class=\"myMessage\"><a href=\"http://google.de\">Link</a></div>"
//				+ "<div class=\"nMessage\">Datei bla uebertragen</div>"
//				+ "<div class=\"oMessage\">du bist doof! <img class=\"emote\" width=25 height=25 src='"+imgsrc+"'></img></div>"
//				+ "<div class=\"myMessage\">danke! das werde ich mir bei gelegenheit mal ansehen, du blöder horst du!</div>");
		
		
		messageBox.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent hle) {
                if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                    String url = hle.getURL().toString();
					System.out.println(url);
					Desktop desktop = Desktop.getDesktop();
                    try {
                    	if (url.startsWith("file:")) {
                    		File f = new File(hle.getURL().toURI().getPath());
                    		desktop.open(f);
                    	}else desktop.browse(hle.getURL().toURI());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
		
		JSplitPane innerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		innerSplit.setLeftComponent(messageScrollPane);
		
		JPanel inputPanel = new JPanel(new BorderLayout());
		
		JPanel buttons = new JPanel(new BorderLayout());
		
		
		FlowLayout l = new FlowLayout();
		l.setHgap(1);
		l.setVgap(1);
		l.setAlignment(FlowLayout.LEFT);
		JPanel left = new JPanel(l); 
		JButton smileyBtn = new JButton(new ImageIcon(IconProvider.getImage(EIcons.SMILEY_SMILE).getScaledInstance(20, 20, 0)));
		Insets zeroInsets = new Insets(0, 0, 0, 0);
		smileyBtn.setMargin(zeroInsets);
		left.add(smileyBtn);
		left.add(new JButton("Fi"));
		left.add(new JButton("AG"));

		
		buttons.add(left, BorderLayout.WEST);
		
		JPanel right = new JPanel(l); 
		right.add(new JButton("!"));
		right.add(new JButton("senden"));

		buttons.add(right, BorderLayout.EAST);
//		
//		JPanel p = new JPanel();
//		p.add();
//		buttons.add(p, BorderLayout.CENTER);
//		buttons.add(new JButton("Senden"), BorderLayout.EAST);
		inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
		inputPanel.add(buttons, BorderLayout.PAGE_START);
		innerSplit.setRightComponent(inputPanel);
		inputArea.setDropTarget(new DropTarget(inputArea, this));
		
		innerSplit.setResizeWeight(0.8);
		innerSplit.setDividerLocation(0.8);
		setLeftComponent(innerSplit);
	}
	
	
	public String getTitle() {
		String buf = "";
		if (getContacts().size()>1) {
			buf+="[G] ";
		}
		buf += getContacts().iterator().next().name;
		return buf;
	}


	 protected void processDrag(DropTargetDragEvent dtde) {
         if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
             dtde.acceptDrag(DnDConstants.ACTION_COPY);
         } else {
             dtde.rejectDrag();
         }
     }

     public void dragEnter(DropTargetDragEvent dtde) {
         processDrag(dtde);
     }

     public void dragOver(DropTargetDragEvent dtde) {
         processDrag(dtde);
     }

     public void dropActionChanged(DropTargetDragEvent dtde) {
     }

     public void dragExit(DropTargetEvent dte) {
     }

     public void drop(DropTargetDropEvent dtde) {


         Transferable transferable = dtde.getTransferable();
         if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
             dtde.acceptDrop(dtde.getDropAction());
             try {

                 @SuppressWarnings("unchecked")
				List<File> transferData = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                 if (transferData != null && transferData.size() > 0) {
//                     importFiles(transferData);
                	 for (Object object : transferData) {
						System.out.println(object);
					}
                     dtde.dropComplete(true);
                 }

             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         } else {
             dtde.rejectDrop();
         }
     }


	public long getChatID() {
		return chatID;
	}


	public Set<ContactDto> getContacts() {
		return contacts;
	}

	public void receiveMessage(ChatMessage m, ContactDto contact){
		sb.append("<div class=\"oMessage\">");
		sb.append("<div class=\"from\"><span>");
		sb.append(contact.name);
		sb.append(" (");
		sb.append(TimestampUtil.getCurrentTimestamp());
		sb.append("):</span></div>");
		sb.append(m);
		sb.append("</div>");
		messageBox.setText(sb.toString());
	}
	
	public void sendMessage(String m){
		sb.append("<div class=\"myMessage\">");
		sb.append("<div class=\"from\"><span>");
		sb.append(ContactList.getMe().name);
		sb.append(" (");
		sb.append(TimestampUtil.getCurrentTimestamp());
		sb.append("):</span></div>");
		sb.append(m);
		sb.append("</div>");
		messageBox.setText(sb.toString());
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.text = m;
		chatMessage.id = getChatID();
		Messenger.sendMessageTo(chatMessage, contacts);
	}
	
	public void infoMessage(String m){
		sb.append("<div class=\"nMessage\">");
		sb.append(m);
		sb.append("</div>");
		messageBox.setText(sb.toString());
	}


	public void messageRecieved(ContactDto contact, ChatMessage message) {
		if (message.id == getChatID() && contacts.contains(contact)) {
			receiveMessage(message, contact);
		}
	}


	public void setChatID(long chatID) {
		this.chatID = chatID;
	}
}
