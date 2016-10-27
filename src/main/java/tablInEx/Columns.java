package tablInEx;

public class Columns {

	private String colID;
	
	public String getColID() {
		return colID;
	}

	public void setColID(String colID) {
		this.colID = colID;
	}


	private String Header="";
	
	private String Columns_type="";
	
	public String[] RowEntries; 
	
	public Cell[] Rowcell;

	public Columns() {
		// TODO Auto-generated constructor stub
	}
	
//	public void CreateCells(int Rows )
//	{
//		RowEntries = new C[Rows];
//		for(int i=0;i<Rows;i++)
//		{
//			 RowEntries[i] = new C();
//			}
//		}
	

	public Cell[] getRowcell() {
		return Rowcell;
	}

	public void setRowcell(Cell[] rowcell) {
		Rowcell = rowcell;
	}

	
	
   	public String getColumns_type() {
		return Columns_type;
	}

	public void setColumns_type(String columns_type) {
		Columns_type = columns_type;
	}

	public String getHeader() {
		return Header;
	}

	public void setHeader(String header) {
		Header = header;
	}
	
	public String[] getRowEntries() {
		return RowEntries;
	}


	public void setRowEntries(String[] rowEntries) {
		RowEntries = rowEntries;
	}

	
	
}
