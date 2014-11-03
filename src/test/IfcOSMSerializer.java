package test;

import java.io.OutputStream;
import java.util.*;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.emf.PackageMetaData;
import org.bimserver.ifc.IfcSerializer;
import org.bimserver.models.ifc2x3tc1.*;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.renderengine.RenderEnginePlugin;
import org.bimserver.plugins.serializers.EmfSerializer;
import org.bimserver.plugins.serializers.ProjectInfo;
import org.bimserver.plugins.serializers.SerializerException;
import org.bimserver.utils.UTF8PrintWriter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class IfcOSMSerializer extends EmfSerializer
{
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
	private HashMap<IfcWall, List<OSMSurface>>	internalSurfaceMap			= new HashMap<IfcWall, List<OSMSurface>>();

	/**
	 */
	private int									surfaceNum					= 0;

	@Override
	public void init(IfcModelInterface model, ProjectInfo projectInfo, PluginManager pluginManager, RenderEnginePlugin renderEnginePlugin, PackageMetaData packageMetaData, boolean normalizeOids) throws SerializerException
	{
		super.init(model, projectInfo, pluginManager, renderEnginePlugin,
				packageMetaData, normalizeOids);

		List<IfcSpace> ifcSpaceList = model.getAll(IfcSpace.class);
		for (IfcSpace ifcSpace : ifcSpaceList)
		{
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
	public void reset()
	{
		setMode(Mode.BODY);
	}

	@Override
	protected boolean write(OutputStream outputStream)
			throws SerializerException
	{
		if (out == null)
		{
			out = new UTF8PrintWriter(outputStream);
		}
		if (getMode() == Mode.BODY)
		{
			out = new UTF8PrintWriter(outputStream);
			generateOutput(out);
			out.flush();
			setMode(Mode.FINISHED);
			return true;
		} else if (getMode() == Mode.FINISHED)
		{
			return false;
		}
		return false;
	}

	/**
	 * write the OSMSpace, OSMSurface, OSMSubSurface to outputStream
	 * each type of element is represented as a JsonArray of JsonObjects
	 * @param out
	 */
	private void generateOutput(UTF8PrintWriter out)
	{
		JsonArray spaceArray = new JsonArray();
		JsonArray surfaceArray = new JsonArray();
		JsonArray subSurfaceArray = new JsonArray();
		//write osmSpace
		for (OSMSpace osmSpace : allSpaces)
		{
			JsonObject space = new JsonObject();
			space.addProperty("Name", osmSpace.getSpaceName());
			space.addProperty("SpaceTypeName", osmSpace.getTypeName());
			space.addProperty("DefaultConstructionSetName", osmSpace.getDefaultConstructionSetName());
			space.addProperty("DefaultScheduleSetName", osmSpace.getDefaultScheduleSetName());
			space.addProperty("DirectionOfRelativeNorth", osmSpace.getDirectionOfRelativeNorth());
			space.addProperty("XOrigin", osmSpace.getxOrigin());
			space.addProperty("YOrigin", osmSpace.getyOrigin());
			space.addProperty("ZOrigin", osmSpace.getzOrigin());
			space.addProperty("BuildingStoryName", osmSpace.getBuildingStoryName());
			space.addProperty("ThermalZoneName", osmSpace.getSpaceName());//Currently thermal zone is the same as space name?
			spaceArray.add(space);
		}
		
		//write osmSurfaces
		for (OSMSpace osmSpace : allSpaces)
		{
			//for all osmSurfaces
			for (OSMSurface osmSurface : osmSpace.getSurfaceList())
			{
				//construct surface JSON
				JsonObject surface = new JsonObject();
				surface.addProperty("Name", osmSurface.getSurfaceName());
				surface.addProperty("SurfaceType", osmSurface.getTypeName());
				surface.addProperty("ConstructionName", osmSurface.getConstructionName());
				surface.addProperty("SpaceName", osmSurface.getOSMSpace().getSpaceName());
				surface.addProperty("OutsideBoundaryCondition", osmSurface.getOutsideBoundaryCondition());
				surface.addProperty("OutsideBoundaryConditionObject", osmSurface.getOutsideBoundaryConditionObject());
				surface.addProperty("SunExposure", osmSurface.getSunExposure());
				surface.addProperty("WindExposure", osmSurface.getWindExposure());
				surface.addProperty("ViewFactorToGround", osmSurface.getViewFactorToGround());
				surface.addProperty("NumberOfVertices", osmSurface.getNumberOfVertices());
				
				JsonArray surfacePointList = new JsonArray();

				if (osmSurface.getOSMPointList().size() > 0)
				{
					for (OSMPoint osmPoint:osmSurface.getOSMPointList())
					{
						JsonObject point = new JsonObject();
						point.addProperty("X", osmPoint.getX());
						point.addProperty("Y", osmPoint.getY());
						point.addProperty("Z", osmPoint.getZ());
						surfacePointList.add(point);
					}
				}
				
				surface.add("Vertices", surfacePointList);
				surfaceArray.add(surface);

				//for all osmSubSurfaces
				for (OSMSubSurface osmSubSurface : osmSurface
						.getSubSurfaceList())
				{					
					//construct subsurface JSON
					JsonObject subSurface = new JsonObject();
					subSurface.addProperty("Name", osmSubSurface.getSubSurfaceName());
					subSurface.addProperty("SubSurfaceType", osmSubSurface.getTypeName());
					subSurface.addProperty("ConstructionName", osmSubSurface.getConstructionName());
					subSurface.addProperty("SurfaceName", osmSubSurface.getOSMSurface().getSurfaceName());
					subSurface.addProperty("OutsideBoundaryConditionObject", osmSubSurface.getOutsideBoundaryConditionObject());
					subSurface.addProperty("ViewFactorToGround", osmSubSurface.getViewFactorToGround());
					subSurface.addProperty("ShadingControlName", osmSubSurface.getShadingControlName());
					subSurface.addProperty("FrameAndDividerName", osmSubSurface.getFrameAndDividerName());
					subSurface.addProperty("Multiplier", osmSubSurface.getMultiplier());
					subSurface.addProperty("NumberOfVertices", osmSubSurface.getNumberOfVertices());
					
					JsonArray subSurfacePointList = new JsonArray();
					
					if (osmSubSurface.getOSMPointList().size() > 0)
					{
						
						for (OSMPoint osmPoint:osmSubSurface.getOSMPointList())
						{
							JsonObject point = new JsonObject();
							point.addProperty("X", osmPoint.getX());
							point.addProperty("Y", osmPoint.getY());
							point.addProperty("Z", osmPoint.getZ());
							subSurfacePointList.add(point);
						}
					}
					
					subSurface.add("Vertices", subSurfacePointList);
					subSurfaceArray.add(subSurface);
				}
			}
		}
		
		//Construct the final assembled OSM JSON
		JsonObject osmJSON = new JsonObject();
		osmJSON.add("Spaces", spaceArray);
		osmJSON.add("Surfaces", surfaceArray);
		osmJSON.add("SubSurfaces", subSurfaceArray);
		
		out.append(osmJSON.toString());
	}

	/**
	 * For internal wall, we link the duplicated walls to each other by
	 * outsideboundaryConditionObject
	 */
	private void addLinkageInformation()
	{
		for (List<OSMSurface> surfaceList : internalSurfaceMap.values())
		{
			// check if each internal wall is shared by 2 spaces.
			if (surfaceList.size() == 2)
			{
				String surface0 = surfaceList.get(0).getSurfaceName();
				String surface1 = surfaceList.get(1).getSurfaceName();
				surfaceList.get(0).setOutsideBoundaryConditionObject(surface1);
				surfaceList.get(1).setOutsideBoundaryConditionObject(surface0);
				System.err.println("Success:" + surface0);
			} else
			{
				System.err
						.println("Error: internal wall matching not equal to 2!");
				System.err.println("size:" + surfaceList.size());
				System.err.println("firstName:"
						+ surfaceList.get(0).getSurfaceName());
			}
		}
	}

	/**
	 * Analyze every IFCSpace and create OSMSpace accordingly
	 * 
	 * @param ifcSpace
	 */
	private void extractSpaces(IfcSpace ifcSpace)
	{

		currentInstance = ifcSpace;

		// Create the temporary OSMSpace object
		OSMSpace osmSpace = new OSMSpace();

		// Assign a name to the OSMSpace
		osmSpace.setSpaceName("sp-" + (allSpaces.size() + 1) + "-Space");

		/**
		 * Obsolete code osmSpace.setSpaceName("sp-" + (allSpaces.size() + 1) +
		 * "-Space-" + ifcSpace.getName()); to be replaced by the line above!!!!
		 */

		// Get all related space boundary objects (including surfaces,
		// subsurfaces)
		List<IfcRelSpaceBoundary> ifcRelSpaceBoundaryList = ifcSpace
				.getBoundedBy();

		// TODO what does this means?
		wallOSMSurfaceMap.clear();

		// for every space bounary such as wall, ceiling or floor
		for (IfcRelSpaceBoundary ifcRelSpaceBoundary : ifcRelSpaceBoundaryList)
		{

			// get the related ifc element of the space boundary to see if it's
			// a wall or floor, etc.
			IfcElement ifcElement = ifcRelSpaceBoundary
					.getRelatedBuildingElement();

			// TODO edit: get the connection geometry points of the space
			// boundary
			IfcConnectionGeometry ifcConnectionGeometry = ifcRelSpaceBoundary
					.getConnectionGeometry();

			if (ifcConnectionGeometry != null && ifcElement != null)
			{

				// extract the space boundary information such as wall, ceiling
				// and floor.
				extractRelatedElement(ifcElement, ifcConnectionGeometry,
						osmSpace);
			}
		}
		if (!osmSpace.containsUnsolvedSurface())
		{
			allSpaces.add(osmSpace);
		} else
		{
			surfaceNum -= osmSpace.getSurfaceList().size();
		}
	}

	private double calUnitConvScale(IfcModelInterface model)
	{
		double scale = 1.0;
		List<IfcUnitAssignment> ifcUnitAssignmentList =  model
				.getAll(IfcUnitAssignment.class);
		if(!ifcUnitAssignmentList.isEmpty())
		{
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
	 * Extract the connection geometry
	 * 
	 * @param ifcConnectionGeometry
	 *            the connection geometry of the space
	 * @param spaceBoundaryPointList
	 *            an empty point list of the connection geometry
	 * @return
	 */
	private boolean extractSpaceBoundary(
			IfcConnectionSurfaceGeometry ifcConnectionGeometry,
			List<OSMPoint> spaceBoundaryPointList)
	{
		// TODO what does this mean?
		boolean isGeometrySolved = false;

		IfcSurfaceOrFaceSurface ifcSurfaceOrFaceSurface = ifcConnectionGeometry
				.getSurfaceOnRelatingElement();
		if (ifcSurfaceOrFaceSurface instanceof IfcCurveBoundedPlane)
		{
			IfcCurveBoundedPlane ifcCurveBoundedPlane = (IfcCurveBoundedPlane) ifcSurfaceOrFaceSurface;
			isGeometrySolved = extractCurveBoundedPlaneSB(ifcCurveBoundedPlane,
					spaceBoundaryPointList);
		} else if (ifcSurfaceOrFaceSurface instanceof IfcSurfaceOfLinearExtrusion)
		{
			IfcSurfaceOfLinearExtrusion ifcSurfaceOfLinearExtrusion = (IfcSurfaceOfLinearExtrusion) ifcSurfaceOrFaceSurface;
			isGeometrySolved = extractSurfaceOfLinearExtrusionSB(
					ifcSurfaceOfLinearExtrusion, spaceBoundaryPointList);
		} else
		{
			System.err.println("Error: unparsed IfcSurfaceOrFaceSurface type!");
		}

		return isGeometrySolved;
	}

	// TODO: might miss one translation
	private boolean extractCurveBoundedPlaneSB(
			IfcCurveBoundedPlane ifcCurveBoundedPlane,
			List<OSMPoint> spaceBoundaryPointList)
	{
		boolean isGeometrySolved = false;
		try
		{
			IfcCurve ifcCurve = ifcCurveBoundedPlane.getOuterBoundary();
			IfcPolyline ifcPolyline = (IfcPolyline) ifcCurve;
			List<IfcCartesianPoint> ifcCartesianPointList = ifcPolyline
					.getPoints();
			List<OSMPoint> OSMPointList = new ArrayList<OSMPoint>();
			;
			for (IfcCartesianPoint ifcCartesianPoint : ifcCartesianPointList)
			{
				List<Double> point = ifcCartesianPoint.getCoordinates();
				OSMPointList.add(new OSMPoint(point.get(0), point.get(1)));
			}

			IfcAxis2Placement3D position = ifcCurveBoundedPlane
					.getBasisSurface().getPosition();
			for (OSMPoint OSMPoint : OSMPointList)
			{
				OSMPoint = coordinate3DTrans(OSMPoint, position); // internal 3D
																	// transformation
				OSMPoint = coordinateSys3DTrans(OSMPoint); // transform
															// according to the
															// space's local
															// coordinate system
				spaceBoundaryPointList.add(OSMPoint);
			}
			isGeometrySolved = true;
		} catch (Exception e)
		{
			System.err
					.println("Error: extractCurveBoundedPlaneSB downcast error!");
		}
		return isGeometrySolved;
	}

	// TODO: might miss one translation
	private boolean extractSurfaceOfLinearExtrusionSB(
			IfcSurfaceOfLinearExtrusion ifcSurfaceOfLinearExtrusion,
			List<OSMPoint> spaceBoundaryPointList)
	{
		boolean isGeometrySolved = false;
		try
		{
			IfcArbitraryOpenProfileDef ifcArbitraryOpenProfileDef = (IfcArbitraryOpenProfileDef) ifcSurfaceOfLinearExtrusion
					.getSweptCurve();
			IfcPolyline ifcPolyline = (IfcPolyline) ifcArbitraryOpenProfileDef
					.getCurve();
			IfcAxis2Placement3D position = ifcSurfaceOfLinearExtrusion
					.getPosition();
			double extrudedDepth = ifcSurfaceOfLinearExtrusion.getDepth();
			if (ifcSurfaceOfLinearExtrusion.getExtrudedDirection()
					.getDirectionRatios().get(2).equals(1.0))
			{
			} else if (ifcSurfaceOfLinearExtrusion.getExtrudedDirection()
					.getDirectionRatios().get(2).equals(-1.0))
			{
				extrudedDepth = -extrudedDepth;
			} else
			{
				System.err.println("Error: Unsolved extruded direction!");
			}

			List<OSMPoint> osmPointList = new ArrayList<OSMPoint>();
			// the points on the swept surface
			for (IfcCartesianPoint ifcCartesianPoint : ifcPolyline.getPoints())
			{
				List<Double> point = ifcCartesianPoint.getCoordinates();
				OSMPoint osmPoint = new OSMPoint(point.get(0), point.get(1));
				osmPoint = coordinate3DTrans(osmPoint, position); // internal 3D
																	// transformation
				osmPointList.add(osmPoint);
			}
			// translate the points on the swept surface
			for (OSMPoint osmPoint : osmPointList)
			{
				osmPoint = coordinateSys3DTrans(osmPoint); // transform
															// according to the
															// space's local
															// coordinate system
				spaceBoundaryPointList.add(osmPoint);
			}

			// the extruded points and the translation
			Collections.reverse(osmPointList); // OSM requirement
			for (OSMPoint osmPoint : osmPointList)
			{
				osmPoint.setZ(osmPoint.getZ() + extrudedDepth); // extruding
				osmPoint = coordinateSys3DTrans(osmPoint); // transform
															// according to the
															// space's local
															// coordinate system
				spaceBoundaryPointList.add(osmPoint);
			}
			isGeometrySolved = true;
		} catch (Exception e)
		{
			System.err
					.println("Error: extractSurfaceOfLinearExtrusionSB downcast error!");
		}
		return isGeometrySolved;
	}

	/**
	 * Extract all properties of the related building element
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
	private void extractRelatedElement(IfcElement ifcElement,
			IfcConnectionGeometry ifcConnectionGeometry, OSMSpace osmSpace)
	{

		// initiate the connecting point list
		List<OSMPoint> spaceBoundaryPointList = new ArrayList<OSMPoint>();

		// Extract the connection geometry, ifcConnectionGeometry is an abstract
		// class, ifcConnectionSurfaceGeometry is a subtype of it.
		boolean isGeometrySolved = extractSpaceBoundary(
				(IfcConnectionSurfaceGeometry) ifcConnectionGeometry,
				spaceBoundaryPointList);

		// Extract Other Properties

		// Wall, surface
		if (ifcElement instanceof IfcWall)
		{
			if (isGeometrySolved)
			{
				IfcWall ifcWall = (IfcWall) ifcElement;
				OSMSurface osmSurface = new OSMSurface();
				String osmSurfaceName = "su-" + (++surfaceNum);
				// String osmSurfaceName = "su-" + (++surfaceNum) + "-" +
				// ifcElement.getName(); //this line should be replaced by the
				// line above!!!!
				osmSurface.setSurfaceName(osmSurfaceName);
				osmSurface.setTypeName("Wall");
				osmSurface.setOSMSpace(osmSpace);
				for (int j = 0; j < ifcWall.getIsDefinedBy().size(); j++)
				{
					IfcRelDefines ifcRelDefines = ifcWall.getIsDefinedBy().get(
							j);
					if (ifcRelDefines instanceof IfcRelDefinesByProperties)
					{
						IfcRelDefinesByProperties ifcRelDefinesByProperties = (IfcRelDefinesByProperties) ifcRelDefines;
						if (ifcRelDefinesByProperties
								.getRelatingPropertyDefinition().getName()
								.equals("Pset_WallCommon"))
						{
							List<IfcProperty> ifcPropertySetList = ((IfcPropertySet) ifcRelDefinesByProperties
									.getRelatingPropertyDefinition())
									.getHasProperties();
							for (int k = 0; k < ifcPropertySetList.size(); k++)
							{
								IfcPropertySingleValue ifcPropertySingleValue = (IfcPropertySingleValue) ifcPropertySetList
										.get(k);
								if (ifcPropertySingleValue.getName().equals(
										"IsExternal"))
								{
									Tristate isExternal = ((IfcBoolean) ifcPropertySingleValue
											.getNominalValue())
											.getWrappedValue();
									if (isExternal.getValue() == 0)
									{ // true
										osmSurface
												.setOutsideBoundaryCondition("Outdoors"); // TODO:
																							// Chong!!!!!
																							// confirmed
																							// with
																							// mechanical
																							// engineers,
																							// Ke
																							// or
																							// OpenStudio
																							// teams
										osmSurface.setSunExposure("SunExposed");
										osmSurface
												.setWindExposure("WindExposed");
									} else if (isExternal.getValue() == 1)
									{ // false
										osmSurface
												.setOutsideBoundaryCondition("Surface");
										osmSurface.setSunExposure("NoSun");
										osmSurface.setWindExposure("NoWind");

										if (internalSurfaceMap
												.containsKey(ifcWall))
										{
											internalSurfaceMap.get(ifcWall)
													.add(osmSurface);
										} else
										{
											ArrayList<OSMSurface> surfaceList = new ArrayList<OSMSurface>();
											surfaceList.add(osmSurface);
											internalSurfaceMap.put(ifcWall,
													surfaceList);
										}
									} else
									{
										System.err
												.println("Error: invalid value of isExternal!!");
									}
									break;
								}
							}
							break;
						}
					}
				}

				for (OSMPoint osmPoint : spaceBoundaryPointList)
				{
					osmSurface.addOSMPoint(osmPoint);
					allOSMPoints.add(osmPoint);
				}
				osmSpace.addSurface(osmSurface);

				wallOSMSurfaceMap.put(ifcWall, osmSurface);
				List<OSMSubSurface> unlinkedOSMSubSurfaceList = unLinkedOSMSubSurfacesMap
						.get(ifcWall);
				if (unlinkedOSMSubSurfaceList != null
						&& unlinkedOSMSubSurfaceList.size() > 0)
				{
					for (OSMSubSurface osmSubSurface : unlinkedOSMSubSurfaceList)
					{
						osmSubSurface.setOSMSurface(osmSurface);
						osmSurface.addSubSurface(osmSubSurface);
					}
					unLinkedOSMSubSurfacesMap.remove(ifcWall);
				}
			} else
			{ // else for if(isGeometrySolved)
				osmSpace.setContainsUnsolvedSurface(true);
				System.err
						.println("Error: unparsed geometry representation of wall!");
			}
		}

		// Slab, surface
		else if (ifcElement instanceof IfcSlab)
		{
			if (isGeometrySolved)
			{
				IfcSlab ifcSlab = (IfcSlab) ifcElement;
				OSMSurface osmSurface = new OSMSurface();
				osmSurface.setSurfaceName("su-" + (++surfaceNum));
				// osmSurface.setSurfaceName("su-" + (++surfaceNum) + "-" +
				// ifcElement.getName()); //this line should be replaced by the
				// line above!!!!
				osmSurface.setOSMSpace(osmSpace);
				osmSurface.setOutsideBoundaryCondition("Ground"); // TODO:
																	// Chong!!!!!
																	// confirmed
																	// with
																	// mechanical
																	// engineers,
																	// Ke or
																	// OpenStudio
																	// teams
				osmSurface.setSunExposure("NoSun");
				osmSurface.setWindExposure("NoWind");

				if (ifcSlab.getPredefinedType().getName().equals("FLOOR"))
				{
					osmSurface.setTypeName("Floor");
				} else if (ifcSlab.getPredefinedType().getName().equals("ROOF"))
				{
					osmSurface.setTypeName("RoofCeiling");
				} else
				{
					System.err.println("Error: Unknown slab type!");
				}

				for (OSMPoint osmPoint : spaceBoundaryPointList)
				{
					osmSurface.addOSMPoint(osmPoint);
					allOSMPoints.add(osmPoint);
				}
				osmSpace.addSurface(osmSurface);
			} else
			{ // else for if(isGeometrySolved)
				osmSpace.setContainsUnsolvedSurface(true);
				System.err
						.println("Error: unparsed geometry representation of slab!");
			}
		}

		// Roof, Surface
		else if (ifcElement instanceof IfcRoof)
		{
			if (isGeometrySolved)
			{
				IfcRoof ifcRoof = (IfcRoof) ifcElement;
				OSMSurface osmSurface = new OSMSurface();
				osmSurface.setSurfaceName("su-" + (++surfaceNum));
				// osmSurface.setSurfaceName("su-" + (++surfaceNum) + "-" +
				// ifcElement.getName()); //this line should be replaced by the
				// line above!!!!
				osmSurface.setTypeName("RoofCeiling");
				osmSurface.setOSMSpace(osmSpace);

				for (int j = 0; j < ifcRoof.getIsDefinedBy().size(); j++)
				{
					IfcRelDefines ifcRelDefines = ifcRoof.getIsDefinedBy().get(j);
					if(ifcRelDefines instanceof IfcRelDefinesByProperties)
					{
						IfcRelDefinesByProperties ifcRelDefinesByProperties = (IfcRelDefinesByProperties) ifcRelDefines;
						if (ifcRelDefinesByProperties
								.getRelatingPropertyDefinition().getName()
								.equals("Pset_RoofCommon"))
						{
							List<IfcProperty> ifcPropertySetList = ((IfcPropertySet) ifcRelDefinesByProperties
									.getRelatingPropertyDefinition())
									.getHasProperties();
							for (int k = 0; k < ifcPropertySetList.size(); k++)
							{
								IfcPropertySingleValue ifcPropertySingleValue = (IfcPropertySingleValue) ifcPropertySetList
										.get(k);
								if (ifcPropertySingleValue.getName().equals(
										"IsExternal"))
								{
									Tristate isExternal = ((IfcBoolean) ifcPropertySingleValue
											.getNominalValue()).getWrappedValue();
									if (isExternal.getValue() == 0)
									{ // true
										osmSurface
												.setOutsideBoundaryCondition("Outdoors");
										osmSurface.setSunExposure("SunExposed");
										osmSurface.setWindExposure("WindExposed");
									} else if (isExternal.getValue() == 1)
									{ // false
										// //TODO:
										// Chong!!!!!
										// confirmed
										// with
										// mechanical
										// engineers,
										// Ke
										// or
										// OpenStudio
										// teams
										osmSurface
												.setOutsideBoundaryCondition("Surface");
										osmSurface.setSunExposure("NoSun");
										osmSurface.setWindExposure("NoWind");
									} else
									{
										System.err
												.println("Error: invalid value of isExternal!!");
									}
									break;
								}
							}
							break;
						}
					}
				}

				for (OSMPoint osmPoint : spaceBoundaryPointList)
				{
					osmSurface.addOSMPoint(osmPoint);
					allOSMPoints.add(osmPoint);
				}
				osmSpace.addSurface(osmSurface);
			} else
			{ // else for if(isGeometrySolved)
				osmSpace.setContainsUnsolvedSurface(true);
				System.err
						.println("Error: unparsed geometry representation of roof!");
			}
		}

		// Window, subsurface
		else if (ifcElement instanceof IfcWindow)
		{
			if (isGeometrySolved)
			{
				IfcWindow ifcWindow = (IfcWindow) ifcElement;
				OSMSubSurface osmSubSurface = new OSMSubSurface();
				osmSubSurface.setSubSurfaceName(ifcElement.getName());
				osmSubSurface.setTypeName("FixedWindow");
				List<IfcRelFillsElement> ifcRelFillsElementList = ifcWindow
						.getFillsVoids();
				if (ifcRelFillsElementList.size() > 0)
				{
					IfcRelFillsElement ifcRelFillsElement = ifcRelFillsElementList
							.get(0);
					IfcElement ifcRelatingBuildingElement = ifcRelFillsElement
							.getRelatingOpeningElement().getVoidsElements()
							.getRelatingBuildingElement();
					for (OSMPoint osmPoint : spaceBoundaryPointList)
					{
						osmSubSurface.addOSMPoint(osmPoint);
						allOSMPoints.add(osmPoint);
					}
					// TODO: Chong!!!!! confirmed with mechanical engineers, Ke
					// or OpenStudio teams
					OSMSurface osmSurface = wallOSMSurfaceMap
							.get(ifcRelatingBuildingElement); // add (IfcWall)
																// cast before
																// ifcRelatingBuildingElement
					if (osmSurface != null)
					{
						osmSubSurface.setOSMSurface(osmSurface);
						osmSurface.addSubSurface(osmSubSurface);
					} else
					{
						List<OSMSubSurface> unlinkedOSMSubSurfaceList = unLinkedOSMSubSurfacesMap
								.get(ifcRelatingBuildingElement);
						if (unlinkedOSMSubSurfaceList == null)
						{
							unlinkedOSMSubSurfaceList = new ArrayList<OSMSubSurface>();
						}
						unlinkedOSMSubSurfaceList.add(osmSubSurface);
						unLinkedOSMSubSurfacesMap.put(
								(IfcWall) ifcRelatingBuildingElement,
								unlinkedOSMSubSurfaceList);
					}
				}
			} else
			{ // else for if(isGeometrySolved)
				System.err
						.println("Error: unparsed geometry representation of window!");
			}
		}

		// Door subsurface
		else if (ifcElement instanceof IfcDoor)
		{
			if (isGeometrySolved)
			{
				IfcDoor ifcDoor = (IfcDoor) ifcElement;
				OSMSubSurface osmSubSurface = new OSMSubSurface();
				osmSubSurface.setSubSurfaceName(ifcElement.getName());
				osmSubSurface.setTypeName("Door");
				List<IfcRelFillsElement> ifcRelFillsElementList = ifcDoor
						.getFillsVoids();
				if (ifcRelFillsElementList.size() > 0)
				{
					IfcRelFillsElement ifcRelFillsElement = ifcRelFillsElementList
							.get(0);
					IfcElement ifcRelatingBuildingElement = ifcRelFillsElement
							.getRelatingOpeningElement().getVoidsElements()
							.getRelatingBuildingElement();
					for (OSMPoint osmPoint : spaceBoundaryPointList)
					{
						osmSubSurface.addOSMPoint(osmPoint);
						allOSMPoints.add(osmPoint);
					}
					// TODO: Chong!!!!! confirmed with mechanical engineers, Ke
					// or OpenStudio teams
					OSMSurface osmSurface = wallOSMSurfaceMap
							.get(ifcRelatingBuildingElement); // add (IfcWall)
																// cast before
																// ifcRelatingBuildingElement
					if (osmSurface != null)
					{
						osmSubSurface.setOSMSurface(osmSurface);
						osmSurface.addSubSurface(osmSubSurface);
					} else
					{
						List<OSMSubSurface> unlinkedOSMSubSurfaceList = unLinkedOSMSubSurfacesMap
								.get(ifcRelatingBuildingElement);
						if (unlinkedOSMSubSurfaceList == null)
						{
							unlinkedOSMSubSurfaceList = new ArrayList<OSMSubSurface>();
						}
						unlinkedOSMSubSurfaceList.add(osmSubSurface);
						unLinkedOSMSubSurfacesMap.put(
								(IfcWall) ifcRelatingBuildingElement,
								unlinkedOSMSubSurfaceList);
					}
				}
			} else
			{ // else for if(isGeometrySolved)
				System.err
						.println("Error: unparsed geometry representation of door!");
			}
		} else
		{ // else for ifcElement instanceof IfcWall, IfcSlab, IfcRoof,
			// IfcWindow, IfcDoor
			System.err.println("Error: unparsed element type!");
		}
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
	 * relPoint: The point to be transformed
	 * 
	 * */
	private OSMPoint coordinateSys3DTrans(OSMPoint relOSMPoint)
	{
		IfcProduct ifcProduct = (IfcProduct) currentInstance;
		IfcLocalPlacement ifcLocalPlacement = (IfcLocalPlacement) ifcProduct
				.getObjectPlacement();
		while (ifcLocalPlacement.getPlacementRelTo() != null)
		{
			relOSMPoint = coordinate3DTrans(relOSMPoint,
					(IfcAxis2Placement3D) ifcLocalPlacement
							.getRelativePlacement());
			ifcLocalPlacement = (IfcLocalPlacement) ifcLocalPlacement
					.getPlacementRelTo();
		}
		return coordinate3DTrans(relOSMPoint,
				(IfcAxis2Placement3D) ifcLocalPlacement.getRelativePlacement());
	}

	/**
	 * relPoint: The point to be transformed ifcRelativePlacement: relative
	 * offset if xDirection is empty, assign it to default value relOrigin: the
	 * relative coordinate system's origin point absPoint: the transformed point
	 * */
	private OSMPoint coordinate3DTrans(OSMPoint relOSMPoint,
			IfcAxis2Placement3D ifcRelativePlacement)
	{
		OSMPoint absOSMPoint = new OSMPoint();

		List<Double> relOrigin = ifcRelativePlacement.getLocation()
				.getCoordinates(); // new origin point

		List<Double> xDirection = new ArrayList<Double>(); // new x Axis
		if (ifcRelativePlacement.getRefDirection() == null)
		{
			xDirection.add(1.0);
			xDirection.add(0.0);
			xDirection.add(0.0);
		} else
		{
			xDirection = ifcRelativePlacement.getRefDirection()
					.getDirectionRatios();
		}
		List<Double> zDirection = new ArrayList<Double>(); // new z Axis
		if (ifcRelativePlacement.getAxis() == null)
		{
			zDirection.add(0.0);
			zDirection.add(0.0);
			zDirection.add(1.0);
		} else
		{
			zDirection = ifcRelativePlacement.getAxis().getDirectionRatios();
		}

		Double ox = relOrigin.get(0);
		Double oy = relOrigin.get(1);
		Double oz = relOrigin.get(2);

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
		absOSMPoint.setX(x);

		Double y = a1 * rxy / rxLength + a2 * ryy / ryLength + a3 * rzy
				/ rzLength + oy;
		absOSMPoint.setY(y);

		Double z = a1 * rxz / rxLength + a2 * ryz / ryLength + a3 * rzz
				/ rzLength + oz;
		absOSMPoint.setZ(z);

		return absOSMPoint;
	}

	class OSMPoint
	{
		private Double	x;
		private Double	y;
		private Double	z;

		public OSMPoint()
		{
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
	}

	class OSMSubSurface
	{
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
	}

	class OSMSpace
	{
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
	}

}
