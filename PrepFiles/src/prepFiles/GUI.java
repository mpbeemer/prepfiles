package prepFiles;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

interface Constants {
	String[] renameInfoColumnNames = { "Old Name", "New Name" };
	String[] gSplitCandidatesColumnNames = { "Name", "Size" };
	String[] timeFixCandidatesColumnNames = { "Name", "Size" };
}

@SuppressWarnings("serial")
public class GUI extends javax.swing.JFrame {

	private static final String BACKSLASH = "\\";
	private static final String CRLF = "\r\n";

	private GUI parentWindow;
	private String workDirectory;
	private String timeFixDirectory;
	private String targetDirectory;
	private String gSplitDirectory;
	private String[] extensions;
	private long targetLength;
	private String pieSize;
	private Class<?>[] xmlClasses;

	private ArrayList<RenameInfo> renameInfo = new ArrayList<RenameInfo>();
	private ArrayList<File> gSplitCandidates = new ArrayList<File>();
	private ArrayList<File> timeFixCandidates = new ArrayList<File>();
	private ArrayList<DVArchiveShow> timefixedShows = new ArrayList<DVArchiveShow>();
	private long selectedFilesTotalSize;

	JMenuBar menuBar;
	JMenu fileMenu;
	JMenuItem exitItem;
	JMenu viewMenu;
	JMenuItem reloadItem;
	JButton timeFixButton;
	JButton checkNamesButton;
	JButton renameButton;
	JButton splitButton;
	JMenu helpMenu;
	JMenuItem aboutItem;
	JPanel contentArea;
	RenameInfoTableModel renameInfoTableModel;
	JTable renameInfoTableContents;
	JScrollPane renameInfoScrollPane;
	TimeFixCandidatesTableModel timeFixCandidatesTableModel;
	JTable timeFixCandidatesTableContents;
	ListSelectionModel timeFixCandidatesListSelectionModel;
	JScrollPane timeFixCandidatesScrollPane;
	GSplitCandidatesTableModel gSplitCandidatesTableModel;
	JTable gSplitCandidatesTableContents;
	ListSelectionModel gSplitCandidatesListSelectionModel;
	JScrollPane gSplitCandidatesScrollPane;
	JTextArea textArea;
	JScrollPane blankPane;
	CardLayout cardLayout;
	JComboBox statusComboBox;
	JPanel statusBar;
	JLabel statusBarLabel;
	JProgressBar progressBar;

	public GUI(String workDirectory, String timeFixDirectory,
			String gSplitDirectory, String targetDirectory, String[] extensions, long targetLength,
			String pieSize, Class<?>[] xmlClasses) {
		super();
		this.parentWindow = this;
		this.workDirectory = workDirectory;
		this.timeFixDirectory = timeFixDirectory;
		this.gSplitDirectory = gSplitDirectory;
		this.targetDirectory = targetDirectory;
		this.extensions = extensions;
		this.targetLength = targetLength;
		this.pieSize = pieSize;
		this.xmlClasses = xmlClasses;
		initGUI(workDirectory);
	}

	private void initGUI(String workDirectory) {

		try {
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

			SharedUtilities.configureLookAndFeel("SeaGlass");

			menuBar = new JMenuBar();
			fileMenu = new JMenu("File");
			fileMenu.setMnemonic(KeyEvent.VK_F);
			exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
			viewMenu = new JMenu("View");
			viewMenu.setMnemonic(KeyEvent.VK_V);
			reloadItem = new JMenuItem("Reload", KeyEvent.VK_R);
			timeFixButton = new JButton("TimeFix");
			checkNamesButton = new JButton("Check Names");
			renameButton = new JButton("Rename");
			splitButton = new JButton("Split");
			helpMenu = new JMenu("Help");
			aboutItem = new JMenuItem("About");
			contentArea = new JPanel(new CardLayout());
			contentArea.setFont(new Font ("Monospaced", Font.PLAIN, 12));
			cardLayout = (CardLayout) contentArea.getLayout();
			statusBar = new JPanel();
			statusBarLabel = new JLabel();
			progressBar = new JProgressBar(0, 100);

			this.setTitle("PrepFiles");

			menuBar.add(fileMenu);
			fileMenu.add(exitItem);
			exitItem.addActionListener(menuItemListener);
			menuBar.add(viewMenu);
			viewMenu.add(reloadItem);
			reloadItem.addActionListener(menuItemListener);
			menuBar.add(timeFixButton);
			timeFixButton.addActionListener(menuItemListener);
			timeFixButton.setVisible(true);
			menuBar.add(checkNamesButton);
			checkNamesButton.setVisible(true);
			checkNamesButton.addActionListener(menuItemListener);
			menuBar.add(renameButton);
			renameButton.setVisible(false);
			renameButton.addActionListener(menuItemListener);
			menuBar.add(splitButton);
			splitButton.setVisible(false);
			splitButton.addActionListener(menuItemListener);
			menuBar.add(Box.createHorizontalGlue());
			menuBar.add(helpMenu);
			helpMenu.add(aboutItem);
			aboutItem.addActionListener(menuItemListener);

			textArea = new JTextArea("");
			textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
			blankPane = new JScrollPane(textArea);
			contentArea.add(blankPane, "blank");
			cardLayout.show(contentArea, "blank");

			renameInfoTableModel = new RenameInfoTableModel();
			renameInfoTableContents = new JTable(renameInfoTableModel);
			renameInfoScrollPane = new JScrollPane(renameInfoTableContents);
			contentArea.add(renameInfoScrollPane, "RenameInfo");

			timeFixCandidatesTableModel = new TimeFixCandidatesTableModel();
			timeFixCandidatesTableContents = new JTable(
					timeFixCandidatesTableModel);
			timeFixCandidatesTableContents.setAutoCreateRowSorter(true);
			timeFixCandidatesListSelectionModel = timeFixCandidatesTableContents
					.getSelectionModel();
			timeFixCandidatesListSelectionModel
					.addListSelectionListener(timeFixCandidateTotalSize);

			timeFixCandidatesScrollPane = new JScrollPane(
					timeFixCandidatesTableContents);
			contentArea.add(timeFixCandidatesScrollPane, "TimeFixCandidates");

			gSplitCandidatesTableModel = new GSplitCandidatesTableModel();
			gSplitCandidatesTableContents = new JTable(
					gSplitCandidatesTableModel);
			gSplitCandidatesListSelectionModel = gSplitCandidatesTableContents
					.getSelectionModel();
			gSplitCandidatesListSelectionModel
					.addListSelectionListener(gSplitCandidateTotalSize);

			gSplitCandidatesScrollPane = new JScrollPane(
					gSplitCandidatesTableContents);
			contentArea.add(gSplitCandidatesScrollPane, "GSplitCandidates");

			// Add status bar.
			statusBarLabel.setBorder(new EmptyBorder(0, 5, 0, 10));
			statusBarLabel.setPreferredSize(new Dimension(800, 15));
			progressBar.setPreferredSize(new Dimension(300, 15));
			statusBar.setBorder(new EmptyBorder(3, 3, 1, 5));
			statusBar.setLayout(new GridLayout());
			statusBar.add(statusBarLabel);
			statusBar.add(progressBar);

			// Add components to window.
			this.getContentPane().add(menuBar, BorderLayout.NORTH);
			this.getContentPane().add(contentArea, BorderLayout.CENTER);
			this.getContentPane().add(statusBar, BorderLayout.SOUTH);

			timefixedShows = recordTimefixedShows(parentWindow, targetDirectory, xmlClasses);
			identifyTimeFixCandidates(workDirectory);

			pack();
			setSize(850, 600);
		} catch (Exception e) {
			// add your error handling code here
			e.printStackTrace();
		}
	}

	private void resizeTable(JTable table) {
		pack();
		setSize(Math.max(SharedUtilities.autoFitTableColumns(table) + 30, 850),
				600);
	}

	ActionListener menuItemListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();

			if (source == exitItem) {
				System.exit(0);
			} else if (source == reloadItem) {
				if (timeFixCandidatesScrollPane.isVisible()) {
					identifyTimeFixCandidates(workDirectory);
				}
			} else if (source == timeFixButton) {
				// 1. Implement "timefixall" processing.
				processTimeFixCandidates(workDirectory, timeFixDirectory, targetDirectory, timefixedShows);
			} else if (source == checkNamesButton) {
				// 3. Implement "checknames" processing.
				timeFixButton.setVisible(false);
				checkNamesButton.setVisible(false);
				renameInfo = Utilities.checknames(parentWindow, workDirectory, extensions,
						xmlClasses);
				renameInfoTableModel.fireTableChanged(null);
				resizeTable(renameInfoTableContents);
				if (renameInfo.size() == 0) {
					splitButton.setVisible(true);
					JOptionPane.showMessageDialog(parentWindow,
							"No files needed to be renamed.", "PrepFiles",
							JOptionPane.INFORMATION_MESSAGE);
					// 4. Implement "gsplitall" processing.
					identifyGSplitCandidates(extensions, targetLength);
				} else {
					renameButton.setVisible(true);
					cardLayout.show(contentArea, "RenameInfo");
				}

			} else if (source == renameButton) {
				performRenameActions(workDirectory, renameInfo);
				// 4. Implement "gsplitall" processing.
				splitButton.setVisible(true);
				identifyGSplitCandidates(extensions, targetLength);
			} else if (source == splitButton) {
				// 4B. Implement "gsplitall" processing.
				processGSplitCandidates(workDirectory, pieSize);
			} else if (source == aboutItem) {
				JOptionPane.showMessageDialog(parentWindow,
						"Something goes here...", "PrepFiles help:",
						JOptionPane.QUESTION_MESSAGE);
			}
		}

	};

	ListSelectionListener gSplitCandidateTotalSize = new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
			selectedFilesTotalSize = 0;
			int nFiles = 0;
			int modelRow;
			if (e.getValueIsAdjusting())
				return; // if you don't want to handle
						// intermediate selections
			ListSelectionModel rowSM = (ListSelectionModel) e.getSource();
			for (int idx = 0; idx < gSplitCandidates.size(); idx++) {
				if (rowSM.isSelectedIndex(idx)) {
					nFiles++;
					modelRow = gSplitCandidatesTableContents
							.convertRowIndexToModel(idx);
					selectedFilesTotalSize = selectedFilesTotalSize
							+ gSplitCandidates.get(modelRow).length();
					setStatusText(nFiles + " files: "
							+ String.format("%,d", selectedFilesTotalSize)
							+ " bytes.");
				}
			}

		}
	};

	ListSelectionListener timeFixCandidateTotalSize = new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
			selectedFilesTotalSize = 0;
			int nFiles = 0;
			int modelRow;
			if (e.getValueIsAdjusting())
				return; // if you don't want to handle
			// intermediate selections
			ListSelectionModel rowSM = (ListSelectionModel) e.getSource();
			for (int idx = 0; idx < timeFixCandidates.size(); idx++) {
				if (rowSM.isSelectedIndex(idx)) {
					nFiles++;
					modelRow = timeFixCandidatesTableContents
							.convertRowIndexToModel(idx);
					selectedFilesTotalSize = selectedFilesTotalSize
							+ timeFixCandidates.get(modelRow).length();
					setStatusText(nFiles + " files: "
							+ String.format("%,d", selectedFilesTotalSize)
							+ " bytes.");
				}
			}

		}
	};

	private void performRenameActions(String workDirectory,
			ArrayList<RenameInfo> renameInfo) {
		final String BACKSLASH = "\\";

		File sourceDir = new File(workDirectory);
		String fileList[] = null;
		File nextFile;
		String nextFileName;
		String baseFileName;
		String newFileName;
		String fileExtension;

		if (sourceDir.exists() && sourceDir.isDirectory()) {
			fileList = sourceDir.list();

			for (RenameInfo info : renameInfo) {
				// System.out.println("rename \"" + info.old_name + "\" \"" +
				// info.new_name + "\"");

				baseFileName = info.old_name.substring(0,
						info.old_name.length() - 2);
				newFileName = info.new_name.substring(0,
						info.new_name.length() - 2);
				for (int idx = 0; idx < fileList.length; idx++) {
					nextFile = new File(sourceDir + BACKSLASH + fileList[idx]);
					if (nextFile.isFile()) {
						nextFileName = nextFile.getName();
						if (nextFileName.startsWith(baseFileName)) {
							fileExtension = nextFileName.substring(
									nextFileName.length() - 3,
									nextFileName.length());
							if (!nextFile.renameTo(new File(sourceDir
									+ BACKSLASH + newFileName + "."
									+ fileExtension))) {
								JOptionPane.showMessageDialog(parentWindow,
										info.old_name + " not renamed(?)",
										"PrepFiles",
										JOptionPane.INFORMATION_MESSAGE);
							}
						}
					}
				}
			}
		} else {
			JOptionPane.showMessageDialog(parentWindow, "Cannot read from "
					+ sourceDir.getPath() + ".");
		}

		renameInfo.clear();
		renameButton.setVisible(false);
		textArea.setText("Files renamed as specified.");
		cardLayout.show(contentArea, "blank");
	}

	private void identifyGSplitCandidates(String[] extensions, long targetLength) {
		String match_string;
		File sourceDir = new File(workDirectory);
		String fileList[] = null;
		File nextFile;
		String nextFileName;

		gSplitCandidates = new ArrayList<File>();

		match_string = Utilities.generateMatchPattern(extensions);

		if (sourceDir.exists() && sourceDir.isDirectory()) {
			fileList = sourceDir.list();

			for (int idx = 0; idx < fileList.length; idx++) {
				nextFile = new File(sourceDir.getAbsolutePath() + BACKSLASH
						+ fileList[idx]);
				if (nextFile.isFile()) {
					nextFileName = nextFile.getName();
					if (nextFileName.toUpperCase().matches(match_string)) {
						if (nextFile.length() > targetLength) {
							gSplitCandidates.add(nextFile);
						}
					}
				}
			}
		} else {
			JOptionPane.showMessageDialog(parentWindow, "Cannot read from "
					+ sourceDir.getPath() + ".");
		}

		Collections.sort(gSplitCandidates, new Comparator<File>() {
			public int compare(File file1, File file2) {
				return (int) (file1.length() - file2.length());
			}
		});
		gSplitCandidatesTableModel.fireTableChanged(null);
		resizeTable(gSplitCandidatesTableContents);
		cardLayout.show(contentArea, "GSplitCandidates");
	}

	private void identifyTimeFixCandidates(String workDirectory) {
		String match_string;
		File sourceDir = new File(workDirectory);
		String fileList[] = null;
		File nextFile;
		String nextFileName;

		timeFixCandidates = new ArrayList<File>();

		match_string = "^.*MPG$";

		if (sourceDir.exists() && sourceDir.isDirectory()) {
			fileList = sourceDir.list();

			for (int idx = 0; idx < fileList.length; idx++) {
				nextFile = new File(sourceDir.getAbsolutePath() + BACKSLASH
						+ fileList[idx]);
				if (nextFile.isFile()) {
					nextFileName = nextFile.getName();
					if (nextFileName.toUpperCase().matches(match_string)) {
						timeFixCandidates.add(nextFile);
					}
				}
			}
		} else {
			JOptionPane.showMessageDialog(parentWindow, "Cannot read from "
					+ sourceDir.getPath() + ".");
		}

		Collections.sort(timeFixCandidates, new Comparator<File>() {
			public int compare(File file1, File file2) {
				return (int) (file1.length() - file2.length());
			}
		});
		timeFixCandidatesTableModel.fireTableChanged(null);
		resizeTable(timeFixCandidatesTableContents);
		cardLayout.show(contentArea, "TimeFixCandidates");
	}

	private void processGSplitCandidates(String workDirectory, String pieSize) {
		int[] rowIndices = gSplitCandidatesTableContents.getSelectedRows();
		int modelRow;
		String gSplitData;
		int gSplitIndex;
		int gSplitFileIndex;
		String batchData;

		File sourceDir = new File(workDirectory);

		if (rowIndices.length == 0) {
			setStatusText("No files selected.");
		} else if (selectedFilesTotalSize > sourceDir.getFreeSpace()) {
			JOptionPane
					.showMessageDialog(
							parentWindow,
							"There is not enough room on the drive for the split files ("
									+ String.format("%,d",
											sourceDir.getFreeSpace())
									+ " bytes available).  Please adjust your selections.",
							"PrepFiles", JOptionPane.WARNING_MESSAGE);
		} else {
			batchData = "";
			gSplitData = Utilities.getGSplitHeader(workDirectory, pieSize);
			gSplitIndex = 0;
			gSplitFileIndex = 1;
			for (int idx : rowIndices) {
				modelRow = gSplitCandidatesTableContents
						.convertRowIndexToModel(idx);
				gSplitData = gSplitData
						+ String.format("%d=%s" + CRLF, gSplitIndex,
								gSplitCandidates.get(modelRow)
										.getAbsoluteFile());
				gSplitIndex++;
				if (gSplitIndex > 9) {
					SharedUtilities.saveStringToFile(
							"gsplit" + String.format("%03d", gSplitFileIndex)
									+ ".dat", gSplitData);

					batchData = Utilities.addFileToBatch(gSplitDirectory,
							gSplitFileIndex, batchData);

					gSplitData = Utilities.getGSplitHeader(workDirectory,
							pieSize);
					gSplitIndex = 0;
					gSplitFileIndex++;
				}
			}
			SharedUtilities.saveStringToFile(workDirectory + BACKSLASH
					+ "gsplit" + String.format("%03d", gSplitFileIndex)
					+ ".dat", gSplitData);

			batchData = Utilities.addFileToBatch(gSplitDirectory,
					gSplitFileIndex, batchData);
			SharedUtilities.saveStringToFile(workDirectory + BACKSLASH
					+ "gsplit_work.bat", batchData);

			Thread gSplitThread;

			// Run GSplit and wait for it to return.
			// TaskGSplit( workDirectory, initialFreeSpace,
			// expectedFreeSpaceWhenFinished)
			gSplitThread = new Thread(new TaskGSplit(this, workDirectory,
					sourceDir.getFreeSpace(), sourceDir.getFreeSpace()
							- selectedFilesTotalSize
							- (98304 * rowIndices.length)));
			gSplitThread.start();
		}
	}

	private void processTimeFixCandidates(String workDirectory, String timeFixDirectory, String targetDirectory, ArrayList<DVArchiveShow> showList) {
		Thread timeFixThread;

		// TaskTimeFix(window, table, timeFixCandidates, workDirectory,
		// timeFixDirectory,
		// targetDirectory)
		timeFixThread = new Thread(new TaskTimeFix(this,
				timeFixCandidatesTableContents, timeFixCandidates,
				workDirectory, timeFixDirectory, targetDirectory, showList, xmlClasses));
		timeFixThread.start();
	}

	private ArrayList<DVArchiveShow> recordTimefixedShows(GUI window, String targetDirectory, Class<?>[] classes) {
		// Examines all existing shows in timefixed directory and eliminates any 
		// collisions between existing show IDs (timestamps).
		ArrayList<DVArchiveShow> result = new ArrayList<DVArchiveShow>();
		File sourceDir = new File(targetDirectory);
		String fileList[] = null;
		String fileExtension;
		File nextFile;
		Object obj;
		long currentShowID;
		long newShowID;

		if (sourceDir.exists() && sourceDir.isDirectory()) {
			fileList = sourceDir.list();

			for (int idx = 0; idx < fileList.length; idx++) {
				fileExtension = fileList[idx].substring(fileList[idx].length()-3,fileList[idx].length());
				if (!fileExtension.toLowerCase().equals("xml")) {
					continue;
				}
				nextFile = new File(sourceDir.getAbsolutePath() + BACKSLASH
						+ fileList[idx]);
				if (nextFile.isFile()) {
					obj = SharedUtilities.getObjectsFromXMLFile(classes,
							targetDirectory + BACKSLASH + fileList[idx]);
					if (obj != null) {
						currentShowID = Long.parseLong(((DVArchiveShow) obj).replayCategory.replayChannel.replayShow.id);

						newShowID = Utilities.detectCollisions(result, currentShowID);
						if (newShowID != currentShowID) {
							((DVArchiveShow) obj).replayCategory.replayChannel.replayShow.id = String.format("%d", newShowID);
							SharedUtilities.saveObjectsToXMLFile(classes,
									targetDirectory	+ BACKSLASH + fileList[idx],
									obj);
						}
						result.add((DVArchiveShow) obj);
					}
				}
			}
		} else {
			JOptionPane.showMessageDialog(window, "Cannot read from "
					+ sourceDir.getPath() + ".");
		}

		return result;
	}
	
	class RenameInfoTableModel extends AbstractTableModel {
		public int getColumnCount() {
			return Constants.renameInfoColumnNames.length;
		}

		public int getRowCount() {
			return renameInfo.size();
		}

		public String getColumnName(int col) {
			return Constants.renameInfoColumnNames[col];
		}

		public Object getValueAt(int row, int col) {
			Object value = null;
			RenameInfo info = renameInfo.get(row);
			switch (col) {
			case 0:
				value = info.old_name;
				break;
			case 1:
				value = info.new_name;
				break;
			}
			return value;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int col) {
			return getValueAt(0, col).getClass();
		}

		public boolean isCellEditable(int row, int col) {
			switch (col) {
			case 1:
				return true;
			default:
				return false;
			}
		}

		public void setValueAt(Object value, int row, int col) {
			RenameInfo info = renameInfo.get(row);
			String newValue = (String) value;

			switch (col) {
			case 0:
				info.old_name = newValue;
				break;
			case 1:
				if (newValue.length() > 58) {
					JOptionPane
							.showMessageDialog(
									parentWindow,
									"The new name specified is too long; please shorten it.",
									"Name too long",
									JOptionPane.WARNING_MESSAGE);
				} else {
					info.new_name = newValue;
					resizeTable(renameInfoTableContents);
				}
				break;
			}
			fireTableCellUpdated(row, col);
		}

	}

	public class GSplitCandidatesTableModel extends AbstractTableModel {
		public int getColumnCount() {
			return Constants.gSplitCandidatesColumnNames.length;
		}

		public int getRowCount() {
			return gSplitCandidates.size();
		}

		public String getColumnName(int col) {
			return Constants.gSplitCandidatesColumnNames[col];
		}

		public Object getValueAt(int row, int col) {
			Object value = null;
			File fileInfo = gSplitCandidates.get(row);
			switch (col) {
			case 0:
				value = fileInfo.getName();
				break;
			case 1:
				value = fileInfo.length();
				break;
			}
			return value;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int col) {
			return getValueAt(0, col).getClass();
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
			// Never called.
		}

	}

	class TimeFixCandidatesTableModel extends AbstractTableModel {
		public int getColumnCount() {
			return Constants.timeFixCandidatesColumnNames.length;
		}

		public int getRowCount() {
			return timeFixCandidates.size();
		}

		public String getColumnName(int col) {
			return Constants.timeFixCandidatesColumnNames[col];
		}

		public Object getValueAt(int row, int col) {
			Object value = null;
			File fileInfo = timeFixCandidates.get(row);
			switch (col) {
			case 0:
				value = fileInfo.getName();
				break;
			case 1:
				value = fileInfo.length();
				break;
			}
			return value;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int col) {
			return getValueAt(0, col).getClass();
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
			// Never called.
		}

	}

	public void setStatusText(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				statusBarLabel.setText(message);
			}
		});
	}

	public void setProgressBar(final int value) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue(value);
			}
		});
	}

	public void setProgressBarIndeterminate(final boolean value) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setIndeterminate(value);
			}
		});
	}

}
