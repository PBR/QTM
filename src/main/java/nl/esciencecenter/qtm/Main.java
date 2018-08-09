package nl.esciencecenter.qtm;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.util.ArrayList;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Main {
	public static void main(String[] args) throws IOException {
		ArgumentParser parser = ArgumentParsers.newFor("QTM").build()
			.description("Extract QTL data from full-text articles.")
			.version(Main.class.getPackage().getImplementationVersion());
		parser.addArgument("-v", "--version")
			.action(Arguments.version())
			.help("show version and exists");
		parser.addArgument("-o", "--output")
			.setDefault("qtl")
			.help("filename prefix for output in SQLite and CSV formats {.db,.csv}");
		parser.addArgument("FILE")
			.help("input file of articles (one PMCID per line)");

		try {
			Namespace res = parser.parseArgs(args);
			String inputFile = res.get("FILE");
			run(inputFile);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
		}
	}

	public static void run(String file) throws IOException {
		ArrayList<Article> articles = new ArrayList<>();

		// read PMCIDs from input file into an array
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
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
}