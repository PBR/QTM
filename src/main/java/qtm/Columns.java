/**
 * @author gurnoor
 * The Class Column. One Column of the table. Contains all necessary information
 * about column
 */
package qtm;

public class Columns {

	private int colID;

	private String header = "";

	private String columns_type = "";

	public String[] entries;

	public Cell[] celz;

	public int getColID() {
		return colID;
	}

	public void setColID(int colID) {
		this.colID = colID;
	}

	public Columns() {
	}

	public Cell[] getcelz() {
		return celz;
	}

	public void setcelz(Cell[] c) {
		celz = c;
	}

	public String getColumns_type() {
		return columns_type;
	}

	public void setColumns_type(String c_type) {
		columns_type = c_type;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String h) {
		header = h;
	}

	public String[] getRowEntries() {
		return entries;
	}

	public void setRowEntries(String[] rowEntries) {
		entries = rowEntries;
	}

}
