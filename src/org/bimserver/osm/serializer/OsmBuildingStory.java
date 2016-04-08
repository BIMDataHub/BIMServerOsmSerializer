package org.bimserver.osm.serializer;

import java.util.UUID;

public class OsmBuildingStory {
	private String handle;
	private String name;
	private double nominalFloortoFloorHeight;
	private String defaultConstructionSetName;
	private String defaultScheduleSetName;
	private String groupRenderingName;
	private double nominalFloortoCeilingHeight;
	
	public OsmBuildingStory(String name) {
		this.handle = UUID.randomUUID().toString();
		this.name = name;
		this.nominalFloortoFloorHeight = 0.01;
		this.defaultConstructionSetName = "";
		this.defaultScheduleSetName = "";
		this.groupRenderingName = "";
		this.nominalFloortoCeilingHeight = 0.01;
	}
	
	public String getHandle() {
		return handle;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();

		str.append("OS:BuildingStory,").append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Handle", "{" + handle + "},")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Name", name + ",")).append(System.getProperty("line.separator"));
	    str.append(String.format("%-60s!- Nominal Z Coordinate", ",")).append(System.getProperty("line.separator"));
	    str.append(String.format("%-60s!- Nominal Floor to Floor Height",  nominalFloortoFloorHeight + ",")).append(System.getProperty("line.separator"));
	    str.append(String.format("%-60s!- Default Construction Set Name",  defaultConstructionSetName + ",")).append(System.getProperty("line.separator"));
	    str.append(String.format("%-60s!- Default Schedule Set Name",  defaultScheduleSetName + ",")).append(System.getProperty("line.separator"));
	    str.append(String.format("%-60s!- Group Rendering Name",  groupRenderingName + ",")).append(System.getProperty("line.separator"));
	    str.append(String.format("%-60s!- Nominal Floor to Ceiling Height",  nominalFloortoCeilingHeight + ";")).append(System.getProperty("line.separator"));  
		str.append(System.getProperty("line.separator"));
		
		return str.toString();
	}
}
