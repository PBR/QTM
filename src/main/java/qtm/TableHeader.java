/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qtm;

/**
 *
 * @author gurnoor
 */
public class TableHeader {
    
    private static Table T;
    private static int numofHeaderRows;
    private static int numofHeaderCols;
    

    public TableHeader(Table T1) {
        T=T1;
        numofHeaderRows=T1.stat.getNum_of_header_cells();
        
    }
    
    
    
}
