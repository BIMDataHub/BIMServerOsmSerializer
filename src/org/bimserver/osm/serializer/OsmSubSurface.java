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
		StringBuilder output = new StringBuilder();
		
		output.append("OS:SubSurface,\n  ");
		output.append("{" + uuid + "}" + ",                              !- Handle\n  ");
		output.append(subSurfaceName + ",                     !- Name\n  ");
		output.append(typeName + ",                     !- Surface Type\n  ");
		output.append(constructionName + ",                         !- Construction Name\n  ");
		output.append(osmSurface.getSurfaceName() + ",                  !- Surface Name\n  ");
		output.append(outsideBoundaryConditionObject + ",                         !- Outside Boundary Condition Object\n  ");
		output.append(viewFactorToGround + ",                         !- View Factor to Ground\n  ");
		output.append(shadingControlName + ",                         !- Shading Control Name\n  ");
		output.append(frameAndDividerName + ",                         !- Frame and Divider Name\n  ");
		output.append(multiplier + ",                         !- Multiplier\n  ");
		output.append(numberOfVertices);
		
		int size = pointList.size();
		if(size <= 0) {
			output.append(";                         !- Number of Vertices\n  ");
		} else{
			output.append(",                         !- Number of Vertices\n  ");
			for(int i = 0; i < size; i ++) {
				OsmPoint osmPoint = pointList.get(i);
				output.append(String.valueOf(osmPoint.getX()));
				output.append(",");
				output.append(String.valueOf(osmPoint.getY()));
				output.append(",");
				output.append(String.valueOf(osmPoint.getZ()));
				if(i != size - 1) {
					output.append(",  !- X,Y,Z Vertex ");
				} else {
					output.append(";  !- X,Y,Z Vertex ");
				}
				output.append(String.valueOf(i + 1));
				output.append(" {m}\n  ");
			}
		}
		output.append("\n");
		return output.toString();
	}

}
