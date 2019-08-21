/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.esciencecenter.resultDb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;

import nl.esciencecenter.qtm.Article;
import nl.esciencecenter.qtm.Cell;
import nl.esciencecenter.qtm.Columns;
import nl.esciencecenter.qtm.Main;
import nl.esciencecenter.qtm.Table;
import nl.esciencecenter.qtm.Trait;
import nl.esciencecenter.solr.tagger.utils.TagItem;
import nl.esciencecenter.solr.tagger.utils.TagResponse;
import nl.esciencecenter.utils.Configs;

/**
 *
 * @author gurnoor
 */
public class QtlDb {

	public static Connection conn = null;

	public static String dbDriver = Configs.getPropertyQTM("dbDriver");
	public static String dbFile = Configs.getPropertyQTM("dbFile");
	private static String solrRun = Configs.getPropertyQTM("solrRun");
	private static String coreTraitDescriptors = Configs.getPropertyQTM("coreTraitDescriptors");
	private static String coreTraitValues = Configs.getPropertyQTM("coreTraitValues");
	private static String coreTraitProperties = Configs.getPropertyQTM("coreTraitProperties");

	private static String match = Configs.getPropertyQTM("match");
	private static String type = Configs.getPropertyQTM("type");

	public static boolean connectionDB() {
		if (conn == null) {
			try {
				String sDBUrl = dbDriver + ":" + dbFile;
				conn = DriverManager.getConnection(sDBUrl);
			} catch (Exception e) {
				Main.logger.error("Can't connect to the database: ", e);
				System.exit(1);
			}
		}
		return true;
	}

	public static void createTables() {
		try {
			Main.logger.info("Populating '" + dbFile + "' database...");
			if (connectionDB()) {
				String[] cmdLine = new String[]{"bash", "-c", "sqlite3 " + dbFile + "< db_schema.sql"};
				Process p = Runtime.getRuntime().exec(cmdLine);
				p.waitFor();
			}
		} catch (Exception e) {
			Main.logger.error("Failed to populate '" + dbFile + "' database: ", e);
			System.exit(1);
		}
	}

	public static String getOnlyStrings(String s) {
		Pattern pattern = Pattern.compile("[^a-z A-Z 0-9]");
		Matcher matcher = pattern.matcher(s);
		String number = matcher.replaceAll("");
		return number;
	}

	public static void insertArticleEntry(Article article) {
		try {
			if (connectionDB() & isArticleEntryAlredyIn(article) == false) {
				Statement abbrevstmt = null;
				abbrevstmt = conn.createStatement();
				Statement getRowidStmt = null;
				getRowidStmt = conn.createStatement();

				// Article Table entry
				Scanner id = new Scanner(article.getPmcid()).useDelimiter("[^0-9]+");
				int pmc_id = id.nextInt();
				String pmc_tittle = article.getTitle();
				try {
					String insertArticleTable = "INSERT INTO ARTICLE VALUES (?,?,?)";
					PreparedStatement articlestmt = conn.prepareStatement(insertArticleTable);
					articlestmt.setInt(1, pmc_id);
					try {
						articlestmt.setString(2, pmc_tittle);
					} catch (NullPointerException e) {
						articlestmt.setNull(2, java.sql.Types.VARCHAR);
					}

					try {
						articlestmt.setString(3, article.getDoi());
					} catch (NullPointerException e) {
						articlestmt.setNull(3, java.sql.Types.VARCHAR);
					}

					articlestmt.executeUpdate();
					articlestmt.close();
				} catch (SQLException e) {
					Main.logger.error("Article with " + pmc_id + " exists already!", e);
				}

				String insertAbrevTable = "INSERT INTO ABBREVIATION (abbrev,expansion,pmc_id) VALUES (?,?,?)";
				PreparedStatement abbrevTableStmt = conn.prepareStatement(insertAbrevTable);
				for (String key : article.getAbbreviations().keySet()) {
					abbrevTableStmt.setString(1, key);
					abbrevTableStmt.setString(2, article.getAbbreviations().get(key));
					abbrevTableStmt.setInt(3, pmc_id);
					abbrevTableStmt.executeUpdate();
				}
				abbrevTableStmt.close();

				// QTL table entries
				for (Table t : article.getTables()) {

					// checking if table exists
					try {
						if (t.isaTraitTable()) {
							Main.logger.debug("Inserting entries into TRAIT_TABLE for table " + t.getTabnum() + " of "
									+ article.getPmcid());

							String insertTraitTable = "INSERT INTO TRAIT_TABLE (tab_lb,pmc_id) VALUES (?,?)";
							PreparedStatement traitTableStmt = conn.prepareStatement(insertTraitTable);

							traitTableStmt.setInt(1, t.getTabnum());
							traitTableStmt.setInt(2, pmc_id);

							traitTableStmt.executeUpdate();

							traitTableStmt.close();

							String getTabid = "SELECT MAX(tab_id) AS tid FROM TRAIT_TABLE";

							ResultSet rs1 = getRowidStmt.executeQuery(getTabid);
							int tab_id = rs1.getInt("tid");

							for (Columns col : t.getTableCol()) {

								TagResponse colAnno = new TagResponse();
								String colHeader = col.getHeader().replaceAll("[^\\w]", "");
								try {
									if (col.getColumns_type() == "QTL value") {
										colAnno = nl.esciencecenter.solr.tagger.recognize.Evaluate
												.processString(colHeader, coreTraitValues, match, type);

									} else if (col.getColumns_type() == "QTL property") {
										colAnno = nl.esciencecenter.solr.tagger.recognize.Evaluate
												.processString(colHeader, coreTraitProperties, match, type);
									}
								} catch (Exception e) {
									Main.logger.error("Error in column annotation " + colHeader, e);
								}

								String insertColTable = "INSERT INTO COLUMN_ENTRY (tab_id,header,type,annot) VALUES (?,?,?,?)";
								PreparedStatement colStmt = conn.prepareStatement(insertColTable);
								colStmt.setInt(1, tab_id);

								try {
									if (colHeader == "" | colHeader == " ")
										colHeader = null;
									colStmt.setString(2, colHeader);
								} catch (NullPointerException e) {
									colStmt.setNull(2, java.sql.Types.VARCHAR);
								}

								try {
									colStmt.setString(3, col.getColumns_type());
								} catch (NullPointerException e) {
									colStmt.setNull(3, java.sql.Types.VARCHAR);
								}

								String colAnnoUri = "";
								if (colAnno.getItems().size() == 1)
									colAnnoUri = colAnno.getItems().get(0).getIcd10();
								else {
									for (TagItem item : colAnno.getItems()) {
										colAnnoUri += item.getIcd10() + ";";
										// Main.logger.trace("%%%" +
										// item.getIcd10());
									}
								}

								try {
									if (colAnnoUri == "")
										colAnnoUri = null;
									colStmt.setString(4, colAnnoUri);
								} catch (NullPointerException e) {
									colStmt.setNull(4, java.sql.Types.VARCHAR);
								}

								colStmt.executeUpdate();
								colStmt.close();

								String getColid = "SELECT MAX(col_id) AS cid FROM COLUMN_ENTRY;";

								ResultSet rs2 = getRowidStmt.executeQuery(getColid);
								int col_id = rs2.getInt("cid");

								for (Cell cel : col.getcelz()) {

									if (cel.getcell_value().indexOf("'") != -1)
										cel.setcell_values(cel.getcell_value().replace("'", "''"));

									if (cel.getcell_value().equals("") || cel.getcell_value().equals(" "))
										cel.setcell_values(null);

									String insertCellTable = "INSERT INTO CELL_ENTRY (row_id,col_id,value) VALUES (?,?,?)";

									PreparedStatement cellStmt = conn.prepareStatement(insertCellTable);

									cellStmt.setInt(1, cel.getRow_number() + 1);
									cellStmt.setInt(2, col_id);

									try {
										cellStmt.setString(3, cel.getcell_value());
									} catch (NullPointerException e) {
										cellStmt.setNull(3, java.sql.Types.VARCHAR);
									}

									cellStmt.executeUpdate();
									cellStmt.close();
								}
							}
						}
					} catch (NullPointerException e) {
						continue;
					}
				}

			} else {
				Main.logger.debug(article.getPmcid() + " already exists!");
			}
		} catch (Exception e) {
			Main.logger.error("Error in Insert Article Function");
			e.printStackTrace();
		}
	}

	public static void insertQTLEntry() {
		try {
			if (connectionDB()) {
				Statement stmtSelectTrait = null;
				stmtSelectTrait = conn.createStatement();

				List<String> coreGenes = new ArrayList<String>();
				List<String> coreMarkers = new ArrayList<String>();

				coreGenes = (Arrays.asList(Configs.getPropertyQTM("coreGenes").split(";")));
				coreMarkers = (Arrays.asList(Configs.getPropertyQTM("coreMarkers").split(";")));

				String sql1 = "SELECT * FROM CELL_ENTRY INNER JOIN COLUMN_ENTRY USING (col_id) "
						+ " WHERE value IS NOT NULL AND type='QTL descriptor'";
				String sql2 = "SELECT * FROM CELL_ENTRY INNER JOIN COLUMN_ENTRY USING (col_id) "
						+ " WHERE tab_id = ? AND row_id = ? AND type != 'QTL descriptor'";
				ResultSet rs1 = stmtSelectTrait.executeQuery(sql1);
				while (rs1.next()) {
					String chrom = "";
					String genes = "";
					String markers = "";
					String genesUri = "";
					String markersUri = "";
					String possibleTrait = rs1.getString("value");
					int tabId = rs1.getInt("tab_id");
					int colId = rs1.getInt("col_id");
					int rowId = rs1.getInt("row_id");
					Trait T = new Trait(possibleTrait);
					TagResponse traitAnno = nl.esciencecenter.solr.tagger.recognize.Evaluate
							.processString(getOnlyStrings(T.getTraitName()), coreTraitDescriptors, match, type);
					PreparedStatement stmtSelectPropertiesandValues = conn.prepareStatement(sql2);
					stmtSelectPropertiesandValues.setInt(1, tabId);
					stmtSelectPropertiesandValues.setInt(2, rowId);
					ResultSet rs2 = stmtSelectPropertiesandValues.executeQuery();

					while (rs2.next()) {
						String cellValue = "";
						String colHeader = "";
						String colType = "";

						try {
							cellValue = rs2.getString("value").replaceAll("\n", "").replace("\r", "");
							colHeader = rs2.getString("header").replaceAll("\n", "").replace("\r", "");
							colType = rs2.getString("type").replaceAll("\n", "").replace("\r", "");

						} catch (NullPointerException e) {
						}

						if (colType.equals("QTL value")) {
							String regex = "chr";
							Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
							Matcher matcher1 = pattern.matcher(cellValue);
							Matcher matcher2 = pattern.matcher(colHeader);

							if (matcher1.find() || matcher2.find()) {
								chrom += cellValue.toString();
							}
						} else if (colType.equals("QTL property")) {
							// Ontology based annotation of markers.
							try {

								for (String core : coreMarkers) {
									TagResponse markerAnno = new TagResponse();
									markerAnno = nl.esciencecenter.solr.tagger.recognize.Evaluate
											.processString(cellValue, core, match, type);

									if (!"".equals(markerAnno.toString())) {
										markers += cellValue;
										if (markerAnno.getItems().size() == 1) {
											markersUri += markerAnno.getItems().get(0).getIcd10() + ";";
										} else if (markerAnno.getItems().size() > 1) {
											for (TagItem item : markerAnno.getItems()) {
												markersUri += item.getIcd10() + ";";
											}
										}
									}
								}
							} catch (Exception e) {
								Main.logger.error("Error in annotating markers: ", e);
							}

							try {

								for (String core : coreGenes) {
									TagResponse geneAnno = new TagResponse();
									geneAnno = nl.esciencecenter.solr.tagger.recognize.Evaluate.processString(cellValue,
											core, match, type);

									if (!"".equals(geneAnno.toString())) {
										genes += cellValue;
										if (geneAnno.getItems().size() == 1)
											genesUri += geneAnno.getItems().get(0).getIcd10() + ";";
										else if (geneAnno.getItems().size() > 1) {
											for (TagItem item : geneAnno.getItems()) {
												genesUri += item.getIcd10() + ";";
											}
										}
									}
								}
							} catch (Exception e) {
								Main.logger.error("Error in annotating genes: ", e);
							}
						}
					}
					stmtSelectPropertiesandValues.close();

					if (!"".equals(markers) || !"".equals(genes)) {

						String traitAnnoUri = "";
						String traitOntoName = "";

						if (traitAnno.getItems().size() == 1) {
							traitAnnoUri = traitAnno.getItems().get(0).getIcd10();
							traitOntoName = traitAnno.getItems().get(0).getPrefTerm();
						} else if (traitAnno.getItems().size() > 1) {
							for (TagItem item_trait : traitAnno.getItems()) {
								traitAnnoUri += item_trait.getIcd10() + ";";
								if (!traitOntoName.contains(item_trait.getPrefTerm()))
									traitOntoName += item_trait.getPrefTerm() + ";";
							}
							traitOntoName = traitOntoName.substring(0, traitOntoName.length() - 1);
							traitAnnoUri = traitAnnoUri.substring(0, traitAnnoUri.length() - 1);
						}

						String insertTableSQL = "INSERT INTO QTL "
								+ "(tab_id,row_id, trait_in_article,trait_in_onto,trait_uri,chromosome,marker,marker_uri, gene,gene_uri) "
								+ "VALUES (?,?,?,?,?,?,?,?,?,?)";
						PreparedStatement stmtInsertQTL = conn.prepareStatement(insertTableSQL);

						stmtInsertQTL.setDouble(1, tabId);
						stmtInsertQTL.setInt(2, rowId);

						try {
							stmtInsertQTL.setString(3, T.getTraitName());
						} catch (NullPointerException e) {
							stmtInsertQTL.setNull(3, java.sql.Types.VARCHAR);
						}

						try {
							stmtInsertQTL.setString(4, traitOntoName);
						} catch (NullPointerException e) {
							stmtInsertQTL.setNull(4, java.sql.Types.VARCHAR);
						}
						try {
							stmtInsertQTL.setString(5, traitAnnoUri);
						} catch (NullPointerException e) {
							stmtInsertQTL.setNull(5, java.sql.Types.VARCHAR);
						}
						try {
							stmtInsertQTL.setString(6, chrom);
						} catch (NullPointerException e) {
							stmtInsertQTL.setNull(6, java.sql.Types.VARCHAR);
						}

						try {
							if (markers == "") {
								markers = null;
							}
							stmtInsertQTL.setString(7, markers);
						} catch (NullPointerException e) {
							stmtInsertQTL.setNull(7, java.sql.Types.VARCHAR);
						}

						try {
							if (markersUri == "") {
								markersUri = null;
							} else {
								markersUri = markersUri.substring(0, markersUri.length() - 1);
							}
							stmtInsertQTL.setString(8, markersUri);
						} catch (NullPointerException e) {
							stmtInsertQTL.setNull(8, java.sql.Types.VARCHAR);
						}
						try {
							if (genes == "") {
								genes = null;
							}
							stmtInsertQTL.setString(9, genes);
						} catch (NullPointerException e) {
							stmtInsertQTL.setNull(9, java.sql.Types.VARCHAR);
						}
						try {
							if ("".equals(genesUri)) {
								genesUri = null;
							} else {
								genesUri = genesUri.substring(0, genesUri.length() - 1);
							}
							stmtInsertQTL.setString(10, genesUri);
						} catch (NullPointerException e) {
							stmtInsertQTL.setNull(10, java.sql.Types.VARCHAR);
						}

						try {
							stmtInsertQTL.executeUpdate();
						} catch (SQLException e) {
							Main.logger.error("Exception is :::", e);
						}
						stmtInsertQTL.close();
					}
				}
				stmtSelectTrait.close();
			}
		} catch (Exception e) {
			Main.logger.error("Error is :::", e);
		}
	}

	public static boolean isPmcIdAlredyInDb(String pmc) {
		Boolean check = false;
		Scanner id = new Scanner(pmc).useDelimiter("[^0-9]+");
		int pid = id.nextInt();

		try {
			Statement stmt = null;
			stmt = conn.createStatement();

			String sql = "SELECT pmc_id FROM ARTICLE";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				int pmcId = rs.getInt("pmc_id");
				if (pmcId == pid) {
					check = true;
					return check;
				}
			}
		} catch (SQLException e) {
			Main.logger.error("Error is :::", e);
		}
		return check;
	}

	public static boolean isArticleEntryAlredyIn(Article a) {
		Boolean check = false;
		try {
			Statement stmt = null;
			stmt = conn.createStatement();

			String sql = "SELECT pmc_id FROM ARTICLE";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				int pmcId = rs.getInt("pmc_id");

				Scanner id = new Scanner(a.getPmcid()).useDelimiter("[^0-9]+");
				int pid = id.nextInt();

				if (pmcId == pid) {
					check = true;
					return check;
				}
			}
		} catch (SQLException e) {
			Main.logger.error("Error is :::", e);
		}
		return check;
	}

	public static JSONObject processSolrOutputtoJson(String output) {
		Main.logger.debug("\n" + output);
		String[] s = output.split(Pattern.quote("|"));
		// use HashMap to parameterize for JSONObject
		HashMap<String, Object> jm = new HashMap<String, Object>();
		jm.put("icd", s[0]);
		jm.put("matchingText", s[1]);
		jm.put("prefTerm", s[2]);
		jm.put("Term", s[3]);
		jm.put("start", s[4]);
		jm.put("end", s[5]);
		jm.put("Uuid", s[6]);
		JSONObject j = new JSONObject(jm);
		return j;
	}

	public static int numberofTraitTable() {
		int tables = 0;
		try {
			Statement stmt = null;
			stmt = conn.createStatement();
			String sql = "SELECT COUNT(*) AS n FROM TRAIT_TABLE";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				tables = rs.getInt("n");
			}
		} catch (SQLException e) {
			Main.logger.error("Error is :::", e);
		}
		return tables;
	}

	public static int numberofQTL() {
		int numQTL = 0;
		try {
			Statement stmt = null;
			stmt = conn.createStatement();
			String sql = "SELECT COUNT(*) AS n FROM QTL";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				numQTL = rs.getInt("n");
			}
		} catch (SQLException e) {
			Main.logger.error("Error is :::", e);
		}
		return numQTL;
	}
}
