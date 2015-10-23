package main.java.de.c4.view;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
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

import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.view.resources.EIcons;
import main.java.de.c4.view.resources.IconProvider;


public class ChatPanel extends JSplitPane implements DropTargetListener {

	private static final long serialVersionUID = 1L;
	
	private Set<ContactDto> contacts = new HashSet<ContactDto>();
	
	private JEditorPane messageBox = new JEditorPane();
	JScrollPane messageScrollPane;
	
	private JTextArea inputArea = new JTextArea();

	public ChatPanel(ContactDto contact) {
		super(JSplitPane.HORIZONTAL_SPLIT);
		contacts.add(contact);
		messageBox.setEditable(false);

		HTMLEditorKit kit = new HTMLEditorKit();
		messageBox.setEditorKit(kit);
		messageScrollPane = new JScrollPane(messageBox);
		
		StyleSheet styleSheet = kit.getStyleSheet();
		styleSheet.addRule("div {padding: 3px; margin-bottom: 3px; border: 2px solid;}");
		styleSheet.addRule(".oMessage {background-color : #3399FF; text-align: left; border-color: #1F5C99; margin-right: 20px;}");
		styleSheet.addRule(".nMessage {background-color : #E6E6E6; text-align: center; border-color: #666666;}");
		styleSheet.addRule(".myMessage {background-color : #99FF99; text-align: left; border-color: #4C804C; margin-left: 20px;}");
		styleSheet.addRule(".emote {margin-bottom: -3px;}");
		
		Document doc = kit.createDefaultDocument();
		messageBox.setDocument(doc);
		String imgsrc = 
	            getClass().getResource("resources/smileys/1.png").toString();
		messageBox.setText("<div class=\"oMessage\">hallo</div>"
				+ "<div class=\"myMessage\"><a href=\"file:///home/artur\">Ordner</a></div>"
				+ "<div class=\"myMessage\"><a href=\"file:///home/artur/arbeit/spielwiese/FileTo/src/ChatPanel.java\">Datei</a></div>"
				+ "<div class=\"nMessage\">Datei bla uebertragen</div>"
				+ "<div class=\"oMessage\">du bist doof! <img class=\"emote\" width=25 height=25 src='"+imgsrc+"'></img></div>"
				+ "<div class=\"myMessage\">danke! das werde ich mir bei gelegenheit mal ansehen, du blöder horst du!</div>");
		
		
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
		
		GridBagConstraints c = new GridBagConstraints();
		
		JPanel buttons = new JPanel(new GridBagLayout());
		
		c.gridx = c.gridy = 0;
		c.anchor = GridBagConstraints.WEST; 
		
		FlowLayout l = new FlowLayout();
		l.setHgap(1);
		l.setVgap(1);
		l.setAlignment(FlowLayout.LEFT);
		JPanel left = new JPanel(l); 
		JButton smileyBtn = new JButton(new ImageIcon(IconProvider.getImage(EIcons.APP_ICON).getScaledInstance(20, 20, 0)));
		Insets zeroInsets = new Insets(0, 0, 0, 0);
		smileyBtn.setMargin(zeroInsets);
		left.add(smileyBtn);
		left.add(new JButton("Fi"));
		left.add(new JButton("AG"));

		
		c.weightx = 1D;
		buttons.add(left, c);
		
		c.gridx++;
		c.anchor = GridBagConstraints.CENTER;
		buttons.add(new JButton("!"), c);

		c.gridx++;
		c.anchor = GridBagConstraints.EAST;
		
		buttons.add(new JButton("senden"), c);
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
		if (contacts.size()>1) {
			buf+="[G] ";
		}
		buf += contacts.iterator().next().name;
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
	

}
