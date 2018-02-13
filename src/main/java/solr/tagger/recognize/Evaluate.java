package solr.tagger.recognize;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.filechooser.FileNameExtensionFilter;

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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.util.ClientUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import solr.tagger.utils.Position;
import solr.tagger.utils.TagItem;
import solr.tagger.utils.TagResponse;

/**
 * @author gurnoor
 */
public class Evaluate {

	static Logger logger = org.apache.log4j.LogManager.getRootLogger();

	static CloseableHttpClient client = HttpClients.createDefault();
	static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd-HHmmss");
	static HashMap<String, String> headers = new HashMap<String, String>();
	static FileNameExtensionFilter filter = new FileNameExtensionFilter(
			"text only", "txt");

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
				throw new ParseException("unknown arguments");
			}

			String solr = line.hasOption("solr")
					? line.getOptionValue("solr")
					: "http://localhost:8983/solr";
			String core = line.hasOption("core")
					? line.getOptionValue("core")
					: "sgnMarkers";
			String match = line.hasOption("match")
					? line.getOptionValue("match")
					: "LONGEST_DOMINANT_RIGHT";
			String input = line.hasOption("input")
					? line.getOptionValue("input")
					: null;
			String outputfolder = line.hasOption("outputfolder")
					? line.getOptionValue("outputfolder")
					: "data/Resultdata";
			String type = line.hasOption("type")
					? line.getOptionValue("type")
					: "dictionary";

			String output = line.hasOption("output")
					? line.getOptionValue("output")
					: "TixdbSolarOutput";

			if (solr != null && core != null && type != null && match != null
					&& output != null) {
				// logger.info("output file = " + output);
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(output), "UTF-8"));
				out.write(StringUtils.join(
						new String[]{"SET", "FILE", "CODE", "MATCHTEXT",
								"PREFTERM", "TERM", "START", "END", "UUID"},
						"|"));
				out.newLine();

				processArray(out, input, core, match, type);

				// String in="solcap_snp_sl_100001";

				// processString(in, core, match, type);

				out.close();
			} else {
				new HelpFormatter().printHelp(Evaluate.class.getCanonicalName(),
						options);
			}

		} catch (ParseException e) {
			System.err.println("Parsing failed.  Reason: " + e.getMessage());
			// logger.error("Parsing failed. Reason: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * this method generates a new output file name based on the various
	 * parameters
	 */
	public static void processArray(BufferedWriter out, String input,
			String core, String match, String type)
			throws UnsupportedEncodingException, FileNotFoundException,
			IOException {
		// String []
		// line={"Solyc03g112630.2.1","Solyc03g112670.2.1","Solyc05g052410.1.1"};
		String[] line = {"solcap_snp_sl_100001", "J1", "TG194-TG523",
				"At4g10050", "cLEX-4-G10", "TG194-J1", "T0408", "TG194-J1",
				"TG194-TG523"};
		// String [] line={"Late blight", "Maturity", "Yield", "Fruit Shape",
		// "Fruit Quality"};
		// String [] line={"chromosome","abbreviation","Mean","H2","Variation",
		// "Regression results P-value","Regression results R2" };
		// String [] line={"Trait","Genotype","Chr.", "Effect under HL
		// a","Detection year under HL","Effect under LL"};
		int offset = 0;
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

				// System.out.println(request);

				TagResponse response = parse(
						getStringContent(request, line[i], headers));

				for (TagItem item : response.getItems()) {
					Integer start = item.getStart();
					Integer end = item.getEnd();
					out.write(StringUtils.join(new String[]{item.getIcd10(),
							item.getMatchText(), item.getPrefTerm(),
							item.getTerm(), start.toString(), end.toString(),
							item.getUuid()}, "|"));
					out.newLine();
				}

				System.out.println(response.toString());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static String processString(String input, String core, String match,
			String type) throws UnsupportedEncodingException,
			FileNotFoundException, IOException {
		logger.setLevel(Level.OFF);
		String output = "";
		try {
			// String request = "http://localhost:8983/solr/" + core +
			// "/tag?fl=uuid,code,prefterm,term&overlaps=" +
			// URLEncoder.encode(match, "UTF-8") +
			// "&matchText=true&tagsLimit=5000&wt=json";
			String request = "http://localhost:8983/solr/" + core
					+ "/tag?fl=uuid,code,prefterm,term&overlaps="
					+ URLEncoder.encode(match)
					+ "&matchText=true&tagsLimit=5000&wt=json";

			String content = getStringContent(request, input, headers);

			System.out.println("***" + content);

			TagResponse response = parse(content);

			for (TagItem item : response.getItems()) {
				Integer start = item.getStart();
				Integer end = item.getEnd();
				output = StringUtils.join(new String[]{item.getIcd10(),
						item.getMatchText(), item.getPrefTerm(), item.getTerm(),
						start.toString(), end.toString(), item.getUuid()}, "|");
				output += "\n";
			}

		} catch (Exception e) {
			e.printStackTrace();
			output = "";
			return output;
		}

		return output;
	}

	public static String processString2(String input, String core, String match,
			String type) throws UnsupportedEncodingException,
			FileNotFoundException, IOException {
		String res = "";
		try {
			String request = "http://localhost:8983/solr/" + core
					+ "/tag?fl=uuid,code,prefterm,term&overlaps="
					+ URLEncoder.encode(match)
					+ "&matchText=true&tagsLimit=5000&wt=json";
			res = getStringContent(request, input, headers);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return (res);
	}

	public static String getStringContent(String uri, String postData,
			HashMap<String, String> headers) throws Exception {
		HttpPost request = new HttpPost(uri);

		request.setEntity(new StringEntity(
				ClientUtils.escapeQueryChars(postData), "UTF-8"));

		for (Entry<String, String> s : headers.entrySet()) {
			request.setHeader(s.getKey(), s.getValue());
		}

		HttpResponse response = client.execute(request);

		InputStream ips = response.getEntity().getContent();
		BufferedReader buf = new BufferedReader(
				new InputStreamReader(ips, "UTF-8"));

		// if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
		// throw new Exception(response.getStatusLine().getReasonPhrase());
		//
		// }
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
		System.out.println(jsonLine);
		Map<String, List<Position>> positions = new HashMap<String, List<Position>>();
		TagResponse result = new TagResponse();
		JsonElement jelement = new JsonParser().parse(jsonLine);
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
	}

}
