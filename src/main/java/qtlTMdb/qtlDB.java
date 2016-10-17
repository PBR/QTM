/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qtlTMdb;

import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import tablInEx.Article;
import tablInEx.Cell;
import tablInEx.Columns;
import tablInEx.Table;

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
			String sTempDb = "Tix.db";

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
				// String sql = "CREATE TABLE IF NOT EXISTS Articles " + "(AID
				// INTEGER PRIMARY KEY AUTOINCREMENT,"
				// + "PMCID Text NOT NULL," + " Tittle TEXT NOT NULL); ";
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
			if (connectionDB()) {
				Statement stmt = null;
				stmt = c.createStatement();

				// Article Table entry
				String articleID = a.getPmc();
				String articleTitle = a.getTitle();

				String insertArticleTable = "INSERT INTO articles Values('" + articleID + "','" + articleTitle + "');";
				stmt.executeUpdate(insertArticleTable);
				
				System.out.println("Article entries inserted in the DB"+"\t"+articleID);
				
				for (String key : a.getAbbreviations().keySet()) {

					String insertAbrevTable = "INSERT INTO abbreviations Values('" + a.getAbbreviations().get(key)
							+ "','" + key + "','" + articleID + "');";
					stmt.executeUpdate(insertAbrevTable);
				}
				//System.out.println("Abbreviation entries inserted in the DB");
				
				
				// QTL table entries
				for (Table t : a.getTables()) {
					if (t.isaTraitTable()) {
						String tableID = t.getTableid();
						int numofCol = t.getNum_of_columns();
						int numofRows = t.getNum_of_rows();
					
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
	}

}
