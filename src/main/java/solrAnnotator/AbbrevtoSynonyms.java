package solrAnnotator;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import qtm.Article;
import utils.Configs;

/**
 *
 * @author gurnoor This class is used to copy all new Abbreviations found to the
 *         file /conf/synonyms.txt of all the dictionary.
 */

public class AbbrevtoSynonyms {

	public static void abbrevToSolrSynonyms(Article articles[])
			throws FileNotFoundException, UnsupportedEncodingException,
			IOException {
		String coreTraitDescSynonmsFile = Configs.getPropertyQTM("solrCorePath") + "/"
				+ Configs.getPropertyQTM("coreTraitDescriptors") + "/conf/synonyms.txt";

		HashMap<String, String> oldAbbrev = new HashMap<String, String>();
		Properties properties = new Properties();

		properties.load(new FileInputStream(coreTraitDescSynonmsFile));

		for (String key : properties.stringPropertyNames()) {
			oldAbbrev.put(key.toString(), properties.get(key).toString());
		}

		FileWriter fw = new FileWriter(coreTraitDescSynonmsFile, true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter writer = new PrintWriter(bw);

		for (int i = 0; i < articles.length; i++) {
			if (articles[i] != null) {
				HashMap<String, String> abbrevMap = articles[i]
						.getAbbreviations();
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
							writer.println(shortform + " => "
									+ abbrevMap.get(shortform));
							System.out.println(shortform + " => "
									+ abbrevMap.get(shortform));
						}
					}

				}
			}
		}
		writer.close();

		String coreTraitValuesDir = Configs.getPropertyQTM("solrCorePath")
				+ Configs.getPropertyQTM("coreTraitValues") + "/conf/";
		String coreTraitPropertiesDir = Configs.getPropertyQTM("solrCorePath")
				+ Configs.getPropertyQTM("coreTraitProperties") + "/conf/";

		try {
			Process p1 = Runtime.getRuntime()
					.exec("cp " + coreTraitDescSynonmsFile + " " + coreTraitValuesDir);
			p1.waitFor();
			Process p2 = Runtime.getRuntime()
					.exec("cp " + coreTraitDescSynonmsFile + " " + coreTraitPropertiesDir);
			p2.waitFor();

		} catch (Exception e) {

		}

	}

}