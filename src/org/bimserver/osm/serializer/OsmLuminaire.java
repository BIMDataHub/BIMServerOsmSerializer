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
		StringBuilder output = new StringBuilder();

		output.append("OS:Luminaire,\n");
		output.append("{" + handle + "}" + ",                              !- Handle\n");
		output.append(name + ",                                            !- Name\n");
		output.append(luminaireDefinitionName + ",                         !- Luminaire Definition Name\n");
		output.append(spaceName + ",                                 	   !- Space or SpaceType Name\n");
		output.append(scheduleName + ",                                    !- Schedule Name\n");
		output.append(point.getX() + ",                                               !- Position X-coordinate\n");
		output.append(point.getY() + ",                                               !- Position Y-coordinate\n");
		output.append(point.getZ() + ",                                               !- Position Z-coordinate\n");
		output.append(",                                                   !- Psi Rotation Around X-axis\n");
		output.append(",                                                   !- Theta Rotation Around Y-axis\n");
		output.append(",                                                   !- Phi Rotation Around Z-axis\n");
		output.append(",                                                   !- Fraction Replaceable\n");
		output.append(",                                                   !- Multiplier\n");
		output.append(";                                                   !- End-Use Subcategory\n");
		
		output.append("\n");

		return output.toString();
	}
}
