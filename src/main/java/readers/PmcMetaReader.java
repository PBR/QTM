/**
 *
 * @author gurnoor
 * PMCXMLReader class is used to read and parse XML data from PubMed Central
 * database The class takes as input folder with XML documents extracted from
 * PMC database and creates array of Articles {@link Article} as output
 */

package readers;

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

import abbreviation.Abbreviator;
import qtm.Article;
import qtm.Table;
import utils.Configs;
import utils.Utilities;

public class PmcMetaReader {

	private File file;

	private String fileName;

	private String pmcId;	
	
	private Abbreviator abbreviator;

	public PmcMetaReader(File file) {
		this.file = file;
		this.fileName = file.getPath();
	}

	public String getPmcId() {
		return pmcId;
	}

	public void setPmcId(String pmcId) {
		this.pmcId = pmcId;
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
		try {
			@SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = null;
			String xmlString = "";

			while ((line = reader.readLine()) != null) {
				if (line.contains("!DOCTYPE article")) {
					continue;
				}
				xmlString += line + '\n';
			}

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource src = new InputSource(new StringReader(xmlString));
			Document doc = builder.parse(src);

			// MetaData
			art = parseMetaData(art, doc, xmlString);

			// Ful-Text
			art = parsePlainText(art, doc, xmlString);

			// abbreviations
			this.abbreviator = new Abbreviator();
			HashMap<String, String> abbreviationsFound = new HashMap<String, String>();
			abbreviationsFound = abbreviator.extractAbbrPairs(art.getPlainText());
			art.setAbbreviations(abbreviationsFound);

			System.out.println("\nList of abbreviations in " + art.getPmcId());
			for (String key : art.getAbbreviations().keySet()) {
				System.out.println(key + "\t->\t" + art.getAbbreviations().get(key));
			}
			// Tables
			System.out.println("Parsing tables in " + art.getPmcId());
			art = TableParser.parseTables(art, doc);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Problem in processing xml file");
		}

		art.setNumQtlTables();
		System.out.println("Number of QTL tables:" + art.getNumQtlTables());

		if (art.getNumQtlTables() > 0) {
			for (Table t : art.getTables()) {
				try {
					if (t.getisTraitTable() == true) {
						System.out.println(t.getTabNum() + "\t\t" + t.getisTraitTable());
					}
				} catch (NullPointerException e) {
					e.printStackTrace();
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
	public Article parseMetaData(Article art, Document doc, String xml) {
		String title = "";
		
		if (doc.getElementsByTagName("article-title") != null && doc
				.getElementsByTagName("article-title").item(0) != null) {
			title = doc.getElementsByTagName("article-title").item(0)
				.getTextContent();
			title = title.replaceAll("\\s+", "");
			System.out.println("Title:\t" + title);
		}

		NodeList articleId = doc.getElementsByTagName("article-id");
		for (int j = 0; j < articleId.getLength(); j++) {
			if (articleId.item(j).getAttributes() != null
					&& articleId.item(j).getAttributes()
						.getNamedItem("pub-id-type") != null
					&& articleId.item(j).getAttributes()
						.getNamedItem("pub-id-type").getNodeValue().equals("pmcid")) {
				String pmcId = "PMC" + articleId.item(j).getTextContent();
				art.setPmcId(pmcId);
			}
			if (articleId.item(j).getAttributes() != null
				&& articleId.item(j).getAttributes()
					.getNamedItem("pub-id-type") != null
				&& articleId.item(j).getAttributes()
					.getNamedItem("pub-id-type").getNodeValue().equals("doi")) {
				art.setDoi(articleId.item(j).getTextContent());
			}
		}
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

	public Article parsePlainText(Article art, Document doc, String xml) {
		String text = "";

		if (doc.getElementsByTagName("article-title").item(0) != null) {
			text += "[TITLE]" + doc.getElementsByTagName("article-title")
				.item(0).getTextContent() + "\n";
		}

		if (doc.getElementsByTagName("abstract").item(0) != null) {
			text += "[Abstract]" + doc.getElementsByTagName("abstract")
				.item(0).getTextContent() + "\n";
		}

		if (doc.getElementsByTagName("body").item(0) != null) {
			text += "[MainText]" + doc.getElementsByTagName("body")
				.item(0).getTextContent() + "\n";
		}

		art.setPlainText(text);
		try {
			String txtFiles = Configs.getPropertyQTM("txtFiles");
			Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(txtFiles + art.getPmcId() + ".txt"), "utf-8"));
			writer.write(text);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
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
		File xmlFile = new File(pmcId + ".xml");
		try{
			if (!xmlFile.exists() || xmlFile.length() == 0) {
				URL url = new URL(Configs.getPropertyQTM("webAPI") + pmcId
					+ "/fullTextXML");
				xmlFile.createNewFile();
				FileUtils.copyURLToFile(url, xmlFile);
			}
			return xmlFile;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return xmlFile;
	}
}
