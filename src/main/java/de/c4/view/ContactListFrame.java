package main.java.de.c4.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import main.java.de.c4.controller.Messenger;
import main.java.de.c4.model.messages.ContactDto;
import main.java.de.c4.model.messages.EOnlineState;
import main.java.de.c4.view.resources.IconProvider;

public class ContactListFrame extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JList<ContactDto> contactList = new JList<ContactDto>();

	public ContactListFrame() {
		
		JPanel content = new JPanel(new BorderLayout());
		
		JPanel labelPanel = new JPanel(new BorderLayout());
		labelPanel.add(new JLabel("Kontakte:"), BorderLayout.CENTER);
		content.add(labelPanel, BorderLayout.NORTH);
		
		content.add(new JScrollPane(contactList), BorderLayout.CENTER);
		
		JComboBox<EOnlineState> stateCombo = new JComboBox<EOnlineState>(EOnlineState.values());
		stateCombo.setRenderer(new ListCellRenderer<EOnlineState>() {
			private static final int SIZE = 26;
			private final Font FONT = new Font("Helvetica", Font.BOLD, 16);
			public Component getListCellRendererComponent(JList<? extends EOnlineState> list, EOnlineState value,
					int index, boolean isSelected, boolean cellHasFocus) {
				JPanel comp = new JPanel(new BorderLayout());
				JLabel l = new JLabel(value.toString());
				l.setFont(FONT);
				comp.add(l, BorderLayout.CENTER);
				
				JLabel image = new JLabel(new ImageIcon(IconProvider.getImage(value.getIcon()).getScaledInstance(SIZE, SIZE, 0)), 0);
				comp.add(image, BorderLayout.WEST);
				return comp;
			}
		});
		
		
		content.add(stateCombo, BorderLayout.SOUTH);
		
		setContentPane(content);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Kontaktliste");
		pack();
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		
	}

	public static void main(String[] args) {
//		new JLabel(new ImageIcon(IconProvider.getImage(EOnlineState.OFFLINE.getIcon())), 0);
		System.setProperty("java.net.preferIPv4Stack" , "true");
		new Thread(new Runnable() {
			
			public void run() {
				Messenger.init();
			}
		}).start();
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		    // Nimbus is not available. Take default-LaF. Just do nothing.
		}
		new ContactListFrame();
	}
}
