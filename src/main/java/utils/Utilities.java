/**
 *
 * @author gurnoor
 */

package utils;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

import qtm.QtmMain;

/**
 * The Class Utilities.
 */
public class Utilities {

	/**
	 * Checks if is string numeric.
	 *
	 * @param str the str
	 * @return true, if is numeric
	 */
	public static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		} catch (NullPointerException e) {
			return false;
		}
		return true;
	}

	/**
	 * Creates the xml string from sub node.
	 *
	 * @param xml the xml
	 * @return the string
	 */
	public static String createXMLStringFromSubNode(Node xml) {
		String result = "";
		try {
			StringWriter sw = new StringWriter();
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.transform(new DOMSource(xml), new StreamResult(sw));
			result = sw.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	/**
	 * Creates the xml string from sub node.
	 *
	 * @param xml the xml
	 * @return the string
	 */
	public static String createXMLStringFromSubNodeWithoutDeclaration(Node xml) {
		xml = xml.getFirstChild();
		String result = "";
		try {
			StringWriter sw = new StringWriter();
			Transformer serializer = TransformerFactory.newInstance().newTransformer();
			serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			serializer.transform(new DOMSource(xml), new StreamResult(sw));
			result = sw.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	public static String getString(Node xml) {
		String result = "";
		if (QtmMain.doXMLInput) {
			result = createXMLStringFromSubNodeWithoutDeclaration(xml);
		} else {
			result = xml.getTextContent();
		}
		return result;
	}

	public static boolean isSpaceOrEmpty(String s) {
		if (s == null)
			return false;
		if (s.length() > 1)
			return false;
		if (s.length() == 0)
			return true;
		char ch = s.charAt(0);
		if ((ch) == 8195 || ch == 160 || ch == ' ' || ch == 8194 || ch == 8201)
			return true;
		else
			return false;
	}

	public static String ReplaceNonBrakingSpaceToSpace(String s) {
		char ch = 160;
		if (s == null)
			return "";
		s = s.replace(ch, ' ');
		ch = 8195;
		s = s.replace(ch, ' ');
		ch = 8194;
		s = s.replace(ch, ' ');
		ch = 8201;
		s = s.replace(ch, ' ');
		return s;
	}

	public static boolean isDouble(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public static int getFirstValue(String s) {
		int numericStart = 0;
		int numericCount = 0;
		boolean isFirst = false;
		for (int i = 0; i < s.length(); i++) {
			if (Utilities.isNumeric(s.charAt(i) + "")) {
				if (!isFirst) {
					numericStart = i;
				}
				isFirst = true;
				numericCount = i - numericStart + 1;
			}
			if (i >= 1 && Utilities.isNumeric(s.charAt(i - 1) + "") && !Utilities.isNumeric(s.charAt(i) + "")) {
				break;
			}
		}
		int num = 0;
		if (numericCount > 0)
			num = Integer.parseInt(s.substring(numericStart, numericStart + numericCount));
		return num;

	}

	public static int GetNumOfOccurrences(String string, String substring) {
		string = string.toLowerCase();
		substring = substring.toLowerCase();
		int index = string.indexOf(substring);
		int occurrences = 0;
		while (index != -1) {
			occurrences++;
			string = string.substring(index + 1);
			index = string.indexOf(substring);
		}
		return occurrences;
	}

	public static String getCellType(String cellContent) {
		int numbers = 0;
		int chars = 0;
		String tempCellVal = cellContent.replaceAll("[\\s\\xA0]", "");
		for (int i = 0; i < tempCellVal.length(); i++) {
			if (Utilities.isNumeric(tempCellVal.substring(i, i + 1))) {
				numbers++;
			} else {
				chars++;
			}
		}
		float proportion = (float) numbers / (chars + numbers);
		if (Utilities.isNumeric(tempCellVal))
			return "PureNumeric";
		// part numeric cell
		if (proportion > 0.49 && !Utilities.isNumeric(tempCellVal)) {
			return "PartNumeric";
		}
		if (proportion <= 0.49 && !Utilities.isNumeric(tempCellVal)) {
			return "PureText";
		}
		return "Other (Empty)";
	}

}
