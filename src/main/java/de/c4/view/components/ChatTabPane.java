package main.java.de.c4.view.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTabbedPane;

import main.java.de.c4.view.listener.TabCloseListener;


public class ChatTabPane extends JTabbedPane implements ActionListener, TabCloseListener{
	
	private static final long serialVersionUID = 1L;
	
	public ChatTabPane() {
		super(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
	}
	
	@Override
	public void addTab(String title, Component component) {
		super.addTab(title, component);
		setTabComponentAt(indexOfComponent(component), new ButtonTabComponent(this, this));
	}
	
	public void addTab(ChatPanel component) {
		addTab(null, component);
	}
	
	
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getTitleAt(int index) {
		return ((ChatPanel)getComponentAt(index)).getTitle();
	}
	
	
	public void tabClosed(int tabIndex) {
		remove(tabIndex);
	}

}
