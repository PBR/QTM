/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qtlTMdb;

<<<<<<< HEAD
import java.nio.channels.CancelledKeyException;
import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;

=======
import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

>>>>>>> 025220fcdbca83bb152b8ed35365aa011b3d41ba
import tablInEx.Article;
import tablInEx.Cell;
import tablInEx.Columns;
import tablInEx.Table;
<<<<<<< HEAD
import tablInEx.Trait;
=======
>>>>>>> 025220fcdbca83bb152b8ed35365aa011b3d41ba

/**
 *
 * @author gurnoor
 */
public class qtlDB {

	public static Connection c;

	public static boolean connectionDB() {
		c = null;
		try {
			Class.forName("org.sqlite.JDBC");

			String sJDBC = "jdbc:sqlite";
<<<<<<< HEAD
			String sTempDb = "TixDB.db";
=======
			String sTempDb = "Tix.db";
>>>>>>> 025220fcdbca83bb152b8ed35365aa011b3d41ba

			String sDBUrl = sJDBC + ":" + sTempDb;

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
<<<<<<< HEAD

=======
				// String sql = "CREATE TABLE IF NOT EXISTS Articles " + "(AID
				// INTEGER PRIMARY KEY AUTOINCREMENT,"
				// + "PMCID Text NOT NULL," + " Tittle TEXT NOT NULL); ";
>>>>>>> 025220fcdbca83bb152b8ed35365aa011b3d41ba
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
						+ "colType Text, tableId TEXT, FOREIGN KEY(tableId) REFERENCES qtlTables(tableID)" + "); ";
				stmt.executeUpdate(colETable);
				System.out.println("Column Entry Table created successfully");

				String cellETable = "CREATE TABLE IF NOT EXISTS cellEntries"
						+ "(cellId INTEGER PRIMARY KEY  AUTOINCREMENT NULL, rowNumber INT, cellValue  TEXT,"
						+ "cellType Text, colId TEXT, FOREIGN KEY(colId) REFERENCES columnEntries(colId)" + "); ";
				stmt.executeUpdate(cellETable);
				System.out.println("Column Entry Table created successfully");

<<<<<<< HEAD
				String traitTable = "CREATE TABLE IF NOT EXISTS traits"
						+ "(TraitId INTEGER PRIMARY KEY  AUTOINCREMENT NULL, TraitName Text, TraitValues  TEXT,"
						+ "TraitProperties TEXT, OtherProperties TEXT, pmcId TEXT, FOREIGN KEY(pmcId) REFERENCES articles(pmcId)"
						+ "); ";
				stmt.executeUpdate(traitTable);
				System.out.println("Trait Table created successfully");

=======
>>>>>>> 025220fcdbca83bb152b8ed35365aa011b3d41ba
				stmt.close();
				c.close();

			}
		} catch (Exception e) {
			System.out.println("SQLite tables not created");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

	public static void insertArticleEntry(Article a) {
		try {
<<<<<<< HEAD
			if (connectionDB() & isArticleEntryAlredyIn(a, c) == false) {
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
=======
			if (connectionDB()) {
				Statement stmt = null;
				stmt = c.createStatement();
>>>>>>> 025220fcdbca83bb152b8ed35365aa011b3d41ba

				// Article Table entry
				String articleID = a.getPmc();
				String articleTitle = a.getTitle();

				String insertArticleTable = "INSERT INTO articles Values('" + articleID + "','" + articleTitle + "');";
<<<<<<< HEAD
				articlestmt.executeUpdate(insertArticleTable);

				System.out.println("Article entries inserted in the DB" + "\t" + articleID);

=======
				stmt.executeUpdate(insertArticleTable);
				
				System.out.println("Article entries inserted in the DB"+"\t"+articleID);
				
>>>>>>> 025220fcdbca83bb152b8ed35365aa011b3d41ba
				for (String key : a.getAbbreviations().keySet()) {

					String insertAbrevTable = "INSERT INTO abbreviations Values('" + a.getAbbreviations().get(key)
							+ "','" + key + "','" + articleID + "');";
<<<<<<< HEAD
					abbrevstmt.executeUpdate(insertAbrevTable);
				}
				abbrevstmt.close();
				// System.out.println("Abbreviation entries inserted in the
				// DB");

=======
					stmt.executeUpdate(insertAbrevTable);
				}
				//System.out.println("Abbreviation entries inserted in the DB");
				
				
>>>>>>> 025220fcdbca83bb152b8ed35365aa011b3d41ba
				// QTL table entries
				for (Table t : a.getTables()) {
					if (t.isaTraitTable()) {
						String tableID = t.getTableid();
						int numofCol = t.getNum_of_columns();
						int numofRows = t.getNum_of_rows();
<<<<<<< HEAD

						String insertQTLTable = "INSERT INTO qtlTables Values('" + tableID + "'," + numofCol + ","
								+ numofRows + ",'" + articleID + "');";
						qtlTablestmt.executeUpdate(insertQTLTable);

						// System.out.println("QTLTable entries inserted in the
						// DB");

						for (Columns col : t.getTableCol()) {

							String insertColTable = "INSERT INTO columnEntries(colId,colHeader,colType,tableId) Values('"
									+ col.getColID() + "','" + col.getHeader() + "','" + col.getColumns_type() + "','"
									+ t.getTableid() + "');";
							colstmt.executeUpdate(insertColTable);
							// System.out.println("Col entries inserted in the
							// DB");

							for (Cell cel : col.getRowcell()) {

								// System.out.println(cel.getRow_number()+"\t"+
								// cel.getcell_value()+tableID);

								try {
									if (cel.getcell_value() != null) {
										String insertCellTable = "INSERT INTO cellEntries(rowNumber, cellValue, cellType, colId) Values("

												+ cel.getRow_number() + ",'" + cel.getcell_value() + "','"
												+ cel.getCell_type() + "','" + col.getColID() + "');";
										cellstmt.executeUpdate(insertCellTable);

										// System.out.println("Cell entries
										// inserted in the
										// DB"+cel.getRow_number());
									}
								} catch (Exception e) {
									// System.out.println(e.getMessage());
									continue;
								}
							}
							cellstmt.close();
						}
						colstmt.close();
					}
				}
				qtlTablestmt.close();

				
				// InsertTraitEntry(a);
				c.close();
			} else {
				System.out.println(a.getPmc() + " already exists");

				c.close();
			}

			System.out.println("entry inserted into DB successfully");
			//System.exit(0);
		} catch (Exception e) {
			// System.out.println("Error in DB" + e.getMessage());
			// System.exit(0);

		}
	}

	public static void InsertTraitEntry(Article a) {
		try {
			if (connectionDB()) {
				Statement stmt1 = null;
				Statement stmt2 = null;
				Statement stmt3 = null;
				stmt1 = c.createStatement();
				stmt2 = c.createStatement();
				stmt3 = c.createStatement();
				// System.out.println("I am here");

				List<Trait> traits = a.getTraits();
				
				
				String sql1 = "select cellValue,colId,rowNumber from cellEntries where cellType !='Empty' AND colId in (select colId from columnEntries where colType='QTL descriptor' AND colId like '"+a.getPmc()+"%')";
				ResultSet rs1 = stmt1.executeQuery(sql1);

				while (rs1.next()) {
					String value = rs1.getString("cellValue");
					String colId = rs1.getString("colId");
					String tableId = colId.substring(0, (colId.length() - 2));
					int row = rs1.getInt("rowNumber");

					 System.out.println("**********");
					 System.out.println(value + "\t" + colId + "\t" + tableId
					 + "\t" + row);
					 System.out.println("**********");

					Trait T = new Trait(value);
					JSONObject vals = new JSONObject();
					JSONObject prop = new JSONObject();
					JSONObject otherProp = new JSONObject();

					String sql2 = "select C.cellValue, Col.colHeader,Col.colType from cellEntries as C inner join columnEntries as Col ON C.colId=Col.colId  where rowNumber ="
							+ row + " and Col.tableId='" + tableId + "' and Col.colType!='QTL descriptor'";
					ResultSet rs2 = stmt2.executeQuery(sql2);

					while (rs2.next()) {
						String cellValue = rs2.getString("CellValue").replaceAll("\n", "").replace("\r", "");
						String colHeader = rs2.getString("colHeader").replaceAll("\n", "").replace("\r", "");
						String colType = rs2.getString("colType").replaceAll("\n", "").replace("\r", "");

						System.out.println("Entries " + "\t" + cellValue + "\t" + colHeader + "\t" + colType);

						if (colType.equals("QTL value")) {
							//System.out.println("cell Values is" + cellValue);
							vals.put(colHeader, cellValue);

						} else if (colType.equals("QTL property")) {
							prop.put(colHeader, cellValue);

						} else {
							otherProp.put(colHeader, cellValue);

						}

					}

					T.setTraitValues(vals);
					T.setTraitProperties(prop);
					T.setOtherProperties(otherProp);

					traits.add(T);

					String insertTraitTable = "INSERT INTO traits(TraitName,TraitValues,TraitProperties,OtherProperties, pmcId) Values('"
							+ value + "','" + T.getTraitValues() + "','" + T.getTraitProperties() + "','"
							+ T.getOtherProperties() + "','"+a.getPmc()+"');";
					
					stmt3.executeUpdate(insertTraitTable);
					stmt3.close();
					stmt2.close();
					//System.out.println("programm running till here  ");

				}
				stmt1.close();
				c.close();

			}
		} catch (SQLException e) {

			e.printStackTrace();
		}
		// System.exit(0);
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

=======
					
						String insertQTLTable = "INSERT INTO qtlTables Values('" + tableID + "'," + numofCol + ","
								+ numofRows + ",'" + articleID + "');";
						stmt.executeUpdate(insertQTLTable);
						
						//System.out.println("QTLTable entries inserted in the DB");
						
						
						for(Columns col: t.getTableCol()){
							String insertColTable = "INSERT INTO columnEntries(colId,colHeader,colType,tableId) Values('"+col.getColID() +"','" + col.getHeader() + "','" + col.getColumns_type() + "','"
									+ t.getTableid()+ "');";
							stmt.executeUpdate(insertColTable);
							//System.out.println("Col entries inserted in the DB");
						
						
							for(Cell cel:col.getRowcell()){
								try{
								if(cel.getcell_value()!= null){
								String insertCellTable = "INSERT INTO cellEntries(rowNumber, cellValue, cellType, colId) Values(" + cel.getRow_number() + ",'" + cel.getcell_value()+ "','"
										+cel.getCell_type()+"','"+col.getColID() + "');";
								stmt.executeUpdate(insertCellTable);
								
								//System.out.println("Cell entries inserted in the DB"+cel.getRow_number());
								
							 
							}
							}catch(Exception e){
								
							}
							}		
						
													
						
						}
						
						
						
						
						
					}
				}

				stmt.close();
				c.close();
			}
		 
		System.out.println("entry inserted into DB successfully");
		System.exit(0);
	}catch(Exception e){
		System.out.println("Error in DB"+e.getMessage());
		System.exit(0);
	}
>>>>>>> 025220fcdbca83bb152b8ed35365aa011b3d41ba
	}

}
