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
import stats.Statistics;
import tablInEx.Article;
import tablInEx.HC;
import tablInEx.C;
import tablInEx.Cell;
import tablInEx.Columns;
import tablInEx.Table;
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
	public static Article ParseTables(Article article, Document parse) throws Exception {

		// read table-wrap in xml
		NodeList tablesxml = parse.getElementsByTagName("table-wrap");

		int numOfTables = getNumOfTablesInArticle(tablesxml);

		// Table[] tables = new Table[tablesxml.getLength()];
		Table[] tables = new Table[numOfTables];
		article.setTables(tables);
		int tableindex = 0;

		// Iterate over <table-wrap></table-wrap>
		for (int i = 0; i < tablesxml.getLength(); i++) {

			List<Node> tb = getChildrenByTagName(tablesxml.item(i), "table");
			// System.out.println("---------"+tb.size());

			for (int s = 0; s < tb.size(); s++) {
				String label = readTableLabel(tablesxml.item(i));

				tables[tableindex] = new Table(label);

				tables[tableindex].setDocumentFileName(article.getPmc());

				// table in xml
				tables[tableindex].setXml(Utilities.CreateXMLStringFromSubNode(tablesxml.item(i)));

				System.out.println("Table label:" + tables[tableindex].getTable_label());

				tables[tableindex].setTableid(
						tables[tableindex].getDocumentFileName().concat("_" + tables[tableindex].getTable_label()));

				String caption = readTableCaption(tablesxml.item(i));
				System.out.println("Caption: " + caption);
				tables[tableindex].setTable_caption(caption);

				String foot = ReadTableFooter(tablesxml.item(i));
				tables[tableindex].setTable_footer(foot);
				System.out.println("Foot: " + foot);

				List<Node> thead = getChildrenByTagName(tb.get(s), "thead");
				List<Node> tbody = getChildrenByTagName(tb.get(s), "tbody");

				int numofCol = CountColumns(tbody, thead);

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

				tables[tableindex] = ProcessTableHeader(tables[tableindex], numofCol, thead);
				tables[tableindex] = ProcessTableBody(tables[tableindex], numofCol, tbody);

				tables[tableindex] = ParseTableCaptionandTableHeadingsforTraitRelatedWords(tables[tableindex]);

				
				if (tables[tableindex].isaTraitTable()) {

					// entering table values of num of rows and cols
					tables[tableindex]
							.setNum_of_rows(tables[tableindex].header_cells.length + tables[tableindex].cells.length);
					tables[tableindex].setNum_of_columns(numofCol);
					System.out.println("Number of Rows in the complete Table" + tables[tableindex].getTable_label()
							+ " is: " + tables[tableindex].getNum_of_rows());
					System.out.println("Number of Cols in the complete Table" + tables[tableindex].getTable_label()
							+ " is: " + tables[tableindex].getNum_of_columns());

					tables[tableindex] = tables[tableindex].tableClassification();
					System.out.println("after classification");
					tables[tableindex].printTable();

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
	public static String ReadTableFooter(Node tablesxmlNode) {
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
	public static int CountColumns(List<Node> tbody, List<Node> thead) {
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
	public static int CountHeaderColumns(List<Node> headerRows) {
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
	public static Table ProcessTableHeader(Table table, int numberofCol, List<Node> thead) {
		List<Node> headerRows = null;

		String[] HeaderCols = new String[numberofCol];
		// String [] HeaderCols = {};
		Columns[] tableCol = new Columns[numberofCol];

		// Check if Header is empty
		if (thead.size() > 0) {
			for (int j = 0; j < thead.size(); j++) {
				headerRows = getChildrenByTagName(thead.get(j), "tr");

				System.out.println("Number of Rows in Table Headings are" + headerRows.size());
				System.out.println("Number of Columns in Table Headings are" + numberofCol);

				// create empty Headercells
				table.CreateHeaderCells(headerRows.size(), numberofCol);
				HC[][] HeaderCells = table.getTable_Headercells();
				// System.out.println(HeaderCells.length);
				// System.out.println(HeaderCells[0].length);

				int rowLine = 0;
				int colLine = 0;

				for (int k = 0; k < headerRows.size(); k++) {

					rowLine = k;
					colLine = 0;

					List<Node> th = null;
					th = getChildrenByTagName(headerRows.get(k), "th");

					// System.out.println("----------------------------header
					// cells Size" + th.size());

					for (int l = 0; l < th.size(); l++) {
						int rowspan = 1;
						int colspan = 1;

						// check Later?????
						while (HeaderCells[rowLine][colLine].getHeadercell_value() != null) {
							colLine++;
						}

						try {
							rowspan = Utilities
									.getFirstValue(th.get(l).getAttributes().getNamedItem("rowspan").getNodeValue());

						} catch (NullPointerException e) {

							// System.out.println("Rowspan not mentioned");
						}

						try {
							colspan = Utilities
									.getFirstValue(th.get(l).getAttributes().getNamedItem("colspan").getNodeValue());

						} catch (NullPointerException e) {

							// System.out.println("Colspan not mentioned");
						}

						// System.out.print("rowSpan is" + rowspan + "\t colspan
						// is " + colspan + "\t\n\n");

						if (rowspan == 1 && colspan == 1) {
							// System.out.println("I am here
							// %%%%%%%%%&&&&&&&&&&&&");
							HeaderCells[rowLine][colLine].setHeadercell_values(th.get(l).getTextContent());

							if (HeaderCols[colLine] == null)
								HeaderCols[colLine] = th.get(l).getTextContent();
							else
								HeaderCols[colLine] = HeaderCols[colLine] + " " + th.get(l).getTextContent();

							colLine++;
						} else if (rowspan == 1 && colspan > 1) {
							// System.out.println("I am here
							// $$$$$$$$$$$$&&&&&&&&&&&&");
							for (int n = colLine; n < colLine + colspan; n++) {

								HeaderCells[rowLine][n].setHeadercell_values(th.get(l).getTextContent());

								if (HeaderCols[n] == null)
									HeaderCols[n] = th.get(l).getTextContent();
								else
									HeaderCols[n] = HeaderCols[n] + " " + th.get(l).getTextContent();
							}
							colLine += colspan;
						} else if (rowspan > 1 && colspan == 1) {
							// System.out.println("9999" +
							// th.get(l).getTextContent());
							HeaderCells[rowLine][colLine].setHeadercell_values(th.get(l).getTextContent());
							for (int m = rowLine + 1; m < rowLine + rowspan; m++) {
								// HeaderCells[m][colLine].setHeadercell_values(th.get(l).getTextContent());
								HeaderCells[m][colLine].setHeadercell_values("            ");

								if (HeaderCols[colLine] == null)
									HeaderCols[colLine] = th.get(l).getTextContent();
								else
									HeaderCols[colLine] = HeaderCols[colLine] + " " + th.get(l).getTextContent();
							}
							colLine++;
						}
					}
				}

				for (int i = 0; i < numberofCol; i++) {
					HeaderCols[i] = HeaderCols[i].trim();

					tableCol[i] = new Columns();
					tableCol[i].setHeader(HeaderCols[i]);

					tableCol[i].setColID(table.getTableid() + "_" + (i + 1));
				}

				table.setTableHeadersColumns(HeaderCols);
				table.setTableCol(tableCol);
				table.setTableHeadercells(HeaderCells);

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
	public static Table ProcessTableBody(Table table, int numberofCol, List<Node> tbody) {

		List<Node> Rows = null;

		Columns[] tableCol = new Columns[numberofCol];

		tableCol = table.getTableCol();

		// Check if Header is empty
		if (tbody.size() > 0) {
			for (int j = 0; j < tbody.size(); j++) {
				Rows = getChildrenByTagName(tbody.get(j), "tr");

				System.out.println("Number of Rows in Table body are" + Rows.size());

				for (Columns C : tableCol) {
					C.RowEntries = new String[Rows.size()];
					C.Rowcell = new Cell[Rows.size()];
				}

				System.out.println("Number of Columns in Table body are" + numberofCol);

				// create empty cells
				table.CreateCells(Rows.size(), numberofCol);
				// table.CreateLOC(Rows.size(),numberofCol);

				C[][] Cells = table.getTable_cells();
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
						
						
						if (rowLine < Rows.size() || colLine < numberofCol) {
							if (rowspan == 1 && colspan == 1) {

								Cells[rowLine][colLine].setcell_values(td.get(l).getTextContent());

								Cell Entry = new Cell(rowLine, td.get(l).getTextContent());
								tableCol[colLine].Rowcell[rowLine] = new Cell(Entry);

								if (tableCol[colLine].getRowEntries()[rowLine] == null)
									tableCol[colLine].getRowEntries()[rowLine] = td.get(l).getTextContent();
								else
									tableCol[colLine]
											.getRowEntries()[rowLine] = tableCol[colLine].getRowEntries()[rowLine] + " "
													+ td.get(l).getTextContent();

								colLine++;
							} else if (rowspan > 1 && colspan == 1) {
								// forloop rowspan
								for (int m = rowLine; m < rowLine + rowspan; m++) {
									Cells[m][colLine].setcell_values(td.get(l).getTextContent());
									// System.out.println("@@@@@"+td.get(l).getTextContent());

									Cell Entry = new Cell(m, td.get(l).getTextContent());
									tableCol[colLine].Rowcell[m] = new Cell(Entry);

									if (tableCol[colLine].getRowEntries()[m] == null)
										tableCol[colLine].getRowEntries()[m] = td.get(l).getTextContent();
									else
										tableCol[colLine].getRowEntries()[m] = tableCol[colLine].getRowEntries()[m]
												+ " " + td.get(l).getTextContent();

								}
								colLine++;
							} else if (rowspan == 1 && colspan > 1) {
								// System.out.println("***rowLine and colLine
								// are*****"+rowLine+"&& "+colLine);

								// forloop colspan
								for (int n = colLine; n < colLine + colspan; n++) {
									Cells[rowLine][n].setcell_values(td.get(l).getTextContent());
									// System.out.println("//////"+td.get(l).getTextContent());

									Cell Entry = new Cell(rowLine, td.get(l).getTextContent());
									tableCol[n].Rowcell[rowLine] = new Cell(Entry);

									if (tableCol[n].getRowEntries()[rowLine] == null)
										tableCol[n].getRowEntries()[rowLine] = td.get(l).getTextContent();
									else
										tableCol[n]
												.getRowEntries()[rowLine] = tableCol[colLine].getRowEntries()[rowLine]
														+ " " + td.get(l).getTextContent();

								}
								colLine += colspan;
							} else if (rowspan > 1 && colspan > 1) {
								for (int m = rowLine; m < rowLine + rowspan; m++) {
									for (int n = colLine; n < colLine + colspan; n++) {
										Cells[m][n].setcell_values(td.get(l).getTextContent());
									}
								}

								colLine += colspan;
							}

						}
					}

				}

				table.setTable_cells(Cells);

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
	public static Table ParseTableCaptionandTableHeadingsforTraitRelatedWords(Table table) {
		List<String> HeaderList = new ArrayList<String>();

		for (HC[] hrow : table.header_cells) {
			for (HC header : hrow) {
				try {
					HeaderList.add(header.getHeadercell_value().toLowerCase());
				} catch (Exception e) {
					continue;
				}
			}
		}
		try {
			String[] wordlist = { "trait", "qtl", "quantitavie trait loci" };

			for (int j = 0; j < wordlist.length; j++) {
				if (table.getTable_caption().toLowerCase().indexOf(wordlist[j]) != -1) {
					table.setisTraitTable(true);
					System.out.println("\n" + table.getisTraitTable() + " is a QTL table\n" + table.getTableid());
					return table;
				}
				for (String h : HeaderList) {
					if (h.indexOf(wordlist[j]) != -1) {
						table.setisTraitTable(true);
						System.out.println("\n" + table.getisTraitTable() + " is a QTL table\n" + table.getTableid());
						return table;
					} else {
						continue;
					}
				}
			}
			System.out.println("\n" + table.getisTraitTable() + " is not a QTL table\n" + table.getTableid());
		} catch (Exception e) {

		}
		return table;

	}
}
