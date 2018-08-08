package nl.esciencecenter.qtm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

	public static void main(String[] args) throws IOException {
		String inFile = args[0];
		ArrayList<Article> articles = new ArrayList<>();

		if (args.length == 0 || Arrays.asList(args).contains("-h") || Arrays.asList(args).contains("--help")) {
			printHelp();
			return;
		}
	
		if (Arrays.asList(args).contains("-v") || Arrays.asList(args).contains("--version")) {
			System.out.println("1.0");
			return;
		}
	
		if (Arrays.asList(args).contains("-o")) {
			String outFile = args[Arrays.asList(args).indexOf("-o") + 1];
			if (!outFile.endsWith(".db")) {
				outFile = args[Arrays.asList(args).indexOf("-o") + 1] + ".db";
			}
			System.out.println("Outfile=" + outFile);
		}
	
		// read list of PMCID from a file
		try (BufferedReader br = new BufferedReader(new FileReader(inFile))) {
			String pmcId = null;
			while ((pmcId = br.readLine()) != null) {
				pmcId = pmcId.toUpperCase().trim();
				if (pmcId.matches("^PMC\\d+$")) {
					Article a = new Article(pmcId);
					articles.add(a);
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}

		// process articles in XML
		for (Article a : articles) {
			a.download();
		}	
	}

	public static void printHelp() {
		System.out.println("DESCRIPTION");
		System.out.println("===========");
		System.out.println("QTLTableMiner++ tool extracts results of QTL mapping experiments "
				+ "described in tables of scientific articles.\n");
		System.out.println("USAGE");
		System.out.println("=====");
		System.out.println("  QTM [-v|-h]");
		System.out.println("  QTM [-o FILE_PREFIX] FILE\n");
		System.out.println("ARGUMENTS");
		System.out.println("=========");
		System.out.println("  FILE\t\t\t\tList of articles from Europe PMC (one PMCID per line).\n");
		System.out.println("OPTIONS");
		System.out.println("=======");
		System.out.println("  -o, --output FILE_PREFIX\tOutput files in SQLite/"
				+ "CSV formats.\n\t\t\t\t(default: qtl.{db,csv})");
		System.out.println("  -v, --version\t\t\tPrint software version.");
		System.out.println("  -h, --help\t\t\tPrint this help message.\n");
	}
}