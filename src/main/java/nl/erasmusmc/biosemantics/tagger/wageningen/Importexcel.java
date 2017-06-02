package nl.erasmusmc.biosemantics.tagger.wageningen;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
//import org.apache.log4j.core.pattern.MarkerSimpleNamePatternConverter;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import nl.erasmusmc.biosemantics.tagger.utils.Permutations;

public class Importexcel {

	static Logger logger = LogManager.getRootLogger();

	public static void main(String[] args) {

		Options options = new Options().addOption("solr", true, "SOLR repository")
				.addOption("file", true, "file to import").addOption("core", true, "core")
				.addOption("clear", false, "false").addOption("permutations", false, "permutations");
		CommandLineParser parser = new DefaultParser();

		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args, false);

			if (line.getArgList().size() > 0) {
				throw new ParseException("unknown arguments");
			}
			String solr = line.hasOption("solr") ? line.getOptionValue("solr") : "http://localhost:8983/solr";
			String file = line.hasOption("file") ? line.getOptionValue("file") : null;
			String core = line.hasOption("core") ? line.getOptionValue("core") : "solaLyco2";
			Boolean permutations = line.hasOption("permutations")
					? Boolean.parseBoolean(line.getOptionValue("permutations")) : false;
			Boolean clear = line.hasOption("clear");

			if (solr != null && file != null && core != null) {
				processDictionary(solr, core, file, permutations, clear);
			} else {
				new HelpFormatter().printHelp(Process.class.getCanonicalName(), options);
			}
		} catch (ParseException e) {
			System.err.println("Parsing failed.  Reason: " + e.getMessage());
			logger.error("Parsing failed.  Reason: " + e.getMessage());
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void processDictionary(String server, String core, String filename, Boolean permutations,
			Boolean clear) throws SolrServerException, IOException {
		SolrClient solr = new HttpSolrClient.Builder(server + "/" + core).build();
		if (clear) {
			solr.deleteByQuery("*:*");
			solr.commit();
		}

		int cnt = 0;
		List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();

		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {
			br = new BufferedReader(new FileReader(filename));
			while ((line = br.readLine()) != null) {

				cnt++;
				if (cnt % 1000 == 0) {
					System.out.println(cnt);
					System.out.flush();
					solr.add(docs);
					docs.clear();
				}

				String[] markerinfo = line.split(cvsSplitBy);

				String markerUri = markerinfo[1];
				String makerName = markerinfo[2];
				String[] mNames = makerName.split(";");
				
					for (int i = 0; i < mNames.length; i++) {
						if (permutations) {
							SolrInputDocument doc = new SolrInputDocument();
							doc.addField("uuid", UUID.randomUUID().toString());
							doc.addField("code", markerUri);
							doc.addField("term", mNames[i]);
							doc.addField("origin", "SGN_graph");
							doc.addField("prefterm", mNames[i]);
							docs.add(doc);
						} else {
							SolrInputDocument doc = new SolrInputDocument();
							doc.addField("uuid", UUID.randomUUID().toString());
							doc.addField("code", markerUri);
							doc.addField("term", mNames[i]);
							doc.addField("origin", "SGN_graph");
							doc.addField("prefterm", mNames[i]);
							docs.add(doc);
						}

					}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			solr.add(docs);
			solr.commit();
			solr.close();

			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
