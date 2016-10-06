/**
 *
 * @author gurnoor
 */

package tablInEx;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import utils.Utilities;
import  java.util.regex.Pattern;


/**
 * The Class Cell. One cell of the table. Contains all necessary information about cell
 *
 */
public class C {

        /** The row_number. */
	private int row_number;
	
	/** The column_number. */
	private int column_number;
	
	/** The cell_content. */
	private String cell_type;
	
        /** The cell_content. */
	private String cell_value;
	
	/** Solanacea Phenotypic Ontology */
	private String spAnnotaion;
	
	/**Plant Ontology */
	private String poAnnotaion;
	
	/**trait Ontology */
	private String toAnnotaion;
	
	
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

	public String getSpAnnotaion(){
		return spAnnotaion;
	}

	public void setSpAnnotaion(String spAnno){
		this.spAnnotaion=spAnno;
	}

	public String getPoAnnotaion() {
		return poAnnotaion;
	}

	public void setPoAnnotaion(String poAnnotaion) {
		this.poAnnotaion = poAnnotaion;
	}

	public String getToAnnotaion() {
		return toAnnotaion;
	}

	public void setToAnnotaion(String toAnnotaion) {
		this.toAnnotaion = toAnnotaion;
	}

	//Constructors
	/**
	 * Instantiates a new cell.
	 *
	 * @param i the i
	 * @param j the j
	 */
	public C(int i, int j)
	{
		row_number = i;
		column_number = j;
	}
	
        public C(int i, int j, String cell_value )
	{
		this.row_number = i;
		this.column_number = j;
                this.cell_value=cell_value;
                //this.cell_type=getCell_type();
	}
        
	public C(C c)
	{
		this.column_number = c.column_number;
		this.row_number = c.row_number;
		this.cell_type = c.cell_type;
                this.cell_value=c.cell_value;
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
	public String getCell_type() {
                if(this.getcell_value()==null || this.getcell_value().equals("") )
		{
			this.setCell_type("Empty");
                        return this.cell_type;
		}
		if(Utilities.isNumeric(this.getcell_value()))
		{
                        this.setCell_type("Numeric");
			return this.cell_type;
		}
		//System.out.print("###########"+this.getcell_value());
                
                
                int numbers = 0;
                int chars = 0;
                
		String tempCellVal = this.getcell_value().replaceAll("[\\s\\xA0]","");
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
		if(proportion>0.49 && !Utilities.isNumeric(this.getcell_value()))
		{
                        this.cell_type="Partially Numeric";
			return "Partially Numeric";
		}
		if(proportion<=0.49 && !Utilities.isNumeric(this.getcell_value()))
		{
                        this.cell_type="Text";
			return "Text";
		}
		if(Utilities.isSpaceOrEmpty(this.getcell_value()))
		{
                        this.cell_type="Empty";
			return "Empty";
		}
                this.cell_type="Others";
		return this.cell_type;
            
	}
        
       
	/**
	 * Sets the cell_content.
	 *
	 * @param cell_content the new cell_content
	 */
	public void setCell_type(String cell_type) {
		this.cell_type = cell_type;
	}
        
        
        /**
         * 
         * @return 
         */
        public String getcell_value(){
            return cell_value;
        }
	
	
	
	public void setcell_values(String value )
	{
		this.cell_value=value;
                
                if(cell_type==null)
                    cell_type=this.getCell_type();
	}
     
	
		
}
        
        
        

	

