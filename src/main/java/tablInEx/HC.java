/**
 *
 * @author gurnoor
 */

package tablInEx;

import java.util.LinkedList;

import org.json.simple.JSONObject;

import utils.Utilities;

/**
 * The Class Cell. One cell of the table. Contains all necessary information about cell
 *
 */
public class HC {

        /** The row_number. */
	private int row_number;
	
	/** The column_number. */
	private int column_number;
	
	/** The cell_content. */
	private String Headercell_type;
	
        /** The cell_content. */
	private String Headercell_value;
	
	private JSONObject Annotations=new JSONObject();
	
	private String abbreviated_value;
	
	
	public String getAbbreviated_value() {
		return abbreviated_value;
	}

	public void setAbbreviated_value(String abbreviated_value) {
		abbreviated_value = abbreviated_value;
	}

	public JSONObject getAnnotations() {
		return Annotations;
	}

	public void setAnnotations(JSONObject annotations) {
		Annotations = annotations;
	}

	//Constructors
	/**
	 * Instantiates a new cell.
	 *
	 * @param i the i
	 * @param j the j
	 */
	public HC(int i, int j)
	{
		row_number = i;
		column_number = j;
	}
	
        public HC(int i, int j, String headercell_value )
	{
		this.row_number = i;
		this.column_number = j;
                this.Headercell_value=headercell_value;
	}
        
	public HC(HC c)
	{
		this.column_number = c.column_number;
		this.row_number = c.row_number;
		this.Headercell_type = c.Headercell_type;
                this.Headercell_value=c.Headercell_value;
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
	public String getHeaderCell_type() {
		return Headercell_type;
	}
        
       
	/**
	 * Sets the cell_content.
	 *
	 * @param cell_content the new cell_content
	 */
	public void setHeaderCell_type(String cell_type) {
		this.Headercell_type = cell_type;
	}
        
        
        /**
         * 
         * @return 
         */
        public String getHeadercell_value(){
            return Headercell_value;
        }
	
	
	
	public void setHeadercell_values(String value )
	{
		this.Headercell_value=value;
                this.Headercell_value=this.Headercell_value.trim();
                this.Headercell_value=this.Headercell_value.replaceAll("\\s+"," ");
	}
        
        
        
        

	
}
