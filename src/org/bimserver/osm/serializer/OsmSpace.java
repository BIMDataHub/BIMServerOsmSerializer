package org.bimserver.osm.serializer;

import java.util.ArrayList;
import java.util.List;

public class OsmSpace {
	private String				uuid						= "";
	private boolean				containsUnsolvedSurface		= false;
	private String				spaceName					= "";
	private String				typeName					= "";
	private String				defaultConstructionSetName	= "";
	private String				defaultScheduleSetName		= "";
	private String				directionOfRelativeNorth	= "";
	private String				xOrigin						= "";
	private String				yOrigin						= "";
	private String				zOrigin						= "";
	private String				buildingStoryName			= "";
	private String				thermalZoneName				= "";
	private List<OsmSurface>	surfaceList					= new ArrayList<OsmSurface>();

	public boolean containsUnsolvedSurface() {
		return containsUnsolvedSurface;
	}

	public void setContainsUnsolvedSurface(boolean containsUnsolvedSurface) {
		this.containsUnsolvedSurface = containsUnsolvedSurface;
	}

	public String getSpaceName() {
		return spaceName;
	}

	public void setSpaceName(String spaceName) {
		this.spaceName = spaceName;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getDefaultConstructionSetName() {
		return defaultConstructionSetName;
	}

	public void setDefaultConstructionSetName(String defaultConstructionSetName) {
		this.defaultConstructionSetName = defaultConstructionSetName;
	}

	public String getDefaultScheduleSetName() {
		return defaultScheduleSetName;
	}

	public void setDefaultScheduleSetName(String defaultScheduleSetName) {
		this.defaultScheduleSetName = defaultScheduleSetName;
	}

	public String getDirectionOfRelativeNorth() {
		return directionOfRelativeNorth;
	}

	public void setDirectionOfRelativeNorth(String directionOfRelativeNorth) {
		this.directionOfRelativeNorth = directionOfRelativeNorth;
	}

	public String getxOrigin() {
		return xOrigin;
	}

	public void setxOrigin(String xOrigin) {
		this.xOrigin = xOrigin;
	}

	public String getyOrigin() {
		return yOrigin;
	}

	public void setyOrigin(String yOrigin) {
		this.yOrigin = yOrigin;
	}

	public String getzOrigin() {
		return zOrigin;
	}

	public void setzOrigin(String zOrigin) {
		this.zOrigin = zOrigin;
	}

	public String getBuildingStoryName() {
		return buildingStoryName;
	}

	public void setBuildingStoryName(String buildingStoryName) {
		this.buildingStoryName = buildingStoryName;
	}

	public String getThermalZoneName() {
		return thermalZoneName;
	}

	public void setThermalZoneName(String thermalZoneName) {
		this.thermalZoneName = thermalZoneName;
	}

	public List<OsmSurface> getSurfaceList() {
		return surfaceList;
	}

	public void addSurface(OsmSurface surface) {
		this.surfaceList.add(surface);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("OS:Space,").append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Handle", "{" + uuid + "},")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Name", spaceName + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Space Type Name", typeName + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Default Construction Set Name", defaultConstructionSetName + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Default Schedule Set Name", defaultScheduleSetName + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Direction of Relative North {deg}", directionOfRelativeNorth + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- !- X Origin {m}", xOrigin + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- !- Y Origin {m}", yOrigin + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- !- Z Origin {m}", zOrigin + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- !- Building Story Name", buildingStoryName + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- !- Thermal Zone Name", thermalZoneName + ";")).append(System.getProperty("line.separator"));
		str.append(System.getProperty("line.separator"));

		return str.toString();	
	}
}
