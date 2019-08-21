/**
 *
 * @author gurnoor
 * PMCXMLReader class is used to read and parse XML data from PubMed Central
 * database The class takes as input folder with XML documents extracted from
 * PMC database and creates array of Articles {@link Article} as output
 */

package nl.esciencecenter.readers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import nl.esciencecenter.abbreviation.Abbreviator;
import nl.esciencecenter.qtm.Article;
import nl.esciencecenter.qtm.Main;
import nl.esciencecenter.qtm.Table;
import nl.esciencecenter.utils.Author;
import nl.esciencecenter.utils.Configs;
import nl.esciencecenter.utils.Utilities;

public class PmcMetaReader {

	private String fileName;

	private String pmcId;

	private Abbreviator abbreviator;

	private File fh;

	public PmcMetaReader(File fh) {
		super();
		this.fh = fh;
		this.fileName = fh.getPath();
	}

	public PmcMetaReader(String fileName) {
		super();
		this.fileName = fileName;
	}

	public String getPmcId() {
		return pmcId;
	}

	public void setPmcId(String pmcId) {
		this.pmcId = pmcId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void init(String fileName) {
		setFileName(fileName);
	}

	/**
	 * This method is the main method for reading PMC XML files. It uses
	 * {@link #ParseMetaData} and {@link #ParseTables} methods. It returns
	 * {@link Article} object that contains structured data from article, including
	 * tables.
	 *
	 * @return Article
	 */

	public Article read() {
		Article art = new Article(fileName);

		try {
			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = null;
			String xmlString = "";

			while ((line = reader.readLine()) != null) {
				if (line.contains("!DOCTYPE article"))
					continue;
				xmlString += line + '\n';
			}
			// Main.logger.trace(xmlString);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			factory.setNamespaceAware(true);
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlString));
			Document parse = builder.parse(is);

			// MetaData
			art = this.parseMetaData(art, parse, xmlString);

			// Ful-Text
			art = parsePlainText(art, parse, xmlString);
			// Main.logger.trace(art.getPlain_text());

			// abbreviations
			this.abbreviator = new Abbreviator();
			HashMap<String, String> abbrevs = new HashMap<String, String>();
			abbrevs = abbreviator.getAbbrevPairs(art.getPlainText());

			art.setAbbreviations(abbrevs);
			Main.logger.debug("List abbreviation->expansion pairs:");
			for (String abbrev : art.getAbbreviations().keySet()) {
				String expansion = art.getAbbreviations().get(abbrev);
				Main.logger.debug(" " + abbrev + "\t->\t" + expansion);
			}

			// Tables
			Main.logger.debug("Parsing tables...");

			art = TableParser.parseTables(art, parse);

		} catch (Exception e) {
			Main.logger.error("Problem in processing XML: ", e);
		}

		art.setNumQtlTables();

		Main.logger.debug("Number of QTL tables: " + art.countQtlTables());
		if (art.countQtlTables() > 0) {
			for (Table t : art.getTables()) {
				try {
					if (t.getisTraitTable() == true) {
						Main.logger.debug(t.getTabnum() + "\t\t" + t.getisTraitTable());
					}
				} catch (NullPointerException e) {
					Main.logger.error(e);
				}
			}
		}
		return art;
	}

	/**
	 * Gets the article keywords.
	 *
	 * @param parse
	 *            the parse
	 * @return the keywords
	 */
	public String[] getKeywords(Document parse) {
		NodeList keywords = parse.getElementsByTagName("kwd");
		String[] keywords_str = new String[keywords.getLength()];
		for (int j = 0; j < keywords.getLength(); j++) {
			if (keywords.item(j).getTextContent().length() > 1) {
				String keyword = keywords.item(j).getTextContent().substring(1);
				keywords_str[j] = keyword;
			}
		}
		return keywords_str;
	}

	/**
	 * Reads metadata from article such as title, authors, publication type etc
	 *
	 * @param art
	 *            - Article where to put data
	 * @param parse
	 *            - Document of XML
	 * @param xmlString
	 *            - XML code
	 * @return Article - populated art
	 */
	public Article parseMetaData(Article art, Document parse, String xml) {
		String title = "";

		if (parse.getElementsByTagName("article-title") != null
				&& parse.getElementsByTagName("article-title").item(0) != null) {
			title = parse.getElementsByTagName("article-title").item(0).getTextContent();
			title = title.replaceAll("\n", "");
		}

		NodeList articleId = parse.getElementsByTagName("article-id");
		for (int j = 0; j < articleId.getLength(); j++) {
			if (articleId.item(j).getAttributes() != null
					&& articleId.item(j).getAttributes().getNamedItem("pub-id-type") != null
					&& articleId.item(j).getAttributes().getNamedItem("pub-id-type").getNodeValue().equals("pmcid")) {
				String pmcid = "PMC" + articleId.item(j).getTextContent();
				art.setPmcid(pmcid);
			}
			if (articleId.item(j).getAttributes() != null
					&& articleId.item(j).getAttributes().getNamedItem("pub-id-type") != null
					&& articleId.item(j).getAttributes().getNamedItem("pub-id-type").getNodeValue().equals("doi")) {
				art.setDoi(articleId.item(j).getTextContent());
			}
		}

		NodeList artAbstract = parse.getElementsByTagName("abstract");

		for (int j = 0; j < artAbstract.getLength(); j++) {
			if (artAbstract.item(j).getAttributes().getNamedItem("abstract-type") != null && artAbstract.item(j)
					.getAttributes().getNamedItem("abstract-type").getNodeValue().equals("short")) {
				art.setAbstract(artAbstract.item(j).getTextContent());
			}
		}

		String[] keywords_str = getKeywords(parse);
		art.setKeywords(keywords_str);
		art.setTitle(title);
		art.setXml(xml);
		return art;
	}

	/**
	 * Reads Full-text from article
	 *
	 * @param art
	 *            - Article where to put data
	 * @param parse
	 *            - Document of XML
	 * @param xmlString
	 *            - XML code
	 * @return Article - populated art
	 */

	public Article parsePlainText(Article art, Document parse, String xmlString) {

		String content = "";
		// String tmpDir = Configs.getPropertyQTM("textFiles");
		String txtFile = art.getPmcid() + ".txt";

		if (parse.getElementsByTagName("article-title").item(0) != null) {
			content += "[TITLE] " + parse.getElementsByTagName("article-title").item(0).getTextContent() + "\n\n";
		}

		if (parse.getElementsByTagName("abstract").item(0) != null) {
			content += "[Abstract] " + parse.getElementsByTagName("abstract").item(0).getTextContent() + "\n\n";
		}

		if (parse.getElementsByTagName("body").item(0) != null) {
			content += "[MainText] " + parse.getElementsByTagName("body").item(0).getTextContent() + "\n";
		}

		art.setPlainText(content);

		// write text into PMC*.txt file
		try (FileWriter writer = new FileWriter(txtFile)) {
			writer.write(art.getPlainText());
		} catch (IOException e) {
			Main.logger.error(e);
		}
		return art;
	}

	/**
	 * Gets the children by tag name.
	 *
	 * @param parent
	 *            the parent
	 * @param name
	 *            the name
	 * @return the children by tag name
	 */
	public static List<Node> getChildrenByTagName(Node parent, String name) {
		List<Node> nodeList = new ArrayList<Node>();
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE && name.equals(child.getNodeName())) {
				nodeList.add(child);
			}
		}
		return nodeList;
	}

	public static File pmcDowloadXml(String pmcId) throws IOException, MalformedURLException {

		File xmlfile = new File(pmcId + ".xml");

		try {
			if (!xmlfile.exists() || xmlfile.length() == 0) {
				xmlfile.createNewFile();
				String pmcWebserviceUrl = Configs.getPropertyQTM("epmcWebService") + pmcId + "/fullTextXML";
				URL url = new URL(pmcWebserviceUrl);
				FileUtils.copyURLToFile(url, xmlfile);
			}
			return xmlfile;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmlfile;
	}
}
