package org.bimserver.osm.serializer;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.models.ifc2x3tc1.IfcArbitraryOpenProfileDef;
import org.bimserver.models.ifc2x3tc1.IfcAxis2Placement;
import org.bimserver.models.ifc2x3tc1.IfcAxis2Placement3D;
import org.bimserver.models.ifc2x3tc1.IfcBoundedCurve;
import org.bimserver.models.ifc2x3tc1.IfcCartesianPoint;
import org.bimserver.models.ifc2x3tc1.IfcCompositeCurve;
import org.bimserver.models.ifc2x3tc1.IfcCompositeCurveSegment;
import org.bimserver.models.ifc2x3tc1.IfcConnectionGeometry;
import org.bimserver.models.ifc2x3tc1.IfcConnectionSurfaceGeometry;
import org.bimserver.models.ifc2x3tc1.IfcConversionBasedUnit;
import org.bimserver.models.ifc2x3tc1.IfcCurve;
import org.bimserver.models.ifc2x3tc1.IfcCurveBoundedPlane;
import org.bimserver.models.ifc2x3tc1.IfcDirection;
import org.bimserver.models.ifc2x3tc1.IfcDoor;
import org.bimserver.models.ifc2x3tc1.IfcElement;
import org.bimserver.models.ifc2x3tc1.IfcFaceBasedSurfaceModel;
import org.bimserver.models.ifc2x3tc1.IfcLocalPlacement;
import org.bimserver.models.ifc2x3tc1.IfcMeasureWithUnit;
import org.bimserver.models.ifc2x3tc1.IfcObjectPlacement;
import org.bimserver.models.ifc2x3tc1.IfcPolyline;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.ifc2x3tc1.IfcProfileDef;
import org.bimserver.models.ifc2x3tc1.IfcProperty;
import org.bimserver.models.ifc2x3tc1.IfcPropertySet;
import org.bimserver.models.ifc2x3tc1.IfcPropertySingleValue;
import org.bimserver.models.ifc2x3tc1.IfcRatioMeasure;
import org.bimserver.models.ifc2x3tc1.IfcRelDefines;
import org.bimserver.models.ifc2x3tc1.IfcRelDefinesByProperties;
import org.bimserver.models.ifc2x3tc1.IfcRelFillsElement;
import org.bimserver.models.ifc2x3tc1.IfcRelSpaceBoundary;
import org.bimserver.models.ifc2x3tc1.IfcRoof;
import org.bimserver.models.ifc2x3tc1.IfcSlab;
import org.bimserver.models.ifc2x3tc1.IfcSpace;
import org.bimserver.models.ifc2x3tc1.IfcSurfaceOfLinearExtrusion;
import org.bimserver.models.ifc2x3tc1.IfcSurfaceOrFaceSurface;
import org.bimserver.models.ifc2x3tc1.IfcTrimmedCurve;
import org.bimserver.models.ifc2x3tc1.IfcUnit;
import org.bimserver.models.ifc2x3tc1.IfcUnitAssignment;
import org.bimserver.models.ifc2x3tc1.IfcValue;
import org.bimserver.models.ifc2x3tc1.IfcVirtualElement;
import org.bimserver.models.ifc2x3tc1.IfcWall;
import org.bimserver.models.ifc2x3tc1.IfcWindow;
import org.bimserver.models.ifc2x3tc1.Tristate;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.renderengine.RenderEnginePlugin;
import org.bimserver.plugins.serializers.EmfSerializer;
import org.bimserver.plugins.serializers.ProjectInfo;
import org.bimserver.plugins.serializers.SerializerException;
import org.bimserver.utils.UTF8PrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsmSerializer extends EmfSerializer {
	private static final double maxWallThickness = 0.8;
	private static final Logger LOGGER = LoggerFactory.getLogger(OsmSerializer.class);
	
	private UTF8PrintWriter						out;
	/**
	 * Current analyzed instance, the value could be wall, floor, roof, window,
	 * door
	 */
	private IfcProduct							currentInstance;
	/**
	 * Wall and its OSM relation map, used by OSM surface-subsurface relation
	 * //TODO clear up this information. Use it with internalSurfaceMap.
	 */
	private Map<IfcWall, OSMSurface>			wallOSMSurfaceMap			= new HashMap<IfcWall, OSMSurface>();

	/**
	 */
	private Map<IfcWall, List<OSMSubSurface>>	unLinkedOSMSubSurfacesMap	= new HashMap<IfcWall, List<OSMSubSurface>>();
	// if sub-surface occurs before surface, add to unLinkedOSMSubSurfaces

	/**
	 * All points, incrementally added from IFC Points. Used to generate the
	 * space name in sequence
	 */
	private List<OSMPoint>						allOSMPoints				= new ArrayList<OSMPoint>();					// all

	/**
	 * All spaces, incrementally added from IFCSpace. Used to generate the space
	 * name in sequence
	 */
	private List<OSMSpace>						allSpaces					= new ArrayList<OSMSpace>();

	/**
	 * A 1 to 2 mapping from IfcWall to OSMSurface to store duplicated internal
	 * walls.
	 */
	private HashMap<IfcWall, LinkedList<OSMSurface>>	internalSurfaceMap			= new HashMap<IfcWall, LinkedList<OSMSurface>>();

	/**
	 */
	private int									surfaceNum					= 0;
	
	private int									subSurfaceNum				= 0;

	@Override
	public void init(IfcModelInterface model, ProjectInfo projectInfo, PluginManager pluginManager, RenderEnginePlugin renderEnginePlugin, boolean normalizeOids) throws SerializerException {
		super.init(model, projectInfo, pluginManager, renderEnginePlugin, normalizeOids);

		List<IfcSpace> ifcSpaceList = model.getAll(IfcSpace.class);
		for (IfcSpace ifcSpace : ifcSpaceList) {
			extractSpaces(ifcSpace);
		}

		// Calculate the unit conversion scale
		double scale = calUnitConvScale(model);

		// Convert the units
		transformUnits(scale);

		// Add linkage information to internal Walls
		addLinkageInformation();
	}

	@Override
	public void reset()	{
		setMode(Mode.BODY);
	}

	@Override
	protected boolean write(OutputStream outputStream) throws SerializerException {
		if (out == null) {
			out = new UTF8PrintWriter(outputStream);
		}
		
		if (getMode() == Mode.BODY) {
			out = new UTF8PrintWriter(outputStream);
			generateOutput(out);
			out.flush();
			setMode(Mode.FINISHED);
			return true;
		} else if (getMode() == Mode.FINISHED) {
			return false;
		}
		return false;
	}

	/**
	 * write the OSMSpace, OSMSurface, OSMSubSurface to outputStream
	 * each type of element is represented as a JsonArray of JsonObjects
	 * @param out
	 */
	private void generateOutput(UTF8PrintWriter outputContent) {
		outputContent.append("OS:Version,\n  ");
		UUID uuid = UUID.randomUUID();
		outputContent.append("{" +uuid.toString()+ "}");
		outputContent.append(",                              !- Handle\n  ");
		outputContent.append("1.3.0;                         !- Version Identifier\n\n");
		
		for(OSMSpace osmSpace: allSpaces) {
			outputContent.append("OS:Space,\n  ");
			outputContent.append("{" +osmSpace.getUuid().toString()+ "}");
			outputContent.append(",                              !- Handle\n  ");
			outputContent.append(osmSpace.getSpaceName());
			outputContent.append(",               ! Name\n  ");
			outputContent.append(osmSpace.getTypeName());
			outputContent.append(",                         !- Space Type Name\n  ");
			outputContent.append(osmSpace.getDefaultConstructionSetName());
			outputContent.append(",                         !- Default Construction Set Name\n  ");
			outputContent.append(osmSpace.getDefaultScheduleSetName());
			outputContent.append(",                         !- Default Schedule Set Name\n  ");
			outputContent.append(osmSpace.getDirectionOfRelativeNorth());
			outputContent.append(",                         !- Direction of Relative North {deg}\n  ");
			outputContent.append(osmSpace.getxOrigin());
			outputContent.append(",                         !- X Origin {m}\n  ");
			outputContent.append(osmSpace.getyOrigin());
			outputContent.append(",                         !- Y Origin {m}\n  ");
			outputContent.append(osmSpace.getzOrigin());
			outputContent.append(",                         !- Z Origin {m}\n  ");
			outputContent.append(osmSpace.getBuildingStoryName());
			outputContent.append(",                         !- Building Story Name\n  ");
			outputContent.append(osmSpace.getSpaceName());
			outputContent.append(" ThermalZone;  ! Thermal Zone Name\n\n");
		}
		
		for(OSMSpace osmSpace: allSpaces) {
			for(OSMSurface osmSurface: osmSpace.getSurfaceList()) {
				outputContent.append("OS:Surface,\n  ");
				outputContent.append("{" +osmSurface.getUuid().toString()+ "}");
				outputContent.append(",                              !- Handle\n  ");
				outputContent.append(osmSurface.getSurfaceName());
				outputContent.append(",                     !- Name\n  ");
				outputContent.append(osmSurface.getTypeName());
				outputContent.append(",                     !- Surface Type\n  ");
				outputContent.append(osmSurface.getConstructionName());
				outputContent.append(",                         !- Construction Name\n  ");
				outputContent.append(osmSurface.getOSMSpace().getSpaceName());
				outputContent.append(",             !- Space Name\n  ");
				outputContent.append(osmSurface.getOutsideBoundaryCondition());
				outputContent.append(",                 !- Outside Boundary Condition\n  ");
				outputContent.append(osmSurface.getOutsideBoundaryConditionObject());
				outputContent.append(",                         !- Outside Boundary Condition Object\n  ");
				outputContent.append(osmSurface.getSunExposure());
				outputContent.append(",               !- Sun Exposure\n  ");
				outputContent.append(osmSurface.getWindExposure());
				outputContent.append(",              !- Wind Exposure\n  ");
				outputContent.append(osmSurface.getViewFactorToGround());
				outputContent.append(",                         !- View Factor to Ground\n  ");
				outputContent.append(osmSurface.getNumberOfVertices());
				
				int size = osmSurface.getOSMPointList().size();
				if(size <= 0) {
					outputContent.append(";                         !- Number of Vertices\n  ");
				} else {
					outputContent.append(",                         !- Number of Vertices\n  ");
					for(int i = 0; i < size; i ++) {
						OSMPoint osmPoint = osmSurface.getOSMPointList().get(i);
						outputContent.append(String.valueOf(osmPoint.getX()));
						outputContent.append(",");
						outputContent.append(String.valueOf(osmPoint.getY()));
						outputContent.append(",");
						outputContent.append(String.valueOf(osmPoint.getZ()));
						if(i < size - 1) {
							outputContent.append(",  !- X,Y,Z Vertex ");
						} else {
							outputContent.append(";  !- X,Y,Z Vertex ");
						}
						outputContent.append(String.valueOf(i+1));
						outputContent.append(" {m}\n  ");
					}
				}
				outputContent.append("\n");
				
				for(OSMSubSurface osmSubSurface: osmSurface.getSubSurfaceList()){
					outputContent.append("OS:SubSurface,\n  ");
					outputContent.append("{" +osmSubSurface.getUuid().toString()+ "}");
					outputContent.append(",                              !- Handle\n  ");
					outputContent.append(osmSubSurface.getSubSurfaceName());
					outputContent.append(",                     !- Name\n  ");
					outputContent.append(osmSubSurface.getTypeName());
					outputContent.append(",                     !- Surface Type\n  ");
					outputContent.append(osmSubSurface.getConstructionName());
					outputContent.append(",                         !- Construction Name\n  ");
					outputContent.append(osmSubSurface.getOSMSurface().getSurfaceName());
					outputContent.append(",                  !- Surface Name\n  ");
					outputContent.append(osmSubSurface.getOutsideBoundaryConditionObject());
					outputContent.append(",                         !- Outside Boundary Condition Object\n  ");
					outputContent.append(osmSubSurface.getViewFactorToGround());
					outputContent.append(",                         !- View Factor to Ground\n  ");
					outputContent.append(osmSubSurface.getShadingControlName());
					outputContent.append(",                         !- Shading Control Name\n  ");
					outputContent.append(osmSubSurface.getFrameAndDividerName());
					outputContent.append(",                         !- Frame and Divider Name\n  ");
					outputContent.append(osmSubSurface.getMultiplier());
					outputContent.append(",                         !- Multiplier\n  ");
					outputContent.append(osmSubSurface.getNumberOfVertices());
					
					size = osmSubSurface.getOSMPointList().size();
					if(size <= 0){
						outputContent.append(";                         !- Number of Vertices\n  ");
					}
					else{
						outputContent.append(",                         !- Number of Vertices\n  ");
						for(int i = 0; i < size; i ++){
							OSMPoint osmPoint = osmSubSurface.getOSMPointList().get(i);
							outputContent.append(String.valueOf(osmPoint.getX()));
							outputContent.append(",");
							outputContent.append(String.valueOf(osmPoint.getY()));
							outputContent.append(",");
							outputContent.append(String.valueOf(osmPoint.getZ()));
							if(i < size - 1){
								outputContent.append(",  !- X,Y,Z Vertex ");
							}
							else{
								outputContent.append(";  !- X,Y,Z Vertex ");
							}
							outputContent.append(String.valueOf(i + 1));
							outputContent.append(" {m}\n  ");
						}
					}
					outputContent.append("\n");
				}
			}
		}
	}

	/**
	 * For internal wall, we link the duplicated walls to each other by
	 * outsideboundaryConditionObject
	 */
	private void addLinkageInformation()
	{
		for (LinkedList<OSMSurface> surfaceList : internalSurfaceMap.values())
		{
			while (!surfaceList.isEmpty())//while not empty
			{
				OSMSurface firstSurface = surfaceList.removeFirst();

				int closestSurfaceIndex = -1;
				double leastDistance = maxWallThickness;
				OSMPoint firstCenter = computeSurfaceCenter(firstSurface);
				
				//loop through the rest of elements
				for (int i = 0; i<surfaceList.size(); i++)
				{
					OSMPoint secondCenter = computeSurfaceCenter(surfaceList.get(i));
					double distance = distanceOfPoints(firstCenter,secondCenter);
					if(distance <= leastDistance)
					{
						closestSurfaceIndex = i;
						leastDistance = distance;
					}
				}
				if(closestSurfaceIndex>=0)//found a match
				{
					OSMSurface secondSurface = surfaceList.remove(closestSurfaceIndex);
					firstSurface.setOutsideBoundaryCondition("Surface");
					firstSurface.setOutsideBoundaryConditionObject(secondSurface.getSurfaceName());
					firstSurface.setSunExposure("NoSun");
					firstSurface.setWindExposure("NoWind");
					secondSurface.setOutsideBoundaryCondition("Surface");
					secondSurface.setOutsideBoundaryConditionObject(firstSurface.getSurfaceName());
					secondSurface.setSunExposure("NoSun");
					secondSurface.setWindExposure("NoWind");
				}
				else//does not contain any element or does not have a match smaller than the threshold
				{
					firstSurface.setOutsideBoundaryCondition("Outdoors");
					firstSurface.setSunExposure("SunExposed");
					firstSurface.setWindExposure("WindExposed");
				}
			}
		}
	}
	
	/**
	 * Calculate the center of a rectangular Surface. 
	 * Assuming all OSMSurfaces are Rectangular and contains four points
	 * @param osmSurface
	 * @return
	 */
	private OSMPoint computeSurfaceCenter(OSMSurface osmSurface)
	{
		List<OSMPoint> list = osmSurface.getOSMPointList();
		if (list.size()!=4)
			System.err.print("Warning! The Surface " + osmSurface.getSurfaceName() 
					+ " contains " + list.size() + " points!");
		double xSum = 0, ySum = 0, zSum = 0;
		for (OSMPoint p : list)
		{
			xSum += p.getX();
			ySum += p.getY();
			zSum += p.getZ();
		}
		return new OSMPoint(xSum/list.size(),ySum/list.size(),zSum/list.size());		
	}
	
	/**
	 * compute the euclidean distance of two OSMPoints
	 * @param point1
	 * @param point2
	 * @return
	 */
	private double distanceOfPoints(OSMPoint point1, OSMPoint point2)
	{
		double x2 = (point1.getX()-point2.getX())*(point1.getX()-point2.getX());
		double y2 = (point1.getY()-point2.getY())*(point1.getY()-point2.getY());
		double z2 = (point1.getZ()-point2.getZ())*(point1.getZ()-point2.getZ());
		return Math.sqrt(x2+y2+z2);
	}

	/**
	 * Analyze every IFCSpace and create OSMSpace accordingly
	 * 
	 * @param ifcSpace
	 */
	private void extractSpaces(IfcSpace ifcSpace) {
		// Create the temporary OSMSpace object
		OSMSpace osmSpace = new OSMSpace();
		UUID uuid = UUID.randomUUID();
		osmSpace.setUuid(uuid.toString());
		// Assign a name to the OSMSpace
		osmSpace.setSpaceName("sp-" + (allSpaces.size() + 1) + "-Space");

		//TODO: Set the Building Storey this space is located at.
		
		/**
		 * Obsolete code osmSpace.setSpaceName("sp-" + (allSpaces.size() + 1) +
		 * "-Space-" + ifcSpace.getName()); to be replaced by the line above!!!!
		 */

		// Get all related space boundary objects (including surfaces,
		// subsurfaces)
		List<IfcRelSpaceBoundary> ifcRelSpaceBoundaryList = ifcSpace.getBoundedBy();

		// TODO what does this means? Can we delete this global variable and put it locally?
		wallOSMSurfaceMap.clear();

		// for every space bounary such as wall, ceiling or floor
		for (IfcRelSpaceBoundary ifcRelSpaceBoundary : ifcRelSpaceBoundaryList) {

			// get the related ifc element of the space boundary to see if it's
			// a wall or floor, etc.
			IfcElement ifcElement = ifcRelSpaceBoundary.getRelatedBuildingElement();

			// get the connection geometry points of the space boundary
			IfcConnectionGeometry ifcConnectionGeometry = ifcRelSpaceBoundary.getConnectionGeometry();

			if (ifcConnectionGeometry != null && ifcElement != null) {

				// extract the space boundary information such as wall, ceiling and floor.
				extractRelatedElement(ifcElement, ifcConnectionGeometry, ifcSpace, osmSpace);
			} else {
				LOGGER.info("Unimplemented type [extractSpaces]:" + ifcRelSpaceBoundary.eClass().getName());
			}
		}

		if (!osmSpace.containsUnsolvedSurface()) {
			allSpaces.add(osmSpace);
		} else {
			surfaceNum -= osmSpace.getSurfaceList().size();
		}
	}

	private double calUnitConvScale(IfcModelInterface model) {
		double scale = 1.0;
		List<IfcUnitAssignment> ifcUnitAssignmentList =  model
				.getAll(IfcUnitAssignment.class);
		if(!ifcUnitAssignmentList.isEmpty()) {
			IfcUnitAssignment ifcUnitAssignment = ifcUnitAssignmentList.get(0);
			List<IfcUnit> ifcUnitList = ifcUnitAssignment.getUnits();
			for (IfcUnit ifcUnit : ifcUnitList)
			{
				if (ifcUnit instanceof IfcConversionBasedUnit)
				{
					IfcConversionBasedUnit ifcConversionBasedUnit = (IfcConversionBasedUnit) ifcUnit;
					if (ifcConversionBasedUnit.getUnitType().getName() == "LENGTHUNIT")
					{
						IfcMeasureWithUnit ifcMeasureWithUnit = ifcConversionBasedUnit
								.getConversionFactor();
						IfcValue ifcValue = ifcMeasureWithUnit.getValueComponent();
						if (ifcValue instanceof IfcRatioMeasure)
						{
							scale = ((IfcRatioMeasure) ifcValue).getWrappedValue();
							break;
						}
					}
	
				}
			}
		}
		return scale;
	}

	/**
	 * Extract the connection geometry //Can only handle 2 situations.
	 * 
	 * @param ifcConnectionGeometry
	 *            the connection geometry of the space
	 * @param spaceBoundaryPointList
	 *            an empty point list of the connection geometry
	 * @return if the connection is a surface, return true.
	 */
	private boolean extractSpaceBoundary(IfcConnectionGeometry ifcConnectionGeometry, List<OSMPoint> spaceBoundaryPointList, IfcSpace ifcSpace) {
		/*
		 * The boundary geometry should always be a surface.
		 * Note that only three types of surface can be used:
		 * IfcCurveBoundedPlane, IfcSurfaceOfLinearExtrusion and IfcFaceBasedSurfaceModel
		 * @author:Pengwei Lan 	 
		 */
		if (ifcConnectionGeometry instanceof IfcConnectionSurfaceGeometry) {
			//provides the boundary geometry relative to the Space coordinate system.
			IfcSurfaceOrFaceSurface ifcSurfaceOrFaceSurface = ((IfcConnectionSurfaceGeometry) ifcConnectionGeometry).getSurfaceOnRelatingElement();
			
			if (ifcSurfaceOrFaceSurface instanceof IfcSurfaceOfLinearExtrusion) {
				IfcSurfaceOfLinearExtrusion ifcSurfaceOfLinearExtrusion = (IfcSurfaceOfLinearExtrusion) ifcSurfaceOrFaceSurface;
				return extractSurfaceOfLinearExtrusionSB(ifcSurfaceOfLinearExtrusion, spaceBoundaryPointList, ifcSpace);
			} else if (ifcSurfaceOrFaceSurface instanceof IfcCurveBoundedPlane) {
				IfcCurveBoundedPlane ifcCurveBoundedPlane = (IfcCurveBoundedPlane) ifcSurfaceOrFaceSurface;
				return extractCurveBoundedPlaneSB(ifcCurveBoundedPlane, spaceBoundaryPointList, ifcSpace);
			} else if (ifcSurfaceOrFaceSurface instanceof IfcFaceBasedSurfaceModel) {
				//TODO: implement IfcFaceBasedSurfaceModel
				return false;
			} else {
				LOGGER.info("Wrong type [extractSpaceBoundary]:" + ifcConnectionGeometry.eClass().getName());
				return false;
			}
		} else {
			LOGGER.info("Unimplemented type [extractSpaceBoundary]" + ifcConnectionGeometry.eClass().getName());
			return false;
		}		
	}

	/**
	 * Extract curved space boundary
	 * @param ifcCurveBoundedPlane
	 * @param spaceBoundaryPointList
	 * @return
	 */
	private boolean extractCurveBoundedPlaneSB(IfcCurveBoundedPlane ifcCurveBoundedPlane, List<OSMPoint> spaceBoundaryPointList, IfcSpace ifcSpace) {
		IfcAxis2Placement3D position = ifcCurveBoundedPlane.getBasisSurface().getPosition();
		IfcCurve ifcCurve = ifcCurveBoundedPlane.getOuterBoundary();
		
		if (ifcCurve instanceof IfcPolyline) {
			List<OSMPoint> osmPointList = new ArrayList<OSMPoint>();
			getPointsOfIfcPolyline((IfcPolyline) ifcCurve, osmPointList);
			
			for (OSMPoint OSMPoint : osmPointList) {
				coordinate3DTrans(OSMPoint, position);
				coordinateSys3DTrans(OSMPoint, ifcSpace); 
				spaceBoundaryPointList.add(OSMPoint);
			}
			return true;
		} else if (ifcCurve instanceof IfcCompositeCurve) { 
			IfcCompositeCurve ifcCompositeCurve = (IfcCompositeCurve) ifcCurve;
			List<IfcCompositeCurveSegment> ifcCompositeCurveSegments = ifcCompositeCurve.getSegments();
			for(IfcCompositeCurveSegment ifcCompositeCurveSegment: ifcCompositeCurveSegments) {
				IfcCurve parentCurve = ifcCompositeCurveSegment.getParentCurve();
				
				if (parentCurve instanceof IfcPolyline) {
					List<OSMPoint> osmPointList = new ArrayList<OSMPoint>();
					getPointsOfIfcPolyline((IfcPolyline) parentCurve, osmPointList);
					for (OSMPoint OSMPoint : osmPointList) {
						coordinate3DTrans(OSMPoint, position);
						coordinateSys3DTrans(OSMPoint, ifcSpace); 
						spaceBoundaryPointList.add(OSMPoint);
					}
				} else if(parentCurve instanceof IfcTrimmedCurve) {
					//TODO: extract geometry from IfcTrimmedCurve
				} else {
					LOGGER.info("Unimplemented type [extractCurveBoundedPlaneSB]" + parentCurve.eClass().getName());
				}
			}
			
			return true;
		} else {
			LOGGER.info("Unimplemented type [extractCurveBoundedPlaneSB]" + ifcCurve.eClass().getName());
			return false;
		}
	}
	
	
	private void getPointsOfIfcPolyline(IfcPolyline ifcPolyline, List<OSMPoint> osmPointList) {
		List<IfcCartesianPoint> ifcCartesianPointList = ifcPolyline.getPoints();
		for (IfcCartesianPoint ifcCartesianPoint : ifcCartesianPointList) {
			List<Double> point = ifcCartesianPoint.getCoordinates();
			
			if(ifcCartesianPoint.isSetDim()) {
				int dimension = ifcCartesianPoint.getDim();
				if (dimension == 2) {
					osmPointList.add(new OSMPoint(point.get(0), point.get(1)));
				} else if(dimension == 3) {
					osmPointList.add(new OSMPoint(point.get(0), point.get(1), point.get(2)));
				} else {
					LOGGER.info("The dimension of this point is incorrect: point dimension = " + dimension);
				}
			} else {
				osmPointList.add(new OSMPoint(point.get(0), point.get(1)));
			}
		}
	}
	

	/**
	 * Extract straight space boundary
	 * @param ifcSurfaceOfLinearExtrusion
	 * @param spaceBoundaryPointList
	 * @return
	 */
	private boolean extractSurfaceOfLinearExtrusionSB(IfcSurfaceOfLinearExtrusion ifcSurfaceOfLinearExtrusion, List<OSMPoint> spaceBoundaryPointList, IfcSpace ifcSpace) {
		IfcProfileDef ifcProfileDef = ifcSurfaceOfLinearExtrusion.getSweptCurve();
		
		
		if(ifcProfileDef instanceof IfcArbitraryOpenProfileDef) {
			IfcArbitraryOpenProfileDef ifcArbitraryOpenProfileDef = (IfcArbitraryOpenProfileDef) ifcProfileDef;
			IfcBoundedCurve ifcBoundedCurve = ifcArbitraryOpenProfileDef.getCurve();
			
			if(ifcBoundedCurve instanceof IfcPolyline) {
				IfcPolyline ifcPolyline = (IfcPolyline) ifcBoundedCurve;
				List<OSMPoint> osmPointList = new ArrayList<OSMPoint>();
				for (IfcCartesianPoint ifcCartesianPoint : ifcPolyline.getPoints()) {
					List<Double> point = ifcCartesianPoint.getCoordinates();
					/*
					 * http://www.buildingsmart-tech.org/ifc/IFC2x4/rc2/html/schema/ifcgeometryresource/lexical/ifccartesianpoint.htm
					 * @author:Pengwei Lan
					 * */
					if(ifcCartesianPoint.isSetDim()) {
						int dimension = ifcCartesianPoint.getDim();
						if (dimension == 2) {
							osmPointList.add(new OSMPoint(point.get(0), point.get(1)));
						} else if(dimension == 3) {
							osmPointList.add(new OSMPoint(point.get(0), point.get(1), point.get(2)));
						} else {
							LOGGER.info("The dimension of this point is incorrect: point dimension = " + dimension);
							return false;
						}
					} else {
						osmPointList.add(new OSMPoint(point.get(0), point.get(1)));
					}	
				}
				
				IfcAxis2Placement3D position = ifcSurfaceOfLinearExtrusion.getPosition();
				
				if(osmPointList.isEmpty()) {
					LOGGER.info("Can't get the points of the current line!");
					return false;
				}
				
				// Translate the points on the swept surface
				for (OSMPoint osmPoint : osmPointList) {
					// Put the curved boundary point in the basis plane coordinate system.
					coordinate3DTrans(osmPoint, position); 
					// Upgrade to global coordinates according to the space's local coordinate system
					coordinateSys3DTrans(osmPoint, ifcSpace); 
					spaceBoundaryPointList.add(osmPoint);
				}
	
				// OSM requirement
				Collections.reverse(osmPointList); 
				
				double extrudedDepth = ifcSurfaceOfLinearExtrusion.getDepth();
				IfcDirection ifcDirection = ifcSurfaceOfLinearExtrusion.getExtrudedDirection();
						
				//Direction
				double x = ifcDirection.getDirectionRatios().get(0);
				double y = ifcDirection.getDirectionRatios().get(1);
				double z = ifcDirection.getDirectionRatios().get(2);
				
				double length = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
				
				//move the point along the direction for depth.
				double mx = extrudedDepth * x / length;
				double my = extrudedDepth * y / length;
				double mz = extrudedDepth * z / length;
				
				for (OSMPoint osmPoint : osmPointList) {
					OSMPoint newPoint = new OSMPoint(osmPoint.getX() + mx, osmPoint.getY() + my, osmPoint.getZ() + mz);
					spaceBoundaryPointList.add(newPoint);
				}
				return true;
			} else {
				LOGGER.info("Unimplemented type [extractSurfaceOfLinearExtrusionSB]" + ifcBoundedCurve.eClass().getName());
				return false;
			}
		} else {
			LOGGER.info("Unimplemented type [extractSurfaceOfLinearExtrusionSB]" + ifcProfileDef.eClass().getName());
			return false;
		}
	}

	/**
	 * Extract all properties of the building elements
	 * 
	 * @param ifcElement
	 *            the space boundary element. It could be wall, ceiling, floor,
	 *            window, door, etc.
	 * @param ifcConnectionGeometry
	 *            the connecting geometry points of the connection points of the
	 *            space boundary
	 * @param osmSpace
	 *            current OSMSpace to be analyzed
	 */
	private void extractRelatedElement(IfcElement ifcElement, IfcConnectionGeometry ifcConnectionGeometry, IfcSpace ifcSpace, OSMSpace osmSpace)
	{

		// initiate the connecting point list
		List<OSMPoint> spaceBoundaryPointList = new ArrayList<OSMPoint>();

		// Extract the connection geometry
		//TODO: Bug: sometimes windows and doors are not contained in the original surfaces.
		//TODO: put extract space boundary inside the IFC Element List as there might be some differences in extracting windows and doors
		boolean isGeometrySolved = extractSpaceBoundary(ifcConnectionGeometry, spaceBoundaryPointList, ifcSpace);

		// Extract Other Properties

		// Wall, surface
		if (ifcElement instanceof IfcWall) {
			if (isGeometrySolved) {
				IfcWall ifcWall = (IfcWall) ifcElement;
				OSMSurface osmSurface = new OSMSurface();
				UUID uuid = UUID.randomUUID();
				osmSurface.setUuid(uuid.toString());
				String osmSurfaceName = "su-" + (++surfaceNum);
				osmSurface.setSurfaceName(osmSurfaceName);
				osmSurface.setTypeName("Wall");
				osmSurface.setOSMSpace(osmSpace);
				
				//add internal surface link information
				if (internalSurfaceMap.containsKey(ifcWall)) {
					internalSurfaceMap.get(ifcWall).add(osmSurface);
				} else {
					LinkedList<OSMSurface> surfaceList = new LinkedList<OSMSurface>();
					surfaceList.add(osmSurface);
					internalSurfaceMap.put(ifcWall,surfaceList);
				}

				for (OSMPoint osmPoint : spaceBoundaryPointList) {
					osmSurface.addOSMPoint(osmPoint);
					allOSMPoints.add(osmPoint);
				}
				osmSpace.addSurface(osmSurface);

				wallOSMSurfaceMap.put(ifcWall, osmSurface);
				List<OSMSubSurface> unlinkedOSMSubSurfaceList = unLinkedOSMSubSurfacesMap
						.get(ifcWall);
				if (unlinkedOSMSubSurfaceList != null && unlinkedOSMSubSurfaceList.size() > 0) {
					for (OSMSubSurface osmSubSurface : unlinkedOSMSubSurfaceList) {
						osmSubSurface.setOSMSurface(osmSurface);
						osmSurface.addSubSurface(osmSubSurface);
					}
					unLinkedOSMSubSurfacesMap.remove(ifcWall);
				}
			} else {
				osmSpace.setContainsUnsolvedSurface(true);
				LOGGER.info("Unparsed geometry representation of wall!" + ifcElement.eClass().getName());
			}
		}

		// Slab, surface
		else if (ifcElement instanceof IfcSlab) {
			if (isGeometrySolved) {
				IfcSlab ifcSlab = (IfcSlab) ifcElement;
				OSMSurface osmSurface = new OSMSurface();
				UUID uuid = UUID.randomUUID();
				osmSurface.setUuid(uuid.toString());
				osmSurface.setSurfaceName("su-" + (++surfaceNum));
				osmSurface.setOSMSpace(osmSpace);
				osmSurface.setOutsideBoundaryCondition("Ground");
				osmSurface.setSunExposure("NoSun");
				osmSurface.setWindExposure("NoWind");

				if (ifcSlab.getPredefinedType().getName().equals("FLOOR")) {
					osmSurface.setTypeName("Floor");
					if(isCCW(spaceBoundaryPointList)) {
						//TODO: There is probably an error here. There are usually a upside down error.
						Collections.reverse(spaceBoundaryPointList);	
					}
				} else if (ifcSlab.getPredefinedType().getName().equals("ROOF")) {
					osmSurface.setTypeName("RoofCeiling");
				} else {
					LOGGER.info("Unparsed slab type for " + ifcElement.eClass().getName());
				}

				for (OSMPoint osmPoint : spaceBoundaryPointList) {
					osmSurface.addOSMPoint(osmPoint);
					allOSMPoints.add(osmPoint);
				}
				osmSpace.addSurface(osmSurface);
			} else {
				osmSpace.setContainsUnsolvedSurface(true);
				LOGGER.info("Unparsed slab geomatry" + ifcElement.eClass().getName());
			}
		}
		
		
		// Roof, Surface
		
		else if (ifcElement instanceof IfcRoof) {
			if (isGeometrySolved) {
				IfcRoof ifcRoof = (IfcRoof) ifcElement;
				OSMSurface osmSurface = new OSMSurface();
				osmSurface.setSurfaceName("su-" + (++surfaceNum));
				// osmSurface.setSurfaceName("su-" + (++surfaceNum) + "-" +
				// ifcElement.getName()); //this line should be replaced by the
				// line above!!!!
				osmSurface.setTypeName("RoofCeiling");
				osmSurface.setOSMSpace(osmSpace);

				for (int j = 0; j < ifcRoof.getIsDefinedBy().size(); j++) {
					IfcRelDefines ifcRelDefines = ifcRoof.getIsDefinedBy().get(j);
					if(ifcRelDefines instanceof IfcRelDefinesByProperties) {
						IfcRelDefinesByProperties ifcRelDefinesByProperties = (IfcRelDefinesByProperties) ifcRelDefines;
						if (ifcRelDefinesByProperties.getRelatingPropertyDefinition().getName().equals("Pset_RoofCommon")) {
							List<IfcProperty> ifcPropertySetList = ((IfcPropertySet) ifcRelDefinesByProperties.getRelatingPropertyDefinition()).getHasProperties();
							for (int k = 0; k < ifcPropertySetList.size(); k++) {
								IfcPropertySingleValue ifcPropertySingleValue = (IfcPropertySingleValue) ifcPropertySetList.get(k);
								if (ifcPropertySingleValue.getName().equals("IsExternal")) {
//									Tristate isExternal = ((IfcBoolean) ifcPropertySingleValue.getNominalValue()).getWrappedValue();
//									if (isExternal.getValue() == 0)
//									{ // true
										osmSurface.setOutsideBoundaryCondition("Outdoors");
										osmSurface.setSunExposure("SunExposed");
										osmSurface.setWindExposure("WindExposed");
//									} else if (isExternal.getValue() == 1)
//									{ // false
//										osmSurface
//												.setOutsideBoundaryCondition("Surface");
//										osmSurface.setSunExposure("NoSun");
//										osmSurface.setWindExposure("NoWind");
//									} else
//									{
//										System.err
//												.println("Error: invalid value of isExternal!!");
//									}
									break;
								}
							}
							break;
						}
					}
				}

				for (OSMPoint osmPoint : spaceBoundaryPointList) {
					osmSurface.addOSMPoint(osmPoint);
					allOSMPoints.add(osmPoint);
				}
				osmSpace.addSurface(osmSurface);
			} else { // else for if(isGeometrySolved)
				osmSpace.setContainsUnsolvedSurface(true);
				System.err.println("Error: unparsed geometry representation of roof!");
			}
		}
		
		// Window, subsurface
		else if (ifcElement instanceof IfcWindow) {
			if (isGeometrySolved) {
				IfcWindow ifcWindow = (IfcWindow) ifcElement;
				OSMSubSurface osmSubSurface = new OSMSubSurface();
				UUID uuid = UUID.randomUUID();
                osmSubSurface.setUuid(uuid.toString());
				osmSubSurface.setSubSurfaceName("sub-" + (++subSurfaceNum));
				osmSubSurface.setTypeName("FixedWindow");
				List<IfcRelFillsElement> ifcRelFillsElementList = ifcWindow.getFillsVoids();
				if (ifcRelFillsElementList.size() > 0) {
					IfcRelFillsElement ifcRelFillsElement = ifcRelFillsElementList.get(0);
					IfcElement ifcRelatingBuildingElement = ifcRelFillsElement.getRelatingOpeningElement().getVoidsElements().getRelatingBuildingElement();
					for (OSMPoint osmPoint : spaceBoundaryPointList) {
						osmSubSurface.addOSMPoint(osmPoint);
						allOSMPoints.add(osmPoint);
					}
					OSMSurface osmSurface = wallOSMSurfaceMap.get(ifcRelatingBuildingElement); // add (IfcWall)
																// cast before
																// ifcRelatingBuildingElement
					if (osmSurface != null) {
						osmSubSurface.setOSMSurface(osmSurface);
						osmSurface.addSubSurface(osmSubSurface);
					} else {
						List<OSMSubSurface> unlinkedOSMSubSurfaceList = unLinkedOSMSubSurfacesMap
								.get(ifcRelatingBuildingElement);
						if (unlinkedOSMSubSurfaceList == null) {
							unlinkedOSMSubSurfaceList = new ArrayList<OSMSubSurface>();
						}
						unlinkedOSMSubSurfaceList.add(osmSubSurface);
						unLinkedOSMSubSurfacesMap.put((IfcWall) ifcRelatingBuildingElement, unlinkedOSMSubSurfaceList);
					}
				}
			} else { // else for if(isGeometrySolved)
				LOGGER.info("Unparsed window for " + ifcElement.eClass().getName());
			}
		}

		// Door subsurface
		else if (ifcElement instanceof IfcDoor) {
			if (isGeometrySolved) {
				IfcDoor ifcDoor = (IfcDoor) ifcElement;
				OSMSubSurface osmSubSurface = new OSMSubSurface();
				UUID uuid = UUID.randomUUID();
                osmSubSurface.setUuid(uuid.toString());
                osmSubSurface.setSubSurfaceName("sub-" + (++subSurfaceNum));
				osmSubSurface.setTypeName("Door");
				List<IfcRelFillsElement> ifcRelFillsElementList = ifcDoor.getFillsVoids();
				if (ifcRelFillsElementList.size() > 0) {
					IfcRelFillsElement ifcRelFillsElement = ifcRelFillsElementList.get(0);
					IfcElement ifcRelatingBuildingElement = ifcRelFillsElement.getRelatingOpeningElement().getVoidsElements().getRelatingBuildingElement();
					for (OSMPoint osmPoint : spaceBoundaryPointList) {
						osmSubSurface.addOSMPoint(osmPoint);
						allOSMPoints.add(osmPoint);
					}
					OSMSurface osmSurface = wallOSMSurfaceMap.get(ifcRelatingBuildingElement); // add (IfcWall)
																// cast before
																// ifcRelatingBuildingElement
					if (osmSurface != null) {
						osmSubSurface.setOSMSurface(osmSurface);
						osmSurface.addSubSurface(osmSubSurface);
					} else {
						List<OSMSubSurface> unlinkedOSMSubSurfaceList = unLinkedOSMSubSurfacesMap.get(ifcRelatingBuildingElement);
						if (unlinkedOSMSubSurfaceList == null) {
							unlinkedOSMSubSurfaceList = new ArrayList<OSMSubSurface>();
						}
						unlinkedOSMSubSurfaceList.add(osmSubSurface);
						unLinkedOSMSubSurfacesMap.put((IfcWall) ifcRelatingBuildingElement, unlinkedOSMSubSurfaceList);
					}
				}
			} else { // else for if(isGeometrySolved)
				LOGGER.info("Unparsed door for " + ifcElement.eClass().getName());
			}
		} 
		
		//Virtual element
		else if(ifcElement instanceof IfcVirtualElement) {
			//TODO: Add IfcVirtualElement
		} 
		
		else { // else for ifcElement instanceof IfcWall, IfcSlab, IfcRoof,
			// IfcWindow, IfcDoor
			LOGGER.info("Unparsed element" + ifcElement.eClass().getName());
		}
	}
	
	/**
	 * Counter Clock-wize
	 * @param point
	 * @return
	 */
	
	public boolean isCCW(List<OSMPoint> point)
    {
		double centroidX = 0.0;
		double centroidY = 0.0;
		int length = point.size();
		
		for(OSMPoint temp : point) {
			centroidX += temp.getX();
			centroidY += temp.getY();
		}
		
		centroidX = centroidX / length;
		centroidY = centroidY / length;
		
		double x1 = Math.atan2(point.get(0).getY() - centroidY, point.get(0).getX() - centroidX);
		double x2 = Math.atan2(point.get(1).getY() - centroidY, point.get(1).getX() - centroidX);	
		
		
        return (x2 - x1 > 0);
    }
	
	

	private void transformUnits(double scale)
	{
		for (OSMPoint osmPoint : allOSMPoints)
		{
			osmPoint.setX(osmPoint.x * scale);
			osmPoint.setY(osmPoint.y * scale);
			osmPoint.setZ(osmPoint.z * scale);
		}
	}
	

	/**
	 * Given relOSMPoint, upgrade it's local coordinate system into the global coordinates.
	 * relPoint: The point to be transformed
	 * */
	private void coordinateSys3DTrans(OSMPoint relOSMPoint, IfcSpace ifcSpace)
	{		
		IfcObjectPlacement ifcObjectPlacement = ifcSpace.getObjectPlacement();
		
		boolean upgradeCompleted = false;
		
		while(!upgradeCompleted)
		{
			if(ifcObjectPlacement instanceof IfcLocalPlacement)
			{
				IfcLocalPlacement ifcLocalPlacement = (IfcLocalPlacement)ifcObjectPlacement;
				IfcAxis2Placement ifcAxis2Placement = ifcLocalPlacement.getRelativePlacement();
				
				if(ifcAxis2Placement instanceof IfcAxis2Placement3D)
				{
					//put the current point into the relative coordinate system.
					coordinate3DTrans(relOSMPoint, (IfcAxis2Placement3D)ifcAxis2Placement);
				}
				else{
					LOGGER.info("Unimplemented type " + ifcAxis2Placement.eClass().getName());
					break;
				}

				//check whether still has a upper layer of transformation
				if (!ifcLocalPlacement.isSetPlacementRelTo())
				{
					upgradeCompleted = true;
				}
				else{
					ifcObjectPlacement =  ifcLocalPlacement.getPlacementRelTo();
				}
			}else
			{
				LOGGER.info("Unimplemented type " + ifcObjectPlacement.eClass().getName());
				break;
			}	
		}
	}
	

	/**
	 * Given the relOSMPoint, put it under ifcRelativePlacement Coordinate system
	 * relPoint: The point to be transformed 
	 * ifcRelativePlacement: relative offset if xDirection is empty, assign it to default value 
	 * relOrigin: the relative coordinate system's origin point 
	 * absPoint: the transformed point
	 * */
	private void coordinate3DTrans(OSMPoint relOSMPoint, IfcAxis2Placement3D ifcRelativePlacement)
	{
		// new origin point
		List<Double> relOrigin = ifcRelativePlacement.getLocation().getCoordinates(); 
		
		Double ox = relOrigin.get(0);
		Double oy = relOrigin.get(1);
		Double oz = relOrigin.get(2);

		List<Double> xDirection = new ArrayList<Double>(); // new x Axis
		if (ifcRelativePlacement.isSetRefDirection()) {
			xDirection = ifcRelativePlacement.getRefDirection().getDirectionRatios();
		} else {
			xDirection.add(1.0);
			xDirection.add(0.0);
			xDirection.add(0.0);
		}
		List<Double> zDirection = new ArrayList<Double>(); // new z Axis
		if (ifcRelativePlacement.isSetAxis()) {
			zDirection = ifcRelativePlacement.getAxis().getDirectionRatios();
		} else {
			zDirection.add(0.0);
			zDirection.add(0.0);
			zDirection.add(1.0);
		}

		/**
		 * according to x axis and z axis, get y axis: ryx, ryy, ryz rxx, rxy,
		 * rxz, ryx, ryy, ryz, rzx, rzy, rzz refer to the relative coordinate
		 * system in scale 1
		 * */
		double rxx = xDirection.get(0);
		double rxy = xDirection.get(1);
		double rxz = xDirection.get(2);

		double rzx = zDirection.get(0);
		double rzy = zDirection.get(1);
		double rzz = zDirection.get(2);

		double ryx = rxz * rzy - rxy * rzz;
		double ryy = rxx * rzz - rxz * rzx;
		double ryz = rxy * rzx - rxx * rzy;
		
		
		double rxLength = Math.sqrt(Math.pow(rxx, 2) + Math.pow(rxy, 2)
				+ Math.pow(rxz, 2));
		double ryLength = Math.sqrt(Math.pow(ryx, 2) + Math.pow(ryy, 2)
				+ Math.pow(ryz, 2));
		double rzLength = Math.sqrt(Math.pow(rzx, 2) + Math.pow(rzy, 2)
				+ Math.pow(rzz, 2));

		Double a1 = relOSMPoint.getX();
		Double a2 = relOSMPoint.getY();
		Double a3 = relOSMPoint.getZ();

		Double x = a1 * rxx / rxLength + a2 * ryx / ryLength + a3 * rzx
				/ rzLength + ox;
		relOSMPoint.setX(x);

		Double y = a1 * rxy / rxLength + a2 * ryy / ryLength + a3 * rzy
				/ rzLength + oy;
		relOSMPoint.setY(y);

		Double z = a1 * rxz / rxLength + a2 * ryz / ryLength + a3 * rzz
				/ rzLength + oz;
		relOSMPoint.setZ(z);
	}

	class OSMPoint
	{
		private Double	x;
		private Double	y;
		private Double	z;

		public OSMPoint()
		{
			this.x = 0.0;
			this.y = 0.0;
			this.z = 0.0;
		}

		public OSMPoint(Double x, Double y)
		{
			this.x = x;
			this.y = y;
			this.z = 0.0;
		}

		public OSMPoint(Double x, Double y, Double z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public OSMPoint(OSMPoint p)
		{
			this.x = p.getX();
			this.y = p.getY();
			this.z = p.getZ();
		}

		public Double getX()
		{
			return x;
		}

		public void setX(Double x)
		{
			this.x = x;
		}

		public Double getY()
		{
			return y;
		}

		public void setY(Double y)
		{
			this.y = y;
		}

		public Double getZ()
		{
			return z;
		}

		public void setZ(Double z)
		{
			this.z = z;
		}

		public String toString()
		{
			return "(" + x + ", " + y + ", " + z + ")";
		}

		public boolean equals(OSMPoint p)
		{
			if (this.x == p.x && this.y == p.y && this.z == p.z)
				return true;
			return false;
		}

		public void addWith(OSMPoint p)
		{
			this.x += p.x;
			this.y += p.y;
			this.z += p.z;
		}
	}

	class OSMSurface
	{
		private String				uuid							= "";
		private String				surfaceName						= "";
		private String				typeName						= "";
		private String				constructionName				= "";
		private OSMSpace			osmSpace						= null;
		private String				outsideBoundaryCondition		= "";
		private String				outsideBoundaryConditionObject	= "";
		private String				sunExposure						= "";
		private String				windExposure					= "";
		private String				viewFactorToGround				= "";
		private String				numberOfVertices				= "";
		private List<OSMPoint>		pointList						= new ArrayList<OSMPoint>();
		
		//TODO: We do not need to maintain a list of subsurfaces of the surface. Remove this and use a global variable to maintain all elements.
		private List<OSMSubSurface>	subSurfaceList					= new ArrayList<OSMSubSurface>();

		public String getSurfaceName()
		{
			return surfaceName;
		}

		public void setSurfaceName(String surfaceName)
		{
			this.surfaceName = surfaceName;
		}

		public String getTypeName()
		{
			return typeName;
		}

		public void setTypeName(String typeName)
		{
			this.typeName = typeName;
		}

		public String getConstructionName()
		{
			return constructionName;
		}

		public void setConstructionName(String constructionName)
		{
			this.constructionName = constructionName;
		}

		public OSMSpace getOSMSpace()
		{
			return osmSpace;
		}

		public void setOSMSpace(OSMSpace osmSpace)
		{
			this.osmSpace = osmSpace;
		}

		public String getOutsideBoundaryCondition()
		{
			return outsideBoundaryCondition;
		}

		public void setOutsideBoundaryCondition(String outsideBoundaryCondition)
		{
			this.outsideBoundaryCondition = outsideBoundaryCondition;
		}

		public String getOutsideBoundaryConditionObject()
		{
			return outsideBoundaryConditionObject;
		}

		public void setOutsideBoundaryConditionObject(
				String outsideBoundaryConditionObject)
		{
			this.outsideBoundaryConditionObject = outsideBoundaryConditionObject;
		}

		public String getSunExposure()
		{
			return sunExposure;
		}

		public void setSunExposure(String sunExposure)
		{
			this.sunExposure = sunExposure;
		}

		public String getWindExposure()
		{
			return windExposure;
		}

		public void setWindExposure(String windExposure)
		{
			this.windExposure = windExposure;
		}

		public String getViewFactorToGround()
		{
			return viewFactorToGround;
		}

		public void setViewFactorToGround(String viewFactorToGround)
		{
			this.viewFactorToGround = viewFactorToGround;
		}

		public String getNumberOfVertices()
		{
			return numberOfVertices;
		}

		public void setNumberOfVertices(String numberOfVertices)
		{
			this.numberOfVertices = numberOfVertices;
		}

		public List<OSMPoint> getOSMPointList()
		{
			return pointList;
		}

		public void addOSMPoint(OSMPoint point)
		{
			this.pointList.add(point);
		}

		public List<OSMSubSurface> getSubSurfaceList()
		{
			return subSurfaceList;
		}

		public void addSubSurface(OSMSubSurface subSurface)
		{
			this.subSurfaceList.add(subSurface);
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
	}

	class OSMSubSurface
	{
		private String			uuid							= "";
		private String			subSurfaceName					= "";
		private String			typeName						= "";
		private String			constructionName				= "";
		private OSMSurface		osmSurface						= null;
		private String			outsideBoundaryConditionObject	= "";
		private String			viewFactorToGround				= "";
		private String			shadingControlName				= "";
		private String			frameAndDividerName				= "";
		private String			multiplier						= "";
		private String			numberOfVertices				= "";
		private List<OSMPoint>	pointList						= new ArrayList<OSMPoint>();

		public String getSubSurfaceName()
		{
			return subSurfaceName;
		}

		public void setSubSurfaceName(String subSurfaceName)
		{
			this.subSurfaceName = subSurfaceName;
		}

		public String getTypeName()
		{
			return typeName;
		}

		public void setTypeName(String typeName)
		{
			this.typeName = typeName;
		}

		public String getConstructionName()
		{
			return constructionName;
		}

		public void setConstructionName(String constructionName)
		{
			this.constructionName = constructionName;
		}

		public OSMSurface getOSMSurface()
		{
			return osmSurface;
		}

		public void setOSMSurface(OSMSurface osmSurface)
		{
			this.osmSurface = osmSurface;
		}

		public String getOutsideBoundaryConditionObject()
		{
			return outsideBoundaryConditionObject;
		}

		public void setOutsideBoundaryConditionObject(
				String outsideBoundaryConditionObject)
		{
			this.outsideBoundaryConditionObject = outsideBoundaryConditionObject;
		}

		public String getViewFactorToGround()
		{
			return viewFactorToGround;
		}

		public void setViewFactorToGround(String viewFactorToGround)
		{
			this.viewFactorToGround = viewFactorToGround;
		}

		public String getShadingControlName()
		{
			return shadingControlName;
		}

		public void setShadingControlName(String shadingControlName)
		{
			this.shadingControlName = shadingControlName;
		}

		public String getFrameAndDividerName()
		{
			return frameAndDividerName;
		}

		public void setFrameAndDividerName(String frameAndDividerName)
		{
			this.frameAndDividerName = frameAndDividerName;
		}

		public String getMultiplier()
		{
			return multiplier;
		}

		public void setMultiplier(String multiplier)
		{
			this.multiplier = multiplier;
		}

		public String getNumberOfVertices()
		{
			return numberOfVertices;
		}

		public void setNumberOfVertices(String numberOfVertices)
		{
			this.numberOfVertices = numberOfVertices;
		}

		public List<OSMPoint> getOSMPointList()
		{
			return pointList;
		}

		public void addOSMPoint(OSMPoint point)
		{
			this.pointList.add(point);
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
	}

	class OSMSpace
	{
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
		private List<OSMSurface>	surfaceList					= new ArrayList<OSMSurface>();

		public boolean containsUnsolvedSurface()
		{
			return containsUnsolvedSurface;
		}

		public void setContainsUnsolvedSurface(boolean containsUnsolvedSurface)
		{
			this.containsUnsolvedSurface = containsUnsolvedSurface;
		}

		public String getSpaceName()
		{
			return spaceName;
		}

		public void setSpaceName(String spaceName)
		{
			this.spaceName = spaceName;
		}

		public String getTypeName()
		{
			return typeName;
		}

		public void setTypeName(String typeName)
		{
			this.typeName = typeName;
		}

		public String getDefaultConstructionSetName()
		{
			return defaultConstructionSetName;
		}

		public void setDefaultConstructionSetName(
				String defaultConstructionSetName)
		{
			this.defaultConstructionSetName = defaultConstructionSetName;
		}

		public String getDefaultScheduleSetName()
		{
			return defaultScheduleSetName;
		}

		public void setDefaultScheduleSetName(String defaultScheduleSetName)
		{
			this.defaultScheduleSetName = defaultScheduleSetName;
		}

		public String getDirectionOfRelativeNorth()
		{
			return directionOfRelativeNorth;
		}

		public void setDirectionOfRelativeNorth(String directionOfRelativeNorth)
		{
			this.directionOfRelativeNorth = directionOfRelativeNorth;
		}

		public String getxOrigin()
		{
			return xOrigin;
		}

		public void setxOrigin(String xOrigin)
		{
			this.xOrigin = xOrigin;
		}

		public String getyOrigin()
		{
			return yOrigin;
		}

		public void setyOrigin(String yOrigin)
		{
			this.yOrigin = yOrigin;
		}

		public String getzOrigin()
		{
			return zOrigin;
		}

		public void setzOrigin(String zOrigin)
		{
			this.zOrigin = zOrigin;
		}

		public String getBuildingStoryName()
		{
			return buildingStoryName;
		}

		public void setBuildingStoryName(String buildingStoryName)
		{
			this.buildingStoryName = buildingStoryName;
		}

		public String getThermalZoneName()
		{
			return thermalZoneName;
		}

		public void setThermalZoneName(String thermalZoneName)
		{
			this.thermalZoneName = thermalZoneName;
		}

		public List<OSMSurface> getSurfaceList()
		{
			return surfaceList;
		}

		public void addSurface(OSMSurface surface)
		{
			this.surfaceList.add(surface);
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
	}

}
