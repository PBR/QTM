package qtm;

public class Columns {

	private String colID;


	private String header="";
	
	private String columns_type="";
	
	public String[] entries; 
	
	public Cell[] celz;

	
	public String getColID() {
		return colID;
	}

	public void setColID(String colID) {
		this.colID = colID;
	}

	
	public Columns() {
	}
	
//	public void CreateCells(int Rows )
//	{
//		RowEntries = new C[Rows];
//		for(int i=0;i<Rows;i++)
//		{
//			 RowEntries[i] = new C();
//			}
//		}
	

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
