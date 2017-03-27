package abbreviation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

public class AbbrevExpander_TableFooters {
	private HashMap mTestDefinitions = new HashMap();
	private HashMap mStats = new HashMap();
	private int truePositives = 0, falsePositives = 0, falseNegatives = 0, trueNegatives = 0;
	private char delimiter = '\t';
	private boolean testMode = false;
	private HashMap<String, String> aPairs = new HashMap<>();
	
	private boolean isValidShortForm(String str) {
		return (hasLetter(str) && (Character.isLetterOrDigit(str.charAt(0)) || (str.charAt(0) == ':')));
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
	
	

	private void loadTrueDefinitions(String inFile) {
		String abbrString, defnString, str = "";
		Vector entry;
		HashMap definitions = mTestDefinitions;

		try {
			BufferedReader fin = new BufferedReader(new FileReader(inFile));
			while ((str = fin.readLine()) != null) {
				int j = str.indexOf(delimiter);
				abbrString = str.substring(0, j).trim();
				defnString = str.substring(j, str.length()).trim();
				entry = (Vector) definitions.get(abbrString);
				if (entry == null)
					entry = new Vector();
				entry.add(defnString);
				definitions.put(abbrString, entry);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(str);
		}
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
		int openParenIndex,colonIndex, abbrevStart, closeParenIndex = -1, sentenceEnd, newCloseParenIndex, tmpIndex = -1;
		boolean newParagraph = true;
		StringTokenizer shortTokenizer;
		try {
			BufferedReader fin = new BufferedReader(new StringReader(text));
			while ((str = fin.readLine()) != null) {
				//System.out.println("print1: "+ str);
				if (str.length() == 0 || newParagraph && !Character.isUpperCase(str.charAt(0))) {
					currSentence = "";
					newParagraph = true;
					continue;
				}
				newParagraph = false;
				str += " ";
				//System.out.println("print2: "+ str);
				currSentence += str;
				//System.out.println("print3: "+ currSentence);
				
				colonIndex= currSentence.indexOf(":");
				//System.out.println("print4: "+ colonIndex);
				
				do {
					//System.out.println("colon index is"+colonIndex);
					sentenceEnd = Math.max(currSentence.lastIndexOf("."), currSentence.lastIndexOf(","));
					//System.out.println("sentence Ends at "+sentenceEnd);
					
					if ((colonIndex == -1) && (sentenceEnd == -1)) {
						// Do nothing
					}
					else if (colonIndex == -1) {
						currSentence = currSentence.substring(sentenceEnd);
					}
					else{
						abbrevStart=currSentence.lastIndexOf(" ", colonIndex);
						
						//System.out.println("abrev Start is"+abbrevStart);
						
						if(currSentence.indexOf(".", colonIndex) != -1 && currSentence.indexOf(",", colonIndex) != -1){
							sentenceEnd = Math.min(currSentence.indexOf(".", colonIndex),currSentence.indexOf(",", colonIndex));
						}
						else if (currSentence.indexOf(".", colonIndex) == -1 && currSentence.indexOf(",", colonIndex) == -1)
						{
							//do Nothing
						}else if(currSentence.indexOf(".", colonIndex) == -1 && currSentence.indexOf(",", colonIndex) != -1){
							sentenceEnd=currSentence.indexOf(",", colonIndex);
						}
						else if(currSentence.indexOf(".", colonIndex) != -1 && currSentence.indexOf(",", colonIndex) == -1){
							sentenceEnd=currSentence.indexOf(".", colonIndex);
						}
						//System.out.println("sentenceEnd now is"+sentenceEnd);
						shortForm = currSentence.substring(abbrevStart+1 , colonIndex);
						longForm = currSentence.substring(colonIndex+1, sentenceEnd);
						
					}
				//	System.out.println("shortform is"+shortForm );
				//	System.out.println("Longform is"+longForm );
					
					
					if (shortForm.length() > 1 || longForm.length() > 1) {
						
						if (!hasCapital(shortForm))
							shortForm = "";
						
						if(isValidShortForm(shortForm)){
						HashMap<String, String> singlePair = extractAbbrPair(shortForm.trim(), longForm.trim());
						abbrev.putAll(singlePair);	
						}
					}
					System.out.println("SE is "+sentenceEnd+"current" + currSentence);
					
					if(sentenceEnd<currSentence.length())
					currSentence = currSentence.substring(sentenceEnd);
				//	System.out.println("CurrSentence is "+currSentence);
					shortForm = "";
					longForm = "";
				} while ( (colonIndex = currSentence.indexOf(":")) > -1 );
			}
			fin.close();
		} catch (Exception ioe) {
			ioe.printStackTrace();
		//	System.out.println(currSentence);
		//	System.out.println(tmpIndex);
		}
		return abbrev;
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
			while (((lIndex >= 0) && (Character.toLowerCase(longForm.charAt(lIndex)) != currChar))
					|| ((sIndex == 0) && (lIndex > 0) && (Character.isLetterOrDigit(longForm.charAt(lIndex - 1)))))
				lIndex--;
			if (lIndex < 0)
				return null;
			lIndex--;
		}
		lIndex = longForm.lastIndexOf(" ", lIndex) + 1;
		return longForm.substring(lIndex);
	}

	private HashMap<String, String> extractAbbrPair(String shortForm, String longForm) {

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
		if (bestLongForm.length() < shortForm.length() || bestLongForm.indexOf(shortForm + " ") > -1
				|| bestLongForm.endsWith(shortForm) || longFormSize > shortFormSize * 2
				|| longFormSize > shortFormSize + 5 || shortFormSize > 10)
			return Singlepairs;

		if (testMode) {
			if (isTrueDefinition(shortForm, bestLongForm)) {
				System.out.println(shortForm + delimiter + bestLongForm + delimiter + "TP");
				truePositives++;
			} else {
				falsePositives++;
				System.out.println(shortForm + delimiter + bestLongForm + delimiter + "FP");
			}
		} else {
			//Singlepairs.put(bestLongForm,shortForm);
			Singlepairs.put(shortForm,bestLongForm);
			//System.out.println(shortForm + delimiter + bestLongForm);

		}
		return Singlepairs;
	}

	private static void usage() {
		System.err.println("Usage: ExtractAbbrev [-options] <filename>");
		System.err.println("       <filename> contains text from which abbreviations are extracted");
		System.err.println("       -testlist <file> = list of true abbreviation definition pairs");
		System.err.println("       -usage or -help = this message");
		System.exit(1);
	}

}
