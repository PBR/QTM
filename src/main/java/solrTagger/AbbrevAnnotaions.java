package solrTagger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import qtm.Article;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class AbbrevAnnotaions {

	public static void abbrevToSolrSynonyms(Article articles[])
			throws FileNotFoundException, UnsupportedEncodingException, IOException {
		String synonymfile = "/var/solr/data/terms/conf/synonyms.txt";

		HashMap<String, String> oldAbbrev = new HashMap<String, String>();
		Properties properties = new Properties();
		properties.load(new FileInputStream(synonymfile));

		for (String key : properties.stringPropertyNames()) {
			oldAbbrev.put(key.toString(), properties.get(key).toString());
		}

		FileWriter fw = new FileWriter(synonymfile, true);
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
					"cp -rf /var/solr/data/terms/conf/synonyms.txt /var/solr/data/statoTerms/conf/synonyms.txt " });
			p.waitFor();
			p = Runtime.getRuntime().exec(new String[] { "bash", "-c",
					"cp -rf /var/solr/data/terms/conf/synonyms.txt /var/solr/data/propTerms/conf/synonyms.txt " });
			p.waitFor();
			p = Runtime.getRuntime().exec(new String[] { "bash", "-c",
					"cp -rf /var/solr/data/terms/conf/synonyms.txt /var/solr/data/solaLyco/conf/synonyms.txt " });
			p.waitFor();
			p = Runtime.getRuntime().exec(new String[] { "bash", "-c",
					"cp -rf /var/solr/data/terms/conf/synonyms.txt /var/solr/data/solaLyco2/conf/synonyms.txt " });
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}