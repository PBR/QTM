/**
 * @author gurnoor
 * The Class Trait.
 */

package nl.esciencecenter.qtm;

import org.json.simple.JSONObject;

public class Trait {

	private static int traitId;

	private String TraitName;

	public static int getTraitID() {
		return traitId;
	}

	public static void setTraitID(int tId) {
		traitId = tId;
	}

	public String getTraitName() {
		return TraitName;
	}

	public void setTraitName(String traitName) {
		TraitName = traitName;
	}

	public JSONObject getTraitProperties() {
		return traitProperties;
	}

	public void setTraitProperties(JSONObject traitProperties) {
		this.traitProperties = traitProperties;
	}

	public JSONObject getTraitValues() {
		return traitValues;
	}

	public void setTraitValues(JSONObject traitValues) {
		this.traitValues = traitValues;
	}

	private JSONObject traitProperties = new JSONObject();

	private JSONObject otherProperties = new JSONObject();

	public JSONObject getOtherProperties() {
		return otherProperties;
	}

	public void setOtherProperties(JSONObject otherProperties) {
		this.otherProperties = otherProperties;
	}

	private JSONObject traitValues = new JSONObject();

	public Trait(String traitName) {
		super();
		this.TraitName = traitName;
	}

	public Trait(String traitName, JSONObject traitProperties, JSONObject traitValues) {
		super();
		this.TraitName = traitName;
		this.traitProperties = traitProperties;
		this.traitValues = traitValues;
	}

}
