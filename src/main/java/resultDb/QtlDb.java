/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package resultDb;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import qtm.Article;
import qtm.Cell;
import qtm.Columns;
import qtm.Table;
import qtm.Trait;
import utils.Configs;

/**
 *
 * @author gurnoor
 */
public class QtlDb {

	public static Connection c;
	
	public static String sJdbc = Configs.getPropertyQTM("sJdbc");
	public static String dbName = Configs.getPropertyQTM("dbName");
	public static String userNameDb = Configs.getPropertyQTM("userName");
	public static String passwordDb = Configs.getPropertyQTM("password");
	// String sTempDb = "TixDb_"+a.getPmc()+".db";
	
	
	
	
	public static boolean connectionDB() {
		c = null;
		try {
			Class.forName("org.sqlite.JDBC");
			String sDBUrl = sJdbc + ":" + dbName;
			c = DriverManager.getConnection(sDBUrl, userNameDb,passwordDb);

		} catch (Exception e) {
		        System.out.println("Error in connecting to the output database");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		
		
		//System.out.println("Results Database file is: \t"+dbName);
				
		return true;
	}

	public static void createTables() {
		try {
			if (connectionDB()) {

				Statement stmt = null;
				stmt = c.createStatement();
				stmt.setQueryTimeout(30);

				String ArticleTable = "CREATE TABLE IF NOT EXISTS Article (pmcId TEXT PRIMARY KEY NOT NULL,"
						+ " tittle TEXT); ";
				stmt.executeUpdate(ArticleTable);
				//System.out.println("Article table created successfully");

				String traitTable = "CREATE TABLE IF NOT EXISTS TraitTable (tableId TEXT PRIMARY KEY NOT NULL,"
						+ "numOfCol INTEGER NOT NULL," + "numOfRows INTEGER NOT NULL," + "pmcId TEXT NOT NULL,"
						+ "FOREIGN KEY(pmcId) REFERENCES article(pmcId)" + "); ";
				stmt.executeUpdate(traitTable);
				//System.out.println("TraitTable created successfully");

				String abbreviationTable = "CREATE TABLE IF NOT EXISTS Abbreviation" + "(abbreviation TEXT  NOT NULL,"
						+ "expansion TEXT NOT NULL," + "pmcId TEXT NOT NULL,"
						+ "FOREIGN KEY(pmcId) REFERENCES Article(pmcId)" + "); ";
				stmt.executeUpdate(abbreviationTable);
				//System.out.println("AbbreviationTable created successfully");

				String columEntry = "CREATE TABLE IF NOT EXISTS ColumnEntry"
						+ "(colId TEXT PRIMARY KEY NOT NULL, colHeader  TEXT,"
						+ "colType TEXT, colAnnotation TEXT, tableId TEXT, FOREIGN KEY(tableId) REFERENCES TraitTable(tableId)"
						+ "); ";
				stmt.executeUpdate(columEntry);
				//System.out.println("ColumnEntry Table created successfully");

				String cellEntry = "CREATE TABLE IF NOT EXISTS CellEntry"
						+ "(cellId INTEGER PRIMARY KEY  AUTOINCREMENT NOT NULL, rowNumber INT, cellValue  TEXT,"
						+ "cellType TEXT, colId TEXT, FOREIGN KEY(colId) REFERENCES columnEntry(colId)"
						+ "); ";
				stmt.executeUpdate(cellEntry);
				//System.out.println("CellEntry Table created successfully");

				String trait = "CREATE TABLE IF NOT EXISTS Trait"
						+ "(traitId INTEGER PRIMARY KEY  AUTOINCREMENT NOT NULL, traitName Text, traitValuePair  TEXT,"
						+ "traitPropertyPair TEXT, otherTraitPair TEXT, pmcId TEXT, tableId TEXT, rowNumber INTEGER, FOREIGN KEY(pmcId) REFERENCES Article(pmcId), FOREIGN KEY(tableId) REFERENCES TraitTable(tableId)"
						+ "); ";
				stmt.executeUpdate(trait);
				//System.out.println("Trait Table created successfully");

//				String traitValuePair = "CREATE TABLE IF NOT EXISTS TraitValuePair"
//						+ "(id INTEGER PRIMARY KEY  AUTOINCREMENT NOT NULL, traitNameInArticle TEXT,  traitNameInOntology TEXT, traitUri TEXT,"
//						+ "featureNameInArticle TEXT, featureNameInOntology TEXT, featureUri TEXT, featureValue TEXT, featureValueInOntology TEXT, featureValueUri TEXT, pmcId TEXT, doi TEXT , tableId TEXT, FOREIGN KEY(pmcId) REFERENCES Article(pmcId), FOREIGN KEY(tableId) REFERENCES TraitTable(tableId) "
//						+ "); ";
//				stmt.executeUpdate(traitValuePair);
//				System.out.println("Trait-Value Table created successfully");
//
//				String traitPropertyPair = "CREATE TABLE IF NOT EXISTS TraitPropertyPair"
//						+ "(id INTEGER PRIMARY KEY  AUTOINCREMENT NOT NULL, traitNameInArticle TEXT,traitNameInOntology TEXT, traitUri TEXT,"
//						+ "featureNameInArticle TEXT, featureNameInOntology TEXT, featureUri TEXT, featureValue TEXT, featureValueInOntology TEXT, featureValueUri TEXT, pmcId TEXT, doi TEXT , tableId TEXT, FOREIGN KEY(pmcId) REFERENCES Article(pmcId), FOREIGN KEY(tableId) REFERENCES TraitTable(tableId) "
//						+ "); ";
//				stmt.executeUpdate(traitPropertyPair);
//				System.out.println("Trait-Property Table created successfully");
//
//				String oldqtlTable = "CREATE TABLE IF NOT EXISTS OldQtl"
//						+ "(qtlId id INTEGER PRIMARY KEY  AUTOINCREMENT NOT NULL, traitNameInArticle Text,traitNameInOntology Text, traitUri TEXT,"
//						+ "featureNameInArticle TEXT, featureNameInOntology TEXT, featureUri TEXT, featureValue TEXT, featureValueInOntology TEXT, featureValueUri TEXT, pmcId TEXT, doi TEXT , tableId TEXT, FOREIGN KEY(pmcId) REFERENCES Article(pmcId), FOREIGN KEY(tableId) REFERENCES TraitTable(tableId) "
//						+ "); ";
//				stmt.executeUpdate(oldqtlTable);
//				System.out.println("qtlTable Table created successfully");

				String qtlTable = "CREATE TABLE IF NOT EXISTS Qtl"
						+ "(qtlId INTEGER PRIMARY KEY  AUTOINCREMENT NOT NULL, traitNameInArticle Text,traitNameInOntology Text, traitUri TEXT,"
						+ "chromosomeNumber TEXT, markerAssociated TEXT, markerUri TEXT, geneAssociated TEXT, geneUri TEXT, snpAssociated TEXT, snpUri TEXT, pmcId TEXT, tableId TEXT, rowNumber TEXT, FOREIGN KEY(pmcId) REFERENCES Article(pmcId), FOREIGN KEY(tableId) REFERENCES TraitTable(tableId) "
						+ "); ";
				stmt.executeUpdate(qtlTable);
				

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
			//System.out.println("Article length is" + articles.length);

			for (int i = 0; i < articles.length; i++) {
				//System.out.println("Article pmcid is" + articles[i].getPmc());

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

					String insertArticleTable = "INSERT INTO Article VALUES('" + articleID + "','" + articleTitle
							+ "');";
					articlestmt.executeUpdate(insertArticleTable);

					// System.out.println("Entry done for article number" + "\t"
					// + articleID);

					for (String key : articles[i].getAbbreviations().keySet()) {

						String insertAbrevTable = "INSERT INTO Abbreviation VALUES('"
								+ key + "','" + articles[i].getAbbreviations().get(key) + "','" + articleID + "');";
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

								System.out.println("Inserting entries for Table: \t"+ tableID);

								String insertQTLTable = "INSERT INTO TraitTable VALUES('" + tableID + "'," + numofCol
										+ "," + numofRows + ",'" + articleID + "');";
								qtlTablestmt.executeUpdate(insertQTLTable);

								for (Columns col : t.getTableCol()) {

									String colAnno = "";

									try {
										if (col.getColumns_type() == "QTL value") {
											colAnno = solr.tagger.recognize.Evaluate2
													.processString(col.getHeader(), "statoTerms",
															"LONGEST_DOMINANT_RIGHT", "dictionary");
										} else if (col.getColumns_type() == "QTL property") {
											colAnno = solr.tagger.recognize.Evaluate2
													.processString(getOnlyStrings(col.getHeader()), "propTerms",
															"LONGEST_DOMINANT_RIGHT", "dictionary");
											;
										}
									} catch (Exception e) {
										colAnno = "";
										System.out.println("error in column Annotation");
										e.getMessage();
									}

									String insertColTable = "INSERT INTO ColumnEntry(colId,colHeader,colType, colAnnotation, tableId) VALUES('"
											+ col.getColID() + "','" + col.getHeader() + "','" + col.getColumns_type()
											+ "','" + colAnno + "','" + t.getTableid() + "');";

									colstmt.executeUpdate(insertColTable);
									// System.out.println("Col entries inserted
									// in the
									// DB");

									for (Cell cel : col.getcelz()) {

										// checknig if cell exists
										try {
											//System.out.println(cel.getRow_number() + "\t" + cel.getcell_value() + "\t\t" + tableID);
										} catch (NullPointerException e) {
											continue;
										}

										if (cel.getcell_value().indexOf("'") != -1)
											cel.setcell_values(cel.getcell_value().replace("'", "''"));

										if (cel.getcell_value() != null) {

											String insertCellTable = "INSERT INTO CellEntry(rowNumber, cellValue, cellType, colId) VALUES("

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

			//System.out.println("entry inserted into DB successfully");
			// System.exit(0);
		} catch (Exception e) {
			System.out.println("Error is Database entry");
			e.printStackTrace();

		}
	}

	public static void insertTraitEntry(Article articles[]) {
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

					String sql1 = "SELECT cellValue,colId,rowNumber from cellEntry"
					        + " WHERE cellType !='Empty' AND"
					        + " colId IN (SELECT colId FROM columnEntry WHERE colType='QTL descriptor' AND colId LIKE '"
							+ articles[i].getPmc() + "%');";
					ResultSet rs1 = stmt1.executeQuery(sql1);

					while (rs1.next()) {
						String value = rs1.getString("cellValue");
						String colId = rs1.getString("colId");
						String tableId = colId.substring(0, (colId.length() - 2));
						int row = rs1.getInt("rowNumber");

						//System.out.println("**********");
						//System.out.println(value + "\t" + colId + "\t" + tableId + "\t" + row);
						//System.out.println("**********");

						Trait T = new Trait(value);
						JSONObject vals = new JSONObject();
						JSONObject prop = new JSONObject();
						JSONObject otherProp = new JSONObject();

						String sql2 = "SELECT C.cellValue, Col.colHeader,Col.colType, Col.colAnnotation FROM cellEntry AS C INNER JOIN columnEntry AS Col ON C.colId=Col.colId  WHERE rowNumber ="
								+ row + " AND Col.tableId='" + tableId + "' AND Col.colType!='QTL descriptor'";
						ResultSet rs2 = stmt2.executeQuery(sql2);

						while (rs2.next()) {
							String cellValue = rs2.getString("CellValue").replaceAll("\n", "").replace("\r", "");
							String colHeader = rs2.getString("colHeader").replaceAll("\n", "").replace("\r", "");
							String colType = rs2.getString("colType").replaceAll("\n", "").replace("\r", "");
							String colAnno = rs2.getString("colAnnotation").replaceAll("\n", "").replace("\r", "");

							JSONObject colAnnoJSON = new JSONObject();

							if (!"".equals(colAnno)) {

								colAnnoJSON = processSolrOutputtoJson(colAnno);
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

							//System.out.println("Entry " + "\t" + cellValue + "\t" + colHeader + "\t" + colType);

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

						String insertTraitTable = "INSERT INTO Trait(traitName,traitValuePair,traitPropertyPair,otherTraitPair, pmcId, tableId, rowNumber) VALUES('"
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

        public static void insertQtlTable() {
            try {

                    String server = "http://localhost:8983/solr";
                    String core1 = "terms";
                    String core2 = "statoTerms";
                    String core3 = "propTerms";
                    String core4 = "solaLyco";
                    String core5 = "solaLyco2";
                    String match = "LONGEST_DOMINANT_RIGHT";
                    String type = "dictionary";
                    
                    String csvfile=Configs.getPropertyQTM("csvFile");
                    
                  FileWriter QTLTableMinerResultsfile = new FileWriter(csvfile);

                      QTLTableMinerResultsfile.append("qtlId,traitName,traitUri,ChromosomeNumber,"
                              + "markers_associated,markerUri,gene_associated,geneUri,"
                              + "snp_associated,snpUri,pmcId,tableId,rowNumber \n");
                    
                    
                    
                    if (connectionDB()) {
                            Statement stmt1 = null;
                            stmt1 = c.createStatement();
                            Statement stmt2 = null;
                            stmt2 = c.createStatement();
                            Statement stmt3 = null;
                            stmt3 = c.createStatement();
                            String sql1 = "SELECT DISTINCT(pmcId) FROM Trait;";

                            ResultSet rs1 = stmt1.executeQuery(sql1);

                            while (rs1.next()) {

                                    String pmcId = rs1.getString("pmcId");
                                    System.out.println("Finding QTL statements in: \t"+pmcId);
                                    String sql2 = "SELECT * FROM Trait WHERE pmcId LIKE '" + pmcId + "';'";
                                    
                                    
                                    ResultSet rs2 = stmt2.executeQuery(sql2);

                                    while (rs2.next()) {
                                           

                                            String traitName = rs2.getString("TraitName");// .replaceAll("\\(.*\\)",
                                            String traitId = rs2.getString("TraitId"); // "");
                                            String rowNumber = rs2.getString("rowNumber");
                                            String tableId = rs2.getString("tableId");
                                            System.out.println("***");
                                            System.out.println("TraitName: " + traitName);

                                            String traitAnno = "";
                                            traitAnno = solr.tagger.recognize.Evaluate2
                                                            .processString(getOnlyStrings(traitName), core1, match, type);

                                            JSONObject traitAnnoJSON = new JSONObject();

                                            if (!"".equals(traitAnno)) {
                                                    traitAnnoJSON = processSolrOutputtoJson(traitAnno);
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
                                            String snpOntologyAnnotation = "";
                                            String gene_associated = "";
                                            String geneOntologyAnnotation = "";
                                            String markers_associated = "";
                                            String markerOntologyAnnotation = "";
                                            
                                            JSONObject markerJSON = new JSONObject();
                                            JSONObject geneJSON = new JSONObject();
                                            JSONObject snpJSON = new JSONObject();
                                            
                                            
                                            
                                            // Parsing Trait Values

                                            String traitValue = rs2.getString("TraitValuePair");
                                            JSONParser parserV = new JSONParser();
                                            JSONObject tValuesJson = (JSONObject) parserV.parse(traitValue);

                                            for (Iterator iterator = tValuesJson.keySet().iterator(); iterator.hasNext();) {
                                                    String key = (String) iterator.next();
                                                    String value = tValuesJson.get(key).toString();
                                                    JSONParser parser2 = new JSONParser();
                                                    JSONObject statJsonv = (JSONObject) parserV.parse(key);

                                                    // Filter out Chromosome Number
                                                    String REGEX = "chr";
                                                    Pattern pattern = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);
                                                    // System.out.println("pref term is \t"+
                                                    // statJsonv.get("prefTerm") );
                                                    // System.out.println("column heading is \t"+ value
                                                    // );
                                                    // System.out.println("actual value is \t"+
                                                    // statJsonv.get("actualValue") );

                                                    if (statJsonv.get("actualValue").equals("chromosome") || pattern.matcher(value).find()
                                                                    || statJsonv.get("actualValue").equals("chromosome")) {
                                                            ChromosomeNumber += statJsonv.get("actualValue").toString();
                                                    }
                                            }

                                            // Parsing Trait Properties

                                            String traitPro = rs2.getString("TraitPropertyPair");
                                            JSONParser parserP = new JSONParser();
                                            JSONObject tProJson = (JSONObject) parserP.parse(traitPro);

                                            for (Iterator iterator = tProJson.keySet().iterator(); iterator.hasNext();) {
                                                    String key = (String) iterator.next();
                                                    String pvalue = tProJson.get(key).toString();
                                                    JSONParser parser2 = new JSONParser();
                                                    JSONObject statJsonp = (JSONObject) parserP.parse(key);

                                                    // Filter out Chromosome Number
                                                    String regex = "chromo";
                                                    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

                                                    if (statJsonp.get("prefTerm").equals("chromosome") || pattern.matcher(pvalue).find()) {
                                                            ChromosomeNumber += statJsonp.get("actualValue").toString();
                                                    }

                                                    // featureName_asinArticle like '%geno%' or
                                                    // featureName_asinArticle like '%gene%' or
                                                    // featureName_asinArticle like '%marker%' or
                                                    // featureValue like '%sol%' or

                                                    // Filterout SNP
                                                    
                                                    regex = "snp";
                                                    String s1= (String) statJsonp.get("actualValue");
                                                    String s2= (String) statJsonp.get("prefTerm");
                                                    String s3= pvalue;
                                                    //System.out.println(s1);
                                                    //System.out.println(s2);
                                                    //System.out.println(s3);
                                                    
                                                    
                                                    pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                                                    Matcher matcher1 = pattern.matcher(s1);
                                                    Matcher matcher2 = pattern.matcher(s2);
                                                    Matcher matcher3 = pattern.matcher(s3);
                                                    if (matcher1.find() || matcher2.find() || matcher3.find()) {
                                                            //System.out.println(matcher1.find()+"\t"+matcher2.find()+"\t"+matcher3.find());
                                                            snp_associated += statJsonp.get("actualValue").toString()+"; ";
                                                            //System.out.println("snp is"+snp_associated);
                                                            
                                                            
                                                            try {
                                                                snpOntologyAnnotation = solr.tagger.recognize.Evaluate2
                                                                                    .processString(snp_associated, core5, match, type);

                                                            } catch (Exception e) {
                                                                   snpOntologyAnnotation = "";
                                                                    System.out.println("error in solar annotations");

                                                            }

                                                            if (!"".equals(snpOntologyAnnotation)) {

                                                                    snpJSON = processSolrOutputtoJson(snpOntologyAnnotation);

                                                            }

                                                            else {
                                                                    snpJSON.put("icd", "");
                                                                    snpJSON.put("matchingText", snp_associated);
                                                                    snpJSON.put("prefTerm", snp_associated);
                                                                    snpJSON.put("Term", "");
                                                                    snpJSON.put("start", "");
                                                                    snpJSON.put("end", "");
                                                                    snpJSON.put("Uuid", "");
                                                            }
                                                    }

                                                    // Filterout Gene
                                                    regex = "gen[eo]"; 
                                                    pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                                                    //System.out.println(pattern);
                                                    matcher1 = pattern.matcher(s1);
                                                    matcher2 = pattern.matcher(s2);
                                                    matcher3 = pattern.matcher(s3);

                                                    
                                                    if ((matcher1.find() || matcher2.find() || matcher3.find()) || (matcher1.find() || matcher2.find() || matcher3.find())) {
                                                        //System.out.println(matcher1.find()+"\t"+matcher2.find()+"\t"+matcher3.find());
                                                            gene_associated += statJsonp.get("actualValue").toString()+"; ";
                                                            //System.out.println("gene is"+gene_associated);
                                                            
                                                                                                                           
                                                             try {
                                                                geneOntologyAnnotation = solr.tagger.recognize.Evaluate2
                                                                                    .processString(gene_associated, core5, match, type);

                                                            } catch (Exception e) {
                                                                geneOntologyAnnotation = "";
                                                                    System.out.println("error in solar annotations");

                                                            }

                                                            if (!"".equals(geneOntologyAnnotation)) {

                                                                    geneJSON = processSolrOutputtoJson(geneOntologyAnnotation);

                                                            }

                                                            else {
                                                                    geneJSON.put("icd", "");
                                                                    geneJSON.put("matchingText", gene_associated);
                                                                    geneJSON.put("prefTerm", gene_associated);
                                                                    geneJSON.put("Term", "");
                                                                    geneJSON.put("start", "");
                                                                    geneJSON.put("end", "");
                                                                    geneJSON.put("Uuid", "");
                                                            }

                                                            
                                                    }
                                                    
//                                                    regex = "genotype";                                                            
//                                                    pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
//                                                    System.out.println(pattern);
//                                                    matcher1 = pattern.matcher(s1);
//                                                    matcher2 = pattern.matcher(s2);
//                                                    matcher3 = pattern.matcher(s3);
//                                                    if ((matcher1.find() || matcher2.find() || matcher3.find()) || (matcher1.find() || matcher2.find() || matcher3.find())) {
//                                                        System.out.println(matcher1.find()+"\t"+matcher2.find()+"\t"+matcher3.find());
//                                                            gene_associated += statJsonp.get("actualValue").toString()+"; ";
//                                                            System.out.println("gene is"+gene_associated);
//                                                            
//                                                                                                                           
//                                                             try {
//                                                                geneOntologyAnnotation = nl.erasmusmc.biosemantics.tagger.recognize.Evaluate2
//                                                                                    .processString(gene_associated, core5, match, type);
//
//                                                            } catch (Exception e) {
//                                                                geneOntologyAnnotation = "";
//                                                                    System.out.println("error in solar annotations");
//
//                                                            }
//
//                                                            if (!"".equals(geneOntologyAnnotation)) {
//
//                                                                    geneJSON = processSolarOutputtoJson(geneOntologyAnnotation);
//
//                                                            }
//
//                                                            else {
//                                                                    geneJSON.put("icd", "");
//                                                                    geneJSON.put("matchingText", gene_associated);
//                                                                    geneJSON.put("prefTerm", gene_associated);
//                                                                    geneJSON.put("Term", "");
//                                                                    geneJSON.put("start", "");
//                                                                    geneJSON.put("end", "");
//                                                                    geneJSON.put("Uuid", "");
//                                                            }
//
//                                                            
//                                                    }
                                                    
                                                    
                                                    
                                                    
                                                    regex = "solyc";
                                                    pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                                                    //System.out.println(pattern);
                                                    matcher1 = pattern.matcher(s1);
                                                    matcher2 = pattern.matcher(s2);
                                                    matcher3 = pattern.matcher(s3);

                                                    if (matcher1.find() || matcher2.find() || matcher3.find()) {
                                                        //System.out.println(matcher1.find()+"\t"+matcher2.find()+"\t"+matcher3.find());
                                                            gene_associated += statJsonp.get("actualValue").toString()+"; ";
                                                           // System.out.println("gene is"+gene_associated);
                                                            
                                                                                                                                                                                          
                                                            try {
                                                                geneOntologyAnnotation = solr.tagger.recognize.Evaluate2
                                                                                    .processString(gene_associated, core5, match, type);

                                                            } catch (Exception e) {
                                                                geneOntologyAnnotation = "";
                                                                    System.out.println("error in solar annotations");

                                                            }

                                                            if (!"".equals(geneOntologyAnnotation)) {

                                                                    geneJSON = processSolrOutputtoJson(geneOntologyAnnotation);

                                                            }

                                                            else {
                                                                    geneJSON.put("icd", "");
                                                                    geneJSON.put("matchingText", gene_associated);
                                                                    geneJSON.put("prefTerm", gene_associated);
                                                                    geneJSON.put("Term", "");
                                                                    geneJSON.put("start", "");
                                                                    geneJSON.put("end", "");
                                                                    geneJSON.put("Uuid", "");
                                                            }
                                                            
                                                            
                                                            
                                                    }
                                                    
                                                    // Filterout Marker
                                                    regex = "marker";
                                                    pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                                                    matcher1 = pattern.matcher(s1);
                                                    matcher2 = pattern.matcher(s2);
                                                    matcher3 = pattern.matcher(s3);
                                                    if (matcher1.find() || matcher2.find() || matcher3.find()) {
                                                            // marker_start -- marker_end -- peak_marker
                                                            // TEXT
                                                            //System.out.println(matcher1.find()+"\t"+matcher2.find()+"\t"+matcher3.find());
                                                            markers_associated += statJsonp.get("actualValue").toString()+"; ";
                                                            
                                                            
                                                                                                                                                                                          
                                                            try {
                                                                markerOntologyAnnotation = solr.tagger.recognize.Evaluate2
                                                                                    .processString(gene_associated, core5, match, type);

                                                            } catch (Exception e) {
                                                                markerOntologyAnnotation = "";
                                                                    System.out.println("error in solar annotations");

                                                            }

                                                            if (!"".equals(markerOntologyAnnotation)) {

                                                                    markerJSON = processSolrOutputtoJson(markerOntologyAnnotation);

                                                            }

                                                            else {
                                                                    markerJSON.put("icd", "");
                                                                    markerJSON.put("matchingText", gene_associated);
                                                                    markerJSON.put("prefTerm", gene_associated);
                                                                    markerJSON.put("Term", "");
                                                                    markerJSON.put("start", "");
                                                                    markerJSON.put("end", "");
                                                                    markerJSON.put("Uuid", "");
                                                            }
                                                            

                                                    }

                                            }
                                            
                                            String qtlId = tableId +"_" + rowNumber;
                                            
                                            
                                            
                                            
                                            if(markers_associated !="" || gene_associated !="" || snp_associated !=""){
                                            String insertQTLZtable = "INSERT INTO Qtl(traitNameInArticle,traitnameInOntology,traitUri,"
                                                            + "chromosomeNumber,markerAssociated,markerUri, geneAssociated,geneUri, snpAssociated, snpUri, pmcId,tableId,rowNumber)"
                                                            + "VALUES('" + traitName + "','" + traitAnnoJSON.get("prefTerm")
                                                            + "','" + traitAnnoJSON.get("icd") + "','" + ChromosomeNumber + "','" + markers_associated + "','" + markerJSON.get("icd")                                                                
                                                            + "','" + gene_associated + "','" + geneJSON.get("icd") + "','" + snp_associated + "','" + snpJSON.get("icd") + "','" + pmcId + "','" + tableId
                                                            + "','" + rowNumber + "');";
                                            
                                            stmt3.executeUpdate(insertQTLZtable);
                                            
                                            QTLTableMinerResultsfile.append(qtlId + "," + traitName +"," + traitAnnoJSON.get("icd") + "," 
                                                    + ChromosomeNumber + "," + markers_associated + "," + markerJSON.get("icd") + "," 
                                                    + gene_associated + "," + geneJSON.get("icd") + "," + snp_associated + "," + snpJSON.get("icd") + "," + pmcId + "," + tableId
                                                    + "," + rowNumber +  "\n");
                                            
                                            }
                                            
                                            
                                    }
                                    
                                    //System.out.println("pmcid IS"+pmcId);
                            }
                            
                            stmt3.close();
                           stmt2.close(); 
                           stmt1.close();
                           
                    }
                    
                    QTLTableMinerResultsfile.close();
                               } catch (Exception e) {
                    e.printStackTrace();
            }
    }

	
//	public static void insertTraitValuesandTraitProperties(Article articles[]) {
//		try {
//
//			if (connectionDB()) {
//
//				for (int i = 0; i < articles.length; i++) {
//					System.out.println("Traits founds in Article" + articles[i].getPmc());
//
//					String sql1 = "SELECT traitId, traitName, tableId FROM Trait WHERE pmcId LIKE '"
//							+ articles[i].getPmc() + "'; ";
//
//					String server = "http://localhost:8983/solr";
//					String core1 = "terms";
//					String core2 = "statoTerms";
//					String core3 = "propTerms";
//					String core4 = "solaLyco";
//					String core5 = "solaLyco2";
//					String match = "LONGEST_DOMINANT_RIGHT";
//					String type = "dictionary";
//
//					Statement stmt1 = null;
//					Statement stmt2 = null;
//					Statement stmt3 = null;
//
//					stmt1 = c.createStatement();
//					stmt2 = c.createStatement();
//					stmt3 = c.createStatement();
//					ResultSet rs1 = stmt1.executeQuery(sql1);
//
//					while (rs1.next()) {
//
//						String traitName = rs1.getString("traitName");// .replaceAll("\\(.*\\)",
//						String traitId = rs1.getString("traitId"); // "");
//						String tableId = rs1.getString("tableId");
//						// traitName=getOnlyStrings(traitName);
//
//						System.out.println("\nTraitNames " + traitName + "\n");
//
//						String traitAnno = "";
//						try {
//							traitAnno = nl.erasmusmc.biosemantics.tagger.recognize.Evaluate2
//									.processString(getOnlyStrings(traitName), core1, match, type);
//
//						} catch (Exception e) {
//							System.out.println("error in solar annotations");
//						}
//
//						JSONObject traitAnnoJSON = new JSONObject();
//						if (!"".equals(traitAnno)) {
//
//							traitAnnoJSON = processSolarOutputtoJson(traitAnno);
//							System.out.println(traitAnnoJSON.toJSONString());
//						}
//
//						else {
//							traitAnnoJSON.put("icd", "");
//							traitAnnoJSON.put("matchingText", traitName);
//							traitAnnoJSON.put("prefTerm", traitName);
//							traitAnnoJSON.put("Term", "");
//							traitAnnoJSON.put("start", "");
//							traitAnnoJSON.put("end", "");
//							traitAnnoJSON.put("Uuid", "");
//						}
//
//						String sql2 = "SELECT TraitValuePair FROM Trait WHERE traitId LIKE '" + traitId
//								+ "'AND pmcId LIKE '" + articles[i].getPmc() + "'; ";
//
//						ResultSet rs2 = stmt2.executeQuery(sql2);
//
//						while (rs2.next()) {
//							String traitValue = rs2.getString("TraitValuePair");
//							JSONParser parser = new JSONParser();
//							JSONObject tValuesJson = (JSONObject) parser.parse(traitValue);
//
//							for (Iterator iterator = tValuesJson.keySet().iterator(); iterator.hasNext();) {
//								String v = (String) iterator.next();
//								String key = tValuesJson.get(v).toString();
//
//								JSONParser parser2 = new JSONParser();
//								JSONObject statJsonv = (JSONObject) parser.parse(v);
//
//								String insertTraitValue = "INSERT INTO TraitValuePair(traitNameInArticle,traitNameInOntology, traitUri, featureNameInArticle, featureNameInOntology, featureUri, featureValue, pmcId, doi, tableId) VALUES('"
//										+ traitName + "','" + traitAnnoJSON.get("prefTerm") + "','"
//										+ traitAnnoJSON.get("icd") + "','" + key + "','" + statJsonv.get("prefTerm")
//										+ "','" + statJsonv.get("icd") + "','" + statJsonv.get("actualValue") + "','"
//										+ articles[i].getPmc() + "','" + articles[i].getDoi() + "','" + tableId + "');";
//
//								stmt3.executeUpdate(insertTraitValue);
//							}
//
//						}
//
//						String sql3 = "SELECT TraitPropertyPair from Trait WHERE traitId LIKE '" + traitId
//								+ "'AND pmcId LIKE '" + articles[i].getPmc() + "'; ";
//
//						ResultSet rs3 = stmt2.executeQuery(sql3);
//
//						while (rs3.next()) {
//							String traitPro = rs3.getString("TraitPropertyPair");
//							JSONParser parser = new JSONParser();
//							JSONObject tProJson = (JSONObject) parser.parse(traitPro);
//
//							for (Iterator iterator = tProJson.keySet().iterator(); iterator.hasNext();) {
//								String p = (String) iterator.next();
//								String key = tProJson.get(p).toString();
//								JSONParser parser2 = new JSONParser();
//								JSONObject statJsonp = (JSONObject) parser.parse(p);
//
//								String feature_value = statJsonp.get("actualValue").toString();
//
//								String feature_valueAnnotation = "";
//								try {
//									feature_valueAnnotation = nl.erasmusmc.biosemantics.tagger.recognize.Evaluate2
//											.processString(feature_value, core5, match, type);
//
//								} catch (Exception e) {
//									feature_valueAnnotation = "";
//									System.out.println("error in solar annotations");
//
//								}
//
//								JSONObject feature_value_JSON = new JSONObject();
//								if (!"".equals(feature_valueAnnotation)) {
//
//									feature_value_JSON = processSolarOutputtoJson(feature_valueAnnotation);
//
//								}
//
//								else {
//									feature_value_JSON.put("icd", "");
//									feature_value_JSON.put("matchingText", feature_value);
//									feature_value_JSON.put("prefTerm", feature_value);
//									feature_value_JSON.put("Term", "");
//									feature_value_JSON.put("start", "");
//									feature_value_JSON.put("end", "");
//									feature_value_JSON.put("Uuid", "");
//								}
//
//								String insertTraitValue = "INSERT INTO TraitPropertyPair(traitNameInArticle,traitNameInOntology, traitUri, featureNameInArticle, featureNameInOntology, featureUri, featureValue, featureValueInOntology, featureValueUri, pmcId, doi, tableId) VALUES('"
//										+ traitName + "','" + traitAnnoJSON.get("prefTerm") + "','"
//										+ traitAnnoJSON.get("icd") + "','" + key + "','" + statJsonp.get("prefTerm")
//										+ "','" + statJsonp.get("icd") + "','" + statJsonp.get("actualValue") + "','"
//										+ feature_value_JSON.get("prefTerm") + "','" + feature_value_JSON.get("icd")
//										+ "','" + articles[i].getPmc() + "','" + articles[i].getDoi() + "','" + tableId
//										+ "');";
//
//								stmt3.executeUpdate(insertTraitValue);
//							}
//
//						}
//
//						stmt3.close();
//					}
//
//					// }
//
//					// }
//					//
//
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}
//
//	public static void insertQTLTable() {
//		try {
//
//			if (connectionDB()) {
//				String sql1 = "SELECT DISTINCT traitNameInArticle,traitNameINOntology,traitUri, featureNameInArticle, featureNameInOntology, featureUri, featureValue, featureValueInontology, featureValueUri,pmcId,doi,tableId from traitPropertyPair WHERE (featureNameInArticle like '%geno%' or featureNameInArticle like '%gene%' or featureNameInArticle like '%marker%' or featureValue like '%sol%' or featureValue like '%snp%' ) and (featureValue not like '%(%)%' and  featureValue not like '%/%') ";
//
//				Statement stmt1 = null;
//				Statement stmt2 = null;
//
//				stmt1 = c.createStatement();
//				stmt2 = c.createStatement();
//				ResultSet rs1 = stmt1.executeQuery(sql1);
//
//				FileWriter QTLTableMinerResultsfile = new FileWriter("QTLTableMiner++Results.csv");
//
//				QTLTableMinerResultsfile.append("qtl_id,traitName_asinArticle,traitName_ontologyTerm,traitUri,"
//						+ "featureName_asinArticle,featureName_ontologyTerm,featureUri_ontologyId,"
//						+ "featureValue,featureValue_ontologyTerm,featureValueUri_ontologyId," + "pmcId,doi,tableId\n");
//
//				int i = 0;
//				while (rs1.next()) {
//					String traitName_asinArticle = rs1.getString("traitName_asinArticle");
//					String traitName_ontologyTerm = rs1.getString("traitName_ontologyTerm");
//					String traitUri = rs1.getString("traitUri");
//					String featureName_asinArticle = rs1.getString("featureName_asinArticle");
//					String featureName_ontologyTerm = rs1.getString("featureName_ontologyTerm");
//					String featureUri_ontologyId = rs1.getString("featureUri_ontologyId");
//					String featureValue = rs1.getString("featureValue");
//					String featureValue_ontologyTerm = rs1.getString("featureValue_ontologyTerm");
//					String featureValueUri_ontologyId = rs1.getString("featureValueUri_ontologyId");
//					String pmcId = rs1.getString("pmcId");
//					String doi = rs1.getString("doi");
//					String tableId = rs1.getString("tableId");
//					String qtlId = tableId + "_" + i;
//
//					QTLTableMinerResultsfile.append(qtlId + "," + traitName_asinArticle + "," + traitName_ontologyTerm
//							+ "," + traitUri + "," + featureName_asinArticle + "," + featureName_ontologyTerm + ","
//							+ featureUri_ontologyId + "," + featureValue + "," + featureValue_ontologyTerm + ","
//							+ featureValueUri_ontologyId + "," + pmcId + "," + doi + "," + tableId + "\n");
//
//					String insertQTLtable = "INSERT INTO OldQtl(qtlId, traitNameInArticle,traitNameInOntology,traitUri,"
//							+ "featureNameInArticle,featureNameInOntology,featureUri,featureValue,featureValueInontology,"
//							+ "featureValueUri,pmcId,doi,tableId) VALUES('" + qtlId + "','"
//							+ traitName_asinArticle + "','" + traitName_ontologyTerm + "','" + traitUri + "','"
//							+ featureName_asinArticle + "','" + featureName_ontologyTerm + "','" + featureUri_ontologyId
//							+ "','" + featureValue + "','" + featureValue_ontologyTerm + "','"
//							+ featureValueUri_ontologyId + "','" + pmcId + "','" + doi + "','" + tableId + "');";
//					stmt2.executeUpdate(insertQTLtable);
//					stmt2.close();
//					i++;
//
//				}
//
//				if (QTLTableMinerResultsfile != null) {
//					QTLTableMinerResultsfile.flush();
//					QTLTableMinerResultsfile.close();
//
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}


	public static boolean isArticleEntryAlredyIn(Article a, Connection c) {
		Boolean check = false;
		try {
			Statement stmt = null;
			stmt = c.createStatement();

			// System.out.println("I am here");
			String sql = "SELECT pmcId FROM Article";
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

	public static JSONObject processSolrOutputtoJson(String output) {
		//System.out.println("\n" + output);
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