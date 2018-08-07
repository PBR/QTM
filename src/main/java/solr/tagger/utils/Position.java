package solr.tagger.utils;

public class Position {

	private Integer start = null;
	private Integer end = null;
	private String matchText = null;

	public Position(String aMatchText, Integer aStartOffset, Integer aEndOffset) {
		this.setMatchText(aMatchText);
		this.start = aStartOffset;
		this.end = aEndOffset;
	}

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

	public String getMatchText() {
		return matchText;
	}

	public void setMatchText(String matchText) {
		this.matchText = matchText;
	}
}
