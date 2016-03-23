package org.bimserver.osm.serializer;

import java.util.UUID;

public class OsmLuminaire {
	private String handle;
	private String name;
	private String luminaireDefinitionName;
	private String spaceName;
	private String scheduleName;
	private OsmPoint point; 
	//double psi;
	//double theta;
	//double phi;
	//double fractionReplaceable;
	//double multiplier;
	//String End-UseSubcategory;
	
	public OsmLuminaire() {
		this.handle = UUID.randomUUID().toString();
		this.name = "";
		this.luminaireDefinitionName = "";
		this.spaceName = "";
		this.scheduleName = "";
		this.point = new OsmPoint();
	}
	
	public OsmLuminaire(String name, String luminaireDefinitionName, String spaceName, OsmPoint point) {
		this.handle = UUID.randomUUID().toString();
		this.name = name;
		this.luminaireDefinitionName = luminaireDefinitionName;
		this.spaceName = spaceName;
		this.scheduleName = "";
		this.point = point;
	}
	
	public String getHandle() {
		return handle;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("OS:Luminaire,").append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Handle", "{" + handle + "},")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Name", name + ",")).append(System.getProperty("line.separator"));
	    str.append(String.format("%-60s!- Luminaire Definition Name", luminaireDefinitionName + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Space or SpaceType Name", spaceName + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Schedule Name", scheduleName + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Position X-coordinate", point.getX() + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Position Y-coordinate", point.getY() + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Position Z-coordinate", point.getZ() + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Psi Rotation Around X-axis", ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Theta Rotation Around Y-axis", ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Phi Rotation Around Z-axis", ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Fraction Replaceable", ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Multiplier", ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- End-Use Subcategory", ";")).append(System.getProperty("line.separator"));	
		str.append(System.getProperty("line.separator"));

		return str.toString();
	}
}
