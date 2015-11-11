package main.java.de.c4.view.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import main.java.de.c4.model.messages.file.FileTransferState;

public class FileTransferTableModel extends DefaultTableModel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String[] COLS = new String[]{" ", "Status", "Dateiname", "Kontakt", " "};
	private static final List<FileTransferState> TRANSFERS = new ArrayList<FileTransferState>();
	
	public FileTransferTableModel(JTable table) {
		table.setModel(this);
	}

	@Override
	public int getColumnCount() {
		return COLS.length;
	}
	
	@Override
	public String getColumnName(int column) {
		return COLS[column];
	}
	
	@Override
	public int getRowCount() {
		return TRANSFERS.size();
	}
	
	@Override
	public Object getValueAt(int row, int column) {
		FileTransferState state = TRANSFERS.get(row);
		if (column == 0) {
			return Boolean.valueOf(state.isUpload);
		} else if (column == 2) {
			return state.request.filenName;
		} else if (column == 3) {
			return state.contact.name;
		} else if (column == 4) {
			return Long.valueOf(state.request.id);
		}
		return TRANSFERS.get(row);
	}
}
