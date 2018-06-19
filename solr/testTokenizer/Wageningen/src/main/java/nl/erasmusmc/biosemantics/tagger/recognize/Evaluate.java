package nl.erasmusmc.biosemantics.tagger.recognize;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import nl.erasmusmc.biosemantics.tagger.utils.Position;
import nl.erasmusmc.biosemantics.tagger.utils.TagItem;
import nl.erasmusmc.biosemantics.tagger.utils.TagResponse;

public class Evaluate {

	static Logger logger = LogManager.getLogger();
	static CloseableHttpClient client = HttpClients.createDefault();
	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
	static HashMap<String, String> headers = new HashMap<String, String>();
	static FileNameExtensionFilter filter = new FileNameExtensionFilter("text only", "txt");

	public static void main(String[] args) throws URISyntaxException {
		Options options = new Options().addOption("solr", true, "SOLR repository").addOption("core", true, "SOLR core")
				.addOption("directory", true, "directory with corpus texts")
				.addOption("outputfolder", true, "output folder for results file")
				.addOption("match", true, "match type: ALL, NO_SUB or LONGEST_DOMINANT_RIGHT (default)")
				.addOption("type", true, "optional file type").addOption("output", true, "optional output file");

		CommandLineParser parser = new DefaultParser();

		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args, false);
			headers.put("Content-Type", "text/plain ; charset=utf-8");

			if (line.getArgList().size() > 0) {
				throw new ParseException("unknown arguments");
			}
			
			String solr = line.hasOption("solr") ? line.getOptionValue("solr") : "http://localhost:8983/solr";
			String core = line.hasOption("core") ? line.getOptionValue("core") : null;
			String match = line.hasOption("match") ? line.getOptionValue("match") : "NO_SUB";
			String directory = line.hasOption("directory") ? line.getOptionValue("directory") : null;
			String outputfolder = line.hasOption("outputfolder") ? line.getOptionValue("outputfolder") : "data";
			String type = line.hasOption("type") ? line.getOptionValue("type") : "dictionary";
			
			String output = line.hasOption("output") ? line.getOptionValue("output")
					: generateFileName(outputfolder + "/ERtask_output_", core, match);

//			String output="solartaggedfile";
			
			
			if (solr != null && core != null && directory != null && type != null && match != null && output != null
					&& directory != null) {
				logger.info("output file = " + output);
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));
				out.write(StringUtils.join(
						new String[] { "SET", "FILE", "CODE", "MATCHTEXT", "PREFTERM", "TERM", "START", "END", "UUID" },
						"|"));
				out.newLine();
				process(out, solr, core, directory, match, type);
				out.close();
			} else {
				new HelpFormatter().printHelp(Evaluate.class.getCanonicalName(), options);
			}
		} catch (ParseException e) {
			System.err.println("Parsing failed.  Reason: " + e.getMessage());
			logger.error("Parsing failed.  Reason: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * this method generates a new output file name based on the various
	 * parameters
	 */
	private static String generateFileName(String file, String core, String match) {
		return FilenameUtils.removeExtension(file) + "_" + core + "_" + match + "_" + dateFormat.format(new Date())
				+ ".out";
	}

	
		
	
	private static void process(BufferedWriter out, String server, String core, String directory, String match,
			String type) throws IOException, URISyntaxException {
		File folder = new File(directory);
		File[] listOfFiles = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return !name.toLowerCase().startsWith(".");
			}
		});

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				processFile(out, server, core, listOfFiles[i], match, type);
			} else if (listOfFiles[i].isDirectory()) {
				process(out, server, core, listOfFiles[i].getAbsolutePath(), match, type);
			}
		}

	}

	// csv values will come in here
	private static void processFile(BufferedWriter out, String server, String core, File file, String match,
			String type) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));) {
			int offset = 0;
			String line = null;
			while ((line = br.readLine()) != null) {
				try {
					String request = "http://localhost:8983/solr/" + core + "/tag?fl=uuid,icd10,prefterm,term&overlaps="
							+ match + "&matchText=true&tagsLimit=5000&wt=json";
					TagResponse response = parse(getStringContent(request, line, headers));
					for (TagItem item : response.getItems()) {
						Integer start = item.getStart() + offset;
						Integer end = item.getEnd() + offset;
						out.write(StringUtils.join(new String[] { file.getParentFile().getName(), file.getName(),
								item.getIcd10(), item.getMatchText(), item.getPrefTerm(), item.getTerm(),
								start.toString(), end.toString(), item.getUuid() }, "|"));
						out.newLine();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				offset += line.length() + 1;
			}
			br.close();
		}
	}

	
	public static String getStringContent(String uri, String postData, HashMap<String, String> headers)
			throws Exception {
		HttpPost request = new HttpPost(uri);
		request.setEntity(new StringEntity(postData, "UTF-8"));
		for (Entry<String, String> s : headers.entrySet()) {
			request.setHeader(s.getKey(), s.getValue());
		}
		HttpResponse response = client.execute(request);

		InputStream ips = response.getEntity().getContent();
		BufferedReader buf = new BufferedReader(new InputStreamReader(ips, "UTF-8"));
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			throw new Exception(response.getStatusLine().getReasonPhrase());
		}
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
				case "matchText":
					aMatchText = tag.get(i + 1).getAsString();
					break;
				case "startOffset":
					aStartOffset = tag.get(i + 1).getAsInt();
					break;
				case "endOffset":
					aEndOffset = tag.get(i + 1).getAsInt();
					break;
				case "ids":
					for (JsonElement id : tag.get(i + 1).getAsJsonArray()) {
						String uuid = id.getAsString();
						if (!positions.containsKey(uuid)) {
							positions.put(uuid, new ArrayList<Position>());
						}
						positions.get(uuid).add(new Position(aMatchText, aStartOffset, aEndOffset));
					}
					break;
				}
			}
		}

		JsonObject response = jobject.getAsJsonObject().getAsJsonObject("response");
		for (JsonElement doc : response.getAsJsonArray("docs")) {
			String uuid = doc.getAsJsonObject().get("uuid").getAsString();
			for (Position position : positions.get(uuid)) {
				TagItem item = new TagItem();
				item.setUuid(uuid);
				item.setIcd10(doc.getAsJsonObject().get("icd10").getAsString());
				item.setTerm(doc.getAsJsonObject().get("term").getAsString());
				item.setPrefTerm(doc.getAsJsonObject().get("prefterm").getAsString());
				item.setStart(position.getStart());
				item.setEnd(position.getEnd());
				item.setMatchText(position.getMatchText());
				result.add(item);
			}
		}
		return result;
	}

}