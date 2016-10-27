/**
 *
 * @author gurnoor
 */

package readers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import abbrviation.AbbrevExpander;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import readers.Reader;
import tablInEx.*;
import utils.Author;
import utils.Utilities;

/**
 * PMCXMLReader class is used to read and parse XML data from PubMed Central
 * database The class takes as input folder with XML documents extracted from
 * PMC database and creates array of Articles {@link Article} as output
 * 
 * @author Nikola Milosevic
 */
public class PmcMetaReader implements Reader {

	private String FileName;

	private AbbrevExpander abbreviator;
	
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

	public Article Read() {
		Article art = new Article(FileName);
		art.setSource("PMC");
		try {
			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new FileReader(FileName));
			String line = null;
			String xmlString = "";
			while ((line = reader.readLine()) != null) {
				if (line.contains("JATS-archivearticle1.dtd") || line.contains("archivearticle.dtd"))
					continue;
				xmlString += line + '\n';
			}
			// System.out.println(xml);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			factory.setNamespaceAware(true);
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlString));
			Document parse = builder.parse(is);

			//MetaData
			art = ParseMetaData(art, parse, xmlString);
			
			//Ful-Text
			art = ParsePlainText(art, parse, xmlString);
			
			//abbreviations
			char deliminator='\t';
			abbreviator=new AbbrevExpander();
			art.setAbbreviations(abbreviator.extractAbbrPairs(art.getPlain_text()));
			
			System.out.println("\n$$$$$Abbreviations$$$$$$$$");
			for(String key: art.getAbbreviations().keySet()){
				System.out.println(key+deliminator+art.getAbbreviations().get(key));						
				
			}
			System.out.println("\n\n");
			
			
			
			//Tables			
			art = TableParser.ParseTables(art, parse);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		art.setNumQTLtables();
		System.out.println("NUMBER OF QTL TABLESSS:" + art.getNumQTLtables());
		return art;
	}

	public String getFileName() {
		return FileName;
	}

	public void setFileName(String fileName) {
		FileName = fileName;
	}

	/**
	 * Gets the list of authors.
	 *
	 * @param parse
	 *            the parse
	 * @return the string[]
	 */

	
	public LinkedList<Author> GetAuthors(Document parse) {
		LinkedList<Author> auths = new LinkedList<Author>();
		NodeList authors = parse.getElementsByTagName("contrib");
		for (int j = 0; j < authors.getLength(); j++) {
			Author auth = new Author();
			String givenName = "";
			String surname = "";
			String email = "";

			for (int k = 0; k < authors.item(j).getChildNodes().getLength(); k++) {
				if (authors.item(j).getChildNodes().item(k).getNodeName() == "name") {
					NodeList name = authors.item(j).getChildNodes().item(k).getChildNodes();
					if (name.item(1) != null)
						surname = Utilities.getString(name.item(0));
					if (name.item(1) != null)
						givenName = Utilities.getString(name.item(1));
					auth.name = surname + ", " + givenName;
				}
				if (authors.item(j).getChildNodes().item(k).getNodeName() == "email") {
					NodeList name = authors.item(j).getChildNodes().item(k).getChildNodes();
					if (name.item(0) != null)
						email = Utilities.getString(name.item(0));
					auth.email = email;
				}

				if (authors.item(j).getChildNodes().item(k).getNodeName() == "xref") {
					Node name = authors.item(j).getChildNodes().item(k);
					NamedNodeMap attr = name.getAttributes();
					if (null != attr) {
						Node p = attr.getNamedItem("ref-type");
						if (p.getNodeValue() == "aff")
							;
						Node p2 = attr.getNamedItem("rid");
						String affId = p2.getNodeValue();
						NodeList affis = parse.getElementsByTagName("aff");
						String[] affilis = new String[affis.getLength()];
						for (int s = 0; s < affis.getLength(); s++) {
							if (affis.item(s).getAttributes() != null
									&& affis.item(s).getAttributes().getNamedItem("id") != null
									&& affis.item(s).getAttributes().getNamedItem("id").getNodeValue().equals(affId)) {
								String affName = Utilities.getString(affis.item(s));
								if (affName.contains("1") || affName.contains("2") || affName.contains("3")
										|| affName.contains("4") || affName.contains("5"))
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
	public String[] GetAffiliations(Document parse) {
		NodeList affis = parse.getElementsByTagName("aff");
		String[] affilis = new String[affis.getLength()];
		for (int j = 0; j < affis.getLength(); j++) {
			String affiliation = Utilities.getString(affis.item(j));
			affilis[j] = affiliation;
			System.out.println("Affiliation:" + affiliation);
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
				String Keyword = keywords.item(j).getTextContent().substring(1);
				keywords_str[j] = Keyword;
				System.out.println("Keyword:" + Keyword);
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
	public Article ParseMetaData(Article art, Document parse, String xml) {
		String title = "";
		String journal = "";

		if (parse.getElementsByTagName("article-title") != null
				&& parse.getElementsByTagName("article-title").item(0) != null) {
			title = parse.getElementsByTagName("article-title").item(0).getTextContent();
			title = title.replaceAll("\n", "");
			title = title.replaceAll("\t", "");
			System.out.println(title);
		}

		// Authors List
		LinkedList<Author> auths = GetAuthors(parse);
		for (int j = 0; j < auths.size(); j++) {
			System.out.println(auths.get(j));
		}

		// journal-title
		if (parse.getElementsByTagName("journal-title") != null
				&& parse.getElementsByTagName("journal-title").item(0) != null) {
			journal = parse.getElementsByTagName("journal-title").item(0).getTextContent();
			journal = journal.replaceAll("\n", "");
			journal = journal.replaceAll("\t", "");
		}

		NodeList issn = parse.getElementsByTagName("issn");
		for (int j = 0; j < issn.getLength(); j++) {
			if (issn == null || issn.item(j) == null || issn.item(j).getAttributes() == null
					|| issn.item(j).getAttributes().getNamedItem("pub-type") == null
					|| issn.item(j).getAttributes().getNamedItem("pub-type").getNodeValue() == null)
				continue;
			if (issn.item(j).getAttributes().getNamedItem("pub-type").getNodeValue().equals("ppub")) {
				String issnp = issn.item(j).getTextContent();
				art.setPissn(issnp);
				if (issnp != null)
					System.out.println(issnp);
			}
			if (issn.item(j).getAttributes().getNamedItem("pub-type").getNodeValue().equals("epub")) {
				String issne = issn.item(j).getTextContent();
				art.setPissn(issne);
				if (issne != null)
					System.out.println(issne);
			}
		}

		NodeList article_id = parse.getElementsByTagName("article-id");

		for (int j = 0; j < article_id.getLength(); j++) {
			if (article_id.item(j).getAttributes() != null
					&& article_id.item(j).getAttributes().getNamedItem("pub-id-type") != null
					&& article_id.item(j).getAttributes().getNamedItem("pub-id-type").getNodeValue().equals("pmid")) {
				String pmid = article_id.item(j).getTextContent();
				art.setPmid(pmid);
				if (pmid != null)
					System.out.println(pmid);
			}
			if (article_id.item(j).getAttributes() != null
					&& article_id.item(j).getAttributes().getNamedItem("pub-id-type") != null
					&& article_id.item(j).getAttributes().getNamedItem("pub-id-type").getNodeValue().equals("pmcid")) {
				String pmc = "PMC" + article_id.item(j).getTextContent();
				art.setPmc(pmc);
				art.setSpec_id(pmc);
				if (pmc != null)
					System.out.println(pmc);
			}
		}

		String[] affilis = GetAffiliations(parse);

		art.setAffiliation(affilis);

		NodeList art_abstract = parse.getElementsByTagName("abstract");

		for (int j = 0; j < art_abstract.getLength(); j++) {
			if (art_abstract.item(j).getAttributes().getNamedItem("abstract-type") != null && art_abstract.item(j)
					.getAttributes().getNamedItem("abstract-type").getNodeValue().equals("short")) {
				art.setShort_abstract(art_abstract.item(j).getTextContent());
			} else {
				art.setAbstract(art_abstract.item(j).getTextContent());
			}
		}

		String[] keywords_str = getKeywords(parse);
		art.setKeywords(keywords_str);

		String publisher_name = "";
		if (parse.getElementsByTagName("publisher-name").item(0) != null)
			publisher_name = parse.getElementsByTagName("publisher-name").item(0).getTextContent();
		art.setPublisher_name(publisher_name);
		if (publisher_name != null)
			System.out.println(publisher_name);
		String publisher_loc = "";
		if (parse.getElementsByTagName("publisher-loc").item(0) != null)
			publisher_loc = parse.getElementsByTagName("publisher-loc").item(0).getTextContent();
		art.setPublisher_loc(publisher_loc);
		if (publisher_loc != null)
			System.out.println(publisher_loc);

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
	
	public Article ParsePlainText(Article art, Document parse, String xmlString) {

		String text = "";

		if (parse.getElementsByTagName("article-title").item(0) != null) {
			text += "\n\n\n[TITLE]" + parse.getElementsByTagName("article-title").item(0).getTextContent() + "\n\n";
		}

		if (parse.getElementsByTagName("abstract").item(0) != null) {
			text += "\n\n\n[Abstract]" + parse.getElementsByTagName("abstract").item(0).getTextContent() + "\n";
		}

		if (parse.getElementsByTagName("body").item(0) != null) {
			text += "\n\n\n[MainText]" + parse.getElementsByTagName("body").item(0).getTextContent() + "\n";

		}

		art.setPlain_text(text);

		try {
			
			Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("/home/gurnoor/workspace/XMLTAB/textFiles/" + art.getPmc() + ".txt"),
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
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE && name.equals(child.getNodeName())) {
				nodeList.add((Node) child);
			}
		}

		return nodeList;
	}

	public static File PmcDowloadXml(String PMCID) throws IOException, MalformedURLException {

		File xmlfile = new File("PMCfiles/" + PMCID + ".xml");

		if (!xmlfile.exists()) {
			xmlfile.createNewFile();
		}

		String API_PMCXML = "http://www.ebi.ac.uk/europepmc/webservices/rest/" + PMCID + "/fullTextXML";
		URL website = new URL(API_PMCXML);
		ReadableByteChannel rbc = Channels.newChannel(website.openStream());

		FileOutputStream fos = new FileOutputStream(xmlfile);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();

		return xmlfile;
	}

}
