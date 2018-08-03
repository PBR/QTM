/**
 * @author gurnoor
 * Artilce class
 */

package qtm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Article {
	private String title;
	private String pmcId;
	private String doi;
	private String fileName;
	private String xml;
	private String plainText;
	private Table[] tables;
	private int numQtlTables;
	private HashMap<String, String> abbreviations;
	private List<Trait> traits = new ArrayList<Trait>();

	public List<Trait> getTrait() {
		return traits;
	}

	public void setTrait(List<Trait> traits) {
		this.traits = traits;
	}

	public HashMap<String, String> getAbbreviations() {
		return abbreviations;
	}

	public void setAbbreviations(HashMap<String, String> h) {
		this.abbreviations = h;
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

	// Constructors
	public Article(String fileName) {
		setFileName(fileName);
	}

	public String getTitle() {
		return title;
	}

	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public String getPlainText() {
		return plainText;
	}

	public void setPlainText(String plainText) {
		this.plainText = plainText;
	}

	public Table[] getTables() {
		return tables;
	}

	public void setTables(Table[] tables) {
		this.tables = tables;
	}

	public String getPmcId() {
		return pmcId;
	}

	public void setPmcId(String pmcId) {
		this.pmcId = pmcId;
	}
}