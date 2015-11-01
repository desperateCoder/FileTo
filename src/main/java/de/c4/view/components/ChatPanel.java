package main.java.de.c4.view.components;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import main.java.de.c4.controller.FileTransferManager;
import main.java.de.c4.controller.Messenger;
import main.java.de.c4.controller.TimestampUtil;
import main.java.de.c4.controller.shared.ChatMessage;
import main.java.de.c4.controller.shared.ContactList;
import main.java.de.c4.controller.shared.listener.FileTransferInfoListener;
import main.java.de.c4.controller.shared.listener.MessageRecievedListener;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.file.FileTransferRequest;
import main.java.de.c4.view.listener.SmartScroller;
import main.java.de.c4.view.listener.SmileySelectionListener;
import main.java.de.c4.view.resources.EIcons;
import main.java.de.c4.view.resources.ESmileys;
import main.java.de.c4.view.resources.IconProvider;

public class ChatPanel extends JSplitPane implements DropTargetListener, MessageRecievedListener, ActionListener,
		FileTransferInfoListener, SmileySelectionListener {

	private static final int SCROLLDOWN_BTN_MARGIN = 5;
	private static final Insets ZERO_INSETS = new Insets(0, 0, 0, 0);

	private static final long serialVersionUID = 1L;

	private Set<ContactDto> contacts = new HashSet<ContactDto>();

	private JEditorPane messageBox = new JEditorPane();
	private JScrollPane messageScrollPane;
	private JLayeredPane layeredPane = new JLayeredPane();
	private JButton scrollDownBtn;
	private JButton smileyBtn;

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

			public void keyReleased(KeyEvent arg0) {
			}

			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					arg0.consume();
					String input = inputArea.getText().trim();
					if (input.isEmpty()) {
						return;
					}
					if (arg0.isShiftDown()) {
						inputArea.append("\n");
						inputArea.setCaretPosition(inputArea.getText().length() - 1);
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
		new SmartScroller(messageScrollPane);
		messageScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}
				if (isScrolledDown()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							updateScrollDownButtonShown();
							messageBox.repaint();
						}
					});
				}
			}
		});

		layeredPane.add(messageScrollPane, Integer.valueOf(0));
		scrollDownBtn = new JButton(IconProvider.getAsScaledIcon(EIcons.ARROW_DOWN, 25, 25));
		removeSpacing(scrollDownBtn);
		scrollDownBtn.addActionListener(this);
		scrollDownBtn.setActionCommand(EButtonActions.SCROLL_DOWN.getActionCommand());

		layeredPane.addComponentListener(new ComponentListener() {

			public void componentResized(ComponentEvent arg0) {
				Rectangle b = layeredPane.getBounds();
				messageScrollPane.setBounds(b);
				setScrollDownBtnPosition();
			}

			public void componentShown(ComponentEvent arg0) {
			}

			public void componentMoved(ComponentEvent arg0) {
			}

			public void componentHidden(ComponentEvent arg0) {
			}
		});

		StyleSheet styleSheet = kit.getStyleSheet();
		styleSheet.addRule("div {padding: 10px; margin-bottom: 3px;}");
		styleSheet.addRule(
				".nMessage, .oMessage, .myMessage {text-align: left; border: 1px solid #000; border-left: 3px solid #000;}");
		styleSheet.addRule(".oMessage {background-color : #C6FFC6; border-color: #AADDAA; margin-right: 20px;}");
		styleSheet.addRule(".myMessage {background-color : #C2EBFF; border-color: #AACCFF; margin-left: 20px;}");
		styleSheet.addRule(".nMessage {background-color : #EEEEEE; border-color: #CCCCCC; color: #666;}");
		styleSheet.addRule(
				".from {padding: 0; margin-bottom: 0; text-align: left; border: none; font-size: 11pt; color: #666666;}");
		styleSheet.addRule(".emote {margin-bottom: -3px;}");

		Document doc = kit.createDefaultDocument();
		messageBox.setDocument(doc);
		// String imgsrc =
		// IconProvider.getImageAsURL(EIcons.SMILEY_SMILE).toString();
		//
		// messageBox.setText("<div class=\"oMessage\">hallo</div>"
		// +
		// "<div class=\"myMessage\"><a
		// href=\"file:///home/artur\">Ordner</a></div>"
		// +
		// "<div class=\"myMessage\"><a
		// href=\"file:///home/artur/arbeit/spielwiese/FileTo/src/ChatPanel.java\">Datei</a></div>"
		// +
		// "<div class=\"myMessage\"><a
		// href=\"http://google.de\">Link</a></div>"
		// + "<div class=\"nMessage\">Datei bla uebertragen</div>"
		// +
		// "<div class=\"oMessage\">du bist doof! <img class=\"emote\" width=25
		// height=25 src='"+imgsrc+"'></img></div>"
		// +
		// "<div class=\"myMessage\">danke! das werde ich mir bei gelegenheit
		// mal ansehen, du blöder horst du!</div>");

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
						} else
							desktop.browse(hle.getURL().toURI());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});

		JSplitPane innerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		innerSplit.setLeftComponent(layeredPane);

		JPanel inputPanel = new JPanel(new BorderLayout());

		JPanel buttons = new JPanel(new BorderLayout());

		FlowLayout l = new FlowLayout();
		l.setHgap(1);
		l.setVgap(1);
		l.setAlignment(FlowLayout.LEFT);
		JPanel left = new JPanel(l);
		final int iconSize = 20;
		final Dimension buttonSize = new Dimension(30, 30);
		smileyBtn = new JButton(IconProvider.getAsScaledIcon(ESmileys.SMILEYS_28, iconSize, iconSize));
		removeSpacing(smileyBtn);
		smileyBtn.addActionListener(this);
		smileyBtn.setActionCommand(EButtonActions.SHOW_SMILEYS.getActionCommand());
		smileyBtn.setPreferredSize(buttonSize);
		smileyBtn.setToolTipText("Smileys...");
		left.add(smileyBtn);
		JButton attachBtn = new JButton(IconProvider.getAsScaledIcon(EIcons.ATTACH, iconSize, iconSize));
		removeSpacing(attachBtn);
		attachBtn.addActionListener(this);
		attachBtn.setActionCommand(EButtonActions.ATTACH_FILE.getActionCommand());
		attachBtn.setToolTipText("Datei senden...");
		attachBtn.setPreferredSize(buttonSize);
		left.add(attachBtn);
		JButton addContactBtn = new JButton(IconProvider.getAsScaledIcon(EIcons.ADD, iconSize, iconSize));
		removeSpacing(addContactBtn);
		addContactBtn.addActionListener(this);
		addContactBtn.setActionCommand(EButtonActions.ADD_TO_GROUP.getActionCommand());
		addContactBtn.setToolTipText("Kontakt zur Konversation hinzufügen...");
		addContactBtn.setPreferredSize(buttonSize);
		left.add(addContactBtn);

		buttons.add(left, BorderLayout.WEST);

		JPanel right = new JPanel(l);
		JButton alarmBtn = new JButton(IconProvider.getAsScaledIcon(EIcons.ALARM, iconSize, iconSize));
		removeSpacing(alarmBtn);
		alarmBtn.addActionListener(this);
		alarmBtn.setActionCommand(EButtonActions.ALARM.getActionCommand());
		alarmBtn.setToolTipText("Aufmerksamkeit holen!");
		alarmBtn.setPreferredSize(buttonSize);
		right.add(alarmBtn);
		JButton sendBtn = new JButton("senden", IconProvider.getAsScaledIcon(EIcons.SEND, iconSize, iconSize));
		sendBtn.addActionListener(this);
		sendBtn.setActionCommand(EButtonActions.SEND.getActionCommand());
		sendBtn.setToolTipText("Nachricht senden");
		removeSpacing(sendBtn);
		Dimension sendDimension = new Dimension(85, buttonSize.height);
		sendBtn.setPreferredSize(sendDimension);
		right.add(sendBtn);

		buttons.add(right, BorderLayout.EAST);
		//
		// JPanel p = new JPanel();
		// p.add();
		// buttons.add(p, BorderLayout.CENTER);
		// buttons.add(new JButton("Senden"), BorderLayout.EAST);
		inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
		inputPanel.add(buttons, BorderLayout.PAGE_START);
		innerSplit.setRightComponent(inputPanel);
		inputArea.setDropTarget(new DropTarget(inputArea, this));

		innerSplit.setResizeWeight(0.8);
		innerSplit.setDividerLocation(0.8);
		setLeftComponent(innerSplit);

		infoMessage("Chat mit " + contact.name + " gestartet");
	}

	private void removeSpacing(final JButton btn) {
		btn.setBorder(null);
		btn.setBorderPainted(false);
		btn.setMargin(ZERO_INSETS);
	}

	public String getTitle() {
		String buf = "";
		if (getContacts().size() > 1) {
			buf += "[G] ";
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
					// importFiles(transferData);
					for (Object object : transferData) {
						File f = (File) object;
						if (f.isDirectory()) {
							infoMessage("Senden von Ordnern nicht möglich, bitte vorher ZIPen!<br/>(\""
									+ f.getAbsolutePath() + "\")");
							continue;
						}
						for (ContactDto c : contacts) {
							FileTransferManager.INSTANCE.sendFileTo(f, c, this);
							infoMessage("Sendeanfrage für Datei \"" + f.getName() + "\" an " + c.name + " gesendet!");
						}
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

	public void receiveMessage(final ChatMessage m, final ContactDto contact) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				sb.append("<div class=\"oMessage\">");
				sb.append("<div class=\"from\"><span>");
				sb.append(contact.name);
				sb.append(" (");
				sb.append(TimestampUtil.getCurrentTimestamp());
				sb.append("):</span></div>");
				sb.append(textToHtml(m.text));
				sb.append("</div>");
				messageBox.setText(sb.toString());
				updateScrollDownButtonShown();
			}
		});
	}

	public void sendMessage(String m) {
		sb.append("<div class=\"myMessage\">");
		sb.append("<div class=\"from\"><span>");
		sb.append(ContactList.getMe().name);
		sb.append(" (");
		sb.append(TimestampUtil.getCurrentTimestamp());
		sb.append("):</span></div>");
		sb.append(textToHtml(m));
		sb.append("</div>");
		messageBox.setText(sb.toString());
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (!isScrolledDown()) {
					scrollDown();
				}
			}
		});
		// updateScrollDownButtonShown();
		ChatMessage chatMessage = new ChatMessage();
		chatMessage.text = m;
		chatMessage.id = getChatID();
		Messenger.sendMessageTo(chatMessage, contacts);
	}

	public void infoMessage(final String m) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				sb.append("<div class=\"nMessage\">");
				sb.append(m);
				sb.append("</div>");
				messageBox.setText(sb.toString());
				if (!isScrolledDown()) {
					updateScrollDownButtonShown();
				}
			}
		});
	}

	private String textToHtml(String s) {
		// line breaks
				String html = s.replaceAll("\n", "<br/>");
				// smileys
				Pattern smileyPattern = Pattern.compile(":[0-9]{1,3}:");
			    Matcher smileyMatcher = smileyPattern.matcher(html);
				while (smileyMatcher.find()) {
					String finding = smileyMatcher.group();
					finding = finding.substring(1,  finding.length()-1);
					StringBuffer replacement = new StringBuffer("<img src=\"");
					URL imageAsURL = IconProvider.getImageAsURL(ESmileys.getByNr(Integer.parseInt(finding)));
					replacement.append(imageAsURL);
					replacement.append("\" />");
					smileyMatcher.replaceFirst(replacement.toString());
				}
				return html;
	}

	public void messageRecieved(ContactDto contact, ChatMessage message) {
		if (message.id == getChatID() && contacts.contains(contact)) {
			receiveMessage(message, contact);
		}
	}

	public void setChatID(long chatID) {
		this.chatID = chatID;
	}

	private void updateScrollDownButtonShown() {
		if (isScrolledDown()) {
			if (layeredPane.isAncestorOf(scrollDownBtn)) {
				layeredPane.remove(scrollDownBtn);
			}
		} else if (!layeredPane.isAncestorOf(scrollDownBtn)) {
			layeredPane.add(scrollDownBtn, Integer.valueOf(5));
			setScrollDownBtnPosition();
		}
	}

	private boolean isScrolledDown() {
		Adjustable sb = messageScrollPane.getVerticalScrollBar();
		int val = sb.getValue();
		int visibleAmount = sb.getVisibleAmount();
		int lowest = val + visibleAmount;
		int maxVal = sb.getMaximum();
		boolean atBottom = maxVal == lowest || (visibleAmount == lowest && visibleAmount > maxVal)
				|| messageBox.getHeight() < visibleAmount;
		return atBottom;
	}

	private void setScrollDownBtnPosition() {
		Rectangle b = layeredPane.getBounds();
		int x = (int) b.getWidth() - scrollDownBtn.getWidth() - messageScrollPane.getVerticalScrollBar().getWidth()
				- SCROLLDOWN_BTN_MARGIN;
		int y = (int) b.getHeight() - scrollDownBtn.getHeight() - SCROLLDOWN_BTN_MARGIN;
		scrollDownBtn.setBounds(x, y, 32, 32);
	}

	public void actionPerformed(ActionEvent e) {
		if (EButtonActions.SCROLL_DOWN.getActionCommand().equals(e.getActionCommand())) {
			scrollDown();
		} else if (EButtonActions.SCROLL_DOWN.getActionCommand().equals(e.getActionCommand())) {
			scrollDown();
		} else if (EButtonActions.ADD_TO_GROUP.getActionCommand().equals(e.getActionCommand())) {
			// TODO implement
		} else if (EButtonActions.ALARM.getActionCommand().equals(e.getActionCommand())) {
			// TODO implement
		} else if (EButtonActions.ATTACH_FILE.getActionCommand().equals(e.getActionCommand())) {
			// TODO implement
		} else if (EButtonActions.SEND.getActionCommand().equals(e.getActionCommand())) {
			String input = inputArea.getText().trim();
			if (input.isEmpty()) {
				return;
			}
			sendMessage(input);
			inputArea.setText("");
		} else if (EButtonActions.SHOW_SMILEYS.getActionCommand().equals(e.getActionCommand())) {
			Point locationOnScreen = smileyBtn.getLocationOnScreen();
			locationOnScreen.y += smileyBtn.getHeight();
			SmileyDialog.showUp(this, locationOnScreen);
		}
	}

	private void scrollDown() {
		JScrollBar sb = messageScrollPane.getVerticalScrollBar();
		sb.setValue(sb.getMaximum());
		layeredPane.remove(scrollDownBtn);
	}

	private enum EButtonActions {
		SCROLL_DOWN("sd"), ALARM("am"), ADD_TO_GROUP("atg"), SEND("snd"), ATTACH_FILE("af"), SHOW_SMILEYS("ssm");

		private String actionCommand;

		private EButtonActions(String actionCommand) {
			this.actionCommand = actionCommand;
		}

		public String getActionCommand() {
			return actionCommand;
		}

	}

	public void started(File f, ContactDto c) {
		infoMessage("Beginne mit der Dateiübertragung (\"" + f.getName() + "\" an " + c.name + ")...");
	}

	public void abroted(File f, ContactDto c) {
		infoMessage("Senden der Datei \"" + f.getName() + "\" an " + c.name + " Fehlgeschlagen!");
	}

	public void finnished(File f, ContactDto c) {
		infoMessage("Datei \"" + f.getName() + "\" wurde erfolgreich an " + c.name + " gesendet!");
	}

	public void declined(ContactDto contact, File file) {
		infoMessage("Sendeanfrage fÜr Datei \"" + file.getName() + "\" wurde von " + contact.name + " abgelehnt!");
	}

	public void fileTransferRequestRecieved(ContactDto contact, FileTransferRequest request) {
	}

	public void smileySelected(ESmileys s) {
		inputArea.insert(":" + s.getNr() + ":", inputArea.getCaretPosition());
	}
}
