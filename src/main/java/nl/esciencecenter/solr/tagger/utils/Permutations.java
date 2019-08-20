package nl.esciencecenter.solr.tagger.utils;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import nl.esciencecenter.qtm.Main;

public class Permutations {
	static Set<String> permutations = new TreeSet<String>();

	public static void main(String[] args) {
		for (String perm : get("this is an example")) {
			Main.logger.debug(perm);
		}
		Main.logger.debug("#perms=" + permutations.size());
	}

	public static Set<String> get(String phrase) {
		permutations.clear();
		String[] c = Arrays.asList(phrase.split("\\s+")).toArray(new String[0]);
		compute(permutations, c, 0);
		return permutations;
	}

	static void swap(String[] c, int pos1, int pos2) {
		String temp = c[pos1];
		c[pos1] = c[pos2];
		c[pos2] = temp;
	}

	public static void compute(Set<String> permutations, String[] c, int start) {
		if (start != 0) {
			permutations.add(StringUtils.join(c, " "));
		}

		for (int i = start; i < c.length; i++) {
			swap(c, start, i);
			compute(permutations, c, start + 1);
			swap(c, start, i);
		}
	}

}
