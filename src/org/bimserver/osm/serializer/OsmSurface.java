package org.bimserver.osm.serializer;

import java.util.ArrayList;
import java.util.Collections;
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
		
		correctDirectionOfFloorCeiling();
		
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
	
	/**
	 * Test Counter Clock-wize for floor and ceilings. Does not work with vertical plane.
	 * @param point
	 * @return
	 */
	private boolean isCCW(List<OsmPoint> point) {
		if (point.size()<3){
			System.err.print("Warning! isCCW only contains " + point.size() + " points!");
			return true;
		}
		
		//calculate the center
		double centroidX = 0.0;
		double centroidY = 0.0;
		int length = point.size();
		
		for(OsmPoint temp : point) {
			centroidX += temp.getX();
			centroidY += temp.getY();
		}
		centroidX = centroidX / length;
		centroidY = centroidY / length;
		
		double x1 = point.get(0).getX() - centroidX;
		double x2 = point.get(1).getX() - centroidX;
		double y1 = point.get(0).getY() - centroidY;
		double y2 = point.get(1).getY() - centroidY;

        return (x1*y2 - x2*y1) > 0;
    }

	/**
	 * correct the direction for floor and ceiling/roof
	 * @param surface
	 * @param isFloor : floor - true; ceiling - false
	 */
	private void correctDirectionOfFloorCeiling(){
		if (typeName.equals("Floor") && isCCW(pointList) || //floor needs to be clockwise
				typeName.equals("RoofCeiling") && !isCCW(pointList) )//ceiling needs to be counter-clockwise
		{
			Collections.reverse(pointList);
		} 
	}
	
	/*
	public static void main(String [] args){
		OsmSurface o = new OsmSurface();
		List<OsmPoint> list = new ArrayList<OsmPoint>();
		list.add(new OsmPoint(0.0, 0.0, 0.0));
		list.add(new OsmPoint(1.0, 0.0, 0.0));
		list.add(new OsmPoint(1.0, 1.0, 0.0));
		list.add(new OsmPoint(0.0, 1.0, 0.0));
		
		System.out.println(o.isCCW(list));
		
	}*/
	
}
