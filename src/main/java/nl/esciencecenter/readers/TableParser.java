/**
 * @author gurnoor
 * Table Parser Class
 */
package nl.esciencecenter.readers;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import nl.esciencecenter.qtm.Article;
import nl.esciencecenter.qtm.Cell;
import nl.esciencecenter.qtm.Columns;
import nl.esciencecenter.qtm.Main;
import nl.esciencecenter.qtm.Table;
import nl.esciencecenter.utils.Utilities;

public class TableParser {

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

			List<Node> tableTags = getChildrenByTagName(tablesxml.item(i), "table");
			// Main.logger.trace("---------"+tableTags.size());

			if (tableTags.size() == 0) {
				tableTags.clear();
				List<Node> alternative = getChildrenByTagName(tablesxml.item(i), "alternatives");
				// Main.logger.trace("---------alternative"+alternative.size());

				if (alternative.size() > 0) {
					for (int p = 0; p < alternative.size(); p++) {
						List<Node> tableTags2 = getChildrenByTagName(alternative.get(p), "table");
						tableTags = tableTags2;
					}

				}

			}

			for (int s = 0; s < tableTags.size(); s++) {

				String label = readTableLabel(tablesxml.item(i));

				tables[tableindex] = new Table(label);

				tables[tableindex].setDocumentFileName(article.getPmc());

				// table in xml
				tables[tableindex].setXml(Utilities.createXMLStringFromSubNode(tablesxml.item(i)));

				Main.logger.debug("Table label: " + tables[tableindex].getTable_label());

				Scanner tlable = new Scanner(tables[tableindex].getTable_label()).useDelimiter("[^0-9]+");
				int tid = tlable.nextInt();

				// Main.logger.debug("sssssssssssssssssssssssssssssss is "+s);
				if (tableTags.size() > 1) {

					// Main.logger.debug("sssssssssssssssssssssssssssssss is
					// "+s);
					// Main.logger.debug("s is "+s%10);

					tables[tableindex].setTableid(tid);
				} else {
					tables[tableindex].setTableid(tid);
				}

				String caption = readTableCaption(tablesxml.item(i)).replaceAll("\n", "").replace("\r", "");
				Main.logger.debug("Caption: " + caption);
				tables[tableindex].setTable_caption(caption);

				String foot = readTableFooter(tablesxml.item(i)).replaceAll("\n", "").replace("\r", "");
				tables[tableindex].setTable_footer(foot);
				// Main.logger.debug("Foot: " + foot);

				List<Node> thead = getChildrenByTagName(tableTags.get(s), "thead");
				List<Node> tbody = getChildrenByTagName(tableTags.get(s), "tbody");

				int numofCol = countColumns(tbody, thead);

				if (numofCol == 0) {
					Main.logger.debug("Table cannot be processed as number of colums are zero");
					tables[tableindex].setisTraitTable(false);
					tables[tableindex].setNum_of_rows(0);
					tables[tableindex].setNum_of_columns(0);
					Main.logger.debug(tables[tableindex].getisTraitTable() + " " + tables[tableindex].getTabnum()
							+ " is not a trait table");
					tableindex++;
					break;
				}

				tables[tableindex] = processTableHeader(tables[tableindex], numofCol, thead);
				tables[tableindex] = processTableBody(tables[tableindex], numofCol, tbody);

				tables[tableindex] = parseTableCaptionandTableHeadingsforTraitRelatedWords(tables[tableindex]);

				if (tables[tableindex].isaTraitTable()) {
					// entering table values of num of rows and cols
					int numOfRows = 0;
					for (Columns col : tables[tableindex].getTableCol()) {
						if (numOfRows < col.getcelz().length) {
							numOfRows = col.getcelz().length;
						}
					}
					tables[tableindex].setNum_of_rows(numOfRows);
					tables[tableindex].setNum_of_columns(numofCol);

					Main.logger.debug("Number of Rows in " + tables[tableindex].getTable_label() + " is: "
							+ tables[tableindex].getNum_of_rows());
					Main.logger.debug("Number of Columns in " + tables[tableindex].getTable_label() + " is: "
							+ tables[tableindex].getNum_of_columns());

					// table classification
					tables[tableindex] = tables[tableindex].tableClassification();
					tables[tableindex].printTable2();
					Main.logger.debug("______________________");
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
		if (numOfTables < tablesxml.getLength()) {
			numOfTables = tablesxml.getLength();
		}
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
				int rowLine = 0;
				int colLine = 0;
				for (int k = 0; k < headerRows.size(); k++) {
					rowLine = k;
					colLine = 0;
					List<Node> th = null;
					th = getChildrenByTagName(headerRows.get(k), "th");
					if (th.isEmpty()) {
						th = getChildrenByTagName(headerRows.get(k), "td");
					}
					for (int l = 0; l < th.size(); l++) {
						int rowspan = 1;
						int colspan = 1;
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
							if (headerCols[colLine] == null)
								headerCols[colLine] = th.get(l).getTextContent().replaceAll("\n", "").replace("\r", "");
							else
								headerCols[colLine] = headerCols[colLine] + " "
										+ th.get(l).getTextContent().replaceAll("\n", "").replace("\r", "");

							colLine++;
						} else if (rowspan == 1 && colspan > 1) {
							for (int n = colLine; n < colLine + colspan; n++) {
								if (headerCols[n] == null)
									headerCols[n] = th.get(l).getTextContent().replaceAll("\n", "").replace("\r", "");
								else
									headerCols[n] = headerCols[n] + " "
											+ th.get(l).getTextContent().replaceAll("\n", "").replace("\r", "");
							}
							colLine += colspan;
						} else if (rowspan > 1 && colspan == 1) {
							for (int m = rowLine + 1; m < rowLine + rowspan; m++) {
								if (headerCols[colLine] == null)
									headerCols[colLine] = th.get(l).getTextContent().replaceAll("\n", "").replace("\r",
											"");
								else
									headerCols[colLine] = headerCols[colLine] + " "
											+ th.get(l).getTextContent().replaceAll("\n", "").replace("\r", "");
							}
							colLine++;
						}
					}
				}

				for (int i = 0; i < numberofCol; i++) {
					try {
						headerCols[i] = headerCols[i].trim();
					} catch (Exception e) {
						headerCols[i] = "";
					}
					tableCol[i] = new Columns();
					tableCol[i].setHeader(headerCols[i]);
					tableCol[i].setColID(i);
				}

				table.setTableHeadersColumns(headerCols);
				table.setTableCol(tableCol);
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
				for (Columns column : tableCol) {
					column.entries = new String[Rows.size()];
					column.celz = new Cell[Rows.size()];
				}
				int rowLine = 0;
				int colLine = 0;
				for (int k = 0; k < Rows.size(); k++) {
					rowLine = k;
					colLine = 0;
					List<Node> td = null;
					td = getChildrenByTagName(Rows.get(k), "td");
					for (int l = 0; l < td.size(); l++) {
						int rowspan = 1;
						int colspan = 1;
						try {
							rowspan = Utilities
									.getFirstValue(td.get(l).getAttributes().getNamedItem("rowspan").getNodeValue());
							colspan = Utilities
									.getFirstValue(td.get(l).getAttributes().getNamedItem("colspan").getNodeValue());
						} catch (NullPointerException e) {
						}

						if (tableCol[colLine].celz[rowLine] != null && rowLine < Rows.size()) {
							colLine++;
						}

						if (rowLine < Rows.size() || colLine < numberofCol) {
							if (rowspan == 1 && colspan == 1) {
								Cell Entry = new Cell(rowLine,
										td.get(l).getTextContent().replaceAll("\n", "").replace("\r", ""));
								tableCol[colLine].celz[rowLine] = new Cell(Entry);
								if (tableCol[colLine].getRowEntries()[rowLine] == null)
									tableCol[colLine].getRowEntries()[rowLine] = td.get(l).getTextContent()
											.replaceAll("\n", "").replace("\r", "");
								else
									tableCol[colLine]
											.getRowEntries()[rowLine] = tableCol[colLine].getRowEntries()[rowLine] + " "
													+ td.get(l).getTextContent().replaceAll("\n", "").replace("\r", "");

								colLine++;
							} else if (rowspan > 1 && colspan == 1) {
								for (int m = rowLine; m < rowLine + rowspan; m++) {
									Cell Entry = new Cell(m,
											td.get(l).getTextContent().replaceAll("\n", "").replace("\r", ""));
									tableCol[colLine].celz[m] = new Cell(Entry);
									Main.logger.trace(tableCol[colLine].celz[m].getcell_value());

									if (tableCol[colLine].getRowEntries()[m] == null) {
										tableCol[colLine].getRowEntries()[m] = td.get(l).getTextContent()
												.replaceAll("\n", "").replace("\r", "");
									} else {
										tableCol[colLine].getRowEntries()[m] = tableCol[colLine].getRowEntries()[m]
												+ " "
												+ td.get(l).getTextContent().replaceAll("\n", "").replace("\r", "");
									}
									Main.logger.trace("Row span entry" + td.get(l).getTextContent());
								}
								colLine++;
							} else if (rowspan == 1 && colspan > 1) {
								for (int n = colLine; n < colLine + colspan; n++) {
									Cell Entry = new Cell(rowLine,
											td.get(l).getTextContent().replaceAll("\n", "").replace("\r", ""));
									tableCol[n].celz[rowLine] = new Cell(Entry);

									if (tableCol[n].getRowEntries()[rowLine] == null) {
										tableCol[n].getRowEntries()[rowLine] = td.get(l).getTextContent()
												.replaceAll("\n", "").replace("\r", "");
									} else {
										tableCol[n]
												.getRowEntries()[rowLine] = tableCol[colLine].getRowEntries()[rowLine]
														+ " " + td.get(l).getTextContent().replaceAll("\n", "")
																.replace("\r", "");
									}
									colLine += colspan;
								}
							}
						}
					}
				}
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
				nodeList.add(child);
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
			String[] wordlist = {"trait", "qtl", "quantitavie trait loci", "phenotype"};

			for (int j = 0; j < wordlist.length; j++) {
				if (table.getTable_caption().toLowerCase().indexOf(wordlist[j]) != -1) {
					table.setisTraitTable(true);
					Main.logger.debug(table.getTabnum() + " is a QTL table");
					return table;
				}
				for (String h : HeaderList) {
					if (h.indexOf(wordlist[j]) != -1) {
						table.setisTraitTable(true);
						Main.logger.debug(table.getTabnum() + " is a QTL table");
						return table;
					} else {
						continue;
					}
				}
			}
			Main.logger.debug(table.getTabnum() + " is NOT a QTL table");
		} catch (Exception e) {
		}
		return table;
	}
}
