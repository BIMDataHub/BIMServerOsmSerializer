package org.bimserver.osm.model;

import java.util.ArrayList;
import java.util.List;

public class OsmSurface {
	private String				uuid							= "";
	private String				surfaceName						= "";
	private String				typeName						= "";
	private String				constructionName				= "";
	private OsmSpace			osmSpace						= null;
	private String				outsideBoundaryCondition		= "";
	private String				outsideBoundaryConditionObject	= "";
	private String				sunExposure						= "";
	private String				windExposure					= "";
	private String				viewFactorToGround				= "";
	private String				numberOfVertices				= "";
	private List<OsmPoint>		pointList						= new ArrayList<OsmPoint>();
	
	//TODO: We do not need to maintain a list of subsurfaces of the surface. Remove this and use a global variable to maintain all elements.
	private List<OsmSubSurface>	subSurfaceList					= new ArrayList<OsmSubSurface>();

	public String getSurfaceName() {
		return surfaceName;
	}

	public void setSurfaceName(String surfaceName) {
		this.surfaceName = surfaceName;
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

	public OsmSpace getOsmSpace() {
		return osmSpace;
	}

	public void setOsmSpace(OsmSpace osmSpace) {
		this.osmSpace = osmSpace;
	}

	public String getOutsideBoundaryCondition() {
		return outsideBoundaryCondition;
	}

	public void setOutsideBoundaryCondition(String outsideBoundaryCondition) {
		this.outsideBoundaryCondition = outsideBoundaryCondition;
	}

	public String getOutsideBoundaryConditionObject() {
		return outsideBoundaryConditionObject;
	}

	public void setOutsideBoundaryConditionObject(String outsideBoundaryConditionObject) {
		this.outsideBoundaryConditionObject = outsideBoundaryConditionObject;
	}

	public String getSunExposure() {
		return sunExposure;
	}

	public void setSunExposure(String sunExposure) {
		this.sunExposure = sunExposure;
	}

	public String getWindExposure() {
		return windExposure;
	}

	public void setWindExposure(String windExposure) {
		this.windExposure = windExposure;
	}

	public String getViewFactorToGround() {
		return viewFactorToGround;
	}

	public void setViewFactorToGround(String viewFactorToGround) {
		this.viewFactorToGround = viewFactorToGround;
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

	public List<OsmSubSurface> getSubSurfaceList() {
		return subSurfaceList;
	}

	public void addSubSurface(OsmSubSurface subSurface) {
		this.subSurfaceList.add(subSurface);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String toString() {
		StringBuilder output = new StringBuilder();
		
		output.append("OS:Surface,\n  ");
		output.append("{" + uuid + "}" + ",!- Handle\n  ");
		output.append(surfaceName + ",     !- Name\n  ");
		output.append(typeName + ",        !- Surface Type\n  ");
		output.append(constructionName + ",       !- Construction Name\n  ");
		output.append(osmSpace.getSpaceName() + ",!- Space Name\n  ");
		output.append(outsideBoundaryCondition + ",                 !- Outside Boundary Condition\n  ");
		output.append(outsideBoundaryConditionObject + ",           !- Outside Boundary Condition Object\n  ");
		output.append(sunExposure + ",               !- Sun Exposure\n  ");
		output.append(windExposure + ",              !- Wind Exposure\n  ");
		output.append(viewFactorToGround + ",                         !- View Factor to Ground\n  ");
		output.append(numberOfVertices);
		
		int size = pointList.size();
		if(size <= 0) {
			output.append(";                                !- Number of Vertices\n");
		} else {
			output.append(",                                !- Number of Vertices\n");
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
				output.append(String.valueOf(i+1));
				output.append(" {m}\n  ");
			}
		}
		output.append("\n");
		
		return output.toString();
	}
}
