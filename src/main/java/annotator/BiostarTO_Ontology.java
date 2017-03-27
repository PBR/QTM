package annotator;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BiostarTO_Ontology {
	private BufferedReader in;
	private String buffer;
	public  Map<String, Term> id2term = new HashMap<String, Term>();

	public class Term {
		String id;
		String name;
		String def;
		Set<String> children = new HashSet<String>();
		Set<String> is_a = new HashSet<String>();

		int depth() {
			int min_child = 0;
			for (String p : is_a) {
				Term parent = id2term.get(p);
				if (parent == null) {
					System.err.println("Cannot get " + p);
					continue;
				}
				int n2 = parent.depth();
				if (min_child == 0 || n2 < min_child)
					min_child = n2;
			}
			return 1 + min_child;
		}

		public String toString() {
			return id + "\t" + name + "\t" + is_a;
		}
	}

	private Set<String> getAllDescendantById(String id) {
		Set<String> set = new HashSet<String>();
		set.add(id);
		Term t = id2term.get(id);
		for (String c : t.children) {
			set.addAll(getAllDescendantById(c));
		}
		return set;
	}

	private Term getTermById(String id, boolean create) {
		Term t = this.id2term.get(id);
		if (t == null && create) {
			t = new Term();
			t.id = id;
			t.name = id;
			t.def = id;
			this.id2term.put(id, t);
		}
		return t;
	}

	private static String nocomment(String s) {
		int excl = s.indexOf('!');
		if (excl != -1)
			s = s.substring(0, excl);
		return s.trim();
	}

	private String next() throws IOException {
		if (buffer != null) {
			String s = buffer;
			buffer = null;
			return s;
		}
		return in.readLine();
	}

	private void parseTerm() throws IOException {
		Term t = null;
		String line;
		while ((line = next()) != null) {
			if (line.startsWith("[")) {
				this.buffer = line;
				break;
			}
			int colon = line.indexOf(':');
			if (colon == -1)
				continue;
			if (line.startsWith("id:") && t == null) {
				t = getTermById(line.substring(colon + 1).trim(), true);
				continue;
			}
			if (t == null)
				continue;
			if (line.startsWith("name:")) {
				t.name = nocomment(line.substring(colon + 1));
				continue;
			} else if (line.startsWith("def:")) {
				t.def = nocomment(line.substring(colon + 1));
				continue;
			} else if (line.startsWith("is_a:")) {
				String rel = nocomment(line.substring(colon + 1));
				t.is_a.add(rel);
				Term parent = getTermById(rel, true);
				parent.children.add(t.id);
				continue;
			}
		}
	}

	public void parse() throws IOException {

		in = new BufferedReader(new FileReader("/home/gurnoor/workspace/XMLTAB/Dictionaries/to.obo"));

		String line;
		while ((line = next()) != null) {
			if (line.equals("[Term]"))
				parseTerm();
		}
		in.close();
	}

//	public static void main(String args[]) throws IOException {
//		Biostar45366 app = new Biostar45366();
//		app.parse();
//		int level = 1;
//		boolean found = true;
//		while (found) {
//			found = false;
//			for (Term t : app.id2term.values()) {
//				if (t.depth() == level) {
//					System.out.println("" + level + "\t" + t);
//					found = true;
//				}
//			}
//			level++;
//		}
//
//		/*
//		 * for(String id: app.getAllDescendantById("TO:0000837")) {
//		 * System.out.println(app.id2term.get(id)); }
//		 */
//	}

}
