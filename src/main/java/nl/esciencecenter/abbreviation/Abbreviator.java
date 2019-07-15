/**
 * @author gurnoor
 * S & H algorithm
 */

package nl.esciencecenter.abbreviation;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.esciencecenter.qtm.Main;

public class Abbreviator {

	private HashMap mTestDefinitions = new HashMap();
	private HashMap mStats = new HashMap();
	private int truePositives = 0, falsePositives = 0, falseNegatives = 0,
			trueNegatives = 0;
	private char delimiter = '\t';
	private boolean testMode = false;

	private boolean isValidShortForm(String str) {
		return (hasLetter(str) && (Character.isLetterOrDigit(str.charAt(0))
				|| (str.charAt(0) == '(')));
	}

	private boolean hasLetter(String str) {
		for (int i = 0; i < str.length(); i++)
			if (Character.isLetter(str.charAt(i)))
				return true;
		return false;
	}

	private boolean hasCapital(String str) {
		for (int i = 0; i < str.length(); i++)
			if (Character.isUpperCase(str.charAt(i)))
				return true;
		return false;
	}

	private boolean isTrueDefinition(String shortForm, String longForm) {
		Vector entry;
		Iterator itr;

		entry = (Vector) mTestDefinitions.get(shortForm);
		if (entry == null)
			return false;
		itr = entry.iterator();
		while (itr.hasNext()) {
			if (itr.next().toString().equalsIgnoreCase(longForm))
				return true;
		}
		return false;
	}

	public HashMap<String, String> extractAbbrPairs(String text) {

		HashMap<String, String> abbrev = new HashMap<String, String>();

		String str, tmpStr, longForm = "", shortForm = "";
		String currSentence = "";
		int openParenIndex, closeParenIndex = -1, sentenceEnd,
				newCloseParenIndex, tmpIndex = -1;
		boolean newParagraph = true;
		StringTokenizer shortTokenizer;
		try {
			BufferedReader fin = new BufferedReader(new StringReader(text));
			while ((str = fin.readLine()) != null) {
				if (str.length() == 0 || newParagraph
						&& !Character.isUpperCase(str.charAt(0))) {
					currSentence = "";
					newParagraph = true;
					continue;
				}
				newParagraph = false;
				str += " ";
				currSentence += str;
				openParenIndex = currSentence.indexOf(" (");
				do {
					if (openParenIndex > -1)
						openParenIndex++;
					sentenceEnd = Math.max(currSentence.lastIndexOf(". "),
							currSentence.lastIndexOf(", "));
					if ((openParenIndex == -1) && (sentenceEnd == -1)) {
						// Do nothing
					} else if (openParenIndex == -1) {
						currSentence = currSentence.substring(sentenceEnd + 2);
					} else if ((closeParenIndex = currSentence.indexOf(')',
							openParenIndex)) > -1) {
						sentenceEnd = Math.max(
								currSentence.lastIndexOf(". ", openParenIndex),
								currSentence.lastIndexOf(", ", openParenIndex));
						if (sentenceEnd == -1)
							sentenceEnd = -2;
						longForm = currSentence.substring(sentenceEnd + 2,
								openParenIndex);
						shortForm = currSentence.substring(openParenIndex + 1,
								closeParenIndex);
					}
					if (shortForm.length() > 0 || longForm.length() > 0) {
						if (shortForm.length() > 1 && longForm.length() > 1) {
							if ((shortForm.indexOf('(') > -1)
									&& ((newCloseParenIndex = currSentence
											.indexOf(')', closeParenIndex
													+ 1)) > -1)) {
								shortForm = currSentence.substring(
										openParenIndex + 1, newCloseParenIndex);
								closeParenIndex = newCloseParenIndex;
							}
							if ((tmpIndex = shortForm.indexOf(", ")) > -1)
								shortForm = shortForm.substring(0, tmpIndex);
							if ((tmpIndex = shortForm.indexOf("; ")) > -1)
								shortForm = shortForm.substring(0, tmpIndex);
							shortTokenizer = new StringTokenizer(shortForm);
							if (shortTokenizer.countTokens() > 2
									|| shortForm.length() > longForm.length()) {
								// Long form in ( )
								tmpIndex = currSentence.lastIndexOf(" ",
										openParenIndex - 2);
								tmpStr = currSentence.substring(tmpIndex + 1,
										openParenIndex - 1);
								longForm = shortForm;
								shortForm = tmpStr;
								if (!hasCapital(shortForm))
									shortForm = "";
							}
							if (isValidShortForm(shortForm)) {
								HashMap<String, String> singlePair = extractAbbrPair(
										getOnlyStrings(shortForm.trim()),
										longForm.trim());
								abbrev.putAll(singlePair);

							}
						}
						currSentence = currSentence
								.substring(closeParenIndex + 1);
					} else if (openParenIndex > -1) {
						if ((currSentence.length() - openParenIndex) > 200)
							// Matching close paren was not found
							currSentence = currSentence
									.substring(openParenIndex + 1);
						break; // Read next line
					}
					shortForm = "";
					longForm = "";
				} while ((openParenIndex = currSentence.indexOf(" (")) > -1);
			}
			fin.close();
		} catch (Exception ioe) {
			ioe.printStackTrace();
			Main.logger.error(currSentence);
			Main.logger.error(tmpIndex);
		}

		return abbrev;
	}

	public static String getOnlyStrings(String s) {
		Pattern pattern = Pattern.compile("[^a-z A-Z]");
		Matcher matcher = pattern.matcher(s);
		String number = matcher.replaceAll("");
		return number;
	}

	private String findBestLongForm(String shortForm, String longForm) {
		int sIndex;
		int lIndex;
		char currChar;

		sIndex = shortForm.length() - 1;
		lIndex = longForm.length() - 1;
		for (; sIndex >= 0; sIndex--) {
			currChar = Character.toLowerCase(shortForm.charAt(sIndex));
			if (!Character.isLetterOrDigit(currChar))
				continue;
			while (((lIndex >= 0) && (Character
					.toLowerCase(longForm.charAt(lIndex)) != currChar))
					|| ((sIndex == 0) && (lIndex > 0) && (Character
							.isLetterOrDigit(longForm.charAt(lIndex - 1)))))
				lIndex--;
			if (lIndex < 0)
				return null;
			lIndex--;
		}
		lIndex = longForm.lastIndexOf(" ", lIndex) + 1;
		return longForm.substring(lIndex);
	}

	private HashMap<String, String> extractAbbrPair(String shortForm,
			String longForm) {

		HashMap<String, String> Singlepairs;
		Singlepairs = new HashMap<String, String>();

		String bestLongForm;
		StringTokenizer tokenizer;
		int longFormSize, shortFormSize;

		if (shortForm.length() == 1)
			return Singlepairs;
		bestLongForm = findBestLongForm(shortForm, longForm);
		if (bestLongForm == null)
			return Singlepairs;
		tokenizer = new StringTokenizer(bestLongForm, " \t\n\r\f-");
		longFormSize = tokenizer.countTokens();
		shortFormSize = shortForm.length();
		for (int i = shortFormSize - 1; i >= 0; i--)
			if (!Character.isLetterOrDigit(shortForm.charAt(i)))
				shortFormSize--;
		if (bestLongForm.length() < shortForm.length()
				|| bestLongForm.indexOf(shortForm + " ") > -1
				|| bestLongForm.endsWith(shortForm)
				|| longFormSize > shortFormSize * 2
				|| longFormSize > shortFormSize + 5 || shortFormSize > 10)
			return Singlepairs;

		if (testMode) {
			if (isTrueDefinition(shortForm, bestLongForm)) {
				Main.logger.debug(shortForm + delimiter + bestLongForm
						+ delimiter + "TP");
				truePositives++;
			} else {
				falsePositives++;
				Main.logger.debug(shortForm + delimiter + bestLongForm
						+ delimiter + "FP");
			}
		} else {
			// Singlepairs.put(bestLongForm,shortForm);
			Singlepairs.put(shortForm, bestLongForm);
			// Main.logger.debug(shortForm + delimiter + bestLongForm);

		}
		return Singlepairs;
	}

	private static void usage() {
		System.err.println("Usage: ExtractAbbrev [-options] <filename>");
		System.err.println(
				"       <filename> contains text from which abbreviations are extracted");
		System.err.println(
				"       -testlist <file> = list of true abbreviation definition pairs");
		System.err.println("       -usage or -help = this message");
		System.exit(1);
	}

}
