package nl.erasmusmc.biosemantics.tagger.wageningen;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

public class ImportMarkers {

	static Logger logger = LogManager.getLogger();

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
			String core = line.hasOption("core") ? line.getOptionValue("core") : "sgn_markers";
			Boolean permutations = line.hasOption("permutations")
					? Boolean.parseBoolean(line.getOptionValue("permutations")) : false;
			Boolean clear = line.hasOption("     clear");

			if (solr != null && file != null && core != null) {
				processDictionary(solr, core, file, permutations, true);
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
		String cvsSplitBy = "\t";

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
				//System.out.println(line);
				String[] markerinfo = line.split(cvsSplitBy);

				String markerUri = markerinfo[0];
				String markerName = markerinfo[1];

						if (permutations) {
							SolrInputDocument doc = new SolrInputDocument();
							doc.addField("uuid", UUID.randomUUID().toString());
							doc.addField("code", markerUri);
							doc.addField("term", markerName);
							doc.addField("origin", "SGN_graph");
							doc.addField("prefterm", markerName);
							docs.add(doc);
						} else {
							SolrInputDocument doc = new SolrInputDocument();
							doc.addField("uuid", UUID.randomUUID().toString());
							doc.addField("code", markerUri);
							doc.addField("term", markerName);
							doc.addField("origin", "SGN_graph");
							doc.addField("prefterm", markerName);
							docs.add(doc);
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
