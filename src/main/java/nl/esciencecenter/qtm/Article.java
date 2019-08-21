/**
 * @author gurnoor
 * Artilce class
 */

package nl.esciencecenter.qtm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Article {
	private String title;
	private String pmid;
	private String pmcid;
	private String doi;
	private String fileName;
	private String[] keywords;
	private String shortAbstract;
	private String xml;
	private String plainText;
	private Table[] tables;
	private boolean hasTraitTables;
	private int numQtlTables;
	private HashMap<String, String> abbrevs;
	private List<Trait> traits = new ArrayList<Trait>();

	public void setTraits(List<Trait> traits) {
		this.traits = traits;
	}

	public List<Trait> getTraits() {
		return traits;
	}

	public void setAbbreviations(HashMap<String, String> abbrevs) {
		this.abbrevs = abbrevs;
	}

	public HashMap<String, String> getAbbreviations() {
		return abbrevs;
	}

	public void setNumQtlTables() {
		int i = 0;
		for (Table t : this.tables) {
			try {
				if (t.getisTraitTable() == true) {
					i++;
				}
			} catch (NullPointerException e) {
				Main.logger.error(e);
			}
		}
		this.numQtlTables = i;
	}

	public int countQtlTables() {
		return numQtlTables;
	}

	// Constructors
	public Article(String fileName) {
		setFileName(fileName);
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

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

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

	public void setPmid(String pmid) {
		this.pmid = pmid;
	}

	public String getPmid() {
		return pmid;
	}

	public void setPmcid(String pmcid) {
		this.pmcid = pmcid;
	}

	public String getPmcid() {
		return pmcid;
	}

	public void setAbstract(String shortAbstract) {
		this.shortAbstract = shortAbstract;
	}

	public String getAbstract() {
		return shortAbstract;
	}

	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}

	public String[] getKeywords() {
		return keywords;
	}

	public void setHasTraitTables(boolean bool) {
		this.hasTraitTables = bool;
	}

	public boolean hasTraitTables() {
		return hasTraitTables;
	}
}
