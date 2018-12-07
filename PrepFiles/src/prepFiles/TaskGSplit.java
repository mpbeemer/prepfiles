package prepFiles;

import java.io.File;
import java.io.IOException;

public class TaskGSplit implements Runnable {

	private static final String BACKSLASH = "\\";
	private final GUI window;
	private final String workDirectory;
	private final long initialFreeSpace;
	private final long expectedFreeSpaceWhenFinished;

	TaskGSplit(GUI w, String wd, long ifs, long efswf) {
		this.window = w;
		this.workDirectory = wd;
		this.initialFreeSpace = ifs;
		this.expectedFreeSpaceWhenFinished = efswf;
	}

	public void run() {
		ProcessBuilder pb = null;
		Process p;
		TaskGSplitMonitor gSplitMonitorTask;
		Thread gSplitMonitorThread;
		File batchFile;

		pb = new ProcessBuilder("cmd", "/c", "gsplit_work.bat");
		pb.directory(new File(this.workDirectory));

		gSplitMonitorTask = new TaskGSplitMonitor(this.window,
				this.workDirectory, this.initialFreeSpace,
				this.expectedFreeSpaceWhenFinished);
		gSplitMonitorThread = new Thread(gSplitMonitorTask);
		gSplitMonitorThread.start();

		try {
			this.window.setStatusText("Processing GSplit command...");

			p = pb.start();
			p.waitFor();

			batchFile = new File(this.workDirectory + BACKSLASH
					+ "gsplit_work.bat");
			batchFile.delete();

			this.window.setStatusText("GSplit command completed.");

			// 5. Implement "movevids" processing."
			implementMovevids(this.window, this.workDirectory);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		gSplitMonitorTask.requestStop();
	}

	private void implementMovevids(GUI parentWindow, String workDir) {
		// Implement "movevids" awk script - move all files from
		// "My Split Files"
		// directory used by GSplit and remove the directories created by
		// GSplit.
		File baseDir;
		File mySplitFilesDir;
		String[] mySplitFilesFileList;
		File mySplitFilesEntry;
		String[] targetDirFileList;
		File targetFile;

		parentWindow.setStatusText("Moving split files...");

		baseDir = new File(workDir);
		mySplitFilesDir = new File(baseDir.getAbsolutePath() + BACKSLASH
				+ "My Split Files");
		mySplitFilesFileList = mySplitFilesDir.list();

		for (int dirIdx = 0; dirIdx < mySplitFilesFileList.length; dirIdx++) {
			mySplitFilesEntry = new File(mySplitFilesDir.getAbsolutePath()
					+ BACKSLASH + mySplitFilesFileList[dirIdx]);
			if (mySplitFilesEntry.isDirectory()) {
				targetDirFileList = mySplitFilesEntry.list();
				for (int fileIdx = 0; fileIdx < targetDirFileList.length; fileIdx++) {
					targetFile = new File(mySplitFilesEntry.getAbsolutePath()
							+ BACKSLASH + targetDirFileList[fileIdx]);
					if (targetFile.isFile()) {
						targetFile.renameTo(new File(baseDir.getAbsolutePath()
								+ BACKSLASH + targetFile.getName()));
					}
				}
				mySplitFilesEntry.delete();
			}
		}
		parentWindow
				.setStatusText("File moves completed.  Please verify results.");

	}

	private class TaskGSplitMonitor implements Runnable {

		private final GUI window;
		private final String workDirectory;
		private final long initialFreeSpace;
		private final long expectedFreeSpaceWhenFinished;
		public volatile boolean stopRequested = false;

		TaskGSplitMonitor(GUI w, String wd, long ifs, long efswf) {
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
			expectedDiskSpaceConsumption = (double) (this.initialFreeSpace 
					- this.expectedFreeSpaceWhenFinished);
			this.window.setProgressBar(0);
			while (!stopRequested) {
				try {
					currentDiskSpaceConsumption = (double) (this.initialFreeSpace 
							- sourceDir.getFreeSpace());

					progress = (int) ((currentDiskSpaceConsumption / expectedDiskSpaceConsumption) * 100);
					this.window.setProgressBar(progress);
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
