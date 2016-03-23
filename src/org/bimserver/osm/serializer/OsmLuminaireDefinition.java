package org.bimserver.osm.serializer;
import java.util.UUID;

public class OsmLuminaireDefinition {
	private String  handle;
	private String  name;
	private String  IESFilePath;
	private double  lightingPower = 0.0;
	private double  fractionRadiant = 0.0;
	private double  fractionVisible = 0.0;
	private double  returnAirFraction = 0.0;
	//private boolean returnAirFractionCalculatedfromPlenumTemp = false;
	private double  coefficient1 = 0.0;
	private double  coefficient2 = 0.0;

	public OsmLuminaireDefinition() {
		this.handle = UUID.randomUUID().toString();
		this.name = "";
		this.IESFilePath = "";
	}

	public OsmLuminaireDefinition(String name, String IESFilePath) {
		this.handle = UUID.randomUUID().toString();
		this.name = name;
		this.IESFilePath = IESFilePath;
	}

	public String getHandle() {
		return handle;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();

		str.append("OS:Luminaire:Definition,").append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Handle", "{" + handle + "},")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Name", name + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- IES File Path", IESFilePath + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Lighting Power", lightingPower + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Fraction Radiant", fractionRadiant + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Fraction Visible", fractionVisible + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Return Air Fraction", returnAirFraction + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Return Air Fraction Calculated from Plenum Temperature", ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Return Air Fraction Function of Plenum Temperature Coefficient 1", coefficient1 + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Return Air Fraction Function of Plenum Temperature Coefficient 2", coefficient2 + ";")).append(System.getProperty("line.separator"));
		str.append(System.getProperty("line.separator"));

		return str.toString();
	}
}