/**
 *
 * @author gurnoor
 */

package tablInEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import utils.Author;


/**
 * 
 * The Class Article. Used for holding various data about crawled article and tables in it
 * @author Nikola Milosevic
 * 
 */
public class Article {
	private String title;
	private String venue;
	private String pmid;
	private String pmc;
	private String pissn;
	private String eissn;
	private String file_name;
	private LinkedList<Author> authors;
	private String[] affiliation;
	private String[] keywords;
	private String article_abstract;
	private String short_abstract;
	private String XML;
	private String plain_text;
	private Table[] tables;
	private String publisher_name;
	private String publisher_loc;
	private String journal_name;
	private String spec_id;
	private String source;
	private boolean hasTraitTables;
	private int numQTLtables;
	private HashMap<String, String> abbreviations;
	private List<Trait> traits = new ArrayList<Trait>();
	
	
	public List<Trait> getTraits() {
		return traits;
	}

	public void setTraits(List<Trait> traits) {
		this.traits = traits;
	}

	public HashMap<String, String> getAbbreviations() {
		return abbreviations;
	}

	public void setAbbreviations(HashMap<String, String> abbreviations) {
		this.abbreviations = abbreviations;
	}

	public int getNumQTLtables() {
		return numQTLtables;
	}

	public void setNumQTLtables() {
		int i=0;
		for (Table t : this.tables) {
			//System.out.println("&*&*"+t.getTableid()+"\t\t"+t.getisTraitTable());
			try{
			if(t.getisTraitTable()==true){
				System.out.println("&*&*"+t.getTableid()+"\t\t"+t.getisTraitTable());
				i++;
		}
		}catch(NullPointerException e){
			
		}
		}
			this.numQTLtables=i;
	}

	//Constructors
	public Article(String filename)
	{
		setFile_name(filename);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFile_name() {
		return file_name;
	}

	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}

	public LinkedList<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(LinkedList<Author> authors) {
		this.authors = authors;
	}

	public String getXML() {
		return XML;
	}

	public void setXML(String xML) {
		XML = xML;
	}

	public String getPlain_text() {
		return plain_text;
	}

	public void setPlain_text(String plain_text) {
		this.plain_text = plain_text;
	}

	public Table[] getTables() {
		return tables;
	}

	public void setTables(Table[] tables) {
		this.tables = tables;
	}

	public String getVenue() {
		return venue;
	}

	public void setVenue(String venue) {
		this.venue = venue;
	}

	public String getPmid() {
		return pmid;
	}

	public void setPmid(String pmid) {
		this.pmid = pmid;
	}

	public String getPmc() {
		return pmc;
	}

	public void setPmc(String pmc) {
		this.pmc = pmc;
	}

	public String getPissn() {
		return pissn;
	}

	public void setPissn(String pissn) {
		this.pissn = pissn;
	}

	public String getEissn() {
		return eissn;
	}

	public void setEissn(String eissn) {
		this.eissn = eissn;
	}

	public String getPublisher_name() {
		return publisher_name;
	}

	public void setPublisher_name(String publisher_name) {
		this.publisher_name = publisher_name;
	}

	public String getPublisher_loc() {
		return publisher_loc;
	}

	public void setPublisher_loc(String publisher_loc) {
		this.publisher_loc = publisher_loc;
	}

	public String[] getAffiliation() {
		return affiliation;
	}

	public void setAffiliation(String[] affiliation) {
		this.affiliation = affiliation;
	}

	public String getAbstract() {
		return article_abstract;
	}

	public void setAbstract(String short_abstract) {
		this.article_abstract = short_abstract;
	}

	public String getShort_abstract() {
		return short_abstract;
	}

	public void setShort_abstract(String short_abstract) {
		this.short_abstract = short_abstract;
	}

	public String[] getKeywords() {
		return keywords;
	}

	public void setKeywords(String[] keywords) {
		this.keywords = keywords;
	}

	/**
	 * @return the journal_name
	 */
	public String getJournal_name() {
		return journal_name;
	}

	/**
	 * @param journal_name the journal_name to set
	 */
	public void setJournal_name(String journal_name) {
		this.journal_name = journal_name;
	}

	/**
	 * @return the spec_id
	 */
	public String getSpec_id() {
		return spec_id;
	}

	/**
	 * @param spec_id the spec_id to set
	 */
	public void setSpec_id(String spec_id) {
		this.spec_id = spec_id;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

    /**
     * @return the ContainingTraitTables
     */
    public boolean isCotainingTraitTables() {
        return hasTraitTables;
    }

    /**
     * @param CotainsTraitTables the ContainingTraitTables to set
     */
    public void setCotainingTraitTables(boolean CotainsTraitTables) {
        this.hasTraitTables = CotainsTraitTables;
    }


}
