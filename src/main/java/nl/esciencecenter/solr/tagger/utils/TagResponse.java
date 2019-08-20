package nl.esciencecenter.solr.tagger.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class TagResponse {

	private List<TagItem> items = new ArrayList<TagItem>();

	public TagResponse() {
		// TODO Auto-generated constructor stub

	}

	public List<TagItem> getItems() {
		return items;
	}

	public void setItems(List<TagItem> items) {
		this.items = items;
	}

	public void add(TagItem item) {
		this.items.add(item);
	}

	@Override
	public String toString() {
		return StringUtils.join(items, ",");
	}
}
