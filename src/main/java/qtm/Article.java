/**
 * @author gurnoor
 * Artilce class
 */

package qtm;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import utils.Configs;

public class Article {
	private String title;
	private String pmcId;
	private String doi;
	//private String fileName;
	private String xml;
	private String plainText;
	private Table[] tables;
	private int numQtlTables;
	private HashMap<String, String> abbreviations;
	private List<Trait> traits = new ArrayList<Trait>();

	public static String fileName(String pmcId) {
		return pmcId + ".xml";
	}

	public Article(String pmcId) {
		this.pmcId = pmcId;
	}

	public List<Trait> getTrait() {
		return traits;
	}

	public void setTrait(List<Trait> traits) {
		this.traits = traits;
	}

	public HashMap<String, String> getAbbreviations() {
		return abbreviations;
	}

	public void setAbbreviations(HashMap<String, String> abbreviations) {
		this.abbreviations = abbreviations;
	}

	public int getNumQtlTables() {
		return numQtlTables;
	}

	public void setNumQtlTables() {
		int i = 0;
		for (Table t : this.tables) {
			try {
				if (t.getisTraitTable() == true) {
					i++;
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
		this.numQtlTables = i;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public String getDoi() {
		return doi;
	}

	/*
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}
	*/

	public void setXml(String xml) {
		this.xml = xml;
	}

	public String getXml() {
		return xml;
	}

	public void setPlainText(String plainText) {
		this.plainText = plainText;
	}

	public String getPlainText() {
		return plainText;
	}

	public void setTables(Table[] tables) {
		this.tables = tables;
	}

	public Table[] getTables() {
		return tables;
	}

	public void setPmcId(String pmcId) {
		this.pmcId = pmcId;
	}

	public String getPmcId() {
		return pmcId;
	}

	public void download() {
		String fileName = fileName(pmcId);
		String api = Configs.getPropertyQTM("webAPI");
		File file = new File(fileName);
		api = api.replace("<PMC_ID>", pmcId);
		
		try {
			URL url = new URL(api);
			try {
				if (file.exists() == false || file.length() == 0) {
					System.out.format("Downloading article '%s' in XML...\t", pmcId);
					FileUtils.copyURLToFile(url, file);
					System.out.println("Done.");
				}
			} catch (IOException e) {
				System.err.println("Failed.\n" + e.getMessage());
				System.exit(1);
			}
		} catch (Exception e) {
			System.err.println("Invalid URL for web API: " + api);
			System.exit(1);
		}
	}
}