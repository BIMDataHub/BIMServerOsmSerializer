package org.bimserver.osm.serializer;

import java.util.ArrayList;
import java.util.List;

public class OsmSubSurface {
	private String			uuid							= "";
	private String			subSurfaceName					= "";
	private String			typeName						= "";
	private String			constructionName				= "";
	private OsmSurface		osmSurface						= null;
	private String			outsideBoundaryConditionObject	= "";
	private String			viewFactorToGround				= "";
	private String			shadingControlName				= "";
	private String			frameAndDividerName				= "";
	private String			multiplier						= "";
	private String			numberOfVertices				= "";
	private List<OsmPoint>	pointList						= new ArrayList<OsmPoint>();

	public String getSubSurfaceName() {
		return subSurfaceName;
	}

	public void setSubSurfaceName(String subSurfaceName) {
		this.subSurfaceName = subSurfaceName;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getConstructionName() {
		return constructionName;
	}

	public void setConstructionName(String constructionName) {
		this.constructionName = constructionName;
	}

	public OsmSurface getOsmSurface() {
		return osmSurface;
	}

	public void setOsmSurface(OsmSurface osmSurface) {
		this.osmSurface = osmSurface;
	}

	public String getOutsideBoundaryConditionObject() {
		return outsideBoundaryConditionObject;
	}

	public void setOutsideBoundaryConditionObject(String outsideBoundaryConditionObject) {
		this.outsideBoundaryConditionObject = outsideBoundaryConditionObject;
	}

	public String getViewFactorToGround() {
		return viewFactorToGround;
	}

	public void setViewFactorToGround(String viewFactorToGround) {
		this.viewFactorToGround = viewFactorToGround;
	}

	public String getShadingControlName() {
		return shadingControlName;
	}

	public void setShadingControlName(String shadingControlName) {
		this.shadingControlName = shadingControlName;
	}

	public String getFrameAndDividerName() {
		return frameAndDividerName;
	}

	public void setFrameAndDividerName(String frameAndDividerName) {
		this.frameAndDividerName = frameAndDividerName;
	}

	public String getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(String multiplier) {
		this.multiplier = multiplier;
	}

	public String getNumberOfVertices() {
		return numberOfVertices;
	}

	public void setNumberOfVertices(String numberOfVertices) {
		this.numberOfVertices = numberOfVertices;
	}

	public List<OsmPoint> getOsmPointList() {
		return pointList;
	}

	public void addOsmPoint(OsmPoint point) {
		this.pointList.add(point);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("OS:SubSurface,").append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Handle", "{" + uuid + "},")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Name", subSurfaceName + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Sub Surface Type", typeName + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Construction Name", "{" + constructionName + "},")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Surface Name", osmSurface.getSurfaceName() + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Outside Boundary Condition Object", outsideBoundaryConditionObject + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- View Factor to Ground", viewFactorToGround + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Shading Control Name", shadingControlName + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Frame and Divider Name", frameAndDividerName + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Multiplier", multiplier + ",")).append(System.getProperty("line.separator"));
		
		int size = pointList.size();
		if (size <= 0) {
			str.append(String.format("%-60s!- Number of Vertices", ";")).append(System.getProperty("line.separator"));
		} else{
			str.append(String.format("%-60s!- Number of Vertices", ",")).append(System.getProperty("line.separator"));
			
			for (int i = 1; i <= size; i++) {
				OsmPoint osmPoint = pointList.get(i - 1);
				String delimiter = i == size ? ";" : ",";
				str.append(String.format("%-60s!- X,Y,Z Vertex %d {m}", osmPoint.getX() + "," + osmPoint.getY() + "," + osmPoint.getZ() + delimiter, i)).append(System.getProperty("line.separator"));	
			}
		}
		str.append(System.getProperty("line.separator"));
		
		return str.toString();
	}
}
