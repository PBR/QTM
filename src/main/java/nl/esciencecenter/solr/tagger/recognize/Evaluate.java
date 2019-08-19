package nl.esciencecenter.solr.tagger.recognize;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.solr.client.solrj.util.ClientUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import nl.esciencecenter.qtm.Main;
import nl.esciencecenter.solr.tagger.utils.Position;
import nl.esciencecenter.solr.tagger.utils.TagItem;
import nl.esciencecenter.solr.tagger.utils.TagResponse;

/**
 * @author gurnoor
 */
public class Evaluate {


	static CloseableHttpClient client = HttpClients.createDefault();

	static HashMap<String, String> headers = new HashMap<String, String>();

	public static void main(String[] args) throws URISyntaxException {

		Options options = new Options()
				.addOption("solr", true, "SOLR repository")
				.addOption("core", true, "SOLR core")
				.addOption("outputfolder", true,
						"output folder for results file")
				.addOption("input", true, "input text to tag")
				.addOption("match", true,
						"match type: ALL, NO_SUB or LONGEST_DOMINANT_RIGHT (default)")
				.addOption("type", true, "optional file type")
				.addOption("output", true, "optional output file");

		CommandLineParser parser = new DefaultParser();

		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args, false);

			headers.put("Content-Type", "text/plain ; charset=utf-8");

			if (line.getArgList().size() > 0) {
			}

			String solr = line.hasOption("solr")
					? line.getOptionValue("solr")
					: "http://localhost:8983/solr";
			String core = line.hasOption("core")
					? line.getOptionValue("core")
					: "trait_properties";
			String match = line.hasOption("match")
					? line.getOptionValue("match")
					: "ALL";
			String input = line.hasOption("input")
					? line.getOptionValue("input")
					: "Abbreviation";
			String outputfolder = line.hasOption("outputfolder")
					? line.getOptionValue("outputfolder")
					: "data/Resultdata";
			String type = line.hasOption("type")
					? line.getOptionValue("type")
					: "dictionary";

			String output = line.hasOption("output")
					? line.getOptionValue("output")
					: "QTMDb";

			if (solr != null && core != null && type != null && match != null
					&& output != null) {

				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(output), "UTF-8"));
				out.write(StringUtils.join(
						new String[]{"SET", "FILE", "CODE", "MATCHTEXT",
								"PREFTERM", "TERM", "START", "END", "UUID"},
						"|"));
				out.newLine();


				String check= "sol";
				TagResponse tag = processString(check, "sgn_potato_markers", "LONGEST_DOMINANT_RIGHT", "dictionary");

				String tagUri = "";
				if (tag.getItems().size() == 1)
					tagUri = tag.getItems().get(0).getIcd10();
				else {
					for (TagItem item : tag.getItems()) {
						tagUri += item.getIcd10() + ";";
					}
				}
				Main.logger.debug(tagUri);

//				processArray("sgn_potato_markers","LONGEST_DOMINANT_RIGHT", type);
				out.close();
			} else {
				new HelpFormatter().printHelp(Evaluate.class.getCanonicalName(),
						options);
			}

		} catch (ParseException e) {
			System.err.println("Parsing failed.  Reason: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
	}






	public static void processArray(String core, String match, String type)
			throws UnsupportedEncodingException, FileNotFoundException,
			IOException {
		String[] line = {"stsnp_c1_2221","stsnp_c1_12614","stsnp_c1_7206","stsnp_c1_12166","solcap_snp_c2_54011" };

		//String[] line = {"2221","12614"};

		for (int i = 0; i < line.length; i++) {
			// input="This invention relates to methods for identifying maize
			// plants having increased culturability and/or transformability.
			// The methods use molecular markers to identify and to select
			// plants with increased culturability and/or transformability or to
			// identify and deselect plants with decreased culturability and/or
			// transformability. Maize plants generated by the methods of the
			// invention are also a feature of the invention.";
			try {
				String request = "http://localhost:8983/solr/" + core
						+ "/tag?fl=uuid,code,prefterm,term&overlaps="
						+ URLEncoder.encode(match, "UTF-8")
						+ "&matchText=true&tagsLimit=5000&wt=json";

				// Main.logger.debug(request);

				URI uri=new URI("http"
						,"localhost:8983",
						"/solr/" + core + "/tag",
						"fl=uuid,code,prefterm,term&overlaps="+ URLEncoder.encode(match, "UTF-8")+"&matchText=true&tagsLimit=5000&wt=json",
						null);

				TagResponse response = parse(
						getStringContent(uri, line[i], headers));

				for (TagItem item : response.getItems()) {
					Main.logger.debug(item.getMatchText()+"\t"+item.getPrefTerm()+"\t"+item.getIcd10());
				}
				Main.logger.debug("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}










	public static TagResponse processString(String input, String core,
			String match, String type) throws UnsupportedEncodingException,
			FileNotFoundException, IOException {

		//Main.logger.debug("Input is: \t "+input);
		TagResponse response = new TagResponse();

		try {
			String request = "http://localhost:8983/solr/" + core
					+ "/tag?fl=uuid,code,prefterm,term&overlaps="
					+ URLEncoder.encode(match, "UTF-8")
					+ "&matchText=true&tagsLimit=5000&wt=json";
			String content = getStringContent(request, input, headers);

			response = parse(content);

		} catch (Exception e) {
			e.printStackTrace();

		}
		return response;
	}

	public static TagResponse processString2(String input, String core,
			String match, String type) throws UnsupportedEncodingException,
			FileNotFoundException, IOException {

		//Main.logger.debug("Input is: \t "+input);
		TagResponse response = new TagResponse();

		try {

			URI uri=new URI("http"
					,"localhost:8983",
					"/solr/" + core + "/tag",
					"fl=uuid,code,prefterm,term&overlaps="+ URLEncoder.encode(match, "UTF-8")+"&matchText=true&tagsLimit=5000&wt=json",
					null);

			String content = getStringContent(uri, input, headers);

			response = parse(content);

		} catch (Exception e) {
			e.printStackTrace();

		}
		return response;
	}


	public static String getStringContent(URI uri, String postData,
			HashMap<String, String> headers) throws Exception {

		HttpPost request = new HttpPost(uri);

		//Main.logger.trace("postdata is: \t"+postData);
		//Main.logger.trace("request is: \t"+request.toString());


		request.setEntity(new StringEntity(
				ClientUtils.escapeQueryChars(postData), "UTF-8"));

		//Main.logger.trace("Request is: \t" + request + "\n");

		for (Entry<String, String> s : headers.entrySet()) {
			request.setHeader(s.getKey(), s.getValue());
		}

		HttpResponse response = client.execute(request);

		 //Main.logger.trace("Response is:" + response + "\n");

		InputStream ips = response.getEntity().getContent();
		BufferedReader buf = new BufferedReader(
				new InputStreamReader(ips, "UTF-8"));

		StringBuilder sb = new StringBuilder();
		String s;
		while (true) {
			s = buf.readLine();
			if (s == null || s.length() == 0)
				break;
			sb.append(s);
		}
		buf.close();
		ips.close();
		return sb.toString();

	}



	public static String getStringContent(String uri, String postData,
			HashMap<String, String> headers) throws Exception {

		HttpPost request = new HttpPost(uri);

		//Main.logger.trace("postdata is: \t"+postData);

		request.setEntity(new StringEntity(
				ClientUtils.escapeQueryChars(postData), "UTF-8"));

		// Main.logger.trace("Request is:" + request + "\n");

		for (Entry<String, String> s : headers.entrySet()) {
			request.setHeader(s.getKey(), s.getValue());
		}

		HttpResponse response = client.execute(request);

		// Main.logger.trace("Response is:" + response + "\n");

		InputStream ips = response.getEntity().getContent();
		BufferedReader buf = new BufferedReader(
				new InputStreamReader(ips, "UTF-8"));

		StringBuilder sb = new StringBuilder();
		String s;
		while (true) {
			s = buf.readLine();
			if (s == null || s.length() == 0)
				break;
			sb.append(s);
		}
		buf.close();
		ips.close();
		return sb.toString();

	}

	public static TagResponse parse(String jsonLine) {

		//Main.logger.trace("Json Response is:" + jsonLine.toString() + "\n");

		Map<String, List<Position>> positions = new HashMap<String, List<Position>>();
		TagResponse result = new TagResponse();

		try{
		JsonElement jelement;
		jelement = new JsonParser().parse(jsonLine.trim());
		JsonObject jobject = jelement.getAsJsonObject();
		JsonArray tags = jobject.getAsJsonArray("tags");

		for (JsonElement tagElt : tags) {
			JsonArray tag = tagElt.getAsJsonArray();
			Integer aStartOffset = null, aEndOffset = null;
			String aMatchText = null;
			for (int i = 0; i < tag.size(); i += 2) {
				switch (tag.get(i).getAsString()) {
					case "matchText" :
						aMatchText = tag.get(i + 1).getAsString();
						break;
					case "startOffset" :
						aStartOffset = tag.get(i + 1).getAsInt();
						break;
					case "endOffset" :
						aEndOffset = tag.get(i + 1).getAsInt();
						break;
					case "ids" :
						for (JsonElement id : tag.get(i + 1).getAsJsonArray()) {
							String uuid = id.getAsString();
							if (!positions.containsKey(uuid)) {
								positions.put(uuid, new ArrayList<Position>());
							}
							positions.get(uuid).add(new Position(aMatchText,
									aStartOffset, aEndOffset));
						}
						break;
				}
			}
		}

		JsonObject response = jobject.getAsJsonObject()
				.getAsJsonObject("response");
		for (JsonElement doc : response.getAsJsonArray("docs")) {
			String uuid = doc.getAsJsonObject().get("uuid").getAsString();
			for (Position position : positions.get(uuid)) {
				TagItem item = new TagItem();
				item.setUuid(uuid);
				item.setIcd10(doc.getAsJsonObject().get("code").getAsString());
				item.setTerm(doc.getAsJsonObject().get("term").getAsString());
				item.setPrefTerm(
						doc.getAsJsonObject().get("prefterm").getAsString());
				item.setStart(position.getStart());
				item.setEnd(position.getEnd());
				item.setMatchText(position.getMatchText());
				result.add(item);
			}
		}
		return result;
	}catch(NullPointerException e){
		return result;
	}
	}
}
