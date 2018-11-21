package nl.esciencecenter.solr.tagger.recognize;

import java.io.FileReader;
import java.io.IOException;
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
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import nl.esciencecenter.solr.tagger.utils.Permutations;


public class ImportJSON {


    public static void main(String[] args) {

	Options options = new Options().addOption("solr", true, "SOLR repository")
			.addOption("file", true, "file to import")
			.addOption("core", true, "core")
			.addOption("clear", false, "false")
			.addOption("permutations", false, "permutations");
	CommandLineParser parser = new DefaultParser();

	try {
	    // parse the command line arguments
	    CommandLine line = parser.parse(options, args, false);

	    if (line.getArgList().size() > 0) {
		throw new ParseException("unknown arguments");
	    }
	    String solr = line.hasOption("solr") ? line.getOptionValue("solr") : "http://localhost:8983/solr";
	    String file = line.hasOption("file") ? line.getOptionValue("file") : null;
	    String core = line.hasOption("core") ? line.getOptionValue("core") : "statoTerms";
	    Boolean permutations = line.hasOption("permutations") ? Boolean.parseBoolean(line.getOptionValue("permutations")) : false;
	    Boolean clear = line.hasOption("clear");

	    if (solr != null && file != null && core != null) {
		processDictionary(solr, core, file, permutations, clear);
	    } else {
		new HelpFormatter().printHelp(Process.class.getCanonicalName(), options);
	    }
	} catch (ParseException e) {
	    System.err.println("Parsing failed.  Reason: " + e.getMessage());
	} catch (SolrServerException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private static void processDictionary(String server, String core, String filename, Boolean permutations, Boolean clear) throws SolrServerException, IOException {
	SolrClient solr = new HttpSolrClient.Builder(server + "/" + core).build();
	if (clear){
		solr.deleteByQuery("*:*");
		solr.commit();
	}

	int count = 0;
	List<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();




	for ( Entry<String, JsonElement> entry : new JsonParser().parse(new FileReader(filename)).getAsJsonObject().entrySet() ){
	    count++;
	    if ( count % 100 == 0 ){
		System.out.println(count);
		System.out.flush();
		solr.add(docs);
		docs.clear();
	    }
	    if (permutations){
		for (String term : Permutations.get(entry.getValue().getAsString())){
		    SolrInputDocument doc = new SolrInputDocument();
		    doc.addField("uuid", UUID.randomUUID().toString());
		    doc.addField("code", entry.getKey());
		    doc.addField("term", term);
		    doc.addField("origin", filename);
		    doc.addField("prefterm", term);
		    docs.add(doc);
		}
	    }
	    else{
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("uuid", UUID.randomUUID().toString());
		doc.addField("code", entry.getKey());
		doc.addField("term", entry.getValue().getAsString());
		doc.addField("origin", "dictionary");
		doc.addField("prefterm", entry.getValue().getAsString());
		docs.add(doc);
	    }
	}
	solr.add(docs);
	solr.commit();
	solr.close();
    }
}
