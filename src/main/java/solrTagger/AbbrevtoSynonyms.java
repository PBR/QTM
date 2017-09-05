package solrTagger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import qtm.Article;
import utils.Configs;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * 
 * @author gurnoor
 * This class is used to copy all new Abbreviations found to the file /conf/synonyms.txt of all the dictionary.
 */

public class AbbrevtoSynonyms {

	public static void abbrevToSolrSynonyms(Article articles[])
			throws FileNotFoundException, UnsupportedEncodingException, IOException {
		String termSynonmsFile = Configs.getPropertySolr("termDir")+"/conf/synonyms.txt";
		
		String statoSynonmsFile = Configs.getPropertySolr("statoDir")+"/conf/synonyms.txt";
		String propSynonmsFile = Configs.getPropertySolr("propDir")+"/conf/synonyms.txt";
		String solanumSynonmsFile = Configs.getPropertySolr("solanumDir")+"/conf/synonyms.txt";
                 
		
		HashMap<String, String> oldAbbrev = new HashMap<String, String>();
		Properties properties = new Properties();
		
		
		properties.load(new FileInputStream(termSynonmsFile));

		for (String key : properties.stringPropertyNames()) {
			oldAbbrev.put(key.toString(), properties.get(key).toString());
		}

		FileWriter fw = new FileWriter(termSynonmsFile, true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter writer = new PrintWriter(bw);

		for (int i = 0; i < articles.length; i++) {

			HashMap<String, String> abbrevMap = articles[i].getAbbreviations();
			if (abbrevMap.isEmpty()) {
				break;
			} else {
				Set<String> totalkeys = abbrevMap.keySet();
				Iterator<String> keys = totalkeys.iterator();
				while (keys.hasNext()) {
					String shortform = keys.next();
					if (oldAbbrev.containsKey(shortform)) {
						break;
					} else {
						writer.println(shortform + " => " + abbrevMap.get(shortform));
						System.out.println(shortform + " => " + abbrevMap.get(shortform));
					}
				}

			}
		}
		writer.close();

		Process p;

		try {
			p = Runtime.getRuntime().exec(new String[] { "bash", "-c",
					"cp -rf "+termSynonmsFile+" "+statoSynonmsFile+" " });
			p.waitFor();
			p = Runtime.getRuntime().exec(new String[] { "bash", "-c",
					"cp -rf "+termSynonmsFile+" "+propSynonmsFile+" " });
			p.waitFor();
			p = Runtime.getRuntime().exec(new String[] { "bash", "-c",
					"cp -rf "+termSynonmsFile+" "+solanumSynonmsFile+" "});
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}