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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

	private Abbreviator abbreviator;

	private String pmcId;

	private File f1;

	public PmcMetaReader(File F1) {
		super();
		this.f1 = F1;
		Main.logger.debug(F1.getPath());
		this.fileName = F1.getPath();
		Main.logger.debug(fileName);
	}

	public PmcMetaReader(String fName) {
		super();
		this.fileName = fName;
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

	public void setFileName(String fName) {
		this.fileName = fName;
	}

	public void init(String file_name) {
		setFileName(file_name);
	}

	/**
	 * This method is the main method for reading PMC XML files. It uses
	 * {@link #ParseMetaData} and {@link #ParseTables} methods. It returns
	 * {@link Article} object that contains structured data from article,
	 * including tables.
	 *
	 * @return Article
	 */

	public Article read() {
		Article art = new Article(fileName);
		art.setSource("PMC");

		try {
			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(
					new FileReader(fileName));
			String line = null;
			String xmlString = "";

			while ((line = reader.readLine()) != null) {
				if (line.contains("!DOCTYPE article"))
					continue;
				xmlString += line + '\n';
			}
			// Main.logger.trace(xmlString);

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();

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
			HashMap<String, String> abbreviationsFound = new HashMap<String, String>();

			abbreviationsFound = abbreviator
					.extractAbbrPairs(art.getPlain_text());

			art.setAbbreviations(abbreviationsFound);

			Main.logger.debug("\nList of abbreviations in " + art.getPmc());
			for (String key : art.getAbbreviations().keySet()) {
				Main.logger.debug(
						key + "\t->\t" + art.getAbbreviations().get(key));

			}
			// Tables
			Main.logger.debug("\n");
			Main.logger.debug("Parsing tables in " + art.getPmc() + "now");

			art = TableParser.parseTables(art, parse);

		} catch (Exception ex) {
			ex.printStackTrace();
			Main.logger.error("Problem in processing xml file");
		}

		art.setNumQTLtables();

		Main.logger.debug("NUMBER OF QTL TABLES:" + art.getNumQTLtables());

		if (art.getNumQTLtables() > 0) {
			for (Table t : art.getTables()) {
				try {

					if (t.getisTraitTable() == true) {
						Main.logger.debug(t.getTabnum() + "\t\t" + t.getisTraitTable());
					}
				} catch (NullPointerException e) {

				}
			}
		}

		return art;
	}

	/**
	 * Gets the list of authors.
	 *
	 * @param parse
	 *            the parse
	 * @return the string[]
	 */

	public LinkedList<Author> getAuthors(Document parse) {
		LinkedList<Author> auths = new LinkedList<Author>();
		NodeList authors = parse.getElementsByTagName("contrib");
		for (int j = 0; j < authors.getLength(); j++) {
			Author auth = new Author();
			String givenName = "";
			String surname = "";
			String email = "";

			for (int k = 0; k < authors.item(j).getChildNodes()
					.getLength(); k++) {
				if (authors.item(j).getChildNodes().item(k)
						.getNodeName() == "name") {
					NodeList name = authors.item(j).getChildNodes().item(k)
							.getChildNodes();
					if (name.item(1) != null)
						surname = Utilities.getString(name.item(0));
					if (name.item(1) != null)
						givenName = Utilities.getString(name.item(1));
					auth.name = surname + ", " + givenName;
				}
				if (authors.item(j).getChildNodes().item(k)
						.getNodeName() == "email") {
					NodeList name = authors.item(j).getChildNodes().item(k)
							.getChildNodes();
					if (name.item(0) != null)
						email = Utilities.getString(name.item(0));
					auth.email = email;
				}

				if (authors.item(j).getChildNodes().item(k)
						.getNodeName() == "xref") {
					Node name = authors.item(j).getChildNodes().item(k);
					NamedNodeMap attr = name.getAttributes();
					if (null != attr) {
						Node p = attr.getNamedItem("ref-type");
						if (p.getNodeValue() == "aff")
							;
						Node p2 = attr.getNamedItem("rid");
						String affId = p2.getNodeValue();
						NodeList affis = parse.getElementsByTagName("aff");
						for (int s = 0; s < affis.getLength(); s++) {
							if (affis.item(s).getAttributes() != null
									&& affis.item(s).getAttributes()
											.getNamedItem("id") != null
									&& affis.item(s).getAttributes()
											.getNamedItem("id").getNodeValue()
											.equals(affId)) {
								String affName = Utilities
										.getString(affis.item(s));
								if (affName.contains("1")
										|| affName.contains("2")
										|| affName.contains("3")
										|| affName.contains("4")
										|| affName.contains("5"))
									affName = affName.substring(1);
								auth.affiliation.add(affName);
							}
						}
					}
				}

			}
			auths.add(auth);
		}
		return auths;
	}

	/**
	 * Gets the affiliations of authors.
	 *
	 * @param parse
	 *            the parse
	 * @return the string[]
	 */
	public String[] getAffiliations(Document parse) {
		NodeList affis = parse.getElementsByTagName("aff");
		String[] affilis = new String[affis.getLength()];
		for (int j = 0; j < affis.getLength(); j++) {
			String affiliation = Utilities.getString(affis.item(j));
			affilis[j] = affiliation;
			// Main.logger.debug("Affiliation:" + affiliation);
		}

		return affilis;
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
				// Main.logger.debug("Keyword:" + keyword);
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
		String journal = "";

		if (parse.getElementsByTagName("article-title") != null && parse
				.getElementsByTagName("article-title").item(0) != null) {
			title = parse.getElementsByTagName("article-title").item(0)
					.getTextContent();
			title = title.replaceAll("\n", "");
			title = title.replaceAll("\t", "");
			Main.logger.debug("Titel of the Article: " + title);
		}

		// Authors List
		LinkedList<Author> auths = getAuthors(parse);
		for (int j = 0; j < auths.size(); j++) {
			// Main.logger.debug(auths.get(j));
		}

		// journal-title
		if (parse.getElementsByTagName("journal-title") != null && parse
				.getElementsByTagName("journal-title").item(0) != null) {
			journal = parse.getElementsByTagName("journal-title").item(0)
					.getTextContent();
			journal = journal.replaceAll("\n", "");
			journal = journal.replaceAll("\t", "");
		}

		NodeList issn = parse.getElementsByTagName("issn");
		for (int j = 0; j < issn.getLength(); j++) {
			if (issn == null || issn.item(j) == null
					|| issn.item(j).getAttributes() == null
					|| issn.item(j).getAttributes()
							.getNamedItem("pub-type") == null
					|| issn.item(j).getAttributes().getNamedItem("pub-type")
							.getNodeValue() == null)
				continue;
			if (issn.item(j).getAttributes().getNamedItem("pub-type")
					.getNodeValue().equals("ppub")) {
				String issnp = issn.item(j).getTextContent();
				art.setPissn(issnp);
				if (issnp != null) {
					// Main.logger.debug(issnp);
				}
			}
			if (issn.item(j).getAttributes().getNamedItem("pub-type")
					.getNodeValue().equals("epub")) {
				String issne = issn.item(j).getTextContent();
				art.setPissn(issne);
				if (issne != null) {
					// Main.logger.debug(issne);
				}
			}
		}

		NodeList article_id = parse.getElementsByTagName("article-id");

		for (int j = 0; j < article_id.getLength(); j++) {
			if (article_id.item(j).getAttributes() != null
					&& article_id.item(j).getAttributes()
							.getNamedItem("pub-id-type") != null
					&& article_id.item(j).getAttributes()
							.getNamedItem("pub-id-type").getNodeValue()
							.equals("pmid")) {
				String pmid = article_id.item(j).getTextContent();
				art.setPmid(pmid);
				if (pmid != null) {
					// Main.logger.debug(pmid);
				}
			}
			if (article_id.item(j).getAttributes() != null
					&& article_id.item(j).getAttributes()
							.getNamedItem("pub-id-type") != null
					&& article_id.item(j).getAttributes()
							.getNamedItem("pub-id-type").getNodeValue()
							.equals("pmcid")) {
				String pmc = "PMC" + article_id.item(j).getTextContent();
				art.setPmc(pmc);
				art.setSpec_id(pmc);
				if (pmc != null) {
					Main.logger.debug("PMC id: " + pmc);
				}
			}
			if (article_id.item(j).getAttributes() != null
					&& article_id.item(j).getAttributes()
							.getNamedItem("pub-id-type") != null
					&& article_id.item(j).getAttributes()
							.getNamedItem("pub-id-type").getNodeValue()
							.equals("doi")) {
				art.setDoi(article_id.item(j).getTextContent());
			}
		}

		String[] affilis = getAffiliations(parse);

		art.setAffiliation(affilis);

		NodeList art_abstract = parse.getElementsByTagName("abstract");

		for (int j = 0; j < art_abstract.getLength(); j++) {
			if (art_abstract.item(j).getAttributes()
					.getNamedItem("abstract-type") != null
					&& art_abstract.item(j).getAttributes()
							.getNamedItem("abstract-type").getNodeValue()
							.equals("short")) {
				art.setShort_abstract(art_abstract.item(j).getTextContent());
			} else {
				art.setAbstract(art_abstract.item(j).getTextContent());
			}
		}

		String[] keywords_str = getKeywords(parse);
		art.setKeywords(keywords_str);

		String publisher_name = "";
		if (parse.getElementsByTagName("publisher-name").item(0) != null)
			publisher_name = parse.getElementsByTagName("publisher-name")
					.item(0).getTextContent();
		art.setPublisher_name(publisher_name);
		if (publisher_name != null)
			Main.logger.debug("Publisher: " + publisher_name);
		String publisher_loc = "";
		if (parse.getElementsByTagName("publisher-loc").item(0) != null)
			publisher_loc = parse.getElementsByTagName("publisher-loc").item(0)
					.getTextContent();
		art.setPublisher_loc(publisher_loc);
		if (publisher_loc != null)
			// Main.logger.debug(publisher_loc);

			art.setTitle(title);
		art.setXML(xml);
		art.setAuthors(auths);
		art.setJournal_name(journal);
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

	public Article parsePlainText(Article art, Document parse,
			String xmlString) {

		String text = "";

		if (parse.getElementsByTagName("article-title").item(0) != null) {
			text += "\n\n\n[TITLE]"
					+ parse.getElementsByTagName("article-title").item(0)
							.getTextContent()
					+ "\n\n";
		}

		if (parse.getElementsByTagName("abstract").item(0) != null) {
			text += "\n\n\n[Abstract]" + parse.getElementsByTagName("abstract")
					.item(0).getTextContent() + "\n";
		}

		if (parse.getElementsByTagName("body").item(0) != null) {
			text += "\n\n\n[MainText]" + parse.getElementsByTagName("body")
					.item(0).getTextContent() + "\n";

		}

		art.setPlain_text(text);

		try {

			String textFiles = Configs.getPropertyQTM("textFiles");

			Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(textFiles + art.getPmc() + ".txt"),
					"utf-8"));
			writer.write(text);
			writer.close();
		} catch (Exception e) {
			e.getMessage();
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
		for (Node child = parent.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE
					&& name.equals(child.getNodeName())) {
				nodeList.add(child);
			}
		}

		return nodeList;
	}

	public static File pmcDowloadXml(String pmcId)
			throws IOException, MalformedURLException {

		File xmlfile = new File(pmcId + ".xml");

		try{
			if (!xmlfile.exists() || xmlfile.length() == 0) {
				xmlfile.createNewFile();

				String pmcWebserviceUrl = Configs.getPropertyQTM(
						"epmcWebService") + pmcId + "/fullTextXML";

				URL url = new URL(pmcWebserviceUrl);
				FileUtils.copyURLToFile(url, xmlfile);

			}
			return xmlfile;
		} catch (Exception e) {

			e.printStackTrace();

		}
		return xmlfile;
	}}
