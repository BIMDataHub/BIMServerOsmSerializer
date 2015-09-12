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
import org.bimserver.models.ifc2x3tc1.IfcUnit;
import org.bimserver.models.ifc2x3tc1.IfcUnitAssignment;
import org.bimserver.models.ifc2x3tc1.IfcValue;
import org.bimserver.models.ifc2x3tc1.IfcWall;
import org.bimserver.models.ifc2x3tc1.IfcWindow;
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
	//private IfcProduct							currentInstance;
	/**
	 * Wall and its Osm relation map, used by Osm surface-subsurface relation
	 * //TODO clear up this information. Use it with internalSurfaceMap.
	 */
	private Map<IfcWall, OsmSurface>			wallOsmSurfaceMap			= new HashMap<IfcWall, OsmSurface>();

	/**
	 */
	private Map<IfcWall, List<OsmSubSurface>>	unLinkedOsmSubSurfacesMap	= new HashMap<IfcWall, List<OsmSubSurface>>();
	// if sub-surface occurs before surface, add to unLinkedOsmSubSurfaces

	/**
	 * All points, incrementally added from IFC Points. Used to generate the
	 * space name in sequence
	 */
	private List<OsmPoint>						allOsmPoints				= new ArrayList<OsmPoint>();					// all

	/**
	 * All spaces, incrementally added from IFCSpace. Used to generate the space
	 * name in sequence
	 */
	private List<OsmSpace>						allSpaces					= new ArrayList<OsmSpace>();

	/**
	 * A 1 to 2 mapping from IfcWall to OsmSurface to store duplicated internal
	 * walls.
	 */
	private HashMap<IfcWall, LinkedList<OsmSurface>>	internalSurfaceMap			= new HashMap<IfcWall, LinkedList<OsmSurface>>();

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
	 * write the OsmSpace, OsmSurface, OsmSubSurface to outputStream
	 * @param outputContent
	 */
	private void generateOutput(UTF8PrintWriter outputContent) {
		outputContent.append("OS:Version,\n");
		UUID uuid = UUID.randomUUID();
		outputContent.append("{" +uuid.toString()+ "}" + ",  !- Handle\n  ");
		outputContent.append("1.3.0;                         !- Version Identifier\n\n");
		
		for(OsmSpace osmSpace: allSpaces) {
			outputContent.append(osmSpace.toString());
			
			for(OsmSurface osmSurface: osmSpace.getSurfaceList()) {
				outputContent.append(osmSurface.toString());
				
				for(OsmSubSurface osmSubSurface: osmSurface.getSubSurfaceList()) {
					outputContent.append(osmSubSurface.toString());
				}
			}
		}
	}

	
	/**
	 * Analyze every IFCSpace and create OsmSpace accordingly
	 * 
	 * @param ifcSpace
	 */
	private void extractSpaces(IfcSpace ifcSpace) {
		OsmSpace osmSpace = new OsmSpace();
		UUID uuid = UUID.randomUUID();
		osmSpace.setUuid(uuid.toString());
		osmSpace.setSpaceName("sp-" + (allSpaces.size() + 1) + "-Space");
	
		List<IfcRelSpaceBoundary> ifcRelSpaceBoundaryList = ifcSpace.getBoundedBy();

		// TODO what does this means? Can we delete this global variable and put it locally?
		wallOsmSurfaceMap.clear();
	
		for (IfcRelSpaceBoundary ifcRelSpaceBoundary : ifcRelSpaceBoundaryList) {
			IfcElement ifcElement = ifcRelSpaceBoundary.getRelatedBuildingElement();
			IfcConnectionGeometry ifcConnectionGeometry = ifcRelSpaceBoundary.getConnectionGeometry();

			if (ifcConnectionGeometry != null && ifcElement != null) {
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
	
	/**
	 * Extract all properties of the building elements
	 * 
	 * @param ifcElement
	 *            the space boundary element. It could be wall, ceiling, floor,
	 *            window, door, etc.
	 * @param ifcConnectionGeometry
	 *            the connecting geometry points of the connection points of the
	 *            space boundary
	 * @param OsmSpace
	 *            current OsmSpace to be analyzed
	 */
	private void extractRelatedElement(IfcElement ifcElement, IfcConnectionGeometry ifcConnectionGeometry, IfcSpace ifcSpace, OsmSpace osmSpace) {

		// initiate the connecting point list
		List<OsmPoint> spaceBoundaryPointList = new ArrayList<OsmPoint>();

		// Extract the connection geometry
		//TODO: Bug: sometimes windows and doors are not contained in the original surfaces.
		//TODO: put extract space boundary inside the IFC Element List as there might be some differences in extracting windows and doors
		boolean isGeometrySolved = extractSpaceBoundary(ifcConnectionGeometry, spaceBoundaryPointList, ifcSpace);

		// Extract Other Properties

		// Wall, surface
		if (ifcElement instanceof IfcWall) {
			if (isGeometrySolved) {
				IfcWall ifcWall = (IfcWall) ifcElement;
				OsmSurface osmSurface = new OsmSurface();
				UUID uuid = UUID.randomUUID();
				osmSurface.setUuid(uuid.toString());
				String osmSurfaceName = "su-" + (++surfaceNum);
				osmSurface.setSurfaceName(osmSurfaceName);
				osmSurface.setTypeName("Wall");
				osmSurface.setOsmSpace(osmSpace);
				
				//add internal surface link information
				if (internalSurfaceMap.containsKey(ifcWall)) {
					internalSurfaceMap.get(ifcWall).add(osmSurface);
				} else {
					LinkedList<OsmSurface> surfaceList = new LinkedList<OsmSurface>();
					surfaceList.add(osmSurface);
					internalSurfaceMap.put(ifcWall,surfaceList);
				}

				for (OsmPoint osmPoint : spaceBoundaryPointList) {
					osmSurface.addOsmPoint(osmPoint);
					allOsmPoints.add(osmPoint);
				}
				osmSpace.addSurface(osmSurface);

				wallOsmSurfaceMap.put(ifcWall, osmSurface);
				List<OsmSubSurface> unlinkedOsmSubSurfaceList = unLinkedOsmSubSurfacesMap
						.get(ifcWall);
				if (unlinkedOsmSubSurfaceList != null && unlinkedOsmSubSurfaceList.size() > 0) {
					for (OsmSubSurface osmSubSurface : unlinkedOsmSubSurfaceList) {
						osmSubSurface.setOsmSurface(osmSurface);
						osmSurface.addSubSurface(osmSubSurface);
					}
					unLinkedOsmSubSurfacesMap.remove(ifcWall);
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
				OsmSurface osmSurface = new OsmSurface();
				UUID uuid = UUID.randomUUID();
				osmSurface.setUuid(uuid.toString());
				osmSurface.setSurfaceName("su-" + (++surfaceNum));
				osmSurface.setOsmSpace(osmSpace);
				osmSurface.setOutsideBoundaryCondition("Ground");
				osmSurface.setSunExposure("NoSun");
				osmSurface.setWindExposure("NoWind");

				if (ifcSlab.getPredefinedType().getName().equals("FLOOR")) {
					osmSurface.setTypeName("Floor");
					if(isCCW(spaceBoundaryPointList)) {
						Collections.reverse(spaceBoundaryPointList);	
					}
				} else if (ifcSlab.getPredefinedType().getName().equals("ROOF")) {
					osmSurface.setTypeName("RoofCeiling");
					if(!isCCW(spaceBoundaryPointList)) {
						Collections.reverse(spaceBoundaryPointList);
					}
				} else {
					LOGGER.info("Unparsed slab type for " + ifcElement.eClass().getName());
				}

				for (OsmPoint osmPoint : spaceBoundaryPointList) {
					osmSurface.addOsmPoint(osmPoint);
					allOsmPoints.add(osmPoint);
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
				OsmSurface osmSurface = new OsmSurface();
				osmSurface.setSurfaceName("su-" + (++surfaceNum));
				UUID uuid = UUID.randomUUID();
				osmSurface.setUuid(uuid.toString());
				osmSurface.setTypeName("RoofCeiling");
				osmSurface.setOsmSpace(osmSpace);

				for (int j = 0; j < ifcRoof.getIsDefinedBy().size(); j++) {
					IfcRelDefines ifcRelDefines = ifcRoof.getIsDefinedBy().get(j);
					if(ifcRelDefines instanceof IfcRelDefinesByProperties) {
						IfcRelDefinesByProperties ifcRelDefinesByProperties = (IfcRelDefinesByProperties) ifcRelDefines;
						if (ifcRelDefinesByProperties.getRelatingPropertyDefinition().getName().equals("Pset_RoofCommon")) {
							List<IfcProperty> ifcPropertySetList = ((IfcPropertySet) ifcRelDefinesByProperties.getRelatingPropertyDefinition()).getHasProperties();
							for (int k = 0; k < ifcPropertySetList.size(); k++) {
								IfcPropertySingleValue ifcPropertySingleValue = (IfcPropertySingleValue) ifcPropertySetList.get(k);
								if (ifcPropertySingleValue.getName().equals("IsExternal")) {
										osmSurface.setOutsideBoundaryCondition("Outdoors");
										osmSurface.setSunExposure("SunExposed");
										osmSurface.setWindExposure("WindExposed");
										break;
								}
							}
							break;
						}
					}
				}

				for (OsmPoint osmPoint : spaceBoundaryPointList) {
					osmSurface.addOsmPoint(osmPoint);
					allOsmPoints.add(osmPoint);
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
				OsmSubSurface osmSubSurface = new OsmSubSurface();
				UUID uuid = UUID.randomUUID();
                osmSubSurface.setUuid(uuid.toString());
				osmSubSurface.setSubSurfaceName("sub-" + (++subSurfaceNum));
				osmSubSurface.setTypeName("FixedWindow");
				List<IfcRelFillsElement> ifcRelFillsElementList = ifcWindow.getFillsVoids();
				if (ifcRelFillsElementList.size() > 0) {
					IfcRelFillsElement ifcRelFillsElement = ifcRelFillsElementList.get(0);
					IfcElement ifcRelatingBuildingElement = ifcRelFillsElement.getRelatingOpeningElement().getVoidsElements().getRelatingBuildingElement();
					for (OsmPoint osmPoint : spaceBoundaryPointList) {
						osmSubSurface.addOsmPoint(osmPoint);
						allOsmPoints.add(osmPoint);
					}
					OsmSurface osmSurface = wallOsmSurfaceMap.get(ifcRelatingBuildingElement); 
					if (osmSurface != null) {
						osmSubSurface.setOsmSurface(osmSurface);
						osmSurface.addSubSurface(osmSubSurface);
					} else {
						List<OsmSubSurface> unlinkedOsmSubSurfaceList = unLinkedOsmSubSurfacesMap
								.get(ifcRelatingBuildingElement);
						if (unlinkedOsmSubSurfaceList == null) {
							unlinkedOsmSubSurfaceList = new ArrayList<OsmSubSurface>();
						}
						unlinkedOsmSubSurfaceList.add(osmSubSurface);
						unLinkedOsmSubSurfacesMap.put((IfcWall) ifcRelatingBuildingElement, unlinkedOsmSubSurfaceList);
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
				OsmSubSurface osmSubSurface = new OsmSubSurface();
				UUID uuid = UUID.randomUUID();
                osmSubSurface.setUuid(uuid.toString());
                osmSubSurface.setSubSurfaceName("sub-" + (++subSurfaceNum));
				osmSubSurface.setTypeName("Door");
				List<IfcRelFillsElement> ifcRelFillsElementList = ifcDoor.getFillsVoids();
				if (ifcRelFillsElementList.size() > 0) {
					IfcRelFillsElement ifcRelFillsElement = ifcRelFillsElementList.get(0);
					IfcElement ifcRelatingBuildingElement = ifcRelFillsElement.getRelatingOpeningElement().getVoidsElements().getRelatingBuildingElement();
					for (OsmPoint osmPoint : spaceBoundaryPointList) {
						osmSubSurface.addOsmPoint(osmPoint);
						allOsmPoints.add(osmPoint);
					}
					OsmSurface osmSurface = wallOsmSurfaceMap.get(ifcRelatingBuildingElement); 
					if (osmSurface != null) {
						osmSubSurface.setOsmSurface(osmSurface);
						osmSurface.addSubSurface(osmSubSurface);
					} else {
						List<OsmSubSurface> unlinkedOsmSubSurfaceList = unLinkedOsmSubSurfacesMap.get(ifcRelatingBuildingElement);
						if (unlinkedOsmSubSurfaceList == null) {
							unlinkedOsmSubSurfaceList = new ArrayList<OsmSubSurface>();
						}
						unlinkedOsmSubSurfaceList.add(osmSubSurface);
						unLinkedOsmSubSurfacesMap.put((IfcWall) ifcRelatingBuildingElement, unlinkedOsmSubSurfaceList);
					}
				}
			} else { // else for if(isGeometrySolved)
				LOGGER.info("Unparsed door for " + ifcElement.eClass().getName());
			}
		} 
		
		/*
		 * Virtual element
		else if(ifcElement instanceof IfcVirtualElement) {
			//TODO: Add IfcVirtualElement
		} 
		*/
		
		else { // else for ifcElement instanceof IfcWall, IfcSlab, IfcRoof,
			// IfcWindow, IfcDoor
			LOGGER.info("Unparsed element" + ifcElement.eClass().getName());
		}
	}	
	
	/**
	 * Extract the connection geometry 
	 * 
	 * @param ifcConnectionGeometry
	 *            the connection geometry of the space
	 * @param spaceBoundaryPointList
	 *            an empty point list of the connection geometry
	 * @return if the connection is a surface, return true.
	 */
	private boolean extractSpaceBoundary(IfcConnectionGeometry ifcConnectionGeometry, List<OsmPoint> spaceBoundaryPointList, IfcSpace ifcSpace) {
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
	 * Extract straight space boundary
	 * @param ifcSurfaceOfLinearExtrusion
	 * @param spaceBoundaryPointList
	 * @return
	 */
	private boolean extractSurfaceOfLinearExtrusionSB(IfcSurfaceOfLinearExtrusion ifcSurfaceOfLinearExtrusion, List<OsmPoint> spaceBoundaryPointList, IfcSpace ifcSpace) {
		IfcProfileDef ifcProfileDef = ifcSurfaceOfLinearExtrusion.getSweptCurve();
		
		
		if(ifcProfileDef instanceof IfcArbitraryOpenProfileDef) {
			IfcArbitraryOpenProfileDef ifcArbitraryOpenProfileDef = (IfcArbitraryOpenProfileDef) ifcProfileDef;
			IfcBoundedCurve ifcBoundedCurve = ifcArbitraryOpenProfileDef.getCurve();
			
			if(ifcBoundedCurve instanceof IfcPolyline) {
				IfcPolyline ifcPolyline = (IfcPolyline) ifcBoundedCurve;
				List<OsmPoint> osmPointList = new ArrayList<OsmPoint>();
				for (IfcCartesianPoint ifcCartesianPoint : ifcPolyline.getPoints()) {
					List<Double> point = ifcCartesianPoint.getCoordinates();
					/**
					 * http://www.buildingsmart-tech.org/ifc/IFC2x4/rc2/html/schema/ifcgeometryresource/lexical/ifccartesianpoint.htm
					 * @author:Pengwei Lan
					 **/
					if(ifcCartesianPoint.isSetDim()) {
						int dimension = ifcCartesianPoint.getDim();
						if (dimension == 2) {
							osmPointList.add(new OsmPoint(point.get(0), point.get(1)));
						} else if(dimension == 3) {
							osmPointList.add(new OsmPoint(point.get(0), point.get(1), point.get(2)));
						} else {
							LOGGER.info("The dimension of this point is incorrect: point dimension = " + dimension);
							return false;
						}
					} else {
						osmPointList.add(new OsmPoint(point.get(0), point.get(1)));
					}	
				}
				
				IfcAxis2Placement3D position = ifcSurfaceOfLinearExtrusion.getPosition();
				
				if(osmPointList.isEmpty()) {
					LOGGER.info("Can't get the points of the current line!");
					return false;
				}
				
				// Translate the points on the swept surface
				for (OsmPoint osmPoint : osmPointList) {
					// Put the curved boundary point in the basis plane coordinate system.
					coordinate3DTrans(osmPoint, position); 
					// Upgrade to global coordinates according to the space's local coordinate system
					coordinateSys3DTrans(osmPoint, ifcSpace); 
					spaceBoundaryPointList.add(osmPoint);
				}
	
				// Osm requirement
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
				
				for (OsmPoint osmPoint : osmPointList) {
					OsmPoint newPoint = new OsmPoint(osmPoint.getX() + mx, osmPoint.getY() + my, osmPoint.getZ() + mz);
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
	 * Extract curved space boundary
	 * @param ifcCurveBoundedPlane
	 * @param spaceBoundaryPointList
	 * @return
	 */
	private boolean extractCurveBoundedPlaneSB(IfcCurveBoundedPlane ifcCurveBoundedPlane, List<OsmPoint> spaceBoundaryPointList, IfcSpace ifcSpace) {
		IfcAxis2Placement3D position = ifcCurveBoundedPlane.getBasisSurface().getPosition();
		IfcCurve ifcCurve = ifcCurveBoundedPlane.getOuterBoundary();
		
		if (ifcCurve instanceof IfcPolyline) {
			List<OsmPoint> osmPointList = new ArrayList<OsmPoint>();
			getPointsOfIfcPolyline((IfcPolyline) ifcCurve, osmPointList);
			
			for (OsmPoint osmPoint : osmPointList) {
				coordinate3DTrans(osmPoint, position);
				coordinateSys3DTrans(osmPoint, ifcSpace); 
				spaceBoundaryPointList.add(osmPoint);
			}
			return true;
		} else if (ifcCurve instanceof IfcCompositeCurve) { 
			IfcCompositeCurve ifcCompositeCurve = (IfcCompositeCurve) ifcCurve;
			List<IfcCompositeCurveSegment> ifcCompositeCurveSegments = ifcCompositeCurve.getSegments();
			for(IfcCompositeCurveSegment ifcCompositeCurveSegment: ifcCompositeCurveSegments) {
				IfcCurve parentCurve = ifcCompositeCurveSegment.getParentCurve();
				
				if (parentCurve instanceof IfcPolyline) {
					List<OsmPoint> osmPointList = new ArrayList<OsmPoint>();
					getPointsOfIfcPolyline((IfcPolyline) parentCurve, osmPointList);
					for (OsmPoint osmPoint : osmPointList) {
						coordinate3DTrans(osmPoint, position);
						coordinateSys3DTrans(osmPoint, ifcSpace); 
						spaceBoundaryPointList.add(osmPoint);
					}
				} /*else if(parentCurve instanceof IfcTrimmedCurve) {
					//TODO: extract geometry from IfcTrimmedCurve
				} */
				else {
					LOGGER.info("Unimplemented type [extractCurveBoundedPlaneSB]" + parentCurve.eClass().getName());
				}
			}
			
			return true;
		} else {
			LOGGER.info("Unimplemented type [extractCurveBoundedPlaneSB]" + ifcCurve.eClass().getName());
			return false;
		}
	}
	
	/**
	 * 
	 * 
	 * Coordinate Transformation Functions
	 * 
	 * 
	 * */
	
	/**
	 * Given relOsmPoint, upgrade it's local coordinate system into the global coordinates.
	 * relPoint: The point to be transformed
	 * */
	private void coordinateSys3DTrans(OsmPoint relOsmPoint, IfcSpace ifcSpace) {
		IfcObjectPlacement ifcObjectPlacement = ifcSpace.getObjectPlacement();
		
		boolean upgradeCompleted = false;
		
		while(!upgradeCompleted) {
			if(ifcObjectPlacement instanceof IfcLocalPlacement) {
				IfcLocalPlacement ifcLocalPlacement = (IfcLocalPlacement)ifcObjectPlacement;
				IfcAxis2Placement ifcAxis2Placement = ifcLocalPlacement.getRelativePlacement();
				
				if(ifcAxis2Placement instanceof IfcAxis2Placement3D) {
					//put the current point into the relative coordinate system.
					coordinate3DTrans(relOsmPoint, (IfcAxis2Placement3D)ifcAxis2Placement);
				} else{
					LOGGER.info("Unimplemented type " + ifcAxis2Placement.eClass().getName());
					break;
				}

				//check whether still has a upper layer of transformation
				if (!ifcLocalPlacement.isSetPlacementRelTo()) {
					upgradeCompleted = true;
				} else {
					ifcObjectPlacement =  ifcLocalPlacement.getPlacementRelTo();
				}
			} else {
				LOGGER.info("Unimplemented type " + ifcObjectPlacement.eClass().getName());
				break;
			}	
		}
	}
	
	/**
	 * Given the relOsmPoint, put it under ifcRelativePlacement Coordinate system
	 * relPoint: The point to be transformed 
	 * ifcRelativePlacement: relative offset if xDirection is empty, assign it to default value 
	 * relOrigin: the relative coordinate system's origin point 
	 * absPoint: the transformed point
	 * */
	private void coordinate3DTrans(OsmPoint relOsmPoint, IfcAxis2Placement3D ifcRelativePlacement) {
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
		
		
		double rxLength = Math.sqrt(Math.pow(rxx, 2) + Math.pow(rxy, 2) + Math.pow(rxz, 2));
		double ryLength = Math.sqrt(Math.pow(ryx, 2) + Math.pow(ryy, 2) + Math.pow(ryz, 2));
		double rzLength = Math.sqrt(Math.pow(rzx, 2) + Math.pow(rzy, 2) + Math.pow(rzz, 2));

		Double a1 = relOsmPoint.getX();
		Double a2 = relOsmPoint.getY();
		Double a3 = relOsmPoint.getZ();

		Double x = a1 * rxx / rxLength + a2 * ryx / ryLength + a3 * rzx / rzLength + ox;
		relOsmPoint.setX(x);

		Double y = a1 * rxy / rxLength + a2 * ryy / ryLength + a3 * rzy / rzLength + oy;
		relOsmPoint.setY(y);

		Double z = a1 * rxz / rxLength + a2 * ryz / ryLength + a3 * rzz / rzLength + oz;
		relOsmPoint.setZ(z);
	}
		
	/**
	 * 
	 * 
	 * Miscellaneous Functions
	 * 
	 * 
	 * */
	
	/**
	 * For internal wall, we link the duplicated walls to each other by
	 * outsideboundaryConditionObject
	 */
	private void addLinkageInformation() {
		for (LinkedList<OsmSurface> surfaceList : internalSurfaceMap.values()) {
			while (!surfaceList.isEmpty()) {
				OsmSurface firstSurface = surfaceList.removeFirst();

				int closestSurfaceIndex = -1;
				double leastDistance = maxWallThickness;
				OsmPoint firstCenter = computeSurfaceCenter(firstSurface);
				
				//loop through the rest of elements
				for (int i = 0; i<surfaceList.size(); i++) {
					OsmPoint secondCenter = computeSurfaceCenter(surfaceList.get(i));
					double distance = distanceOfPoints(firstCenter,secondCenter);
					if(distance <= leastDistance) {
						closestSurfaceIndex = i;
						leastDistance = distance;
					}
				}
				if(closestSurfaceIndex>=0) {
					OsmSurface secondSurface = surfaceList.remove(closestSurfaceIndex);
					firstSurface.setOutsideBoundaryCondition("Surface");
					firstSurface.setOutsideBoundaryConditionObject(secondSurface.getSurfaceName());
					firstSurface.setSunExposure("NoSun");
					firstSurface.setWindExposure("NoWind");
					secondSurface.setOutsideBoundaryCondition("Surface");
					secondSurface.setOutsideBoundaryConditionObject(firstSurface.getSurfaceName());
					secondSurface.setSunExposure("NoSun");
					secondSurface.setWindExposure("NoWind");
				} else {
					firstSurface.setOutsideBoundaryCondition("Outdoors");
					firstSurface.setSunExposure("SunExposed");
					firstSurface.setWindExposure("WindExposed");
				}
			}
		}
	}
	
	/**
	 * Calculate the center of a rectangular Surface. 
	 * Assuming all OsmSurfaces are Rectangular and contains four points
	 * @param OsmSurface
	 * @return
	 */
	private OsmPoint computeSurfaceCenter(OsmSurface osmSurface) {
		List<OsmPoint> list = osmSurface.getOsmPointList();
		if (list.size() != 4) {
			System.err.print("Warning! The Surface " + osmSurface.getSurfaceName() + " contains " + list.size() + " points!");
		}
		double xSum = 0, ySum = 0, zSum = 0;
		for (OsmPoint p : list) {
			xSum += p.getX();
			ySum += p.getY();
			zSum += p.getZ();
		}
		return new OsmPoint(xSum/list.size(),ySum/list.size(),zSum/list.size());		
	}
	
	/**
	 * compute the euclidean distance of two OsmPoints
	 * @param point1
	 * @param point2
	 * @return
	 */
	private double distanceOfPoints(OsmPoint point1, OsmPoint point2) {
		double x2 = (point1.getX()-point2.getX())*(point1.getX()-point2.getX());
		double y2 = (point1.getY()-point2.getY())*(point1.getY()-point2.getY());
		double z2 = (point1.getZ()-point2.getZ())*(point1.getZ()-point2.getZ());
		return Math.sqrt(x2+y2+z2);
	}

	private double calUnitConvScale(IfcModelInterface model) {
		double scale = 1.0;
		List<IfcUnitAssignment> ifcUnitAssignmentList =  model.getAll(IfcUnitAssignment.class);
		if(!ifcUnitAssignmentList.isEmpty()) {
			IfcUnitAssignment ifcUnitAssignment = ifcUnitAssignmentList.get(0);
			List<IfcUnit> ifcUnitList = ifcUnitAssignment.getUnits();
			for (IfcUnit ifcUnit : ifcUnitList) {
				if (ifcUnit instanceof IfcConversionBasedUnit) {
					IfcConversionBasedUnit ifcConversionBasedUnit = (IfcConversionBasedUnit) ifcUnit;
					if (ifcConversionBasedUnit.getUnitType().getName() == "LENGTHUNIT") {
						IfcMeasureWithUnit ifcMeasureWithUnit = ifcConversionBasedUnit.getConversionFactor();
						IfcValue ifcValue = ifcMeasureWithUnit.getValueComponent();
						if (ifcValue instanceof IfcRatioMeasure) {
							scale = ((IfcRatioMeasure) ifcValue).getWrappedValue();
							break;
						}
					}
	
				}
			}
		}
		return scale;
	}

	private void getPointsOfIfcPolyline(IfcPolyline ifcPolyline, List<OsmPoint> osmPointList) {
		List<IfcCartesianPoint> ifcCartesianPointList = ifcPolyline.getPoints();
		for (IfcCartesianPoint ifcCartesianPoint : ifcCartesianPointList) {
			List<Double> point = ifcCartesianPoint.getCoordinates();
			
			if(ifcCartesianPoint.isSetDim()) {
				int dimension = ifcCartesianPoint.getDim();
				if (dimension == 2) {
					osmPointList.add(new OsmPoint(point.get(0), point.get(1)));
				} else if(dimension == 3) {
					osmPointList.add(new OsmPoint(point.get(0), point.get(1), point.get(2)));
				} else {
					LOGGER.info("The dimension of this point is incorrect: point dimension = " + dimension);
				}
			} else {
				osmPointList.add(new OsmPoint(point.get(0), point.get(1)));
			}
		}
	}
	
	/**
	 * Counter Clock-wise
	 * @param point
	 * @return
	 */
	
	public boolean isCCW(List<OsmPoint> point) {
		double centroidX = 0.0;
		double centroidY = 0.0;
		int length = point.size();
		
		for(OsmPoint temp : point) {
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
		for(OsmPoint osmPoint : allOsmPoints) {
			osmPoint.setX(osmPoint.getX() * scale);
			osmPoint.setY(osmPoint.getY() * scale);
			osmPoint.setZ(osmPoint.getZ() * scale);
		}
	}
}
