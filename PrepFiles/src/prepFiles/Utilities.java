package prepFiles;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

public class Utilities {

	private static final String INVALID_CHARACTERS = "[\\.,;:]";
	private static final String BACKSLASH = "\\";
	private static final String CRLF = "\r\n";

	public static String addFileToBatch(String gSplitDirectory,
			int gSplitFileIndex, String batchData) {
		StringBuilder result = new StringBuilder(batchData);

		result.append(gSplitDirectory);
		result.append(BACKSLASH);
		result.append("gsplit \"no\" -b-i-t ");
		result.append(String.format("\"gsplit%03d.dat\"", gSplitFileIndex));
		result.append(CRLF);
		result.append(String.format("del gsplit%03d.dat", gSplitFileIndex));
		result.append(CRLF);

		return result.toString();
	}

	public static ArrayList<RenameInfo> checknames(GUI parentWindow,
			String workDirectory, String[] extensions, Class<?>[] xmlClasses) {
		// "Checknames" finds all files of specified file extensions and
		// ensures they are less than 56 characters long. This is accomplished
		// by generating suggested names from specified aliases or truncation.
		// The suggested names may be modified before they are applied.
		final String BACKSLASH = "\\";

		ArrayList<RenameInfo> result = new ArrayList<RenameInfo>();

		String match_string;
		Substitutions substitutions = new Substitutions();
		File sourceDir = new File(workDirectory);
		String fileList[] = null;
		File nextFile;
		String nextFileName;
		String nextFileNameUpperCase;
		Pattern patternRename;
		Matcher matcher;
		String oldNamePortion;
		String newName;
		boolean suggestionAdded;
		Pattern patternTruncateNameWithoutExtension = Pattern
				.compile("^(.{56})(.*)$");
		Pattern patternTruncateNameWithExtension = Pattern
				.compile("^(.{56})(.+)\\....$");
		Pattern patternInvalidCharacters = Pattern.compile(INVALID_CHARACTERS);

		match_string = generateMatchPattern(extensions);
		substitutions = getSubstitutionsFromResource(xmlClasses);

		if (!sourceDir.exists() || !sourceDir.isDirectory()) {
			JOptionPane.showMessageDialog(parentWindow, "Cannot read from "
					+ sourceDir.getPath() + ".");
			return result;
		}

		// sourceDir is a valid directory; continue.
		fileList = sourceDir.list();
		for (int idx = 0; idx < fileList.length; idx++) {
			nextFile = new File(sourceDir + BACKSLASH + fileList[idx]);

			if ((!nextFile.isFile()) || (nextFile.length() <= 0)) {
				continue;
			}

			// nextFile is a valid file; continue.
			nextFileName = nextFile.getName();
			nextFileNameUpperCase = nextFileName.toUpperCase();

			if (!nextFileNameUpperCase.matches(match_string)) {
				continue;
			}

			// nextFile has one of the specified file extensions; check for
			// substitution.
			suggestionAdded = false;
			for (Substitutions.Alias alias : substitutions.alias_list) {
				if (!nextFileNameUpperCase.startsWith(alias.getName()
						.toUpperCase())) {
					continue;
				}

				// nextFile begins with the current substitution's name; extract
				// remainder of file name.
				patternRename = Pattern.compile("^" + alias.getName()
						+ "(.*)\\....$");
				matcher = patternRename.matcher(nextFileName);
				if (!matcher.find()) {
					// Not expected. Pattern and matcher should extract portion
					// of file name. Skip if failed.
					continue;
				}

				// Construct proposed new name using current substitution's
				// alias.
				oldNamePortion = matcher.group(1);
				newName = alias.getAlias() + oldNamePortion;

				// Check proposed name for name length.
				matcher = patternTruncateNameWithoutExtension.matcher(newName);
				if (matcher.find()) {
					newName = matcher.group(1);
				}

				newName = newName.replaceAll(INVALID_CHARACTERS, "-");
				result.add(new RenameInfo(alias.getName() + oldNamePortion
						+ ".*", newName + ".*"));
				suggestionAdded = true;
			}

			if (!suggestionAdded) {
				// Check for name length.
				matcher = patternTruncateNameWithExtension
						.matcher(nextFileName);
				if (matcher.find()) {
					newName = matcher.group(1);
					newName = newName.replaceAll(INVALID_CHARACTERS, "-");
					result.add(new RenameInfo(matcher.group(1)
							+ matcher.group(2) + ".*", newName + ".*"));
					suggestionAdded = true;
				}
			}

			if (!suggestionAdded) {
				// Check for invalid characters.
				nextFileName = nextFileName.substring(0,
						nextFileName.length() - 4);
				matcher = patternInvalidCharacters.matcher(nextFileName);
				if (matcher.find()) {
					newName = nextFileName.replaceAll(INVALID_CHARACTERS, "-");
					result.add(new RenameInfo(nextFileName + ".*", newName
							+ ".*"));
					suggestionAdded = true;
				}
			}
		}

		return result;
	}

	public static long detectCollisions(ArrayList<DVArchiveShow> showList, long currentShowID) {
		long result = currentShowID;
		long thisShowID;
		
		for (DVArchiveShow showIdx : showList) {
			thisShowID = Long.parseLong(showIdx.replayCategory.replayChannel.replayShow.id);
			if (thisShowID == result) {
				result++;
			}
		}
		
		// If ID has changed, scan again for new collisions.
		if (result != currentShowID) {
			result = detectCollisions(showList, result);
		}
		return result;
	}

	public static String generateMatchPattern(String[] extensions) {
		StringBuilder result = new StringBuilder("^.*(");

		result.append(extensions[0]);
		for (int idx = 1; idx < extensions.length; idx++) {
			result.append("|");
			result.append(extensions[idx]);
		}
		result.append(")$");

		return result.toString();
	}

	public static String getGSplitHeader(String workDirectory, String pieSize) {
		StringBuilder result = new StringBuilder("; GSplit Batch File");

		result.append(CRLF);
		result.append("; Pass these parameters to GSplit in order to run this file:");
		result.append(CRLF);
		result.append("; GSplit.exe \"no\" -b-i-t \"[path to this file]\"");
		result.append(CRLF);
		result.append(CRLF);
		result.append("[Main]");
		result.append(CRLF);
		result.append("MulFiles=1");
		result.append(CRLF);
		result.append("DestFolder=");
		result.append(workDirectory);
		result.append(CRLF);
		result.append("TypePieSpanned=0");
		result.append(CRLF);
		result.append("SplitMethod=0");
		result.append(CRLF);
		result.append("PieSize=");
		result.append(pieSize);
		result.append(CRLF);
		result.append("PieSizeUnit=MB");
		result.append(CRLF);
		result.append("PieMask={orf}.{oru}");
		result.append(CRLF);
		result.append(CRLF);
		result.append("[FileList]");
		result.append(CRLF);

		return result.toString();
	}

	private static Substitutions getSubstitutionsFromResource(
			Class<?>[] xmlClasses) {
		Substitutions result = null;
		Object obj;

		obj = SharedUtilities.getObjectsFromXMLStream(xmlClasses,
				Utilities.class.getResourceAsStream("/substitutions.xml"));
		if (obj != null) {
			result = (Substitutions) obj;
		}

		return result;
	}

}
