package main.java.de.c4.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import main.java.de.c4.controller.FileTransferManager;
import main.java.de.c4.controller.shared.Settings;
import main.java.de.c4.controller.shared.listener.FileTransferStateListener;
import main.java.de.c4.model.messages.file.FileTransferState;
import main.java.de.c4.view.model.FileTransferTableModel;
import main.java.de.c4.view.resources.EIcons;
import main.java.de.c4.view.resources.IconProvider;

import com.esotericsoftware.minlog.Log;

public class FileTransferFrame extends JFrame implements
		FileTransferStateListener, ActionListener, MouseListener {
	

	private static final int ICON_SIZE = 24;
	private static final long serialVersionUID = 1L;
	
	public static final FileTransferFrame INSTANCE = new FileTransferFrame();
	private JTable table = new JTable();
	private FileTransferTableModel model = new FileTransferTableModel(table);
	private final Map<Long, JProgressBar> BARS = new HashMap<Long, JProgressBar>();

	public FileTransferFrame() {
		
		table.addMouseListener(this);
		table.setRowHeight(ICON_SIZE);
		table.setFont(new Font("Sans", Font.PLAIN, 14));
		TableColumn column = table.getColumnModel().getColumn(0);
		column.setMaxWidth(ICON_SIZE);
		column.setResizable(false);
		column.setCellRenderer(new TableCellRenderer() {
			
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int col) {
				JLabel label = new JLabel(IconProvider.getAsScaledIcon(((Boolean)value).booleanValue()?EIcons.UPLOAD:EIcons.DOWNLOAD, 24, ICON_SIZE));
				return label;
			}
		});
		
		column = table.getColumnModel().getColumn(1);
		column.setCellRenderer(new TableCellRenderer() {
			
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int col) {
				FileTransferState state = (FileTransferState)value;
				JProgressBar bar;
				Long id = Long.valueOf(state.request.id);
				if (BARS.containsKey(id)) {
					bar = BARS.get(id);
				} else {
					bar = new JProgressBar(0, 100);
					bar.setStringPainted(true);
					BARS.put(id, bar);
					
				}
				if (state.bytesDone==0) {
					bar.setIndeterminate(true);
					bar.setString("Warte auf Beginn...");
				} else {
					bar.setIndeterminate(false);
					StringBuffer b = new StringBuffer(FileTransferManager.toReadableByteCount(state.bytesDone, false));
					b.append(" von ");
					b.append(FileTransferManager.toReadableByteCount(state.request.fileSize, false));
					b.append(" (");
					long prozent = state.bytesDone*100L/state.request.fileSize;
					b.append(prozent);
					b.append(" %)");
					bar.setString(b.toString());
					bar.setValue((int)prozent);
				}
				return bar;
			}
		});

		column = table.getColumnModel().getColumn(4); 
		column.setMaxWidth(ICON_SIZE);
		column.setResizable(false);
//		new ButtonColumn(table, new AbstractAction() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				System.out.println(e.getActionCommand());
//			}
//		}, 4);
		
		column.setCellRenderer(new TableCellRenderer() {
			
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int col) {
				JLabel label = new JLabel(IconProvider.getAsScaledIcon(EIcons.ABORT, ICON_SIZE, ICON_SIZE));
				return label;
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
		model.setState(state);
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println(e.getActionCommand());
	}

	public static void main(String[] args) {
		try {
			String lookAndFeel = Settings.INSTANCE.get(Settings.LOOK_AND_FEEL);
			if (lookAndFeel != null && !lookAndFeel.isEmpty()) {
				for (LookAndFeelInfo info : UIManager
						.getInstalledLookAndFeels()) {
					System.out.println(info.getName());
					if (lookAndFeel.equals(info.getName())) {
						UIManager.setLookAndFeel(info.getClassName());
						break;
					}
				}
			} else {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			}
		} catch (Exception e) {
			Log.error("Error settings Look and Feel.");
		}
		INSTANCE.setDefaultCloseOperation(EXIT_ON_CLOSE);
		INSTANCE.setVisible(true);
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int column = table.getColumnModel().getColumnIndexAtX(e.getX()); // get the coloum of the button
        if (column != 4) {
			return;
		}
		int row    = e.getY()/table.getRowHeight(); //get the row of the button
		System.out.println(table.getModel().getValueAt(row, column));
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
