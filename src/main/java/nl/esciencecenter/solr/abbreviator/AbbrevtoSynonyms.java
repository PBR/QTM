package nl.esciencecenter.solr.abbreviator;

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
import nl.esciencecenter.utils.Configs;

/**
 *
 * @author gurnoor This class is used to copy all new Abbreviations found to the
 *         file /conf/synonyms.txt of all the dictionary.
 */

public class AbbrevtoSynonyms {

	public static void abbrevToSolrSynonyms(Article articles[])
			throws FileNotFoundException, UnsupportedEncodingException,
			IOException {
		String coreTraitDescSynonmsFile = Configs.getPropertyQTM("solrCorePath")
				+ "/" + Configs.getPropertyQTM("coreTraitDescriptors")
				+ "/conf/synonyms.txt";

		// HashMap<String, String> oldAbbrev = new HashMap<String, String>();
		// Properties properties = new Properties();

		// properties.load(new FileInputStream(coreTraitDescSynonmsFile));

		// System.out.println(properties.toString());

		// for (String key : properties.stringPropertyNames()) {
		// oldAbbrev.put(key.toString(), properties.get(key).toString());
		// }



		FileWriter fw = new FileWriter(coreTraitDescSynonmsFile, true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter writer = new PrintWriter(bw);


		HashMap<String, String> allAbbreviation = new HashMap<String, String>();



		try{
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
		}
		catch(Exception e){
			System.out.println("No Abbreviations, detected or copied");
		}

		Set<String> totalkeys = allAbbreviation.keySet();
		Iterator<String> keys = totalkeys.iterator();
		while (keys.hasNext()) {
			String shortform = keys.next();
			writer.println(shortform + " => "
					+ allAbbreviation.get(shortform));
			//System.out.println(shortform + " => "
			//		+ allAbbreviation.get(shortform));

		}


		writer.close();

		// String coreTraitValuesDir = Configs.getPropertyQTM("solrCorePath")
		// + Configs.getPropertyQTM("coreTraitValues") + "/conf/";
		// String coreTraitPropertiesDir =
		// Configs.getPropertyQTM("solrCorePath")
		// + Configs.getPropertyQTM("coreTraitProperties") + "/conf/";
		//
		// try {
		// Process p1 = Runtime.getRuntime()
		// .exec("cp " + coreTraitDescSynonmsFile + " " + coreTraitValuesDir);
		// p1.waitFor();
		// Process p2 = Runtime.getRuntime()
		// .exec("cp " + coreTraitDescSynonmsFile + " " +
		// coreTraitPropertiesDir);
		// p2.waitFor();
		//
		// } catch (Exception e) {
		//
		// }

	}

}