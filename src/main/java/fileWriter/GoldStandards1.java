package fileWriter;

import java.io.File;
import java.io.IOException;
import java.io.WriteAbortedException;

import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class GoldStandards1 {

	public static String cp1="/home/gurnoor/Documents/Text Corpus/Checkpoint1.csv";
	
	public static void checkpoint1() {
		try{
		//Creates a writable workbook with the given file name
	    WritableWorkbook workbook = Workbook.createWorkbook(new File(cp1));
	    WritableSheet sheet = workbook.createSheet("My Sheet", 0);
	    
	    // Create cell font and format
	    WritableFont cellFont = new WritableFont(WritableFont.TIMES, 10);
	    cellFont.setColour(Colour.BLUE);
	    
	    WritableCellFormat cellFormat = new WritableCellFormat(cellFont);
	    cellFormat.setBackground(Colour.ORANGE);
	    cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

	    Label lable = null;
	    int insertColumn = 3;
	    
	    for (int i = 0 ; i < 4 ; i ++) {
	      sheet.insertColumn(insertColumn); 
	      //Insert new column at column 2 [Moves the column 2 to next column]
	      lable = new Label(insertColumn, 0, "Cell in Row [i=" + i + "]", cellFormat);
	      sheet.addCell(lable);
	    }
	    
	    //Writes out the data held in this workbook in Excel format
	    workbook.write(); 
	    
	    //Close and free allocated memory 
	    workbook.close(); 
	  	}
		catch(Exception e){
		System.out.println(e.getMessage());
		}
	}
}
