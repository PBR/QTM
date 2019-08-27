package nl.esciencecenter.qtm.solr.abbreviator;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import nl.esciencecenter.qtm.Article;
import nl.esciencecenter.qtm.Main;
import nl.esciencecenter.qtm.utils.Configs;

/**
 *
 * @author gurnoor This class is used to copy all new Abbreviations found to the
 *         file /conf/synonyms.txt of all the dictionary.
 */

public class AbbrevtoSynonyms {

	public static void abbrevToSolrSynonyms(Article articles[])
		throws IOException {
		String line = "";
		String synonymsFile = Configs.getPropertyQTM("solrCorePath")
				+ "/" + Configs.getPropertyQTM("coreTraitDescriptors")
				+ "/conf/synonyms.txt";
		FileWriter fw = new FileWriter(synonymsFile, true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter writer = new PrintWriter(bw);
		HashMap<String, String> allAbbreviation = new HashMap<String, String>();
		try {
			for (int i = 0; i < articles.length; i++) {
				HashMap<String, String> abbrevin1article = articles[i].getAbbreviations();
				if (abbrevin1article.isEmpty()) {
					continue;
				} else {
					Set<String> totalkeys = abbrevin1article.keySet();
					Iterator<String> keys = totalkeys.iterator();
					while (keys.hasNext()) {
						String shortform = keys.next();
						if (allAbbreviation.containsKey(shortform)) {
							allAbbreviation.put(shortform, allAbbreviation.get(shortform) + "," + abbrevin1article.get(shortform));
						} else {
							allAbbreviation.put(shortform, abbrevin1article.get(shortform));
						}
					}
				}
			}
		} catch(Exception e){
			Main.logger.error("No abbreviations detected or copied.");
		}

		Set<String> totalkeys = allAbbreviation.keySet();
		Iterator<String> keys = totalkeys.iterator();
		Main.logger.info("Appending abbreviation/expansion pairs to file '"
				+ synonymsFile + "'.");
		while (keys.hasNext()) {
			String shortform = keys.next();
			line = shortform + " => " + allAbbreviation.get(shortform);
			writer.println(line);
			Main.logger.debug(line);
		}
		writer.close();
	}
}