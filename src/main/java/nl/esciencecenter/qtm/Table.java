/**
 * @author gurnoor
 * The Class Table. Contain various information about table, including the Columns that further contains cells
 */
package nl.esciencecenter.qtm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import nl.esciencecenter.qtm.solr.tagger.utils.TagResponse;
import nl.esciencecenter.qtm.utils.Configs;
import nl.esciencecenter.qtm.solr.tagger.recognize.Evaluate;

public class Table {

	private int tab_num;
	private String xml;
	private String documentFileName;
	public String pragmaticClass;
	public boolean isEmptyOnlyHeaders = true;
	private boolean isTraitTable = false;
	private String[] tableHeadersColumns;
	private Columns[] tableCol;

	public enum StructureType {
		LIST, MATRIX, SUBHEADER, MULTI, NULL
	};

	private String sectionOfTable;
	/** The num_of_rows. */
	private int num_of_rows;

	public int tableInTable;

	/** The num_of_columns. */
	private int num_of_columns;

	/** The table_title. */
	private String table_label;

	/** The table_caption. */
	private String table_caption;

	/** The table_footer. */
	private String table_footer;

	private boolean hasHeader = true;

	private boolean hasBody = true;

	private boolean isNoXMLTable = false;

	private boolean isRowSpanning = false;

	private boolean isColSpanning = false;
	private int structureClass = 0; // 0 - no class,1- simplest, 2 - simple, 3 -
									// medium, 4 - complex
	private StructureType tableStructureType;

	// Constructors
	/**
	 * Instantiates a new table.
	 *
	 * @param label
	 *            the title
	 */
	public Table(String label) {
		table_label = label;
		hasHeader = true;
		hasBody = true;
		isNoXMLTable = false;
		isRowSpanning = false;
		isColSpanning = false;
	}

	/**
	 * Instantiates a new table.
	 *
	 * @param label
	 *            the title
	 * @param Caption
	 *            the caption
	 * @param Footer
	 *            the footer
	 */
	public Table(String label, String Caption, String Footer) {
		table_label = label;
		table_caption = Caption;
		table_footer = Footer;
		// stat = new TableStats();
	}

	/**
	 * Instantiates a new table.
	 *
	 * @param label
	 *            the title
	 * @param Caption
	 *            the caption
	 * @param Footer
	 *            the footer
	 * @param Columns
	 *            the columns
	 * @param Rows
	 *            the rows
	 */
	public Table(String label, String Caption, String Footer, int Columns,
			int Rows) {
		table_label = label;
		table_caption = Caption;
		table_footer = Footer;
		num_of_rows = Rows;
		num_of_columns = Columns;
		// stat = new TableStats();
	}

	public boolean isaTraitTable() {
		String word1 = "QTL";
		String word2 = "trait";
		// String word3="Quantitavie Trait loci";

		if (this.table_caption.toLowerCase().indexOf(word1.toLowerCase()) != -1
				|| this.table_caption.toLowerCase()
						.indexOf(word2.toLowerCase()) != -1)
			return true;
		if (this.table_footer.toLowerCase().indexOf(word1.toLowerCase()) != -1
				|| this.table_footer.toLowerCase()
						.indexOf(word2.toLowerCase()) != -1)
			return true;
		Columns tc[] = this.getTableCol();
		String word3 = "phenotype";
		for (Columns col : tc) {
			if (col.getColumns_type().indexOf("QTL value") == -1) {
				if (col.getHeader().toLowerCase()
						.indexOf(word1.toLowerCase()) != -1
						|| col.getHeader().toLowerCase()
								.indexOf(word2.toLowerCase()) != -1
						|| col.getHeader().toLowerCase()
								.indexOf(word3.toLowerCase()) != -1) {
					return true;
				}
			}
		}

		return false;
	}

	// Getters and setters

	public int getTabnum() {
		return tab_num;
	}

	public void setTableid(int tab_num) {
		this.tab_num = tab_num;
	}

	public String[] getTableHeadersColumns() {
		return tableHeadersColumns;
	}

	public void setTableHeadersColumns(String[] tableHeadersCols) {
		tableHeadersColumns = tableHeadersCols;
	}

	public boolean getisTraitTable() {
		return isTraitTable;
	}

	public void setisTraitTable(boolean isTraitTable) {
		this.isTraitTable = isTraitTable;
	}

	public Columns[] getTableCol() {
		return tableCol;
	}

	public void setTableCol(Columns[] tCol) {
		tableCol = tCol;
	}

	/**
	 * Gets the num_of_rows.
	 *
	 * @return the num_of_rows
	 */
	public int getNum_of_rows() {
		return num_of_rows;
	}

	/**
	 * Sets the num_of_rows.
	 *
	 * @param num_of_rows
	 *            the new num_of_rows
	 */
	public void setNum_of_rows(int num_of_rows) {
		this.num_of_rows = num_of_rows;
	}

	/**
	 * Gets the num_of_columns.
	 *
	 * @return the num_of_columns
	 */
	public int getNum_of_columns() {
		return num_of_columns;
	}

	/**
	 * Sets the num_of_columns.
	 *
	 * @param num_of_columns
	 *            the new num_of_columns
	 */
	public void setNum_of_columns(int num_of_columns) {
		this.num_of_columns = num_of_columns;
	}

	/**
	 * Gets the table_title.
	 *
	 * @return the table_title
	 */
	public String getTable_label() {
		return table_label;
	}

	/**
	 * Sets the table_title.
	 *
	 * @param table_title
	 *            the new table_title
	 */
	public void setTable_label(String table_label) {
		this.table_label = table_label;
	}

	/**
	 * Gets the table_caption.
	 *
	 * @return the table_caption
	 */
	public String getTable_caption() {
		return table_caption;
	}

	/**
	 * Sets the table_caption.
	 *
	 * @param table_caption
	 *            the new table_caption
	 */
	public void setTable_caption(String table_caption) {
		this.table_caption = table_caption;
	}

	/**
	 * Gets the table_footer.
	 *
	 * @return the table_footer
	 */
	public String getTable_footer() {
		return table_footer;
	}

	/**
	 * Sets the table_footer.
	 *
	 * @param table_footer
	 *            the new table_footer
	 */
	public void setTable_footer(String table_footer) {
		this.table_footer = table_footer;
	}

	/**
	 * Gets the table_cells.
	 *
	 * @return the table_cells
	 */
	// public C[][] getTable_cells() {
	// return cells;
	// }

	// public Hc[][] getTable_Headercells() {
	// return header_cells;
	// }

	// public List<C[]> getTable_cellList() {
	// return LOC;
	// }

	/**
	 * Sets the table_cells.
	 *
	 * @param cells
	 *            the new table_cells
	 */

	// public void setTableHeadercells(Hc[][] cells) {
	// this.header_cells = cells;
	// }

	// public void setTable_cells(C[][] cells) {
	// this.cells = cells;
	// }

	// public void setTable_cellList(List<C[]> L) {
	// this.LOC=L;
	// }

	public boolean isHasHeader() {
		return hasHeader;
	}

	public void setHasHeader(boolean hasHeader) {
		this.hasHeader = hasHeader;
	}

	public boolean isHasBody() {
		return hasBody;
	}

	public void setHasBody(boolean hasBody) {
		this.hasBody = hasBody;
	}

	public boolean isNoXMLTable() {
		return isNoXMLTable;
	}

	public void setNoXMLTable(boolean isNoXMLTable) {
		this.isNoXMLTable = isNoXMLTable;
	}

	public boolean isRowSpanning() {
		return isRowSpanning;
	}

	public void setRowSpanning(boolean isRowSpanning) {
		this.isRowSpanning = isRowSpanning;
	}

	public boolean isColSpanning() {
		return isColSpanning;
	}

	public void setColSpanning(boolean isColSpanning) {
		this.isColSpanning = isColSpanning;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public String getDocumentFileName() {
		return documentFileName;
	}

	public void setDocumentFileName(String documentFileName) {
		this.documentFileName = documentFileName;
	}

	public int getStructureClass() {
		return structureClass;
	}

	public void setStructureClass(int sClass) {
		structureClass = sClass;
	}

	/**
	 * @return the tableStructureType
	 */
	public StructureType getTableStructureType() {
		return tableStructureType;
	}

	/**
	 * @param tableStructureType
	 *            the tableStructureType to set
	 */
	public void setTableStructureType(StructureType tStructureType) {
		tableStructureType = tStructureType;
	}

	/**
	 * @return the sectionOfTable
	 */
	public String getSectionOfTable() {
		return sectionOfTable;
	}

	/**
	 * @param sectionOfTable
	 *            the sectionOfTable to set
	 */
	public void setSectionOfTable(String secOfTable) {
		sectionOfTable = secOfTable;
	}

	public void printTable2() throws Exception {

		StringBuffer sb = new StringBuffer();
		
		for (Columns c : this.getTableCol()) {
			sb.append(
					c.getHeader() + "(" + c.getColumns_type() + ")" + "\t\t");
		}
		Main.logger.debug(sb.toString());
		
		int i = 0;
		while (i < this.num_of_rows) {
			sb = new StringBuffer();
			for (Columns c : this.getTableCol()) {
				try {
					sb.append(c.getcelz()[i].getcell_value() + "("
							+ c.getcelz()[i].getCell_type() + ")" + "\t\t");
				} catch (Exception e) {
					sb.append("Null(Null" + "\t\t");
				}
			}
			Main.logger.debug(sb.toString());
			i++;
		}
	}

	public Table tableClassification() {
		// C[][] cells=this.getTable_cells();

		Columns[] tc = this.getTableCol();

		// int rows = cells.length;
		int cols = this.num_of_columns;

		HashMap<String, Integer> ColTypes = new HashMap<String, Integer>();

		for (int l = 0; l < tc.length; l++) {
			ColTypes.clear();
			ColTypes.put("Partially Numeric", 0);
			ColTypes.put("Numeric", 0);
			ColTypes.put("Text", 0);
			ColTypes.put("Empty", 0);
			try {
				for (int k = 0; k < tc[l].getcelz().length; k++) {
					if (tc[l].getcelz()[k].getCell_type() == "Numeric") {
						ColTypes.put("Numeric", ColTypes.get("Numeric") + 1);
					} else if (tc[l].getcelz()[k]
							.getCell_type() == "Partially Numeric") {
						ColTypes.put("Partially Numeric",
								ColTypes.get("Partially Numeric") + 1);
					} else if (tc[l].getcelz()[k].getCell_type() == "Text") {

						ColTypes.put("Text", ColTypes.get("Text") + 1);
					} else if (tc[l].getcelz()[k].getCell_type() == "Empty") {
						ColTypes.put("Empty", ColTypes.get("Empty") + 1);
					}
				}
			} catch (Exception e) {
				Main.logger.debug(e);
			}

			String word1 = "qtl";
			String word2 = "trait";
			String word3 = "phenotype";

			float totalNumeric = (float) ColTypes.get("Numeric")
					/ (float) (tc[l].getcelz().length - ColTypes.get("Empty"));
			float totalPartiallyNumeric = (float) ColTypes
					.get("Partially Numeric")
					/ (float) (tc[l].getcelz().length - ColTypes.get("Empty"));
			float totalText = (float) ColTypes.get("Text")
					/ (float) (tc[l].getcelz().length - ColTypes.get("Empty"));

			if (totalNumeric >= 0.60)
				tc[l].setColumns_type("QTL value");
			else
				tc[l].setColumns_type("QTL property");

			int countwords = 0;
			try {

				if (tc[l].getColumns_type().equals("QTL property")) {

					for (int k = 0; k < tc[l].getcelz().length; k++) {
						if (tc[l].getcelz()[k].getcell_value().toLowerCase()
								.indexOf(word1) != -1
								|| tc[l].getcelz()[k].getcell_value()
										.toLowerCase().indexOf(word2) != -1
								|| tc[l].getcelz()[k].getcell_value()
										.toLowerCase().indexOf(word3) != -1) {
							countwords++;
						}
					}

					if (tc[l].getHeader().toLowerCase().indexOf(word1) != -1
							|| tc[l].getHeader().toLowerCase().toLowerCase()
									.indexOf(word2) != -1
							|| tc[l].getHeader().toLowerCase().toLowerCase()
									.indexOf(word3) != -1)
						countwords++;
				}
			} catch (Exception e) {
				Main.logger.warn("Failed to classify column #" + l + " header.");
				Main.logger.debug(e);
			}

			if (countwords > 0)
				tc[l].setColumns_type("QTL descriptor");

			if (tc[l].getColumns_type() == null) {
				tc[l].setColumns_type("NotIdentified");
			}
		}

		// filter out 1 QTL descriptor based on annotations
		int num_QTLdescriptors = 0;
		List<Integer> QTLdescriptorPosition = new ArrayList<Integer>();
		for (int l = 0; l < tc.length; l++) {
			if (tc[l].getColumns_type().equals("QTL descriptor")) {
				num_QTLdescriptors++;
				QTLdescriptorPosition.add(l);
			}
		}

		if (num_QTLdescriptors > 1) {
			Iterator<Integer> myListIterator = QTLdescriptorPosition.iterator();
			int bestmatch = QTLdescriptorPosition.get(0);
			int numofbestmatchAnnotations = 0;

			try {
				while (myListIterator.hasNext()) {
					int numofannotatedTerms = 0;
					Integer j = myListIterator.next();
					tc[j].setColumns_type("QTL property");
					for (int k = 0; k < tc[j].getcelz().length; k++) {
						TagResponse QTLannotation = Evaluate.processString(tc[j]
								.getcelz()[k]
								.getcell_value()
								.toLowerCase(),
								Configs.getPropertyQTM("coreTraitDescriptors"),
								Configs.getPropertyQTM("match"),
								Configs.getPropertyQTM("type"));

						if (QTLannotation.getItems().size() != 0) {
							numofannotatedTerms++;
						}
					}

					if (numofannotatedTerms > numofbestmatchAnnotations) {
						numofbestmatchAnnotations = numofannotatedTerms;
						bestmatch = j;
					}
				}
				tc[bestmatch].setColumns_type("QTL descriptor");
			} catch (Exception e) {
				Main.logger.debug(e);
			}
		}
		return this;
	}
}
