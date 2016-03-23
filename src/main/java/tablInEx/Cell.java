/**
 *
 * @author gurnoor
 */

package tablInEx;

import java.util.LinkedList;

import Utils.Utilities;

/**
 * The Class Cell. One cell of the table. Contains all necessary information about cell
 * @author Nikola Milosevic
 */
public class Cell {
	
	private String subheader_values;
	
	private String stub_values;
	
	public String CellId;
	
	private String header_values;
	private String header_ref;
	private String stub_ref;
	private String head_stub_ref;
	private String super_row_ref;
	public LinkedList<String> CellRoles = new LinkedList<String>();
	//public LinkedList<Annotation> annotations = new LinkedList<Annotation>();
	public LinkedList<String>headers = new LinkedList<String>();
	public LinkedList<String>stubs = new LinkedList<String>();
	
	private String head00;
	
	private boolean isUnderSubheader = false;
	
	private boolean isBreakingLineOverRow = false;
	
	private int subheader_level=0;
	
	
	/** The row_number. */
	private int row_number;
	
	/** The column_number. */
	private int column_number;
	
	/** The cell_content. */
	private String cell_content;
	
	/** The is_header. */
	private boolean is_header;
	
	/** The is_stub. */
	private boolean is_stub;
	
	private boolean is_subheader=false;
	
	private boolean is_filled = false;
	
	/** The stub_probability. */
	private float stub_probability;
	
	/** The header_probability. */
	private float header_probability;
	
	/** The is_rowspanning. */
	private boolean is_rowspanning;
	
	/** The is_columnspanning. */
	private boolean is_columnspanning;
	
	/** The cells_rowspanning. */
	private int cells_rowspanning;
	
	/** The rowspanning_index. */
	private int rowspanning_index;
	
	/** The columnspanning_index. */
	private int columnspanning_index;
	
	/** The cells_columnspanning. */
	private int cells_columnspanning;
	
	private String superRowIndex;
	
	//Constructors
	/**
	 * Instantiates a new cell.
	 *
	 * @param i the i
	 * @param j the j
	 */
	public Cell(int i, int j)
	{
		column_number = i;
		row_number = j;
	}
	
	public Cell(Cell c)
	{
		this.subheader_values = c.subheader_values;
		this.stub_values = c.stub_values;	
		this.CellId = c.CellId;
		this.header_values=c.header_values;
		this.superRowIndex = c.superRowIndex;
		this.headers = new LinkedList<String>();
		for(String head:c.headers)
		{
			this.headers.add(head);
		}
		this.stubs = new LinkedList<String>();
		for(String stub:c.stubs)
		{
			this.stubs.add(stub);
		}
		this.head00 = c.head00;
		this.isUnderSubheader = c.isUnderSubheader;
		this.isBreakingLineOverRow = c.isBreakingLineOverRow;
		this.row_number = c.row_number;
		this.column_number = c.column_number;
		this.cell_content = c.cell_content;
		this.is_header = c.is_header;
		this.is_stub = c.is_stub;
		this.is_filled = c.is_filled;
		this.stub_probability = c.stub_probability;
		this.header_probability = c.header_probability;
		this.is_rowspanning = c.is_rowspanning;
		this.is_columnspanning = c.is_columnspanning;
		this.is_subheader = c.is_subheader;
		this.cells_rowspanning = c.cells_rowspanning;
		this.cells_columnspanning = c.cells_columnspanning;
		this.columnspanning_index = c.columnspanning_index;
		this.rowspanning_index = c.rowspanning_index;
		this.subheader_level = c.subheader_level;
	}
	
	// Getters and setters
	/**
	 * Gets the row_number.
	 *
	 * @return the row_number
	 */
	public int getRow_number() {
		return row_number;
	}
	
	/**
	 * Sets the row_number.
	 *
	 * @param row_number the new row_number
	 */
	public void setRow_number(int row_number) {
		this.row_number = row_number;
	}
	
	/**
	 * Gets the column_number.
	 *
	 * @return the column_number
	 */
	public int getColumn_number() {
		return column_number;
	}
	
	/**
	 * Sets the column_number.
	 *
	 * @param column_number the new column_number
	 */
	public void setColumn_number(int column_number) {
		this.column_number = column_number;
	}
	
	/**
	 * Gets the cell_content.
	 *
	 * @return the cell_content
	 */
	public String getCell_content() {
		return cell_content;
	}
	
	/**
	 * Sets the cell_content.
	 *
	 * @param cell_content the new cell_content
	 */
	public void setCell_content(String cell_content) {
		this.cell_content = cell_content;
	}
	
	/**
	 * Checks if is is_header.
	 *
	 * @return true, if is is_header
	 */
	public boolean isIs_header() {
		return is_header;
	}
	
	/**
	 * Sets the is_header.
	 *
	 * @param is_header the new is_header
	 */
	public void setIs_header(boolean is_header) {
		this.is_header = is_header;
	}
	
	/**
	 * Checks if is is_stub.
	 *
	 * @return true, if is is_stub
	 */
	public boolean isIs_stub() {
		return is_stub;
	}
	
	/**
	 * Sets the is_stub.
	 *
	 * @param is_stub the new is_stub
	 */
	public void setIs_stub(boolean is_stub) {
		this.is_stub = is_stub;
	}
	
	/**
	 * Gets the stub_probability.
	 *
	 * @return the stub_probability
	 */
	public float getStub_probability() {
		return stub_probability;
	}
	
	/**
	 * Sets the stub_probability.
	 *
	 * @param stub_probability the new stub_probability
	 */
	public void setStub_probability(float stub_probability) {
		this.stub_probability = stub_probability;
	}
	
	/**
	 * Gets the header_probability.
	 *
	 * @return the header_probability
	 */
	public float getHeader_probability() {
		return header_probability;
	}
	
	/**
	 * Sets the header_probability.
	 *
	 * @param header_probability the new header_probability
	 */
	public void setHeader_probability(float header_probability) {
		this.header_probability = header_probability;
	}
	
	/**
	 * Checks if is is_rowspanning.
	 *
	 * @return true, if is is_rowspanning
	 */
	public boolean isIs_rowspanning() {
		return is_rowspanning;
	}
	
	/**
	 * Sets the is_rowspanning.
	 *
	 * @param is_rowspanning the new is_rowspanning
	 */
	public void setIs_rowspanning(boolean is_rowspanning) {
		this.is_rowspanning = is_rowspanning;
	}
	
	/**
	 * Checks if is is_columnspanning.
	 *
	 * @return true, if is is_columnspanning
	 */
	public boolean isIs_columnspanning() {
		return is_columnspanning;
	}
	
	/**
	 * Sets the is_columnspanning.
	 *
	 * @param is_columnspanning the new is_columnspanning
	 */
	public void setIs_columnspanning(boolean is_columnspanning) {
		this.is_columnspanning = is_columnspanning;
	}
	
	/**
	 * Gets the cells_rowspanning.
	 *
	 * @return the cells_rowspanning
	 */
	public int getCells_rowspanning() {
		return cells_rowspanning;
	}
	
	/**
	 * Sets the cells_rowspanning.
	 *
	 * @param cells_rowspanning the new cells_rowspanning
	 */
	public void setCells_rowspanning(int cells_rowspanning) {
		this.cells_rowspanning = cells_rowspanning;
	}
	
	/**
	 * Gets the cells_columnspanning.
	 *
	 * @return the cells_columnspanning
	 */
	public int getCells_columnspanning() {
		return cells_columnspanning;
	}
	
	/**
	 * Sets the cells_columnspanning.
	 *
	 * @param cells_columnspanning the new cells_columnspanning
	 */
	public void setCells_columnspanning(int cells_columnspanning) {
		this.cells_columnspanning = cells_columnspanning;
	}
	
	
	public String getCellType()
	{
		if(this.getCell_content()==null)
		{
			this.setCell_content("");
		}
		if(Utilities.isNumeric(this.getCell_content()))
		{
			return "Numeric";
		}
		
		int numbers = 0;
		int chars = 0;
		String tempCellVal = this.getCell_content().replaceAll("[\\s\\xA0]","");
		for(int i=0;i<tempCellVal.length();i++)
		{
			if(Utilities.isNumeric(tempCellVal.substring(i, i+1)) )
			{
				numbers++;
			}
			else
			{
				chars++;
			}
		}
		float proportion = (float)numbers / (chars+numbers);
		//part numeric cell
		if(proportion>0.49 && !Utilities.isNumeric(this.getCell_content()))
		{
			return "Partially Numeric";
		}
		if(proportion<=0.49 && !Utilities.isNumeric(this.getCell_content()))
		{
			return "Text";
		}
		if(Utilities.isSpaceOrEmpty(this.getCell_content()))
		{
			return "Empty";
		}
		return "Other";
	}
	
	/**
	 * Sets the cell values. For spanning cells
	 *
	 * @param cell the cell
	 * @param cell_content the cell_content
	 * @param is_columnspanning the is_columnspanning
	 * @param colspanVal the colspan val
	 * @param is_rowSpanning the is_row spanning
	 * @param rowspanning the rowspanning
	 * @param isHeader the is header
	 * @param headerProbability the header probability
	 * @param isStub the is stub
	 * @param stubProbability the stub probability
	 * @param ColumnIndex the column index
	 * @param RowIndex the row index
	 * @param columnspanning_index the columnspanning_index
	 * @param rowspanning_index the rowspanning_index
	 * @return the cell
	 */
	public static Cell setCellValues(Article a, Cell cell, String cell_content, boolean is_columnspanning, int colspanVal, boolean is_rowSpanning, int rowspanning, boolean isHeader,float headerProbability, boolean isStub, float stubProbability, int ColumnIndex,int RowIndex, int columnspanning_index,int rowspanning_index)
	{
		cell.setCell_content(cell_content);
		cell.setCells_columnspanning(colspanVal);
		cell.setHeader_probability(headerProbability);
		cell.setIs_header(isHeader);
		cell.setIs_columnspanning(is_columnspanning);
		cell.setIs_rowspanning(is_rowSpanning);
		cell.setColumn_number(ColumnIndex);
		cell.setRow_number(RowIndex);
		cell.setIs_stub(isStub);
		cell.setStub_probability(stubProbability);
		cell.rowspanning_index = rowspanning_index;
		cell.columnspanning_index = columnspanning_index;
		cell.is_filled = true;
		if(TablInExMainGnr.learnheaders && cell.isIs_header()){
		if(!TablInExMainGnr.headermap.containsKey(cell.getCell_content()))
		{
			TablInExMainGnr.headermap.put(cell.getCell_content(), 1);
		}
		else
		{
			int freq = TablInExMainGnr.headermap.get(cell.getCell_content());
			freq++;
			TablInExMainGnr.headermap.put(cell.getCell_content(), freq);
		}
		}
		
		if(TablInExMainGnr.learnheaders && cell.isIs_stub()){
			if(!TablInExMainGnr.stubmap.containsKey(cell.getCell_content()))
			{
				TablInExMainGnr.stubmap.put(cell.getCell_content(), 1);
			}
			else
			{
				int freq = TablInExMainGnr.stubmap.get(cell.getCell_content());
				freq++;
				TablInExMainGnr.stubmap.put(cell.getCell_content(), freq);
			}
			}
		if((cell.getCell_content().toLowerCase().contains("bmi")||cell.getCell_content().toLowerCase().contains("b.m.i.")||cell.getCell_content().toLowerCase().contains("weight")||cell.getCell_content().toLowerCase().contains("body mass index")||cell.getCell_content().toLowerCase().contains("bodyweight")||cell.getCell_content().toLowerCase().contains("quetelet index")))
        {
			if(!TablInExMainGnr.PMCBMI.contains(a.getPmc()))
			{
				TablInExMainGnr.PMCBMI.add(a.getPmc());
			}
        }
		
		return cell;

	}

	/**
	 * Gets the rowspanning_index.
	 *
	 * @return the rowspanning_index
	 */
	public int getRowspanning_index() {
		return rowspanning_index;
	}

	/**
	 * Sets the rowspanning_index.
	 *
	 * @param rowspanning_index the new rowspanning_index
	 */
	public void setRowspanning_index(int rowspanning_index) {
		this.rowspanning_index = rowspanning_index;
	}

	/**
	 * Gets the columnspanning_index.
	 *
	 * @return the columnspanning_index
	 */
	public int getColumnspanning_index() {
		return columnspanning_index;
	}

	/**
	 * Sets the columnspanning_index.
	 *
	 * @param columnspanning_index the new columnspanning_index
	 */
	public void setColumnspanning_index(int columnspanning_index) {
		this.columnspanning_index = columnspanning_index;
	}

	public boolean isIs_filled() {
		return is_filled;
	}

	public void setIs_filled(boolean is_filled) {
		this.is_filled = is_filled;
	}

	public String getStub_values() {
		return stub_values;
	}

	public void setStub_values(String stub_values) {
		this.stub_values = stub_values;
	}

	public String getHeader_values() {
		return header_values;
	}

	public void setHeader_values(String header_values) {
		this.header_values = header_values;
	}

	public String getHead00() {
		return head00;
	}

	public void setHead00(String head00) {
		this.head00 = head00;
	}

	public String getSubheader_values() {
		return subheader_values;
	}

	public void setSubheader_values(String subheader_values) {
		this.subheader_values = subheader_values;
	}

	public boolean isUnderSubheader() {
		return isUnderSubheader;
	}

	public void setUnderSubheader(boolean isSubheader) {
		this.isUnderSubheader = isSubheader;
	}

	/**
	 * @return the isBreakingLineOverRow
	 */
	public boolean isBreakingLineOverRow() {
		return isBreakingLineOverRow;
	}

	/**
	 * @param isBreakingLineOverRow the isBreakingLineOverRow to set
	 */
	public void setBreakingLineOverRow(boolean isBreakingLineOverRow) {
		this.isBreakingLineOverRow = isBreakingLineOverRow;
	}

	/**
	 * @return the is_subheader
	 */
	public boolean isIs_subheader() {
		return is_subheader;
	}

	/**
	 * @param is_subheader the is_subheader to set
	 */
	public void setIs_subheader(boolean is_subheader) {
		this.is_subheader = is_subheader;
	}

	/**
	 * @return the superRowIndex
	 */
	public String getSuperRowIndex() {
		return superRowIndex;
	}

	/**
	 * @param superRowIndex the superRowIndex to set
	 */
	public void setSuperRowIndex(String superRowIndex) {
		this.superRowIndex = superRowIndex;
	}

	/**
	 * @return the header_ref
	 */
	public String getHeader_ref() {
		return header_ref;
	}

	/**
	 * @param header_ref the header_ref to set
	 */
	public void setHeader_ref(String header_ref) {
		this.header_ref = header_ref;
	}

	/**
	 * @return the stub_ref
	 */
	public String getStub_ref() {
		return stub_ref;
	}

	/**
	 * @param stub_ref the stub_ref to set
	 */
	public void setStub_ref(String stub_ref) {
		this.stub_ref = stub_ref;
	}

	/**
	 * @return the super_row_ref
	 */
	public String getSuper_row_ref() {
		return super_row_ref;
	}

	/**
	 * @param super_row_ref the super_row_ref to set
	 */
	public void setSuper_row_ref(String super_row_ref) {
		this.super_row_ref = super_row_ref;
	}

	/**
	 * @return the head_stub_ref
	 */
	public String getHead_stub_ref() {
		return head_stub_ref;
	}

	/**
	 * @param head_stub_ref the head_stub_ref to set
	 */
	public void setHead_stub_ref(String head_stub_ref) {
		this.head_stub_ref = head_stub_ref;
	}
	
}
