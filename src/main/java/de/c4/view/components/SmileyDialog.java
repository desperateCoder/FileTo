package main.java.de.c4.view.components;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import main.java.de.c4.view.listener.SmileySelectionListener;
import main.java.de.c4.view.resources.ESmileyType;
import main.java.de.c4.view.resources.ESmileys;
import main.java.de.c4.view.resources.IconProvider;

public class SmileyDialog extends JDialog implements WindowFocusListener, ActionListener{

	private static final long serialVersionUID = 1L;

	
	private static final int SIZE = 18;
	private static final Dimension BTN_SIZE = new Dimension(20,20);
	private static final Insets ZERO_INSETS = new Insets(1, 1, 1, 1);
	private static final SmileyDialog INSTANCE = new SmileyDialog(true);
	
	private JTabbedPane tabs = new JTabbedPane();
	
	private SmileySelectionListener listener = null;
	
	public SmileyDialog() {
		this(false);
	}
	
	private SmileyDialog(boolean isInternalCall) {
		if (!isInternalCall) {
			throw new RuntimeException("Singleton should not be instanciated!");
		}
		addWindowFocusListener(this);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.CENTER;
		c.gridx = c.gridy = 0;
		c.insets = ZERO_INSETS;
		
		
		JPanel panel = new JPanel(new GridBagLayout());
		for (ESmileys s : ESmileys.values()) {
			if (s.getType() == ESmileyType.STUFF) {
				continue;
			}
			JButton b = new JButton(IconProvider.getAsScaledIcon(s, SIZE, SIZE));
			b.setActionCommand(""+s.getNr());
			removeSpacing(b);
			b.addActionListener(this);
			b.setPreferredSize(BTN_SIZE);
			panel.add(b, c);
			c.gridx ++;
			if (c.gridx%10 == 0) {
				c.gridx = 0;
				c.gridy ++;
			}
		}
		tabs.addTab("Smileys", panel);
		
		panel = new JPanel(new GridBagLayout());
		for (ESmileys s : ESmileys.values()) {
			if (s.getType() == ESmileyType.SMILEY) {
				continue;
			}
			JButton b = new JButton(IconProvider.getAsScaledIcon(s, SIZE, SIZE));
			b.setActionCommand(""+s.getNr());
			b.addActionListener(this);
			removeSpacing(b);
			
			b.setPreferredSize(BTN_SIZE);
			panel.add(b, c);
			c.gridx ++;
			if (c.gridx%10 == 0) {
				c.gridx = 0;
				c.gridy ++;
			}
		}
		tabs.addTab("Sonstige", panel);
		
		setUndecorated(true);
		setContentPane(tabs);
		pack();
	}

	private void removeSpacing(final JButton btn) {
		btn.setBorder(null);
		btn.setBorderPainted(false);
		btn.setMargin(ZERO_INSETS);
	}
	

	public void windowGainedFocus(WindowEvent e) {/* do nothing! */}

	public void windowLostFocus(WindowEvent e) {
		listener.smileySelectionAbroted();
		dispose();
	}
	
	public void popUp(SmileySelectionListener l, Point p){
		listener = l;
		setLocation(p);
		setVisible(true);
	}

	public static void main(String[] args) {
		new SmileyDialog(true).popUp(new SmileySelectionListener() {
			public void smileySelected(ESmileys s) {
				System.out.println(s.name());
			}

			@Override
			public void smileySelectionAbroted() {
				
			}
		}, new Point(0, 0));
	}

	public void actionPerformed(ActionEvent e) {
		listener.smileySelected(ESmileys.getByNr(Integer.parseInt(e.getActionCommand())));
		dispose();
	}
	
	public static void showUp(SmileySelectionListener l, Point p){
		INSTANCE.popUp(l, p);
	}
}
