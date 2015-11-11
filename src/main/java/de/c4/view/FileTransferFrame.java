package main.java.de.c4.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import main.java.de.c4.controller.shared.listener.FileTransferStateListener;
import main.java.de.c4.model.messages.file.FileTransferState;
import main.java.de.c4.view.model.FileTransferTableModel;
import main.java.de.c4.view.resources.EIcons;
import main.java.de.c4.view.resources.IconProvider;

import com.esotericsoftware.minlog.Log;

public class FileTransferFrame extends JFrame implements
		FileTransferStateListener, ActionListener {
	

	private static final int ICON_SIZE = 24;
	private static final long serialVersionUID = 1L;
	
	public static final FileTransferFrame INSTANCE = new FileTransferFrame();
	
	public FileTransferFrame() {
		
		JTable table = new JTable();
		new FileTransferTableModel(table);
		table.setRowHeight(ICON_SIZE);
		table.setFont(new Font("Sans", Font.PLAIN, 14));
		TableColumn column = table.getColumnModel().getColumn(0);
		column.setCellRenderer(new TableCellRenderer() {
			
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int col) {
				return new JLabel(IconProvider.getAsScaledIcon(((Boolean)value).booleanValue()?EIcons.UPLOAD:EIcons.DOWNLOAD, 24, ICON_SIZE));
			}
		});
		table.setFillsViewportHeight(true);
		setContentPane(new JScrollPane(table));
		setPreferredSize(new Dimension(500, 300));
		setTitle("Dateiübertragungen");
		setIconImage(IconProvider.getImage(EIcons.TRANSFER));
		pack();
	}

	public void setTransferState(FileTransferState state) {
		Log.debug("Status of FileTransfer \""+state.request.filenName+
				"\" updated: "+state.bytesDone+" of total "+
				state.request.fileSize+"bytes.");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
	}

	public static void main(String[] args) {
		INSTANCE.setDefaultCloseOperation(EXIT_ON_CLOSE);
		INSTANCE.setVisible(true);
		
	}
}
