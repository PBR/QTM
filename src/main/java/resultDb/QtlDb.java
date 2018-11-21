/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package resultDb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;

import nl.esciencecenter.qtm.Article;
import nl.esciencecenter.qtm.Cell;
import nl.esciencecenter.qtm.Columns;
import nl.esciencecenter.qtm.Table;
import nl.esciencecenter.qtm.Trait;
import solr.tagger.utils.TagItem;
import solr.tagger.utils.TagResponse;
import utils.Configs;

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

				Class.forName("org.sqlite.JDBC");

				String sDBUrl = dbDriver + ":" + dbFile;
				conn = DriverManager.getConnection(sDBUrl);

			} catch (Exception e) {
				System.out
						.println("Error in connecting to the output database");
				e.printStackTrace();
				System.err.println(
						e.getClass().getName() + ": " + e.getMessage());
				System.exit(0);
			}
			return true;
		} else
			return true;
	}

	public static void createTables() {
		try {
			if (connectionDB()) {
				Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c",
						"sqlite3 " + QtlDb.dbFile + "< db_schema.sql"});
				p.waitFor();

			}
		} catch (Exception e) {
			System.out.println("Error in creating database tables");
			e.printStackTrace();
		}
	}

	public static String getOnlyStrings(String s) {
		Pattern pattern = Pattern.compile("[^a-z A-Z]");
		Matcher matcher = pattern.matcher(s);
		String number = matcher.replaceAll("");
		return number;
	}

	public static void insertArticleEntry(Article article) {

		// System.out.println("Article length is" + articles.length);
		// System.out.println("Article pmcid is" + articles[i].getPmc());
		try {
			if (connectionDB() & isArticleEntryAlredyIn(article) == false) {

				Statement abbrevstmt = null;
				abbrevstmt = conn.createStatement();

				Statement getRowidStmt = null;
				getRowidStmt = conn.createStatement();

				// Article Table entry
				Scanner id = new Scanner(article.getPmc())
						.useDelimiter("[^0-9]+");
				int pmc_id = id.nextInt();
				String pmc_tittle = article.getTitle();

				try {

					String insertArticleTable = "INSERT INTO ARTICLE VALUES"
							+ "(?,?,?)";

					PreparedStatement articlestmt = conn
							.prepareStatement(insertArticleTable);

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
					// e.printStackTrace();
					System.out.println(
							"*************************************************");

					System.out.println(
							"Article already exits, Please provide unique entries");

					System.out.println(
							"*************************************************");

				}
				// System.out.println("Entry done for article number" + "\t"
				// + articleID);

				for (String key : article.getAbbreviations().keySet()) {

					String insertAbrevTable = "INSERT INTO ABBREVIATION VALUES('"
							+ key + "','" + article.getAbbreviations().get(key)
							+ "','" + pmc_id + "');";
					abbrevstmt.executeUpdate(insertAbrevTable);
				}
				abbrevstmt.close();
				// System.out.println("Abbreviation entries inserted in the
				// DB");
				// QTL table entries
				for (Table t : article.getTables()) {

					// checking if table exists
					try {
						if (t.isaTraitTable()) {

							System.out.println(
									"Inserting entries into TRAIT_TABLE for: \t"
											+ "Table Number: " + t.getTabnum()
											+ " of " + article.getPmc());

							String insertTraitTable = "INSERT INTO TRAIT_TABLE (tab_lb, pmc_id) VALUES"
									+ "(?,?); ";

							PreparedStatement traitTableStmt = conn
									.prepareStatement(insertTraitTable);

							traitTableStmt.setInt(1, t.getTabnum());
							traitTableStmt.setInt(2, pmc_id);

							traitTableStmt.executeUpdate();

							traitTableStmt.close();

							String getTabid = "SELECT MAX(tab_id) FROM TRAIT_TABLE";

							ResultSet rs1 = getRowidStmt.executeQuery(getTabid);
							int tab_id = rs1.getInt("max(tab_id)");

							for (Columns col : t.getTableCol()) {

								TagResponse colAnno = new TagResponse();
								String colHeader = col.getHeader()
										.replaceAll("[^\\w]", "");
								try {
									if (col.getColumns_type() == "QTL value") {
										colAnno = solr.tagger.recognize.Evaluate
												.processString(colHeader, coreTraitValues,
														match, type);

									} else if (col
											.getColumns_type() == "QTL property") {
										colAnno = solr.tagger.recognize.Evaluate
												.processString(colHeader, coreTraitProperties,
														match, type);
									}
								} catch (Exception e) {
									e.getStackTrace();
									System.out.println(
											"Error in column Annotation"
													+ colHeader);
								}

								String insertColTable = "INSERT INTO COLUMN_ENTRY (tab_id, header,type, annot) VALUES"
										+ "(?,?,?,?)";

								PreparedStatement colStmt = conn
										.prepareStatement(insertColTable);

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
									colAnnoUri = colAnno.getItems().get(0)
											.getIcd10();
								else {
									for (TagItem item : colAnno.getItems()) {
										colAnnoUri += item.getIcd10() + ";";
										// System.out.println("%%%" +
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

								String getColid = "SELECT MAX(col_id) FROM COLUMN_ENTRY;";

								ResultSet rs2 = getRowidStmt
										.executeQuery(getColid);
								int col_id = rs2.getInt("max(col_id)");

								for (Cell cel : col.getcelz()) {

									if (cel.getcell_value().indexOf("'") != -1)
										cel.setcell_values(cel.getcell_value()
												.replace("'", "''"));

									if (cel.getcell_value().equals("")
											|| cel.getcell_value().equals(" "))
										cel.setcell_values(null);

									String insertCellTable = "INSERT INTO CELL_ENTRY (row_id, col_id, value) VALUES"
											+ "(?, ?, ?)";

									PreparedStatement cellStmt = conn
											.prepareStatement(insertCellTable);

									cellStmt.setInt(1, cel.getRow_number() + 1);
									cellStmt.setInt(2, col_id);

									try {
										cellStmt.setString(3,
												cel.getcell_value());
									} catch (NullPointerException e) {
										cellStmt.setNull(3,
												java.sql.Types.VARCHAR);
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
				System.out.println(article.getPmc() + " already exists");
			}

			// System.out.println("entry inserted into DB successfully");
			// System.exit(0);
		} catch (Exception e) {
			System.out.println("Error is Insert Article Function");
			e.printStackTrace();
		}
	}

	public static void insertQTLEntry() {
		try {
			if (connectionDB()) {
				Statement stmtSelectTrait = null;
				stmtSelectTrait = conn.createStatement();

				List<String> cores_genes=new ArrayList<String>();
				List<String> cores_markers=new ArrayList<String>();

				cores_genes= (Arrays.asList(Configs.getPropertyQTM("cores_genes").split(";")));
				cores_markers= (Arrays.asList(Configs.getPropertyQTM("cores_markers").split(";")));

				// List<Trait> traits = articles[i].getTrait();

				String sql1 = "SELECT  Cel.value, Cel.col_id, Cel.row_id, Col.tab_id, Col.type from CELL_ENTRY AS Cel INNER JOIN COLUMN_ENTRY AS Col ON Cel.col_id=Col.col_id"
						+ " WHERE Cel.value IS NOT null AND "
						+ " Col.type='QTL descriptor'";

				ResultSet rs1 = stmtSelectTrait.executeQuery(sql1);
				while (rs1.next()) {
					String possibleTrait = rs1.getString("value");
					int colId = rs1.getInt("col_id");
					int tableId = rs1.getInt("tab_id");
					int rowId = rs1.getInt("row_id");

					// System.out.println("**********");
					// System.out.println(possibleTrait + "\t" + colId + "\t"
					// + tableId + "\t" + rowId);
					// System.out.println("**********");

					Trait T = new Trait(possibleTrait);

					TagResponse traitAnno = solr.tagger.recognize.Evaluate
							.processString(getOnlyStrings(T.getTraitName()),
									coreTraitDescriptors, match, type);

					Statement stmtSelectPropertiesandValues = null;
					stmtSelectPropertiesandValues = conn.createStatement();

					String ChromosomeNumber = "";
					String genes_associated = "";

					String markers_associated = "";



					String sql2 = "SELECT Cel.Value, Col.header,Col.type FROM CELL_ENTRY AS Cel INNER JOIN COLUMN_ENTRY AS Col ON Cel.col_id=Col.col_id"
							+ " WHERE row_id =" + rowId + " AND"
							+ " Col.tab_id='" + tableId + "' AND"
							+ " Col.Type!='QTL descriptor' ;";
					ResultSet rs2 = stmtSelectPropertiesandValues.executeQuery(sql2);

					String genesUri = "";
					String markersUri = "";

					while (rs2.next()) {
						String cellValue = "";
						String colHeader = "";
						String colType = "";

						try {
							cellValue = rs2.getString("value")
									.replaceAll("\n", "").replace("\r", "");
							colHeader = rs2.getString("header")
									.replaceAll("\n", "").replace("\r", "");
							colType = rs2.getString("type").replaceAll("\n", "")
									.replace("\r", "");

						} catch (NullPointerException e) {
						}

						// System.out.println("Entry " + "\t" + cellValue + "\t"
						// + colHeader + "\t" + colType);

						if (colType.equals("QTL value")) {
							// System.out.println("cell Values is" +
							// cellValue);

							String regex = "chr";
							Pattern pattern = Pattern.compile(regex,
									Pattern.CASE_INSENSITIVE);

							Matcher matcher1 = pattern.matcher(cellValue);
							Matcher matcher2 = pattern.matcher(colHeader);

							if (matcher1.find() || matcher2.find()) {
								ChromosomeNumber += cellValue.toString();
							}

							// vals.put(colAnnoJSON, colHeader);

						} else if (colType.equals("QTL property")) {

							// Ontology based annotation of markers.

							try {

								for (String core : cores_markers) {
									TagResponse markerAnno = new TagResponse();
									markerAnno = solr.tagger.recognize.Evaluate
											.processString(cellValue, core,
													match, type);

									if (!"".equals(markerAnno.toString())) {
										markers_associated += cellValue;
										if (markerAnno.getItems().size() == 1)
											markersUri += markerAnno.getItems()
													.get(0).getIcd10() + ";";
										else if (markerAnno.getItems()
												.size() > 1) {
											for (TagItem item : markerAnno
													.getItems()) {
												markersUri += item.getIcd10()
														+ ";";
											}
										}
									}
								}

							} catch (Exception e) {
								e.getStackTrace();
								System.out.println(
										"Error in Marker annotations in apache solr");
							}

							try {

								for (String core : cores_genes) {
									TagResponse geneAnno = new TagResponse();
									geneAnno = solr.tagger.recognize.Evaluate
											.processString(cellValue, core,
													match, type);

									if (!"".equals(geneAnno.toString())) {
										genes_associated += cellValue;
										if (geneAnno.getItems().size() == 1)
											genesUri += geneAnno.getItems()
													.get(0).getIcd10() + ";";
										else if (geneAnno.getItems()
												.size() > 1) {
											for (TagItem item : geneAnno
													.getItems()) {
												genesUri += item.getIcd10()
														+ ";";
											}
										}
									}
								}

							} catch (Exception e) {
								e.getStackTrace();
								System.out.println(
										"Error in Gene annotations in apache solr");
							}

						}
					}
					stmtSelectPropertiesandValues.close();

					if (!"".equals(markers_associated)
							|| !"".equals(genes_associated)) {

						String traitAnnoUri = "";
						String traitOntoName = "";

						if (traitAnno.getItems().size() == 1) {
							traitAnnoUri = traitAnno.getItems().get(0)
									.getIcd10();
							traitOntoName = traitAnno.getItems().get(0)
									.getPrefTerm();
						} else if (traitAnno.getItems().size() > 1) {
							for (TagItem item_trait : traitAnno.getItems()) {
								traitAnnoUri += item_trait.getIcd10() + ";";
								if (!traitOntoName
										.contains(item_trait.getPrefTerm()))
									traitOntoName += item_trait.getPrefTerm()
											+ ";";
							}
							traitOntoName = traitOntoName.substring(0,
									traitOntoName.length() - 1);
							traitAnnoUri = traitAnnoUri.substring(0,
									traitAnnoUri.length() - 1);
						}

						String insertTableSQL = "INSERT INTO QTL"
								+ "(tab_id,row_id, trait_in_article,trait_in_onto,trait_uri,chromosome,marker,marker_uri, gene,gene_uri) VALUES"
								+ "(?,?,?,?,?,?,?,?,?,?)";
						PreparedStatement stmtInsertQTL = conn
								.prepareStatement(insertTableSQL);

						stmtInsertQTL.setDouble(1, tableId);
						stmtInsertQTL.setInt(2, rowId);

						try {
							stmtInsertQTL.setString(3, T.getTraitName());
						} catch (NullPointerException e) {
							stmtInsertQTL.setNull(3,
									java.sql.Types.VARCHAR);
						}

						try {
							stmtInsertQTL.setString(4, traitOntoName);
						} catch (NullPointerException e) {
							stmtInsertQTL.setNull(4,
									java.sql.Types.VARCHAR);
						}
						try {
							stmtInsertQTL.setString(5, traitAnnoUri);
						} catch (NullPointerException e) {
							stmtInsertQTL.setNull(5,
									java.sql.Types.VARCHAR);
						}
						try {
							stmtInsertQTL.setString(6, ChromosomeNumber);
						} catch (NullPointerException e) {
							stmtInsertQTL.setNull(6,
									java.sql.Types.VARCHAR);
						}

						try {
							if (markers_associated == "")
								markers_associated = null;
							stmtInsertQTL.setString(7, markers_associated);
						} catch (NullPointerException e) {
							stmtInsertQTL.setNull(7,
									java.sql.Types.VARCHAR);
						}

						try {
							if (markersUri == "")
								markersUri = null;
							else
							markersUri = markersUri.substring(0,
									markersUri.length() - 1);
							stmtInsertQTL.setString(8, markersUri);
						} catch (NullPointerException e) {
							stmtInsertQTL.setNull(8,
									java.sql.Types.VARCHAR);
						}
						try {
							if (genes_associated == "")
								genes_associated = null;
							stmtInsertQTL.setString(9, genes_associated);
						} catch (NullPointerException e) {
							stmtInsertQTL.setNull(9,
									java.sql.Types.VARCHAR);
						}
						try {
							if ("".equals(genesUri))
								genesUri = null;
							else
								genesUri = genesUri.substring(0,
									genesUri.length() - 1);

							stmtInsertQTL.setString(10, genesUri);
						} catch (NullPointerException e) {
							stmtInsertQTL.setNull(10,
									java.sql.Types.VARCHAR);
						}

						try {
							stmtInsertQTL.executeUpdate();
						} catch (SQLException e) {
							e.getStackTrace();

						}
						stmtInsertQTL.close();

					}

				}

				stmtSelectTrait.close();


			}
		} catch (Exception e) {
			System.out.println("Error in QTLDB.insertQTLdata entry function ");
			e.printStackTrace();
		}

	}

	public static boolean isPmcIdAlredyInDb(String pmc) {
		Boolean check = false;
		Scanner id = new Scanner(pmc).useDelimiter("[^0-9]+");
		int pid = id.nextInt();

		try {
			Statement stmt = null;
			stmt = conn.createStatement();

			// System.out.println("I am here");
			String sql = "SELECT pmc_id FROM Article";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				int pmcId = rs.getInt("pmc_id");

				if (pmcId == pid) {
					check = true;
					return check;
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();

		}
		return check;

	}

	public static boolean isArticleEntryAlredyIn(Article a) {
		Boolean check = false;
		try {
			Statement stmt = null;
			stmt = conn.createStatement();

			// System.out.println("I am here");
			String sql = "SELECT pmc_id FROM Article";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				int pmcId = rs.getInt("pmc_id");

				Scanner id = new Scanner(a.getPmc()).useDelimiter("[^0-9]+");
				int pid = id.nextInt();

				if (pmcId == pid) {
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

	public static int numberofTraitTable() {
		int tables = 0;
		try {
			Statement stmt = null;
			stmt = conn.createStatement();
			String sql = "SELECT COUNT(*) as count FROM TRAIT_TABLE";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				tables = rs.getInt("count");
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		return tables;
	}

	public static int numberofQTL() {
		int numQTL = 0;
		try {
			Statement stmt = null;
			stmt = conn.createStatement();
			String sql = "SELECT COUNT(*) as count FROM QTL";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				numQTL = rs.getInt("count");
			}
		} catch (SQLException e) {
			e.printStackTrace();

		}
		return numQTL;
	}

}
