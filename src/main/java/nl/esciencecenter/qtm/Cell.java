/**
 * @author gurnoor
 * The Class Cell. One cell of the table. Contains all necessary information
 * about cell
 */

package nl.esciencecenter.qtm;

import org.json.simple.JSONObject;

import nl.esciencecenter.qtm.utils.Utilities;

public class Cell {

	/** The row_number. */
	private int row_number;

	/** The cell_content. */
	private String cell_type;

	/** The cell_content. */
	private String cell_value;

	private JSONObject annotations = new JSONObject();

	private String abbreviated_value;

	// Constructors
	/**
	 * Instantiates a new cell.
	 *
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 */
	public Cell(int i) {
		row_number = i;

	}

	public Cell(int i, String cell_value) {
		this.row_number = i;
		this.cell_value = cell_value;
		// this.cell_type=getCell_type();
	}

	public Cell(Cell c) {
		this.row_number = c.row_number;
		this.cell_type = c.cell_type;
		this.cell_value = c.cell_value;
	}

	// Getters and setters
	public String getAbbreviated_value() {
		return abbreviated_value;
	}

	public void setAbbreviated_value(String abbreviated_value) {
		this.abbreviated_value = abbreviated_value;
	}

	public JSONObject getAnnotations() {
		return annotations;
	}

	public void setAnnotations(JSONObject annotationss) {
		annotations = annotationss;
	}

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
	 * @param row_number
	 *            the new row_number
	 */
	public void setRow_number(int row_number) {
		this.row_number = row_number;
	}

	/**
	 * Gets the cell_type.
	 *
	 * @return the cell_type
	 */
	public String getCell_type() {
		if (this.getcell_value() == null || this.getcell_value().equals("")) {
			this.setCell_type("Empty");
			return this.cell_type;
		}
		if (Utilities.isNumeric(this.getcell_value())) {
			this.setCell_type("Numeric");
			return this.cell_type;
		}
		// Main.logger.trace("###########"+this.getcell_value());

		int numbers = 0;
		int chars = 0;

		String tempCellVal = this.getcell_value().replaceAll("[\\s\\xA0]", "");
		for (int i = 0; i < tempCellVal.length(); i++) {
			if (Utilities.isNumeric(tempCellVal.substring(i, i + 1))) {
				numbers++;
			} else {
				chars++;
			}
		}
		float proportion = (float) numbers / (chars + numbers);
		// part numeric cell
		if (proportion > 0.49 && !Utilities.isNumeric(this.getcell_value())) {
			this.cell_type = "Partially Numeric";
			return "Partially Numeric";
		}
		if (proportion <= 0.49 && !Utilities.isNumeric(this.getcell_value())) {
			this.cell_type = "Text";
			return "Text";
		}
		if (Utilities.isSpaceOrEmpty(this.getcell_value())) {
			this.cell_type = "Empty";
			return "Empty";
		}
		this.cell_type = "Others";
		return this.cell_type;

	}

	/**
	 * Sets the cell_content.
	 *
	 * @param cell_content
	 *            the new cell_content
	 */
	public void setCell_type(String cell_type) {
		this.cell_type = cell_type;
	}

	/**
	 *
	 * @return
	 */
	public String getcell_value() {
		return cell_value;
	}

	public void setcell_values(String value) {
		this.cell_value = value;

		if (cell_type == null)
			cell_type = this.getCell_type();
	}

}
