package prepFiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JTable;

public class TaskTimeFix implements Runnable {

	private static final String BACKSLASH = "\\";
	private final GUI window;
	private final JTable table;
	private final ArrayList<File> timeFixCandidates;
	private final String workDirectory;
	private final String timeFixDirectory;
	private final String targetDirectory;
	private final ArrayList<DVArchiveShow> timefixedShows;
	private final Class<?>[] xmlClasses;

	TaskTimeFix(GUI w, JTable table, ArrayList<File> tfc, String wd,
			String tfd, String td, ArrayList<DVArchiveShow> tfs, Class<?>[] xc) {
		this.window = w;
		this.table = table;
		this.timeFixCandidates = tfc;
		this.workDirectory = wd;
		this.timeFixDirectory = tfd;
		this.targetDirectory = td;
		this.timefixedShows = tfs;
		this.xmlClasses = xc;
	}

	public void run() {
		int[] rowIndices = this.table.getSelectedRows();
		int modelRow;
		File sourceDir = new File(this.workDirectory);
		File fileObj;
		String fileName;
		ProcessBuilder pb = null;
		Process p;
		TaskTimeFixMonitor timeFixMonitorTask;
		Thread timeFixMonitorThread;
		BufferedReader stdError;
		@SuppressWarnings("unused")
		String buffer;

		if (rowIndices.length == 0) {
			this.window.setStatusText("No files selected.");
		} else {
			for (int idx : rowIndices) {
				modelRow = table.convertRowIndexToModel(idx);
				fileName = timeFixCandidates.get(modelRow).getName();
				fileObj = new File(workDirectory + BACKSLASH + fileName);
				if (!fileObj.exists()) {
					JOptionPane
							.showMessageDialog(
									this.window,
									fileName
											+ " has already been processed.  Skipping to next file.",
									"PrepFiles",
									JOptionPane.INFORMATION_MESSAGE);
					continue;
				}
				if (timeFixCandidates.get(modelRow).length() > sourceDir
						.getFreeSpace()) {
					JOptionPane.showMessageDialog(
							this.window,
							"There is not enough room on the drive for the potential fixed files ("
									+ String.format("%,d",
											sourceDir.getFreeSpace())
									+ " bytes available).  "
									+ timeFixCandidates.get(modelRow).getName()
									+ " skipped.", "PrepFiles",
							JOptionPane.WARNING_MESSAGE);
					// Continue to next candidate.
					continue;
				}
				// Run TimeFix and wait for it to return.
				fileName = timeFixCandidates.get(modelRow).getName();
				pb = new ProcessBuilder("cmd", "/c", this.timeFixDirectory
						+ BACKSLASH + "timefix", fileName);
				pb.directory(new File(this.workDirectory));

				timeFixMonitorTask = new TaskTimeFixMonitor(this.window,
						this.workDirectory, sourceDir.getFreeSpace(),
						sourceDir.getFreeSpace()
								- timeFixCandidates.get(modelRow).length());
				timeFixMonitorThread = new Thread(timeFixMonitorTask);
				timeFixMonitorThread.start();

				try {
					this.window.setStatusText("Running TimeFix on "
							+ fileName.substring(0,
									Math.min(40, fileName.length())) + "...");

					p = pb.start();
					stdError = new BufferedReader(new InputStreamReader(
							p.getErrorStream()));
					while ((buffer = stdError.readLine()) != null) {
						// Wait until completion...
					}

					this.window.setStatusText("TimeFix command completed.");
					this.window.setProgressBarIndeterminate(false);

					// 2. Implement "makeparts" processing.
					implementMakeparts(this.workDirectory,
							this.targetDirectory, fileName, this.xmlClasses);
				} catch (IOException e) {
					e.printStackTrace();
				}
				timeFixMonitorTask.requestStop();
			}
		}
	}

	private void implementMakeparts(String workDirectory,
			String targetDirectory, String fileName, Class<?>[] classes) {
		// Implement "makeparts" awk script - check log file from timefix
		// process and generate corresponding RTV .ndx and .xml files if
		// needed.
		String logFileContents;
		String lastLineInLog;
		int nParts;
		int nDigitsInName;
		String formatString;
		String formatStringWithParens;
		String baseFileName;
		String targetFileName;
		int suffixIdx;
		String episodeSuffix;
		File fileObj;
		ProcessBuilder pb = null;
		@SuppressWarnings("unused")
		Process p;

		this.window.setStatusText("Moving files...");
		// Identify log file.
		baseFileName = fileName.substring(0, fileName.length() - 4);
		// Check log file for number of parts.
		logFileContents = SharedUtilities.getStringFromFile(
				workDirectory + BACKSLASH + baseFileName + ".log").trim();
		while (logFileContents.endsWith("\n")) {
			logFileContents = logFileContents.substring(0,
					logFileContents.length() - 1);
		}
		lastLineInLog = logFileContents
				.substring(logFileContents.lastIndexOf("\n") + 1,
						logFileContents.length());

		if (lastLineInLog
				.equals("No breaks in the timecode!  File did not need to be divided!")) {
			nParts = 1;
			formatString = "%s";
			formatStringWithParens = "%s";
		} else {
			nParts = Integer.parseInt(lastLineInLog.substring(0,
					lastLineInLog.indexOf(':')));
			nDigitsInName = (int) (Math.log(nParts) / Math.log(10)) + 1;
			formatString = "%s -P- %0" + String.format("%d", nDigitsInName)
					+ "d";
			formatStringWithParens = formatString.replace("-P-", "(P)");
		}

		// Set initial name for result files (may be changed due to name
		// collisions).
		targetFileName = baseFileName;
		episodeSuffix = "";

		// Check for file name collisions:
		fileObj = new File(targetDirectory + BACKSLASH + baseFileName + ".mpg");
		if (fileObj.exists()) {
			suffixIdx = 1;
			while (episodeSuffix.length() == 0) {
				fileObj = new File(targetDirectory + BACKSLASH + baseFileName
						+ "_" + String.format("%d", suffixIdx) + ".mpg");
				if (!fileObj.exists()) {
					episodeSuffix = "_" + String.format("%d", suffixIdx);
				}
				suffixIdx++;
			}
			targetFileName = targetFileName + episodeSuffix;
		}

		// for all file segments:

		// Copy dummy.ndx to 'fileName + " -P- #.ndx"' in the workDirectory,
		// where # is number of segment padded to maximum number of digits.
		// (This requires a DOS task.)
		if (nParts > 1) {
			for (int idx = 1; idx <= nParts; idx++) {
				pb = new ProcessBuilder("cmd", "/c", "copy", workDirectory
						+ BACKSLASH + "stub_ndx.dat", targetDirectory
						+ BACKSLASH
						+ String.format(formatString, targetFileName, idx)
						+ ".ndx");
				pb.directory(new File(this.workDirectory));
				try {
					p = pb.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// Process XML file:
		// create 'fileName + " -P- #.xml"' in the workDirectory, where # is
		// number of segment padded to maximum number of digits.
		// add "(P) #" to episode name, where # is number of segment padded
		// to maximum number of digits.
		// add "-P- #" to base file name, where # is number of segment
		// padded to maximum number of digits.
		// add number of segment to episode ID (timestamp).
		// (write XML files without CRs(?))
		Object obj;
		DVArchiveShow archiveShow;
		ReplayShow showData;
		String showEpisodeName;
		long showID;
		long newShowID;
		fileObj = new File(this.workDirectory + BACKSLASH + baseFileName
				+ ".xml");
		if (fileObj.exists()) {
			obj = SharedUtilities.getObjectsFromXMLFile(classes, workDirectory
					+ BACKSLASH + baseFileName + ".xml");
			if (obj != null) {
				archiveShow = (DVArchiveShow) obj;
				showData = archiveShow.replayCategory.replayChannel.replayShow;
				showID = Long.parseLong(showData.id);
				showEpisodeName = showData.episode;

				// Rewrite original .xml file after checking for ID collisions
				// and taking name collisions into account.
				showData.episode = showEpisodeName + episodeSuffix;
				showData.localShowInfo.baseFilename = targetFileName;

				// Check for ID (time stamp) collisions with existing files.
				newShowID = Utilities.detectCollisions(timefixedShows, showID);
				if (newShowID != showID) {
					((DVArchiveShow) obj).replayCategory.replayChannel.replayShow.id = String
							.format("%d", newShowID);
				}
				timefixedShows.add((DVArchiveShow) obj);

				SharedUtilities.saveObjectsToXMLFile(classes, targetDirectory
						+ BACKSLASH + targetFileName + ".xml", obj);

				if (nParts > 1) {
					// Write remaining .xml files.
					for (int idx = 1; idx <= nParts; idx++) {
						obj = new Object();
						obj = SharedUtilities.getObjectsFromXMLFile(classes,
								workDirectory + BACKSLASH + baseFileName
										+ ".xml");
						archiveShow = (DVArchiveShow) obj;
						showData = archiveShow.replayCategory.replayChannel.replayShow;

						showData.episode = String.format(
								formatStringWithParens, showEpisodeName
										+ episodeSuffix, idx);
						showData.localShowInfo.baseFilename = String.format(
								formatString, targetFileName, idx);
						showData.id = String.format("%d", showID + idx);

						// Check for ID (time stamp) collisions with existing
						// files.
						newShowID = Utilities.detectCollisions(timefixedShows,
								showID + idx);
						if (newShowID != showID + idx) {
							((DVArchiveShow) obj).replayCategory.replayChannel.replayShow.id = String
									.format("%d", newShowID);
						}
						timefixedShows.add((DVArchiveShow) obj);

						SharedUtilities.saveObjectsToXMLFile(
								classes,
								targetDirectory
										+ BACKSLASH
										+ String.format(formatString,
												targetFileName, idx) + ".xml",
								obj);
					}
				}
			}
		}

		// Move 'fileName + " -P-.mpg"' to target directory; rename to
		// include part number.
		if (nParts > 1) {
			fileObj = new File(workDirectory + BACKSLASH + baseFileName
					+ " -P-.mpg");
			fileObj.renameTo(new File(targetDirectory + BACKSLASH
					+ String.format(formatString, targetFileName, 1) + ".mpg"));
		}

		// for all but first file segment:
		// Move 'fileName + " -P- #.mpg"' to target directory, where # is
		// number of segment padded to maximum number of digits.
		for (int idx = 2; idx <= nParts; idx++) {
			fileObj = new File(workDirectory + BACKSLASH
					+ String.format(formatString, baseFileName, idx) + ".mpg");
			fileObj.renameTo(new File(targetDirectory + BACKSLASH
					+ String.format(formatString, targetFileName, idx) + ".mpg"));
		}

		// replace original .mpg file.
		if (nParts > 1) {
			fileObj = new File(this.workDirectory + BACKSLASH + baseFileName
					+ ".mpg");
			fileObj.delete();
			pb = new ProcessBuilder("cmd", "/c", "copy", workDirectory
					+ BACKSLASH + "stub_mpg.dat", targetDirectory + BACKSLASH
					+ targetFileName + ".mpg");
			pb.directory(new File(this.workDirectory));
			try {
				p = pb.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			fileObj = new File(workDirectory + BACKSLASH + baseFileName
					+ ".mpg");
			fileObj.renameTo(new File(targetDirectory + BACKSLASH
					+ targetFileName + ".mpg"));
		}

		// Resolve rest of base file set.
		fileObj = new File(workDirectory + BACKSLASH + baseFileName + ".ndx");
		fileObj.renameTo(new File(targetDirectory + BACKSLASH + targetFileName
				+ ".ndx"));
		fileObj = new File(workDirectory + BACKSLASH + baseFileName + ".xml");
		fileObj.delete();

		// Remove timefix program log file.
		fileObj = new File(this.workDirectory + BACKSLASH + baseFileName
				+ ".log");
		fileObj.delete();
		this.window.setStatusText("File moves completed.");
	}

	private class TaskTimeFixMonitor implements Runnable {

		private final GUI window;
		private final String workDirectory;
		private final long initialFreeSpace;
		private final long expectedFreeSpaceWhenFinished;
		public volatile boolean stopRequested = false;

		TaskTimeFixMonitor(GUI w, String wd, long ifs, long efswf) {
			this.window = w;
			this.workDirectory = wd;
			this.initialFreeSpace = ifs;
			this.expectedFreeSpaceWhenFinished = efswf;
		}

		public void run() {
			File sourceDir;
			double currentDiskSpaceConsumption;
			double expectedDiskSpaceConsumption;
			int progress;

			sourceDir = new File(this.workDirectory);
			expectedDiskSpaceConsumption = (double) (this.initialFreeSpace - this.expectedFreeSpaceWhenFinished);
			this.window.setProgressBarIndeterminate(true);
			while (!stopRequested) {
				try {
					currentDiskSpaceConsumption = (double) (this.initialFreeSpace 
							- sourceDir.getFreeSpace());

					progress = (int) ((currentDiskSpaceConsumption / expectedDiskSpaceConsumption) * 100);
					if (progress > 0) {
						this.window.setProgressBarIndeterminate(false);
						this.window.setProgressBar(progress);
					}
					Thread.sleep(250L);
				} catch (InterruptedException e) {
				}
			}
			this.window.setProgressBar(0);
		}

		public void requestStop() {
			stopRequested = true;
		}

	}

}
