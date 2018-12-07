package prepFiles;

import javax.swing.SwingUtilities;

public class PrepFiles {

	private static final String WORK_DIRECTORY = "C:\\Data\\Videos";
//	private static final String WORK_DIRECTORY = ".";
	private static final long TARGET_LENGTH = 522190848L; 
	// (1024L for testing; 522190848L for use.)
	private static final String PIESIZE = "498"; 
	// (100 for testing; 498 for use.)
	private static final String TIMEFIX_DIRECTORY = "C:\\Programs\\Timefix";
	private static final String GSPLIT_DIRECTORY = "C:\\Programs\\GSplit";
	private static final String TARGET_DIRECTORY = "C:\\Data\\Videos\\Timefixed";
	private static final String[] TARGET_EXTENSIONS = { "AVI", "FLV", "ISO",
			"MKV", "MP4", "MPG", "ZIP" };
	private static GUI window;
	private static final Class<?>[] XML_CLASSES = { Substitutions.class, DVArchiveShow.class, ReplayShow.class };

	public static void main(String[] args) {
		// Automates processing of RTV and other files for storage on DVD.
		// 1. Implement "timefixall" processing.
		// 2. Implement "makeparts" processing.
		// 3. Implement "checknames" processing.
		// 4. Implement "gsplitall" processing.
		// 5. Implement "movevids" processing."
		// 6. (Leave checking files against originals, moving files to split
		// directory and deleting originals to manual review.)

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				window = new GUI(WORK_DIRECTORY, TIMEFIX_DIRECTORY,
						GSPLIT_DIRECTORY, TARGET_DIRECTORY, TARGET_EXTENSIONS, TARGET_LENGTH,
						PIESIZE, XML_CLASSES);
				window.setLocationRelativeTo(null);
				window.setVisible(true);
			}
		});

	}

}
