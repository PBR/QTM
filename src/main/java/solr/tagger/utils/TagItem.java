package solr.tagger.utils;

public class TagItem {

	private Integer start = null;
	private Integer end = null;
	private String uuid = null;
	private String icd10 = null;
	private String term = null;
	private String prefterm = null;
	private String matchtext = null;

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getEnd() {
		return end;
	}

	public void setEnd(Integer end) {
		this.end = end;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getIcd10() {
		return icd10;
	}

	public void setIcd10(String icd10) {
		this.icd10 = icd10;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getPrefTerm() {
		return prefterm;
	}
	public void setPrefTerm(String prefterm) {
		this.prefterm = prefterm;
	}

	public String getMatchText() {
		return matchtext;
	}

	public void setMatchText(String matchtext) {
		this.matchtext = matchtext;
	}

	@Override
	public String toString() {
		return icd10 + ":" + start + ":" + end;
	}

}
