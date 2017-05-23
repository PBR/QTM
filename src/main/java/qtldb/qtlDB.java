/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qtldb;

import java.io.FileWriter;
import java.nio.channels.CancelledKeyException;
import java.sql.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.rowset.RowSetWarning;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import qtm.Article;
import qtm.Cell;
import qtm.Columns;
import qtm.Table;
import qtm.Trait;

/**
 *
 * @author gurnoor
 */
public class qtlDB {

	public static Connection c;
	public static String sJDBC = "jdbc:sqlite";
	public static String TempDb = "TixDb.db";
	// String sTempDb = "TixDb_"+a.getPmc()+".db";

	public static boolean connectionDB() {
		c = null;
		try {
			Class.forName("org.sqlite.JDBC");

			String sDBUrl = sJDBC + ":" + TempDb;

			c = DriverManager.getConnection(sDBUrl);

		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Opened database successfully \n");
		return true;
	}

	public static void createTables() {
		try {
			if (connectionDB()) {

				Statement stmt = null;
				stmt = c.createStatement();
				stmt.setQueryTimeout(30);

				String ArticleTable = "CREATE TABLE IF NOT EXISTS articles(pmcId Text PRIMARY KEY NOT NULL,"
						+ " tittle TEXT); ";
				stmt.executeUpdate(ArticleTable);
				System.out.println("Article Table created successfully");

				String QTLTable = "CREATE TABLE IF NOT EXISTS qtlTables" + "(tableId TEXT PRIMARY KEY NOT NULL,	 "
						+ "numofCol INTEGER NOT NULL," + "numofRows INTEGER NOT NULL," + "pmcId TEXT NOT NULL,"
						+ "FOREIGN KEY(pmcId) REFERENCES articles(pmcId)" + "); ";
				stmt.executeUpdate(QTLTable);
				System.out.println("QTLTable created successfully");

				String abbreviationTable = "CREATE TABLE IF NOT EXISTS abbreviations" + "(abbreviation Text  NOT NULL,"
						+ "expansion Text NOT NULL," + "pmcId TEXT NOT NULL,"
						+ "FOREIGN KEY(pmcId) REFERENCES articles(pmcId)" + "); ";
				stmt.executeUpdate(abbreviationTable);
				System.out.println("Abbreviations Table created successfully");

				String colETable = "CREATE TABLE IF NOT EXISTS columnEntries"
						+ "(colId TEXT PRIMARY KEY NOT NULL, colHeader  TEXT,"
						+ "colType Text, colAnnotations TEXT, tableId TEXT, FOREIGN KEY(tableId) REFERENCES qtlTables(tableID)"
						+ "); ";
				stmt.executeUpdate(colETable);
				System.out.println("Column Entry Table created successfully");

				String cellETable = "CREATE TABLE IF NOT EXISTS cellEntries"
						+ "(cellId INTEGER PRIMARY KEY  AUTOINCREMENT NOT NULL, rowNumber INT, cellValue  TEXT,"
						+ "cellType Text, cellAnnotations TEXT, colId TEXT, FOREIGN KEY(colId) REFERENCES columnEntries(colId)"
						+ "); ";
				stmt.executeUpdate(cellETable);
				System.out.println("Cell Entry Table created successfully");

				String traitTable = "CREATE TABLE IF NOT EXISTS traits"
						+ "(TraitId INTEGER PRIMARY KEY  AUTOINCREMENT NOT NULL, TraitName Text, TraitValues  TEXT,"
						+ "TraitProperties TEXT, OtherProperties TEXT, pmcId TEXT, tableId TEXT, rowNumber INTEGER, FOREIGN KEY(pmcId) REFERENCES articles(pmcId), FOREIGN KEY(tableId) REFERENCES qtlTables(tableId)"
						+ "); ";
				stmt.executeUpdate(traitTable);
				System.out.println("Trait Table created successfully");

				String traitValueTable = "CREATE TABLE IF NOT EXISTS traitValues"
						+ "(id INTEGER PRIMARY KEY  AUTOINCREMENT NOT NULL, traitName_asinArticle Text,  traitName_ontologyTerm Text, traitUri TEXT,"
						+ "featureName_asinArticle TEXT, featureName_ontologyTerm TEXT, featureUri_ontologyId TEXT, featureValue TEXT, featureValue_ontologyTerm TEXT, featureValueUri_ontologyId TEXT, pmcId TEXT, doi TEXT , tableId TEXT, FOREIGN KEY(pmcId) REFERENCES articles(pmcId), FOREIGN KEY(tableId) REFERENCES qtlTables(tableId) "
						+ "); ";
				stmt.executeUpdate(traitValueTable);
				System.out.println("Trait-Value Table created successfully");

				String traitPropertyTable = "CREATE TABLE IF NOT EXISTS traitProperties"
						+ "(id INTEGER PRIMARY KEY  AUTOINCREMENT NOT NULL, traitName_asinArticle Text,traitName_ontologyTerm Text, traitUri TEXT,"
						+ "featureName_asinArticle TEXT, featureName_ontologyTerm TEXT, featureUri_ontologyId TEXT, featureValue TEXT, featureValue_ontologyTerm TEXT, featureValueUri_ontologyId TEXT, pmcId TEXT, doi TEXT , tableId TEXT, FOREIGN KEY(pmcId) REFERENCES articles(pmcId), FOREIGN KEY(tableId) REFERENCES qtlTables(tableId) "
						+ "); ";
				stmt.executeUpdate(traitPropertyTable);
				System.out.println("Trait-Property Table created successfully");

				String qtlTable = "CREATE TABLE IF NOT EXISTS qtl"
						+ "(qtl_id TEXT PRIMARY KEY NOT NULL, traitName_asinArticle Text,traitName_ontologyTerm Text, traitUri TEXT,"
						+ "featureName_asinArticle TEXT, featureName_ontologyTerm TEXT, featureUri_ontologyId TEXT, featureValue TEXT, featureValue_ontologyTerm TEXT, featureValueUri_ontologyId TEXT, pmcId TEXT, doi TEXT , tableId TEXT, FOREIGN KEY(pmcId) REFERENCES articles(pmcId), FOREIGN KEY(tableId) REFERENCES qtlTables(tableId) "
						+ "); ";
				stmt.executeUpdate(qtlTable);
				System.out.println("qtlTable Table created successfully");

				String qtlzTable = "CREATE TABLE IF NOT EXISTS QTLZ"
						+ "(qtl_id TEXT PRIMARY KEY NOT NULL, traitname_in_article Text, traitname_in_ontology Text, trait_uri TEXT,"
						+ "chromosome_number Text, marker_associated TEXT, genes_associated TEXT, snp_associated TEXT, pmcId TEXT, tableId TEXT, row_number TEXT, FOREIGN KEY(pmcId) REFERENCES articles(pmcId), FOREIGN KEY(tableId) REFERENCES qtlTables(tableId) "
						+ "); ";
				stmt.executeUpdate(qtlzTable);
				System.out.println("qtlz Table Table created successfully");

				stmt.close();

			}
		} catch (Exception e) {
			System.out.println("SQLite tables not created");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

	public static String getOnlyStrings(String s) {
		Pattern pattern = Pattern.compile("[^a-z A-Z]");
		Matcher matcher = pattern.matcher(s);
		String number = matcher.replaceAll("");
		return number;
	}

	public static void insertArticleEntry(Article articles[]) {
		try {
			System.out.println("Article length is" + articles.length);

			for (int i = 0; i < articles.length; i++) {
				System.out.println("Article pmcid is" + articles[i].getPmc());

				if (connectionDB() & isArticleEntryAlredyIn(articles[i], c) == false) {
					Statement articlestmt = null;
					articlestmt = c.createStatement();

					Statement abbrevstmt = null;
					abbrevstmt = c.createStatement();

					Statement qtlTablestmt = null;
					qtlTablestmt = c.createStatement();

					Statement colstmt = null;
					colstmt = c.createStatement();

					Statement cellstmt = null;
					cellstmt = c.createStatement();

					// Article Table entry
					String articleID = articles[i].getPmc();
					String articleTitle = articles[i].getTitle();

					String insertArticleTable = "INSERT INTO articles Values('" + articleID + "','" + articleTitle
							+ "');";
					articlestmt.executeUpdate(insertArticleTable);

					// System.out.println("Entry done for article number" + "\t"
					// + articleID);

					for (String key : articles[i].getAbbreviations().keySet()) {

						String insertAbrevTable = "INSERT INTO abbreviations Values('"
								+ articles[i].getAbbreviations().get(key) + "','" + key + "','" + articleID + "');";
						abbrevstmt.executeUpdate(insertAbrevTable);
					}
					abbrevstmt.close();
					// System.out.println("Abbreviation entries inserted in the
					// DB");
					// QTL table entries
					for (Table t : articles[i].getTables()) {

						// checking if table exists
						try {
							if (t.isaTraitTable()) {

								String tableID = t.getTableid();
								int numofCol = t.getNum_of_columns();
								int numofRows = t.getNum_of_rows();

								// System.out.println("Entry inserted for
								// QTLTable number"+ tableID);

								String insertQTLTable = "INSERT INTO qtlTables Values('" + tableID + "'," + numofCol
										+ "," + numofRows + ",'" + articleID + "');";
								qtlTablestmt.executeUpdate(insertQTLTable);

								for (Columns col : t.getTableCol()) {

									String colAnno = "";

									try {
										if (col.getColumns_type() == "QTL value") {
											colAnno = nl.erasmusmc.biosemantics.tagger.recognize.Evaluate2
													.processString(col.getHeader(), "statoTerms",
															"LONGEST_DOMINANT_RIGHT", "dictionary");
										} else if (col.getColumns_type() == "QTL property") {
											colAnno = nl.erasmusmc.biosemantics.tagger.recognize.Evaluate2
													.processString(getOnlyStrings(col.getHeader()), "propTerms",
															"LONGEST_DOMINANT_RIGHT", "dictionary");
											;
										}
									} catch (Exception e) {
										colAnno = "";
										System.out.println("error in column Annotation");
										e.getMessage();
									}

									String insertColTable = "INSERT INTO columnEntries(colId,colHeader,colType, colAnnotations, tableId) Values('"
											+ col.getColID() + "','" + col.getHeader() + "','" + col.getColumns_type()
											+ "','" + colAnno + "','" + t.getTableid() + "');";

									colstmt.executeUpdate(insertColTable);
									// System.out.println("Col entries inserted
									// in the
									// DB");

									for (Cell cel : col.getcelz()) {

										// checknig if cell exists
										try {
											System.out.println(cel.getRow_number() + "\t" + cel.getcell_value() + "\t\t"
													+ tableID);
										} catch (NullPointerException e) {
											continue;
										}

										if (cel.getcell_value().indexOf("'") != -1)
											cel.setcell_values(cel.getcell_value().replace("'", "''"));

										if (cel.getcell_value() != null) {

											String insertCellTable = "INSERT INTO cellEntries(rowNumber, cellValue, cellType, colId) Values("

													+ cel.getRow_number() + ",'" + cel.getcell_value() + "','"
													+ cel.getCell_type() + "','" + col.getColID() + "');";
											cellstmt.executeUpdate(insertCellTable);

											// System.out.println("Cell entries
											// inserted in the
											// DB"+cel.getRow_number());
										}

									}
									cellstmt.close();
								}
								colstmt.close();
							}
						} catch (NullPointerException e) {
							continue;
						}

					}
					qtlTablestmt.close();

					// InsertTraitEntry(a);

				} else {
					System.out.println(articles[i].getPmc() + " already exists");

				}
			}

			System.out.println("entry inserted into DB successfully");
			// System.exit(0);
		} catch (Exception e) {
			System.out.println("Error is Database entry");
			e.printStackTrace();

		}
	}

	// public static void TablesReclassify(Article a){
	// System.out.println("reclassifying the entries in the databases");
	// try {
	// HashMap<String, Integer> AnnotatedCols = new HashMap<String, Integer>();
	// if (connectionDB()) {
	// Statement stmt1 = null;
	// Statement stmt2 = null;
	// Statement stmt3 = null;
	// Statement stmt4 = null;
	//
	// stmt1 = c.createStatement();
	// stmt2 = c.createStatement();
	// stmt3 = c.createStatement();
	// stmt4 = c.createStatement();
	//
	// for(Table t: a.getTables()){
	// if(t.isaTraitTable()){
	// String sql1 ="select colId,colType from columnEntries where colType in
	// ('QTL property', 'QTL descriptor') AND tableId ='"+t.getTableid()+"';";
	// System.out.println(" reclassifying table"+ t.getTableid());
	// ResultSet rs1 = stmt1.executeQuery(sql1);
	//
	// while (rs1.next()){
	// String colId=rs1.getString("colId");
	// String sql2= "select count(cellId) from cellEntries where colId =
	// '"+colId+"' and cellAnnotations != '' or cellAnnotations != null;";
	// ResultSet rs2= stmt2.executeQuery(sql2);
	// int numofAnnotatedcolumns=rs2.getInt("count(cellId)");
	// AnnotatedCols.put(colId, numofAnnotatedcolumns);
	//
	// }
	// //maximum annotated column
	// String maxAnnotatedCol = Collections.max(AnnotatedCols.keySet());
	//
	// while (rs1.next()){
	// String colId=rs1.getString("colId");
	// String colType=rs1.getString("colType");
	// if(colId==maxAnnotatedCol && colType=="QTL descriptor")
	// continue;
	// else if(colId!=maxAnnotatedCol && colType=="QTL descriptor"){
	// String sql3= "update columnEntries SET colType='QTL property' where colId
	// = '"+colId +"';";
	// stmt3.executeUpdate(sql3);
	// }
	// else if(colId==maxAnnotatedCol && colType!="QTL descriptor"){
	// String sql4= "update columnEntries SET colType='QTL descriptor' where
	// colId = '"+colId +"';";
	// stmt4.executeUpdate(sql4);
	// }
	//
	// }
	//
	//
	// }
	// }
	// c.close();
	//
	// }
	// }catch (SQLException e) {
	//
	// e.printStackTrace();
	// }
	// }

	public static void InsertTraitEntry(Article articles[]) {
		try {

			for (int i = 0; i < articles.length; i++) {
				if (connectionDB()) {
					Statement stmt1 = null;
					Statement stmt2 = null;
					Statement stmt3 = null;
					stmt1 = c.createStatement();
					stmt2 = c.createStatement();
					stmt3 = c.createStatement();
					// System.out.println("I am here");

					List<Trait> traits = articles[i].getTraits();

					String sql1 = "select cellValue,colId,rowNumber from cellEntries where cellType !='Empty' AND colId in (select colId from columnEntries where colType='QTL descriptor' AND colId like '"
							+ articles[i].getPmc() + "%');";
					ResultSet rs1 = stmt1.executeQuery(sql1);

					while (rs1.next()) {
						String value = rs1.getString("cellValue");
						String colId = rs1.getString("colId");
						String tableId = colId.substring(0, (colId.length() - 2));
						int row = rs1.getInt("rowNumber");

						System.out.println("**********");
						System.out.println(value + "\t" + colId + "\t" + tableId + "\t" + row);
						System.out.println("**********");

						Trait T = new Trait(value);
						JSONObject vals = new JSONObject();
						JSONObject prop = new JSONObject();
						JSONObject otherProp = new JSONObject();

						String sql2 = "select C.cellValue, Col.colHeader,Col.colType, Col.colAnnotations from cellEntries as C inner join columnEntries as Col ON C.colId=Col.colId  where rowNumber ="
								+ row + " and Col.tableId='" + tableId + "' and Col.colType!='QTL descriptor'";
						ResultSet rs2 = stmt2.executeQuery(sql2);

						while (rs2.next()) {
							String cellValue = rs2.getString("CellValue").replaceAll("\n", "").replace("\r", "");
							String colHeader = rs2.getString("colHeader").replaceAll("\n", "").replace("\r", "");
							String colType = rs2.getString("colType").replaceAll("\n", "").replace("\r", "");
							String colAnno = rs2.getString("colAnnotations").replaceAll("\n", "").replace("\r", "");

							JSONObject colAnnoJSON = new JSONObject();

							if (!"".equals(colAnno)) {

								colAnnoJSON = processSolarOutputtoJson(colAnno);
								colAnnoJSON.put("actualValue", cellValue);
							} else {
								colAnnoJSON.put("icd", "");
								colAnnoJSON.put("matchingText", colHeader);
								colAnnoJSON.put("prefTerm", colHeader);
								colAnnoJSON.put("Term", "");
								colAnnoJSON.put("start", "");
								colAnnoJSON.put("end", "");
								colAnnoJSON.put("Uuid", "");
								colAnnoJSON.put("actualValue", cellValue);
							}

							System.out.println("Entries " + "\t" + cellValue + "\t" + colHeader + "\t" + colType);

							if (colType.equals("QTL value")) {
								// System.out.println("cell Values is" +
								// cellValue);

								vals.put(colAnnoJSON, colHeader);

							} else if (colType.equals("QTL property")) {
								prop.put(colAnnoJSON, colHeader);

							} else {
								otherProp.put(colAnnoJSON, colHeader);

							}

						}

						T.setTraitValues(vals);
						T.setTraitProperties(prop);
						T.setOtherProperties(otherProp);

						traits.add(T);

						String insertTraitTable = "INSERT INTO traits(TraitName,TraitValues,TraitProperties,OtherProperties, pmcId, tableId, rowNumber) Values('"
								+ value + "','" + T.getTraitValues() + "','" + T.getTraitProperties() + "','"
								+ T.getOtherProperties() + "','" + articles[i].getPmc() + "','" + tableId + "','" + row
								+ "');";

						stmt3.executeUpdate(insertTraitTable);
						stmt3.close();
						stmt2.close();
						// System.out.println("programm running till here ");

					}
					stmt1.close();

				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// System.exit(0);
	}

	public static void insertTraitValuesandTraitProperties(Article articles[]) {
		try {

			if (connectionDB()) {

				for (int i = 0; i < articles.length; i++) {
					System.out.println("Traits founds in Article" + articles[i].getPmc());

					String sql1 = "Select TraitId, TraitName, tableId from traits where pmcId like '"
							+ articles[i].getPmc() + "'; ";

					String server = "http://localhost:8983/solr";
					String core1 = "terms";
					String core2 = "statoTerms";
					String core3 = "propTerms";
					String core4 = "solaLyco";
					String core5 = "solaLyco2";
					String match = "LONGEST_DOMINANT_RIGHT";
					String type = "dictionary";

					Statement stmt1 = null;
					Statement stmt2 = null;
					Statement stmt3 = null;

					stmt1 = c.createStatement();
					stmt2 = c.createStatement();
					stmt3 = c.createStatement();
					ResultSet rs1 = stmt1.executeQuery(sql1);

					while (rs1.next()) {

						String traitName = rs1.getString("TraitName");// .replaceAll("\\(.*\\)",
						String traitId = rs1.getString("TraitId"); // "");
						String tableId = rs1.getString("tableId");
						// traitName=getOnlyStrings(traitName);

						System.out.println("\nTraitNames " + traitName + "\n");

						String traitAnno = "";
						try {
							traitAnno = nl.erasmusmc.biosemantics.tagger.recognize.Evaluate2
									.processString(getOnlyStrings(traitName), core1, match, type);

						} catch (Exception e) {
							System.out.println("error in solar annotations");
						}

						JSONObject traitAnnoJSON = new JSONObject();
						if (!"".equals(traitAnno)) {

							traitAnnoJSON = processSolarOutputtoJson(traitAnno);
							System.out.println(traitAnnoJSON.toJSONString());
						}

						else {
							traitAnnoJSON.put("icd", "");
							traitAnnoJSON.put("matchingText", traitName);
							traitAnnoJSON.put("prefTerm", traitName);
							traitAnnoJSON.put("Term", "");
							traitAnnoJSON.put("start", "");
							traitAnnoJSON.put("end", "");
							traitAnnoJSON.put("Uuid", "");
						}

						String sql2 = "Select TraitValues from traits where TraitId like '" + traitId
								+ "'AND pmcId like '" + articles[i].getPmc() + "'; ";

						ResultSet rs2 = stmt2.executeQuery(sql2);

						while (rs2.next()) {
							String traitValue = rs2.getString("TraitValues");
							JSONParser parser = new JSONParser();
							JSONObject tValuesJson = (JSONObject) parser.parse(traitValue);

							for (Iterator iterator = tValuesJson.keySet().iterator(); iterator.hasNext();) {
								String v = (String) iterator.next();
								String key = tValuesJson.get(v).toString();

								JSONParser parser2 = new JSONParser();
								JSONObject statJsonv = (JSONObject) parser.parse(v);

								String insertTraitValue = "INSERT INTO traitValues(traitName_asinArticle,traitName_ontologyTerm, traitUri, featureName_asinArticle, featureName_ontologyTerm, featureUri_ontologyId, featureValue, pmcId, doi, tableId) Values('"
										+ traitName + "','" + traitAnnoJSON.get("prefTerm") + "','"
										+ traitAnnoJSON.get("icd") + "','" + key + "','" + statJsonv.get("prefTerm")
										+ "','" + statJsonv.get("icd") + "','" + statJsonv.get("actualValue") + "','"
										+ articles[i].getPmc() + "','" + articles[i].getDoi() + "','" + tableId + "');";

								stmt3.executeUpdate(insertTraitValue);
							}

						}

						String sql3 = "Select TraitProperties from traits where TraitId like '" + traitId
								+ "'AND pmcId like '" + articles[i].getPmc() + "'; ";

						ResultSet rs3 = stmt2.executeQuery(sql3);

						while (rs3.next()) {
							String traitPro = rs3.getString("TraitProperties");
							JSONParser parser = new JSONParser();
							JSONObject tProJson = (JSONObject) parser.parse(traitPro);

							for (Iterator iterator = tProJson.keySet().iterator(); iterator.hasNext();) {
								String p = (String) iterator.next();
								String key = tProJson.get(p).toString();
								JSONParser parser2 = new JSONParser();
								JSONObject statJsonp = (JSONObject) parser.parse(p);

								String feature_value = statJsonp.get("actualValue").toString();

								String feature_valueAnnotation = "";
								try {
									feature_valueAnnotation = nl.erasmusmc.biosemantics.tagger.recognize.Evaluate2
											.processString(feature_value, core5, match, type);

								} catch (Exception e) {
									feature_valueAnnotation = "";
									System.out.println("error in solar annotations");

								}

								JSONObject feature_value_JSON = new JSONObject();
								if (!"".equals(feature_valueAnnotation)) {

									feature_value_JSON = processSolarOutputtoJson(feature_valueAnnotation);

								}

								else {
									feature_value_JSON.put("icd", "");
									feature_value_JSON.put("matchingText", feature_value);
									feature_value_JSON.put("prefTerm", feature_value);
									feature_value_JSON.put("Term", "");
									feature_value_JSON.put("start", "");
									feature_value_JSON.put("end", "");
									feature_value_JSON.put("Uuid", "");
								}

								String insertTraitValue = "INSERT INTO traitProperties(traitName_asinArticle,traitName_ontologyTerm, traitUri, featureName_asinArticle, featureName_ontologyTerm, featureUri_ontologyId, featureValue, featureValue_ontologyTerm, featureValueUri_ontologyId, pmcId, doi, tableId) Values('"
										+ traitName + "','" + traitAnnoJSON.get("prefTerm") + "','"
										+ traitAnnoJSON.get("icd") + "','" + key + "','" + statJsonp.get("prefTerm")
										+ "','" + statJsonp.get("icd") + "','" + statJsonp.get("actualValue") + "','"
										+ feature_value_JSON.get("prefTerm") + "','" + feature_value_JSON.get("icd")
										+ "','" + articles[i].getPmc() + "','" + articles[i].getDoi() + "','" + tableId
										+ "');";

								stmt3.executeUpdate(insertTraitValue);
							}

						}

						stmt3.close();
					}

					// }

					// }
					//

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void insertQTLTable() {
		try {

			if (connectionDB()) {
				String sql1 = "select DISTINCT traitName_asinArticle,traitName_ontologyTerm,traitUri, featureName_asinArticle, featureName_ontologyTerm, featureUri_ontologyId, featureValue, featureValue_ontologyTerm, featureValueUri_ontologyId,pmcId,doi,tableId from traitProperties where (featureName_asinArticle like '%geno%' or featureName_asinArticle like '%gene%' or featureName_asinArticle like '%marker%' or featureValue like '%sol%' or featureValue like '%snp%' ) and (featureValue not like '%(%)%' and  featureValue not like '%/%') ";

				Statement stmt1 = null;
				Statement stmt2 = null;

				stmt1 = c.createStatement();
				stmt2 = c.createStatement();
				ResultSet rs1 = stmt1.executeQuery(sql1);

				FileWriter QTLTableMinerResultsfile = new FileWriter("QTLTableMiner++Results.csv");

				QTLTableMinerResultsfile.append("qtl_id,traitName_asinArticle,traitName_ontologyTerm,traitUri,"
						+ "featureName_asinArticle,featureName_ontologyTerm,featureUri_ontologyId,"
						+ "featureValue,featureValue_ontologyTerm,featureValueUri_ontologyId," + "pmcId,doi,tableId\n");

				int i = 0;
				while (rs1.next()) {
					String traitName_asinArticle = rs1.getString("traitName_asinArticle");
					String traitName_ontologyTerm = rs1.getString("traitName_ontologyTerm");
					String traitUri = rs1.getString("traitUri");
					String featureName_asinArticle = rs1.getString("featureName_asinArticle");
					String featureName_ontologyTerm = rs1.getString("featureName_ontologyTerm");
					String featureUri_ontologyId = rs1.getString("featureUri_ontologyId");
					String featureValue = rs1.getString("featureValue");
					String featureValue_ontologyTerm = rs1.getString("featureValue_ontologyTerm");
					String featureValueUri_ontologyId = rs1.getString("featureValueUri_ontologyId");
					String pmcId = rs1.getString("pmcId");
					String doi = rs1.getString("doi");
					String tableId = rs1.getString("tableId");
					String qtlId = tableId + "_" + i;

					QTLTableMinerResultsfile.append(qtlId + "," + traitName_asinArticle + "," + traitName_ontologyTerm
							+ "," + traitUri + "," + featureName_asinArticle + "," + featureName_ontologyTerm + ","
							+ featureUri_ontologyId + "," + featureValue + "," + featureValue_ontologyTerm + ","
							+ featureValueUri_ontologyId + "," + pmcId + "," + doi + "," + tableId + "\n");

					String insertQTLtable = "INSERT INTO qtl(qtl_id, traitName_asinArticle,traitName_ontologyTerm,traitUri,"
							+ "featureName_asinArticle,featureName_ontologyTerm,featureUri_ontologyId,featureValue,featureValue_ontologyTerm,"
							+ "featureValueUri_ontologyId,pmcId,doi,tableId) Values('" + qtlId + "','"
							+ traitName_asinArticle + "','" + traitName_ontologyTerm + "','" + traitUri + "','"
							+ featureName_asinArticle + "','" + featureName_ontologyTerm + "','" + featureUri_ontologyId
							+ "','" + featureValue + "','" + featureValue_ontologyTerm + "','"
							+ featureValueUri_ontologyId + "','" + pmcId + "','" + doi + "','" + tableId + "');";
					stmt2.executeUpdate(insertQTLtable);
					stmt2.close();
					i++;

				}

				if (QTLTableMinerResultsfile != null) {
					QTLTableMinerResultsfile.flush();
					QTLTableMinerResultsfile.close();

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void insertQTLZTable() {
		try {

			String server = "http://localhost:8983/solr";
			String core1 = "terms";
			String core2 = "statoTerms";
			String core3 = "propTerms";
			String core4 = "solaLyco";
			String core5 = "solaLyco2";
			String match = "LONGEST_DOMINANT_RIGHT";
			String type = "dictionary";

			if (connectionDB()) {
				Statement stmt1 = null;
				Statement stmt2 = null;
				Statement stmt3 = null;
				stmt1 = c.createStatement();
				stmt2 = c.createStatement();
				stmt3 = c.createStatement();

				String sql1 = "select distinct(pmcId) from traits;";

				ResultSet rs1 = stmt1.executeQuery(sql1);

				while (rs1.next()) {

					String pmcId = rs1.getString("pmcId");

					String sql2 = "Select * from traits where pmcId like '" + pmcId + "';'";

					ResultSet rs2 = stmt1.executeQuery(sql2);

					while (rs2.next()) {

						String traitName = rs2.getString("TraitName");// .replaceAll("\\(.*\\)",
						String traitId = rs2.getString("TraitId"); // "");
						String rowNumber = rs2.getString("rowNumber");
						String tableId = rs2.getString("tableId");

						System.out.println("\nTraitNames " + traitName + "\n");

						String traitAnno = "";
						traitAnno = nl.erasmusmc.biosemantics.tagger.recognize.Evaluate2
								.processString(getOnlyStrings(traitName), core1, match, type);

						JSONObject traitAnnoJSON = new JSONObject();

						if (!"".equals(traitAnno)) {
							traitAnnoJSON = processSolarOutputtoJson(traitAnno);
							System.out.println(traitAnnoJSON.toJSONString());
						}

						else {
							traitAnnoJSON.put("icd", "");
							traitAnnoJSON.put("matchingText", traitName);
							traitAnnoJSON.put("prefTerm", traitName);
							traitAnnoJSON.put("Term", "");
							traitAnnoJSON.put("start", "");
							traitAnnoJSON.put("end", "");
							traitAnnoJSON.put("Uuid", "");
						}

						String ChromosomeNumber = "";
						String snp_associated = "";
						String gene_associated = "";
						String peak_marker = "";

						// Parsing Trait Values

						String traitValue = rs2.getString("TraitValues");
						JSONParser parserV = new JSONParser();
						JSONObject tValuesJson = (JSONObject) parserV.parse(traitValue);

						for (Iterator iterator = tValuesJson.keySet().iterator(); iterator.hasNext();) {
							String v = (String) iterator.next();
							String key = tValuesJson.get(v).toString();

							JSONParser parser2 = new JSONParser();
							JSONObject statJsonv = (JSONObject) parserV.parse(v);

							// Filter out Chromosome Number
							String REGEX = "chromo";
							Pattern pattern = Pattern.compile(REGEX);

							if (statJsonv.get("prefTerm").equals("chromosome") || pattern.matcher(key).find()) {
								ChromosomeNumber += statJsonv.get("actualValue").toString();
							}
						}

						// Parsing Trait Properties

						String traitPro = rs2.getString("TraitProperties");
						JSONParser parserP = new JSONParser();
						JSONObject tProJson = (JSONObject) parserP.parse(traitPro);

						for (Iterator iterator = tProJson.keySet().iterator(); iterator.hasNext();) {
							String p = (String) iterator.next();
							String key = tProJson.get(p).toString();
							JSONParser parser2 = new JSONParser();
							JSONObject statJsonp = (JSONObject) parserP.parse(p);

							// Filter out Chromosome Number
							String REGEX = "chromo";
							Pattern pattern = Pattern.compile(REGEX);

							if (statJsonp.get("prefTerm").equals("chromosome") || pattern.matcher(key).find()) {
								ChromosomeNumber += statJsonp.get("actualValue").toString();
							}

							// featureName_asinArticle like '%geno%' or
							// featureName_asinArticle like '%gene%' or
							// featureName_asinArticle like '%marker%' or
							// featureValue like '%sol%' or

							// Filterout SNP
							REGEX = "snp";
							pattern = Pattern.compile(REGEX);
							if (pattern.matcher(statJsonp.get("actualvalue").toString()).find()
									|| pattern.matcher(key).find()) {
								snp_associated += statJsonp.get("actualValue").toString();
							}

							// Filterout Gene
							REGEX = "gene";
							pattern = Pattern.compile(REGEX);
							if (pattern.matcher(statJsonp.get("actualvalue").toString()).find()
									|| pattern.matcher(statJsonp.get("matchingText").toString()).find()
									|| pattern.matcher(statJsonp.get("prefTerm").toString()).find()
									|| pattern.matcher(key).find()) {
								gene_associated += statJsonp.get("actualValue").toString();
							}

							// Filterout Marker
							REGEX = "marker";
							pattern = Pattern.compile(REGEX);
							if (pattern.matcher(statJsonp.get("actualvalue").toString()).find()
									|| pattern.matcher(statJsonp.get("matchingText").toString()).find()
									|| pattern.matcher(statJsonp.get("prefTerm").toString()).find()
									|| pattern.matcher(key).find()) {
								// marker_start -- marker_end -- peak_marker
								// TEXT
								peak_marker = peak_marker + ";" + statJsonp.get("actualValue").toString();

							}

						}
						
						String qtl_id=tableId+rowNumber;
						
					
						
						String insertQTLZtable = "INSERT INTO QTLZ(qtl_id, traitName_asinArticle,traitName_ontologyTerm,traitUri,"
								+ "chromosome_number,marker_associated,genes_associated, snp_associated,pmcId,tableId,row_number)"
								+ "Values('" + qtl_id + "','"
								+ traitName + "','" + traitAnnoJSON.get("prefTerm") + "','" + traitAnnoJSON.get("icd") + "','"
								+ ChromosomeNumber + "','"
								+ peak_marker + "','" 
								+ gene_associated + "','" 
								+ snp_associated + "','" 
								+ pmcId + "','" 
								+ tableId + "','"
								+ rowNumber + "');";
						stmt3.executeUpdate(insertQTLZtable);
						stmt3.close();
					}

					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isArticleEntryAlredyIn(Article a, Connection c) {
		Boolean check = false;
		try {
			Statement stmt = null;
			stmt = c.createStatement();

			// System.out.println("I am here");
			String sql = "SELECT pmcId FROM articles";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				String pmcId = rs.getString("pmcId");
				if (pmcId.equals(a.getPmc())) {
					check = true;
					return check;
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();

		}
		return check;

	}

	public static JSONObject processSolarOutputtoJson(String output) {
		System.out.println("\n" + output);
		JSONObject j = new JSONObject();
		String[] s = output.split(Pattern.quote("|"));
		// System.out.println("I am here" + s[1].toString());

		j.put("icd", s[0]);
		j.put("matchingText", s[1]);
		j.put("prefTerm", s[2]);
		j.put("Term", s[3]);
		j.put("start", s[4]);
		j.put("end", s[5]);
		j.put("Uuid", s[6]);
		return j;
	}

}