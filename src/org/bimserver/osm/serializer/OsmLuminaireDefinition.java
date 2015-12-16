package org.bimserver.osm.serializer;
import java.util.UUID;

public class OsmLuminaireDefinition {
	private String handle;
	private String name;
	//private String IESFilePath;
	//private double lightingPower;
	//private double fractionRadiant;
	//private double fractionVisible;
	//private double returnAirFraction;
	//private boolean returnAirFractionCalculatedfromPlenumTemp;
	//private double coefficient1;
	//private double coefficient2;
	
	public OsmLuminaireDefinition() {
		this.handle = UUID.randomUUID().toString();
		this.name = "";
	}
	
	public OsmLuminaireDefinition(String name) {
		this.handle = UUID.randomUUID().toString();
		this.name = name;
	}
	
	public String getHandle() {
		return handle;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		StringBuilder output = new StringBuilder();

		output.append("OS:Luminaire:Definition,\n");
		output.append("{" + handle + "}" + ",                              !- Handle\n");
		output.append(name + ",                                            !- Name\n");
		output.append(",                                                   !- IES File Path\n");
		output.append(",                                 	               !- Lighting Power\n");
		output.append(",                                                   !- Fraction Radiant\n");
		output.append(",                                                   !- Fraction Visible\n");
		output.append(",                                                   !- Return Air Fraction\n");
		output.append(",                                                   !- Return Air Fraction Calculated from Plenum Temperature\n");
		output.append(",                                                   !- Return Air Fraction Function of Plenum Temperature Coefficient 1\n");
		output.append(";                                                   !- Return Air Fraction Function of Plenum Temperature Coefficient 2\n");
	
		output.append("\n");

		return output.toString();
	}
	
}