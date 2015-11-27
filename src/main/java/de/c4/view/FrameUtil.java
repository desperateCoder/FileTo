package main.java.de.c4.view;

import java.awt.EventQueue;

import javax.swing.JFrame;

public class FrameUtil {

	private final static int SHAKE_LENGTH = 20;
	private final static int SHAKE_VELOCITY = 5;

	/**
	 * shakes around the frame
	 */
	public static void shake(final JFrame f) { // deren.exe ;)
		EventQueue.invokeLater(new Runnable() {
		    public void run() {
		    	try {
					final int originalX = f.getLocationOnScreen().x;
					final int originalY = f.getLocationOnScreen().y;
					for (int i = 0; i < SHAKE_LENGTH; i++) {
						Thread.sleep(10);
						f.setLocation(originalX, originalY + SHAKE_VELOCITY);
						Thread.sleep(10);
						f.setLocation(originalX, originalY - SHAKE_VELOCITY);
						Thread.sleep(10);
						f.setLocation(originalX + SHAKE_VELOCITY, originalY);
						Thread.sleep(10);
						f.setLocation(originalX, originalY);
					}
				} catch (Exception err) {
					err.printStackTrace();
				}
		    }
		});
	}
	
	public static void bringToFront(final JFrame f) {
		EventQueue.invokeLater(new Runnable() {
		    public void run() {
		    	f.setVisible(true);
		    	f.toFront();
		    	f.repaint();
		    }
		});
	}
}
