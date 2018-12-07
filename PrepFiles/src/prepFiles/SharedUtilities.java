package prepFiles;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

// import com.seaglasslookandfeel.SeaGlassLookAndFeel;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyReplacer;
import com.thoughtworks.xstream.io.xml.XppDriver;

public class SharedUtilities {

	public static int autoFitTableColumns(JTable table) {
		int tableWidth = 0;
		
		// Auto-fit column widths.
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	
		for (int i = 0; i < table.getColumnCount(); i++) {
			DefaultTableColumnModel colModel = (DefaultTableColumnModel) table
					.getColumnModel();
			TableColumn col = colModel.getColumn(i);
			int width = 0;
	
			TableCellRenderer renderer = col.getHeaderRenderer();
			if (renderer == null) {
				renderer = table.getTableHeader().getDefaultRenderer();
			}
	
			Component comp = renderer.getTableCellRendererComponent(table,
					col.getHeaderValue(), false, false, 0, 0);
	
			width = comp.getPreferredSize().width;
			for (int r = 0; r < table.getRowCount(); r++) {
				renderer = table.getCellRenderer(r, i);
				comp = renderer.getTableCellRendererComponent(table,
						table.getValueAt(r, i), false, false, r, i);
				width = Math.max(width, comp.getPreferredSize().width);
			}
			col.setPreferredWidth(width + 2);
			tableWidth += width + 2;
		}
		return tableWidth;
	}

	public static void configureLookAndFeel(String selectedLookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		// List of Look And Feel alternatives built into the system,
		// (extracted by the succeeding code):
		//
		// Metal (CrossPlatform)
		// Nimbus
		// CDE/Motif
		// Windows (System)
		// Windows Classic
		/*
		 * for (LookAndFeelInfo info : UIManager .getInstalledLookAndFeels()) {
		 * System.err.println(info.getName()); }
		 */
	
		// UIManager alternatives:
		// Windows Look And Feel ("System") -
		
		UIManager.setLookAndFeel(UIManager .getSystemLookAndFeelClassName());
		
		// Java Look And Feel ("Metal") -
		/*
		 * UIManager.setLookAndFeel(UIManager
		 * .getCrossPlatformLookAndFeelClassName());
		 */
		// Nimbus Look And Feel - requires override of textArea font.
		/*
		 * for (LookAndFeelInfo info : UIManager .getInstalledLookAndFeels()) {
		 * if ("Nimbus".equals(info.getName())) { UIManager.setLookAndFeel(new
		 * NimbusLookAndFeel() {
		 * 
		 * @Override public UIDefaults getDefaults() { UIDefaults ret =
		 * super.getDefaults();
		 * 
		 * ret.put("TextArea.font", new Font( Font.MONOSPACED, Font.PLAIN, 13));
		 * return ret; } });
		 * 
		 * break; } }
		 */
	
		// Set Look And Feel to Sea Glass. Modify textArea to
		// use mono-space font.
		// UIManager.setLookAndFeel(new SeaGlassLookAndFeel() {
		// @Override
		// public UIDefaults getDefaults() {
		// UIDefaults ret = super.getDefaults();
		//
		// // Enumeration newKeys = ret.keys();
		// //
		// // while (newKeys.hasMoreElements()) {
		// // Object obj = newKeys.nextElement();
		// // System.out.printf("%50s : %s\n", obj,
		// // UIManager.get(obj));
		// // }
		//
		// ret.put("TextArea.font", new Font(Font.MONOSPACED,
		// Font.PLAIN, 13));
		// return ret;
		// }
		// });
	
		// Report all entries for UIManager in effect.
		/*
		 * UIDefaults ret = UIManager.getDefaults();
		 * 
		 * Enumeration newKeys = ret.keys();
		 * 
		 * while (newKeys.hasMoreElements()) { Object obj =
		 * newKeys.nextElement(); System.out.printf("%50s : %s\n", obj,
		 * UIManager.get(obj)); }
		 */

		/*
		if (selectedLookAndFeel.equals("System")) {
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			}
		} else if (selectedLookAndFeel.equals("Metal")) {
			try {
				UIManager.setLookAndFeel(UIManager
						.getCrossPlatformLookAndFeelClassName());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			}
		} else if (selectedLookAndFeel.equals("SeaGlass")) {
			try {
				UIManager.setLookAndFeel(new SeaGlassLookAndFeel());
			} catch (UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			}
		}
		*/
	}

	public static Object convertFromXML(Class<?>[] classes, String XMLString) {
		XStream xstream = new XStream(new DomDriver());
		for (Class<?> classIdx : classes) {
			xstream.processAnnotations(classIdx);
		}
		xstream.setMode(XStream.NO_REFERENCES);
		Object obj = xstream.fromXML(XMLString);
		return obj;
	}

	public static String convertToXML(Class<?>[] classes, Object objectArray) {
		XStream xstream = new XStream(new XppDriver(new XmlFriendlyReplacer("$", "_"))); 
		for (Class<?> classIdx : classes) {
			xstream.processAnnotations(classIdx);
		}
		xstream.setMode(XStream.NO_REFERENCES);
		return xstream.toXML(objectArray);
	}

	public static Object getObjectsFromXMLFile(Class<?>[] xmlClasses, String fileName) {
		return convertFromXML(xmlClasses, getStringFromFile(fileName));
	}

	public static Object getObjectsFromXMLStream(Class<?>[] xmlClasses, InputStream is) {
		return convertFromXML(xmlClasses, getStringFromStream(is));
	}

	public static String getStringFromFile(String fileName) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
	
		try {
			br = new BufferedReader(new FileReader(fileName));
	
			try {
				String s;
				while ((s = br.readLine()) != null) {
					// add linefeed stripped by readline back to string.
					sb.append(s);
					sb.append("\n");
				}
			} finally {
				br.close();
			}
		} catch (IOException ex) {
			System.err.println("Unable to read from file: " + fileName + ".");
			System.exit(0);
		}
		return sb.toString();
	}

	public static String getStringFromStream(InputStream is) {
		/*
		 * To convert the InputStream to String we use the Reader.read(char[]
		 * buffer) method. We iterate until the Reader return -1 which means
		 * there's no more data to read. We use the StringWriter class to
		 * produce the string.
		 */
		if (is != null) {
			Writer writer = new StringWriter();
	
			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} catch (UnsupportedEncodingException e) {
				return "";
			} catch (IOException e) {
				return "";
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					return "";
				}
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	public static String postProcessXML(String xmlStream) {
		// - Collapses empty elements into closed tags.
		// (<THING></THING> becomes <THING/>)
		// - Also embeds XML header not provided by xstream.
		String result = xmlStream;
		String target, replacement;
		Pattern pattern = Pattern.compile(".*<(.*)></\\1>.*");
		Matcher matcher = pattern.matcher(result);
		while (matcher.find()) {
			target = "<" + matcher.group(1) + "></" + matcher.group(1) + ">";
			replacement = "<" + matcher.group(1) + "/>";
			result = result.replace(target, replacement);
			matcher = pattern.matcher(result);
		}
		return "<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n" + result + "\n";
	}

	public static boolean saveObjectsToXMLFile(Class<?>[] classes, String fileName,
			Object objectArray) {
		return saveStringToFile(fileName,postProcessXML(convertToXML(classes, objectArray)));
	}

	public static boolean saveStringToFile(String fileName, String saveString) {
		boolean saved = false;
		BufferedWriter bw = null;
	
		try {
			bw = new BufferedWriter(new FileWriter(fileName));
	
			try {
				bw.write(saveString);
				saved = true;
			} finally {
				bw.close();
			}
		} catch (IOException ex) {
			System.err.println("Unable to save to file: " + fileName + ".");
			System.exit(0);
		}
		return saved;
	}

}
