/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package readers;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import qtm.Article;
//import qtm.C;
import qtm.Cell;
import qtm.Columns;
//import qtm.Hc;
import qtm.Table;
//import stats.Statistics;
import utils.Utilities;

/**
 *
 * @author gurnoor
 */
public class TableParser {

	// private static String FileName;

	/**
	 * Parses table, makes matrix of cells and put it into Article object
	 * 
	 * @param article
	 *            - Article to populate
	 * @param parse
	 *            - Document which is being parsed
	 * @return populated Article
	 */
	public static Article parseTables(Article article, Document parse) throws Exception {

		// read table-wrap in xml
		NodeList tablesxml = parse.getElementsByTagName("table-wrap");

		int numOfTables = getNumOfTablesInArticle(tablesxml);
			
		// Table[] tables = new Table[tablesxml.getLength()];
		Table[] tables = new Table[numOfTables];
		article.setTables(tables);
		int tableindex = 0;

		// Iterate over <table-wrap></table-wrap>
		for (int i = 0; i < tablesxml.getLength(); i++) {

			List<Node> tableTags = getChildrenByTagName(tablesxml.item(i),"table");
			//System.out.println("---------"+tableTags.size());
			
			if(tableTags.size()==0){
				tableTags.clear();
				List<Node> alternative = getChildrenByTagName(tablesxml.item(i),"alternatives");
				//System.out.println("---------alternative"+alternative.size());
				
				if(alternative.size()>0){
					for(int p=0; p < alternative.size(); p++ ){
					List<Node> tableTags2 = getChildrenByTagName(alternative.get(p),"table");
					tableTags=tableTags2;
					}
					
				}
				
			}
			
			
			for (int s = 0; s < tableTags.size(); s++) {
			
					
				String label = readTableLabel(tablesxml.item(i));

				tables[tableindex] = new Table(label);

				tables[tableindex].setDocumentFileName(article.getPmc());

				// table in xml
				tables[tableindex].setXml(Utilities.createXMLStringFromSubNode(tablesxml.item(i)));

				System.out.println("Table label :\t" + tables[tableindex].getTable_label());

				if(tableTags.size()>1){
					
				tables[tableindex].setTableid(
						tables[tableindex].getDocumentFileName().concat("_" + tables[tableindex].getTable_label()).concat("_"+s));
				}
				else{
					tables[tableindex].setTableid(
							tables[tableindex].getDocumentFileName().concat("_" + tables[tableindex].getTable_label()));
				}
				
				String caption = readTableCaption(tablesxml.item(i)).replaceAll("\n", "").replace("\r", "");
				System.out.println("Caption :\t" + caption);
				tables[tableindex].setTable_caption(caption);

				String foot = readTableFooter(tablesxml.item(i)).replaceAll("\n", "").replace("\r", "");
				tables[tableindex].setTable_footer(foot);
				//System.out.println("Foot: " + foot);

				List<Node> thead = getChildrenByTagName(tableTags.get(s), "thead");
				List<Node> tbody = getChildrenByTagName(tableTags.get(s), "tbody");

				int numofCol = countColumns(tbody, thead);

				if (numofCol == 0) {
					System.out.println("Table cannot be processed as number of colums are zero");
					tables[tableindex].setisTraitTable(false);
					tables[tableindex].setNum_of_rows(0);
					tables[tableindex].setNum_of_columns(0);
					System.out.println(tables[tableindex].getisTraitTable() + " " + tables[tableindex].getTableid()
							+ " is not a trait table");
					tableindex++;
					break;
				}

				tables[tableindex] = processTableHeader(tables[tableindex], numofCol, thead);
				tables[tableindex] = processTableBody(tables[tableindex], numofCol, tbody);

				tables[tableindex] = parseTableCaptionandTableHeadingsforTraitRelatedWords(tables[tableindex]);

				
				if (tables[tableindex].isaTraitTable()) {
				    // entering table values of num of rows and cols
					int numOfRows=0;
				    for (Columns col : tables[tableindex].getTableCol()) {
				        if (numOfRows < col.getcelz().length){
				           numOfRows=col.getcelz().length;
				        }
				        
				        
				    }
				        tables[tableindex]
							.setNum_of_rows(numOfRows);
					
										
					tables[tableindex].setNum_of_columns(numofCol);
					
					
					System.out.println("Number of Rows in " + tables[tableindex].getTable_label()
							+ " is: " + tables[tableindex].getNum_of_rows());
					System.out.println("Number of Columns in "+ tables[tableindex].getTable_label()
							+ " is: " + tables[tableindex].getNum_of_columns());
					System.out.println("\n");

					
					//table classification
					tables[tableindex] = tables[tableindex].tableClassification();
					tables[tableindex].printTable2();
					System.out.println(
		                                "____________________________________________________________________________________________________________________________\n");

					tableindex++;
				}

			}

		}

		return article;
	}

	public static int getNumOfTablesInArticle(NodeList tablesxml) {

		int numOfTables = 0;
		for (int i = 0; i < tablesxml.getLength(); i++) {
			List<Node> tb = getChildrenByTagName(tablesxml.item(i), "table");
			numOfTables += tb.size();
		}
		if (numOfTables < tablesxml.getLength())
			numOfTables = tablesxml.getLength();

		return numOfTables;
	}

	/**
	 * Read table label.
	 *
	 * @param tablexmlNode
	 *            the tablexml node
	 * @return the string
	 */
	public static String readTableLabel(Node tablexmlNode) {
		String label = "Table without label";
		List<Node> nl = getChildrenByTagName(tablexmlNode, "label");
		if (nl.size() > 0) {
			label = Utilities.getString(nl.get(0));
		}

		return label;
	}

	/**
	 * Read table caption.
	 *
	 * @param tablexmlNode
	 *            the tablexml node
	 * @return the string
	 */
	public static String readTableCaption(Node tablexmlNode) {
		String caption = "";
		List<Node> nl = getChildrenByTagName(tablexmlNode, "caption");
		if (nl.size() > 0) {
			caption = Utilities.getString(nl.get(0));
		}
		nl = getChildrenByTagName(tablexmlNode, "p");
		if (nl.size() > 0) {
			caption = Utilities.getString(nl.get(0));
		}
		nl = getChildrenByTagName(tablexmlNode, "title");
		if (nl.size() > 0) {
			caption = Utilities.getString(nl.get(0));
		}
		return caption;
	}

	/**
	 * Read table footer.
	 *
	 * @param tablesxmlNode
	 *            the tablesxml node
	 * @return the string
	 */
	public static String readTableFooter(Node tablesxmlNode) {
		String foot = "";
		List<Node> nl = getChildrenByTagName(tablesxmlNode, "table-wrap-foot");
		if (nl.size() >= 1) {
			foot = Utilities.getString(nl.get(0));
		}
		return foot;
	}

	/**
	 * Count columns.
	 *
	 * @param rowsbody
	 *            the rowsbody
	 * @param rowshead
	 *            the rowshead
	 * @return the int
	 */
	public static int countColumns(List<Node> tbody, List<Node> thead) {
		int cols = 0;

		try {
			List<Node> rowshead = getChildrenByTagName(thead.get(0), "tr");
			List<Node> rowsbody = getChildrenByTagName(tbody.get(0), "tr");

			int headrowscount = 0;
			if (rowshead != null) {
				headrowscount = rowshead.size();
			}
			if (rowsbody != null) {
				for (int row = 0; row < rowsbody.size(); row++) {
					int cnt = 0;
					List<Node> tds = getChildrenByTagName(rowsbody.get(row), "td");
					for (int k = 0; k < tds.size(); k++) {
						if (tds.get(k).getAttributes().getNamedItem("colspan") != null && Utilities
								.getFirstValue(tds.get(k).getAttributes().getNamedItem("colspan").getNodeValue()) > 1) {
							cnt += Utilities
									.getFirstValue(tds.get(k).getAttributes().getNamedItem("colspan").getNodeValue());
						} else {
							cnt++;
						}
					}
					cols = Math.max(cols, cnt);
				}
			}
			if (headrowscount != 0) {
				List<Node> tdsh = getChildrenByTagName(rowshead.get(0), "td");
				if (tdsh.size() == 0) {
					tdsh = getChildrenByTagName(rowshead.get(0), "th");
				}
				cols = Math.max(cols, tdsh.size());
			}
		} catch (IndexOutOfBoundsException e) {
			return 0;
		}
		return cols;
	}

	/**
	 * Count columns.
	 *
	 * @param rowsbody
	 *            the rowsbody
	 * @param rowshead
	 *            the rowshead
	 * @return the int
	 */
	public static int countHeaderColumns(List<Node> headerRows) {
		int cols = 0;
		for (int k = 0; k < headerRows.size(); k++) {
			List<Node> headerCells = getChildrenByTagName(headerRows.get(k), "td");
			if (headerCells.size() == 0) {
				headerCells = getChildrenByTagName(headerRows.get(k), "th");
			}
			cols = Math.max(cols, headerCells.size());
		}

		return cols;
	}

	/**
	 * Process table header.
	 *
	 * @param table
	 *            the table
	 * @param cells
	 *            the cells
	 * @param rowshead
	 *            the rowshead
	 * @param headrowscount
	 *            the headrowscount
	 * @param num_of_columns
	 *            the num_of_columns
	 * @return the table
	 */
	public static Table processTableHeader(Table table, int numberofCol, List<Node> thead) {
		List<Node> headerRows = null;

		String[] headerCols = new String[numberofCol];
		// String [] HeaderCols = {};
		Columns[] tableCol = new Columns[numberofCol];

		// Check if Header is empty
		if (thead.size() > 0) {
			for (int j = 0; j < thead.size(); j++) {
				headerRows = getChildrenByTagName(thead.get(j), "tr");

				//System.out.println("Number of Rows in Table Headings are" + headerRows.size());
				//System.out.println("Number of Columns in Table Headings are" + numberofCol);

				int rowLine = 0;
				int colLine = 0;

				for (int k = 0; k < headerRows.size(); k++) {

					rowLine = k;
					colLine = 0;

					List<Node> th = null;
					th = getChildrenByTagName(headerRows.get(k), "th");
					
					if(th.isEmpty()){
						th = getChildrenByTagName(headerRows.get(k), "td");
					}
					
					// System.out.println("----------------------------header
					// cells Size" + th.size());

					for (int l = 0; l < th.size(); l++) {
						int rowspan = 1;
						int colspan = 1;

						// check Later?????
//						while (headerCells[rowLine][colLine].getHeadercell_value() != null) {
//							colLine++;
//						}

						try {
							rowspan = Utilities
									.getFirstValue(th.get(l).getAttributes().getNamedItem("rowspan").getNodeValue());

						} catch (NullPointerException e) {
						}

						try {
							colspan = Utilities
									.getFirstValue(th.get(l).getAttributes().getNamedItem("colspan").getNodeValue());

						} catch (NullPointerException e) {
						}

						
						
						
						if (rowspan == 1 && colspan == 1) {
//							headerCells[rowLine][colLine].setHeadercell_values(th.get(l).getTextContent().replaceAll("\n", "").replace("\r", ""));

							if (headerCols[colLine] == null)
								headerCols[colLine] = th.get(l).getTextContent().replaceAll("\n", "").replace("\r", "");
							else
								headerCols[colLine] = headerCols[colLine] + " " + th.get(l).getTextContent().replaceAll("\n", "").replace("\r", "");

							colLine++;
						} else if (rowspan == 1 && colspan > 1) {
								for (int n = colLine; n < colLine + colspan; n++) {

//								headerCells[rowLine][n].setHeadercell_values(th.get(l).getTextContent().replaceAll("\n", "").replace("\r", ""));

								if (headerCols[n] == null)
									headerCols[n] = th.get(l).getTextContent().replaceAll("\n", "").replace("\r", "");
								else
									headerCols[n] = headerCols[n] + " " + th.get(l).getTextContent().replaceAll("\n", "").replace("\r", "");
							}
							colLine += colspan;
						} else if (rowspan > 1 && colspan == 1) {
//							headerCells[rowLine][colLine].setHeadercell_values(th.get(l).getTextContent().replaceAll("\n", "").replace("\r", ""));
							for (int m = rowLine + 1; m < rowLine + rowspan; m++) {
//								headerCells[m][colLine].setHeadercell_values(th.get(l).getTextContent().replaceAll("\n", "").replace("\r", ""));

								if (headerCols[colLine] == null)
									headerCols[colLine] = th.get(l).getTextContent().replaceAll("\n", "").replace("\r", "");
								else
									headerCols[colLine] = headerCols[colLine] + " " + th.get(l).getTextContent().replaceAll("\n", "").replace("\r", "");
							}
							colLine++;
						}
					}
				}

				for (int i = 0; i < numberofCol; i++) {
					try{
					headerCols[i] = headerCols[i].trim();
					}catch(Exception e){
						headerCols[i]="";
					}
					
					tableCol[i] = new Columns();
					tableCol[i].setHeader(headerCols[i]);

					tableCol[i].setColID(table.getTableid() + "_" + (i + 1));
				}

				table.setTableHeadersColumns(headerCols);
				table.setTableCol(tableCol);
//				table.setTableHeadercells(headerCells);

				// Printing Header Cells
				// System.out.println("***Header Tables***");
				// for(int p=0;p<headerRows.size();p++){
				// for(int q=0;q<numberofCol;q++){
				// System.out.print(table.header_cells[p][q].getHeadercell_value()+"\t");
				// }
				// System.out.println("*******");
				// }
			}
		} else {
			table.setHasHeader(false);

		}
		return table;
	}

	/**
	 * Process table body.
	 *
	 * @param table
	 *            the table
	 * @param cells
	 *            the cells
	 * @param tbody
	 *            the rowsbody
	 * @param headrowscount
	 *            the headrowscount
	 * @param numberofCol
	 *            the num_of_columns
	 * @return the table
	 */
	public static Table processTableBody(Table table, int numberofCol, List<Node> tbody) {

		List<Node> Rows = null;

		Columns[] tableCol = new Columns[numberofCol];

		tableCol = table.getTableCol();

		// Check if Header is empty
		if (tbody.size() > 0) {
			for (int j = 0; j < tbody.size(); j++) {
				Rows = getChildrenByTagName(tbody.get(j), "tr");

				//System.out.println("Number of Rows in Table body are" + Rows.size());

				for (Columns column : tableCol) {
					column.entries = new String[Rows.size()];
					column.celz = new Cell[Rows.size()];
				}

				//System.out.println("Number of Columns in Table body are" + numberofCol);

				// create empty cells
				//table.createCells(Rows.size(), numberofCol);
				// table.CreateLOC(Rows.size(),numberofCol);

				//C[][] c = table.getTable_cells();
				// List<C[]> LOC=table.getTable_cellList();

				// System.out.println("&&"+LOC.size());
				// System.out.println("//" + Cells[0].length);

				int rowLine = 0;
				int colLine = 0;

				for (int k = 0; k < Rows.size(); k++) {

					rowLine = k;
					colLine = 0;
					// System.out.println("k**"+k+headerRows.get(k).toString());
					List<Node> td = null;
					td = getChildrenByTagName(Rows.get(k), "td");

					// System.out.println("header cells Size"+th.size());

					for (int l = 0; l < td.size(); l++) {
						int rowspan = 1;
						int colspan = 1;

						try {
							rowspan = Utilities
									.getFirstValue(td.get(l).getAttributes().getNamedItem("rowspan").getNodeValue());
							colspan = Utilities
									.getFirstValue(td.get(l).getAttributes().getNamedItem("colspan").getNodeValue());
						} catch (NullPointerException e) {
							// continue;
						}
						
						
						if(tableCol[colLine].celz[rowLine]!=null && rowLine < Rows.size()) //|| tableCol[colLine].celz[rowLine]!=null
							colLine++;
						//System.out.println("row line is::: \t\t "+rowLine +"\t\t col line is"+colLine+"value is \t"+td.get(l).getTextContent().replaceAll("\n", "").replace("\r", ""));
						
						if (rowLine < Rows.size() || colLine < numberofCol) {
							if (rowspan == 1 && colspan == 1) {

								//c[rowLine][colLine].setcell_values(td.get(l).getTextContent().replaceAll("\n", "").replace("\r", ""));

								Cell Entry = new Cell(rowLine, td.get(l).getTextContent().replaceAll("\n", "").replace("\r", ""));
								tableCol[colLine].celz[rowLine] = new Cell(Entry);

								if (tableCol[colLine].getRowEntries()[rowLine] == null)
									tableCol[colLine].getRowEntries()[rowLine] = td.get(l).getTextContent().replaceAll("\n", "").replace("\r", "");
								else
									tableCol[colLine]
											.getRowEntries()[rowLine] = tableCol[colLine].getRowEntries()[rowLine] + " "
													+ td.get(l).getTextContent().replaceAll("\n", "").replace("\r", "");
								
								colLine++;
							} else if (rowspan > 1 && colspan == 1) {
								// forloop rowspan
								for (int m = rowLine; m < rowLine + rowspan; m++) {
									//c[m][colLine].setcell_values(td.get(l).getTextContent().replaceAll("\n", "").replace("\r", ""));
									// System.out.println("@@@@@"+td.get(l).getTextContent());

									//System.out.print("m is "+m +"\t::");
									Cell Entry = new Cell(m, td.get(l).getTextContent().replaceAll("\n", "").replace("\r", ""));
									tableCol[colLine].celz[m] = new Cell(Entry);
									System.out.println(tableCol[colLine].celz[m].getcell_value());
									
									if (tableCol[colLine].getRowEntries()[m] == null)
										tableCol[colLine].getRowEntries()[m] = td.get(l).getTextContent().replaceAll("\n", "").replace("\r", "");
									else
										tableCol[colLine].getRowEntries()[m] = tableCol[colLine].getRowEntries()[m]
												+ " " + td.get(l).getTextContent().replaceAll("\n", "").replace("\r", "");
									
									System.out.println("Row span entry"+td.get(l).getTextContent());
								}
								colLine++;
							} else if (rowspan == 1 && colspan > 1) {
								// System.out.println("***rowLine and colLine
								// are*****"+rowLine+"&& "+colLine);

								// forloop colspan
								for (int n = colLine; n < colLine + colspan; n++) {
									//c[rowLine][n].setcell_values(td.get(l).getTextContent().replaceAll("\n", "").replace("\r", ""));
									// System.out.println("//////"+td.get(l).getTextContent());

									Cell Entry = new Cell(rowLine, td.get(l).getTextContent().replaceAll("\n", "").replace("\r", ""));
									tableCol[n].celz[rowLine] = new Cell(Entry);

									if (tableCol[n].getRowEntries()[rowLine] == null)
										tableCol[n].getRowEntries()[rowLine] = td.get(l).getTextContent().replaceAll("\n", "").replace("\r", "");
									else
										tableCol[n]
												.getRowEntries()[rowLine] = tableCol[colLine].getRowEntries()[rowLine]
														+ " " + td.get(l).getTextContent().replaceAll("\n", "").replace("\r", "");

								}
								colLine += colspan;
							} else if (rowspan > 1 && colspan > 1) {
								for (int m = rowLine; m < rowLine + rowspan; m++) {
									for (int n = colLine; n < colLine + colspan; n++) {
										//c[m][n].setcell_values(td.get(l).getTextContent().replaceAll("\n", "").replace("\r", ""));
									}
								}

								colLine += colspan;
							}

						}
					}

				}

				//table.setTable_cells(c);

				// System.out.println("***body Tables***");
				// for(int p=0;p<Rows.size();p++){
				// for(int q=0;q<numberofCol;q++){
				// System.out.print(table.cells[p][q].getcell_value()+"("+table.cells[p][q].getCell_type()+")"+"\t");
				// //System.out.print(table.cells[p][q].getcell_value()+"\t");
				// }
				// System.out.println("*******");
				// }

			}

		}
		return table;

	}

	/**
	 * Gets the children by tag name.
	 *
	 * @param parent
	 *            the parent
	 * @param name
	 *            the name
	 * @return the children by tag name
	 */
	public static List<Node> getChildrenByTagName(Node parent, String name) {
		List<Node> nodeList = new ArrayList<Node>();
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE && name.equals(child.getNodeName())) {
				nodeList.add((Node) child);
			}
		}

		return nodeList;
	}

	/*
	 * Parse Table caption for Traits
	 */
	public static Table parseTableCaptionandTableHeadingsforTraitRelatedWords(Table table) {
		List<String> HeaderList = new ArrayList<String>();

		for (Columns col : table.getTableCol()) {
			HeaderList.add(col.getHeader().toLowerCase());
				} 
			try {
			String[] wordlist = { "trait", "qtl", "quantitavie trait loci", "phenotype" };

			for (int j = 0; j < wordlist.length; j++) {
				if (table.getTable_caption().toLowerCase().indexOf(wordlist[j]) != -1) {
					table.setisTraitTable(true);
					System.out.println(table.getTableid() + " is a QTL table\n");
					return table;
				}
				for (String h : HeaderList) {
					if (h.indexOf(wordlist[j]) != -1) {
						table.setisTraitTable(true);
						System.out.println(table.getTableid() + " is a QTL table\n");
						return table;
					} else {
						continue;
					}
				}
			}
			
			System.out.println(table.getTableid() + " is NOT a QTL table\n");
		} catch (Exception e) {

		}
		return table;

	}
}