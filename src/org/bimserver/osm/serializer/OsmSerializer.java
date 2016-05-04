package org.bimserver.osm.serializer;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bimserver.emf.IfcModelInterface;
import org.bimserver.models.ifc2x3tc1.IfcArbitraryOpenProfileDef;
import org.bimserver.models.ifc2x3tc1.IfcAxis2Placement;
import org.bimserver.models.ifc2x3tc1.IfcAxis2Placement3D;
import org.bimserver.models.ifc2x3tc1.IfcBoundedCurve;
import org.bimserver.models.ifc2x3tc1.IfcBuildingStorey;
import org.bimserver.models.ifc2x3tc1.IfcCartesianPoint;
import org.bimserver.models.ifc2x3tc1.IfcCompositeCurve;
import org.bimserver.models.ifc2x3tc1.IfcCompositeCurveSegment;
import org.bimserver.models.ifc2x3tc1.IfcConnectionGeometry;
import org.bimserver.models.ifc2x3tc1.IfcConnectionSurfaceGeometry;
import org.bimserver.models.ifc2x3tc1.IfcConversionBasedUnit;
import org.bimserver.models.ifc2x3tc1.IfcCurve;
import org.bimserver.models.ifc2x3tc1.IfcCurveBoundedPlane;
import org.bimserver.models.ifc2x3tc1.IfcDerivedUnit;
import org.bimserver.models.ifc2x3tc1.IfcDerivedUnitElement;
import org.bimserver.models.ifc2x3tc1.IfcDirection;
import org.bimserver.models.ifc2x3tc1.IfcDoor;
import org.bimserver.models.ifc2x3tc1.IfcDoorStyle;
import org.bimserver.models.ifc2x3tc1.IfcElement;
import org.bimserver.models.ifc2x3tc1.IfcFaceBasedSurfaceModel;
import org.bimserver.models.ifc2x3tc1.IfcFlowTerminal;
import org.bimserver.models.ifc2x3tc1.IfcGroup;
import org.bimserver.models.ifc2x3tc1.IfcInteger;
import org.bimserver.models.ifc2x3tc1.IfcLengthMeasure;
import org.bimserver.models.ifc2x3tc1.IfcLightFixtureType;
import org.bimserver.models.ifc2x3tc1.IfcLocalPlacement;
import org.bimserver.models.ifc2x3tc1.IfcMaterial;
import org.bimserver.models.ifc2x3tc1.IfcMaterialLayer;
import org.bimserver.models.ifc2x3tc1.IfcMaterialLayerSet;
import org.bimserver.models.ifc2x3tc1.IfcMaterialLayerSetUsage;
import org.bimserver.models.ifc2x3tc1.IfcMaterialSelect;
import org.bimserver.models.ifc2x3tc1.IfcMeasureWithUnit;
import org.bimserver.models.ifc2x3tc1.IfcNamedUnit;
import org.bimserver.models.ifc2x3tc1.IfcObject;
import org.bimserver.models.ifc2x3tc1.IfcObjectDefinition;
import org.bimserver.models.ifc2x3tc1.IfcObjectPlacement;
import org.bimserver.models.ifc2x3tc1.IfcPolyline;
import org.bimserver.models.ifc2x3tc1.IfcProduct;
import org.bimserver.models.ifc2x3tc1.IfcProfileDef;
import org.bimserver.models.ifc2x3tc1.IfcProperty;
import org.bimserver.models.ifc2x3tc1.IfcPropertySet;
import org.bimserver.models.ifc2x3tc1.IfcPropertySetDefinition;
import org.bimserver.models.ifc2x3tc1.IfcPropertySingleValue;
import org.bimserver.models.ifc2x3tc1.IfcRatioMeasure;
import org.bimserver.models.ifc2x3tc1.IfcReal;
import org.bimserver.models.ifc2x3tc1.IfcRelAggregates;
import org.bimserver.models.ifc2x3tc1.IfcRelAssignsToGroup;
import org.bimserver.models.ifc2x3tc1.IfcRelAssociatesMaterial;
import org.bimserver.models.ifc2x3tc1.IfcRelContainedInSpatialStructure;
import org.bimserver.models.ifc2x3tc1.IfcRelDefines;
import org.bimserver.models.ifc2x3tc1.IfcRelDefinesByProperties;
import org.bimserver.models.ifc2x3tc1.IfcRelDefinesByType;
import org.bimserver.models.ifc2x3tc1.IfcRelFillsElement;
import org.bimserver.models.ifc2x3tc1.IfcRelSpaceBoundary;
import org.bimserver.models.ifc2x3tc1.IfcRoof;
import org.bimserver.models.ifc2x3tc1.IfcRoot;
import org.bimserver.models.ifc2x3tc1.IfcSIUnit;
import org.bimserver.models.ifc2x3tc1.IfcSlab;
import org.bimserver.models.ifc2x3tc1.IfcSpace;
import org.bimserver.models.ifc2x3tc1.IfcSpatialStructureElement;
import org.bimserver.models.ifc2x3tc1.IfcSurfaceOfLinearExtrusion;
import org.bimserver.models.ifc2x3tc1.IfcSurfaceOrFaceSurface;
import org.bimserver.models.ifc2x3tc1.IfcText;
import org.bimserver.models.ifc2x3tc1.IfcTypeObject;
import org.bimserver.models.ifc2x3tc1.IfcUnit;
import org.bimserver.models.ifc2x3tc1.IfcUnitAssignment;
import org.bimserver.models.ifc2x3tc1.IfcValue;
import org.bimserver.models.ifc2x3tc1.IfcWall;
import org.bimserver.models.ifc2x3tc1.IfcWindow;
import org.bimserver.models.ifc2x3tc1.IfcWindowStyle;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.renderengine.RenderEnginePlugin;
import org.bimserver.plugins.serializers.EmfSerializer;
import org.bimserver.plugins.serializers.ProjectInfo;
import org.bimserver.plugins.serializers.SerializerException;
import org.bimserver.utils.UTF8PrintWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsmSerializer extends EmfSerializer {
	private static final double maxWallThickness = 0.8;  //after unit conversion, meter.
	private static final Logger LOGGER = LoggerFactory.getLogger(OsmSerializer.class);

	private UTF8PrintWriter						out;

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
	private List<OsmPoint>						allOsmPoints				= new ArrayList<OsmPoint>();

	/**
	 * All spaces, incrementally added from IFCSpace. Used to generate the space
	 * name in sequence
	 */
	private List<OsmSpace>						allSpaces					= new ArrayList<OsmSpace>();

	/**
	 * A 1 to 2 mapping from IfcElements(Wall, Slab) to OsmSurface to store duplicated internal
	 * walls. Used to determine outside boundaries.
	 */
	private HashMap<IfcElement, LinkedList<OsmSurface>>	internalWallMap			= new HashMap<IfcElement, LinkedList<OsmSurface>>();

	/**
	 */
	private int									surfaceNum					= 0;

	private int									subSurfaceNum				= 0;
	private double                              unit                        = 1.0;

	@Override
	public void init(IfcModelInterface model, ProjectInfo projectInfo, PluginManager pluginManager, RenderEnginePlugin renderEnginePlugin, boolean normalizeOids) throws SerializerException {
		super.init(model, projectInfo, pluginManager, renderEnginePlugin, normalizeOids);
		// Calculate the unit conversion scale
		double scale = calUnitConvScale(model);
		setDefaultUnit(model);
		unit = scale;
		//Create Element-Material map
		mapElementMaterial(model);
		extractWindowsInformation(model);
		extractBuildingStories(model);

		List<IfcSpace> ifcSpaceList = model.getAll(IfcSpace.class);
		for (IfcSpace ifcSpace : ifcSpaceList) {
			extractSpaces(ifcSpace);
		}
		lightFixture(model);
		

		// Convert the units
		transformUnits(scale);
		// Add linkage information to internal Walls
		addLinkageInformation();
	}
	
	



	private HashMap<IfcElement, IfcMaterialSelect> elementMaterialMap = new HashMap<IfcElement, IfcMaterialSelect>();
	public void mapElementMaterial(IfcModelInterface model) {
		for (IfcRelAssociatesMaterial ifcRelAssociatesMaterial : model.getAll(IfcRelAssociatesMaterial.class)) {
			IfcMaterialSelect ifcMaterialSelect = ifcRelAssociatesMaterial.getRelatingMaterial();
			for (IfcRoot ifcRoot : ifcRelAssociatesMaterial.getRelatedObjects()) {
				if (ifcRoot instanceof IfcElement) {
					IfcElement ifcElement = (IfcElement) ifcRoot;
					elementMaterialMap.put(ifcElement, ifcMaterialSelect);
				}
			}
		}
	}
	
	HashMap<Long, OsmConstruction> windowOrDoorTypeMap = new HashMap<Long, OsmConstruction>();
	HashMap<Long, String> windowOrDoorMap = new HashMap<Long, String>();
	List<OsmConstruction> windowOrDoorConstructionMap = new ArrayList<OsmConstruction>();
	List<OsmWindowMaterialSimpleGlazingSystem> windowOrDoorType = new ArrayList<OsmWindowMaterialSimpleGlazingSystem>();
	public void extractWindowsInformation(IfcModelInterface model) {
		for (IfcRelDefinesByType ifcRelDefinesByType : model.getAll(IfcRelDefinesByType.class)) {
			IfcTypeObject ifcTypeObject = ifcRelDefinesByType.getRelatingType();
			if (ifcTypeObject instanceof IfcWindowStyle || ifcTypeObject instanceof IfcDoorStyle) {
				Long oid = ifcTypeObject.getOid();
				String typeHandle = "";
				if (windowOrDoorTypeMap.containsKey(oid)) {
					typeHandle = windowOrDoorTypeMap.get(oid).getHandle();
				} else {
					String name = ifcTypeObject.getName().replace(',', '_');
					double uFactor = 0.0;
					double solarHeatGainCoefficient = 0.0;
					double visibleTransmittance = 0.0;
					if (ifcTypeObject.isSetHasPropertySets()) {
						for (IfcPropertySetDefinition ifcPropertySetDefinition : ifcTypeObject.getHasPropertySets()) {
							if (ifcPropertySetDefinition instanceof IfcPropertySet) {
								IfcPropertySet ifcPropertySet = (IfcPropertySet) ifcPropertySetDefinition;
								for (IfcProperty ifcProperty : ifcPropertySet.getHasProperties()) {
									if (ifcProperty instanceof IfcPropertySingleValue) {
										IfcPropertySingleValue ifcPropertySingleValue = (IfcPropertySingleValue) ifcProperty;
										String propertyName = ifcPropertySingleValue.getName();
										if (propertyName.equals("Heat Transfer Coefficient (U)")) {
											if (ifcPropertySingleValue.getNominalValue() instanceof IfcReal) {
												IfcReal ifcReal = (IfcReal) ifcPropertySingleValue.getNominalValue();
												uFactor = ifcReal.getWrappedValue() * getDefaultUnit("THERMALTRANSMITTANCEUNIT");
												if (nearlyEqual(uFactor, 0)) {
													uFactor = 0.001;
												} else if(nearlyEqual(uFactor, 7)) {
													uFactor = 6.999;
												}
											}
										} else if (propertyName.equals("Solar Heat Gain Coefficient")) {
											if (ifcPropertySingleValue.getNominalValue() instanceof IfcReal) {
												IfcReal ifcReal = (IfcReal) ifcPropertySingleValue.getNominalValue();
												solarHeatGainCoefficient = ifcReal.getWrappedValue();
												if (nearlyEqual(solarHeatGainCoefficient, 0)) {
													solarHeatGainCoefficient = 0.001;
												} else if(nearlyEqual(solarHeatGainCoefficient, 1)) {
													solarHeatGainCoefficient = 0.999;
												}
											}
										} else if (propertyName.equals("Visual Light Transmittance")) {
											if (ifcPropertySingleValue.getNominalValue() instanceof IfcReal) {
												IfcReal ifcReal = (IfcReal) ifcPropertySingleValue.getNominalValue();
												visibleTransmittance = ifcReal.getWrappedValue();
												if (nearlyEqual(visibleTransmittance, 0)) {
													visibleTransmittance = 0.001;
												} else if(nearlyEqual(visibleTransmittance, 1)) {
													visibleTransmittance = 0.999;
												}
											}
										}
									}
								}
							}
						}
					}

					OsmWindowMaterialSimpleGlazingSystem osmWindowMaterialSimpleGlazingSystem = new OsmWindowMaterialSimpleGlazingSystem(name + "_Material", uFactor, solarHeatGainCoefficient, visibleTransmittance);
					windowOrDoorType.add(osmWindowMaterialSimpleGlazingSystem);
					OsmConstruction osmConstruction = new OsmConstruction(name, "", new ArrayList<String>(Arrays.asList(osmWindowMaterialSimpleGlazingSystem.getHandle())));
					windowOrDoorTypeMap.put(oid, osmConstruction);
					windowOrDoorConstructionMap.add(osmConstruction);
					typeHandle = osmConstruction.getHandle();
				}

				for (IfcObject ifcObject: ifcRelDefinesByType.getRelatedObjects()) {
					if (ifcObject instanceof IfcWindow || ifcObject instanceof IfcDoor) {
						windowOrDoorMap.put(ifcObject.getOid(), typeHandle);
					}
				}
			}
		}
	}
	
	
	HashMap<Long, String> storeySpaceMap = new HashMap<Long, String>();
	List<OsmBuildingStory> storyMap = new ArrayList<OsmBuildingStory>();
	public void extractBuildingStories(IfcModelInterface model) {
		for (IfcRelAggregates ifcRelAggregates : model.getAll(IfcRelAggregates.class)) {
			IfcObjectDefinition ifcObjectDefinition =  ifcRelAggregates.getRelatingObject();
			if (ifcObjectDefinition instanceof IfcBuildingStorey) {
				IfcBuildingStorey ifcBuildingStorey = (IfcBuildingStorey) ifcObjectDefinition;
				String name = ifcBuildingStorey.getName();
				OsmBuildingStory osmBuildingStory = new OsmBuildingStory(name);
				storyMap.add(osmBuildingStory);
				String handle = osmBuildingStory.getHandle();
				for (IfcObjectDefinition object : ifcRelAggregates.getRelatedObjects()) {
					if (object instanceof IfcSpace) {
						IfcSpace ifcSpace = (IfcSpace) object;
						storeySpaceMap.put(ifcSpace.getOid(), handle);
					}
				}
			}
		}
	}
	
	HashMap<Long, OsmLuminaireDefinition> lightFixtureTypeMap = new HashMap<Long, OsmLuminaireDefinition>();
	HashMap<Long, String> groupMap = new HashMap<Long, String>();
	HashMap<Long, String> spatialMap = new HashMap<Long, String>();
	List<OsmLuminaire> lights = new ArrayList<OsmLuminaire>();
	public void lightFixture(IfcModelInterface model) {

		for (IfcRelAssignsToGroup ifcRelAssignsToGroup : model.getAll(IfcRelAssignsToGroup.class)) {
			IfcGroup ifcGroup = ifcRelAssignsToGroup.getRelatingGroup();
			String groupName = ifcGroup.getName();
			for (IfcObjectDefinition ifcObjectDefinition : ifcRelAssignsToGroup.getRelatedObjects()) {
				if (ifcObjectDefinition instanceof IfcFlowTerminal) {
					groupMap.put(ifcObjectDefinition.getOid(), groupName);
				}
			}
		}

		for (IfcRelContainedInSpatialStructure ifcRelContainedInSpatialStructure : model.getAll(IfcRelContainedInSpatialStructure.class)) {
			IfcSpatialStructureElement ifcSpatialStructureElement = ifcRelContainedInSpatialStructure.getRelatingStructure();

			String spaceId = "";
			if (ifcSpatialStructureElement instanceof IfcSpace && spaceMap.containsKey(ifcSpatialStructureElement.getOid())) {
				OsmSpace osmSpace = spaceMap.get(ifcSpatialStructureElement.getOid());
				spaceId = osmSpace.getUuid();
			} /*else {
				OsmSpace osmSpace = new OsmSpace();
				UUID uuid = UUID.randomUUID();
				osmSpace.setUuid(uuid.toString());
				osmSpace.setSpaceName("sp-" + (allSpaces.size() + 1) + "-Space");
				allSpaces.add(osmSpace);
				spaceMap.put(ifcSpatialStructureElement.getOid(), osmSpace);
				spaceId = uuid.toString();
			}*/

			for (IfcProduct ifcProduct : ifcRelContainedInSpatialStructure.getRelatedElements()) {
				if (ifcProduct instanceof IfcFlowTerminal) {
					spatialMap.put(ifcProduct.getOid(), spaceId);
				}
			}
		}
		
		for (IfcRelDefinesByType ifcRelDefinesByType : model.getAll(IfcRelDefinesByType.class)) {
			IfcTypeObject ifcTypeObject = ifcRelDefinesByType.getRelatingType();
			if(ifcTypeObject instanceof IfcLightFixtureType) {
				IfcLightFixtureType ifcLightFixtureType = (IfcLightFixtureType) ifcTypeObject;
				Long oid = ifcLightFixtureType.getOid();

				String lightFixtureTypeHandle = new String("");
				
				if (lightFixtureTypeMap.containsKey(oid)) {
					lightFixtureTypeHandle = lightFixtureTypeMap.get(oid).getHandle();
				} else {
					String name = ifcLightFixtureType.getName();
					
					String IESFilePath = new String("");
					for (IfcPropertySetDefinition ifcPropertySetDefinition : ifcLightFixtureType.getHasPropertySets()) {
						if (ifcPropertySetDefinition instanceof IfcPropertySet) {
							IfcPropertySet ifcPropertySet = (IfcPropertySet) ifcPropertySetDefinition;
							if (ifcPropertySet.getName().equals("Photometrics")) {
								for (IfcProperty ifcProperty : ifcPropertySet.getHasProperties()) {
									if (ifcProperty instanceof IfcPropertySingleValue) {
										IfcPropertySingleValue ifcPropertySingleValue = (IfcPropertySingleValue) ifcProperty;
										if (ifcPropertySingleValue.getName().equals("Photometric Web File")) {
											if (ifcPropertySingleValue.getNominalValue() instanceof IfcText) {
												IfcText ifcText = (IfcText) ifcPropertySingleValue.getNominalValue();
												IESFilePath = ifcText.getWrappedValue(); 
											}
										}
									}
								}
							}
						}
					}
					
					OsmLuminaireDefinition osmLuminaireDefinition = new OsmLuminaireDefinition(name, IESFilePath);
					lightFixtureTypeHandle = osmLuminaireDefinition.getHandle();
					lightFixtureTypeMap.put(oid, osmLuminaireDefinition);
				}

				for (IfcObject ifcObject : ifcRelDefinesByType.getRelatedObjects()) {
					if (ifcObject instanceof IfcFlowTerminal) {
						IfcFlowTerminal ifcFlowTerminal = (IfcFlowTerminal) ifcObject;
						String name = groupMap.getOrDefault(ifcFlowTerminal.getOid(), "0") + "_" + ifcFlowTerminal.getName();
						String luminaireDefinitionName = lightFixtureTypeHandle;
						String spaceName = spatialMap.getOrDefault(ifcFlowTerminal.getOid(), "0");
						
						OsmPoint point = new OsmPoint();
						coordinateSys3DTrans(point, ifcFlowTerminal.getObjectPlacement());
						allOsmPoints.add(point);
						lights.add(new OsmLuminaire(name, luminaireDefinitionName, spaceName, point));
					}
				}
			}
		}
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
		outputContent.append("1.8.0;                         !- Version Identifier\n\n");

		for (OsmSpace osmSpace : allSpaces) {
			outputContent.append(osmSpace.toString());

			for (OsmSurface osmSurface : osmSpace.getSurfaceList()) {
				outputContent.append(osmSurface.toString());

				for (OsmSubSurface osmSubSurface : osmSurface.getSubSurfaceList()) {
					outputContent.append(osmSubSurface.toString());
				}
			}
		}

		for(OsmConstruction construction : constructionMap.values()) {
			outputContent.append(construction.toString());
		}

		for(OsmMaterial material : materialMap.values()) {
			outputContent.append(material.toString());
		}

		for(OsmLuminaire light : lights) {
			outputContent.append(light.toString());
		}

		for(OsmLuminaireDefinition osmLuminaireDefinition : lightFixtureTypeMap.values()) {
			outputContent.append(osmLuminaireDefinition.toString());
		}

		for(OsmWindowMaterialSimpleGlazingSystem windowOrDoor : windowOrDoorType) {
			outputContent.append(windowOrDoor.toString());
		}

		for(OsmConstruction osmConstruction : windowOrDoorConstructionMap) {
			outputContent.append(osmConstruction.toString());
		}
		
		for(OsmMaterial osmMaterial : materialList) {
			outputContent.append(osmMaterial.toString());
		}
		
		for(OsmBuildingStory osmBuildingStory : storyMap) {
			outputContent.append(osmBuildingStory.toString());
		}
	}


	/**
	 * Analyze every IFCSpace and create OsmSpace accordingly
	 *
	 * @param ifcSpace
	 */

	HashMap<Long, OsmSpace> spaceMap = new HashMap<Long, OsmSpace>();
	private void extractSpaces(IfcSpace ifcSpace) {
		OsmSpace osmSpace = new OsmSpace();
		UUID uuid = UUID.randomUUID();
		osmSpace.setUuid(uuid.toString());
		osmSpace.setSpaceName("sp-" + (allSpaces.size() + 1) + "-Space");
		osmSpace.setBuildingStoryName(storeySpaceMap.getOrDefault(ifcSpace.getOid(), ""));

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

		allSpaces.add(osmSpace);
		spaceMap.put(ifcSpace.getOid(), osmSpace);

		/*
		if (!osmSpace.containsUnsolvedSurface()) {
			allSpaces.add(osmSpace);
		} else {
			surfaceNum -= osmSpace.getSurfaceList().size();
		}
		*/
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
	
	List<OsmMaterial> materialList = new ArrayList<OsmMaterial>();
	private void extractRelatedElement(IfcElement ifcElement, IfcConnectionGeometry ifcConnectionGeometry, IfcSpace ifcSpace, OsmSpace osmSpace) {

		// initiate the connecting point list
		List<OsmPoint> spaceBoundaryPointList = new ArrayList<OsmPoint>();

		// Extract the connection geometry
		//TODO: Bug: sometimes windows and doors are not contained in the original surfaces.
		//TODO: put extract space boundary inside the IFC Element List as there might be some differences in extracting windows and doors
		boolean isGeometrySolved = extractSpaceBoundary(ifcConnectionGeometry, spaceBoundaryPointList, ifcSpace);
		deleteDuplicatePoints(spaceBoundaryPointList);//after extracting boundary points, remove duplicates.
		// Extract Other Properties

		// Wall, surface  HashMap<IfcElement, IfcMaterialSelect> elementMaterialMap
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
				IfcMaterialSelect ifcMaterialSelect = elementMaterialMap.get(ifcElement);
				osmSurface.setConstructionName(materialInformation(ifcMaterialSelect));
				
				if (ifcWall.isSetHasAssociations()) {
					LOGGER.info("Has associations" + ifcElement.eClass().getName());
				}

				//add internal surface link information
				if (internalWallMap.containsKey(ifcWall)) {
					internalWallMap.get(ifcWall).add(osmSurface);
				} else {
					LinkedList<OsmSurface> surfaceList = new LinkedList<OsmSurface>();
					surfaceList.add(osmSurface);
					internalWallMap.put(ifcWall,surfaceList);
				}

				for (OsmPoint osmPoint : spaceBoundaryPointList) {
					osmSurface.addOsmPoint(osmPoint);
					allOsmPoints.add(osmPoint);
				}
				osmSpace.addSurface(osmSurface);

				//TODO: Lan: do you know what this chunk is for?
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
				IfcMaterialSelect ifcMaterialSelect = elementMaterialMap.get(ifcElement);
				osmSurface.setConstructionName(materialInformation(ifcMaterialSelect));

				if (ifcSlab.getPredefinedType().getName().equals("Roof")) {
					osmSurface.setTypeName("RoofCeiling");
					osmSurface.setSunExposure("SunExposed");
					osmSurface.setWindExposure("WindExposed");
				} else {//Floor or BaseSlab or Landing
					//if Floor, it could be a foundation floor or a slab between stories, it is then processed at the end in addlinkage
					//add internal surface link information
					if (internalWallMap.containsKey(ifcSlab)) {
						internalWallMap.get(ifcSlab).add(osmSurface);
					} else {
						LinkedList<OsmSurface> surfaceList = new LinkedList<OsmSurface>();
						surfaceList.add(osmSurface);
						internalWallMap.put(ifcSlab,surfaceList);
					}
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
				
				String name = ifcRoof.getName();
				String roughness = "";
				double conductivity = 0.0;
				double specificHeat = 0.0;
				double thickness = 0.0;
				

				for (IfcRelDefines ifcRelDefines : ifcRoof.getIsDefinedBy()) {
					if (ifcRelDefines instanceof IfcRelDefinesByProperties) {
						IfcRelDefinesByProperties ifcRelDefinesByProperties = (IfcRelDefinesByProperties) ifcRelDefines;
						IfcPropertySetDefinition ifcPropertySetDefinition = ifcRelDefinesByProperties.getRelatingPropertyDefinition();
						if (ifcPropertySetDefinition instanceof IfcPropertySet) {
							IfcPropertySet ifcPropertySet = (IfcPropertySet) ifcPropertySetDefinition;
							
							if (ifcPropertySet.getName().equals("Pset_RoofCommon")) {
								for (IfcProperty ifcProperty : ifcPropertySet.getHasProperties()) {
									if (ifcProperty instanceof IfcPropertySingleValue) {
										IfcPropertySingleValue ifcPropertySingleValue = (IfcPropertySingleValue) ifcProperty;
										if (ifcPropertySingleValue.getName().equals("IsExternal")) {
											osmSurface.setOutsideBoundaryCondition("Outdoors");
											osmSurface.setSunExposure("SunExposed");
											osmSurface.setWindExposure("WindExposed");
										}
									}
								}
							} else if (ifcPropertySet.getName().equals("Dimensions")) {
								for (IfcProperty ifcProperty : ifcPropertySet.getHasProperties()) {
									if (ifcProperty instanceof IfcPropertySingleValue) {
										IfcPropertySingleValue ifcPropertySingleValue = (IfcPropertySingleValue) ifcProperty;
										if (ifcPropertySingleValue.getName().equals("Thickness")) {
											if (ifcPropertySingleValue.getNominalValue() instanceof IfcLengthMeasure) {
												IfcLengthMeasure ifcLengthMeasure = (IfcLengthMeasure) ifcPropertySingleValue.getNominalValue();
												thickness = ifcLengthMeasure.getWrappedValue() * getDefaultUnit("LENGTHUNIT");
											}
										}
									}
								}
							} else if (ifcPropertySet.getName().equals("Analytical Properties")) {
								for (IfcProperty ifcProperty : ifcPropertySet.getHasProperties()) {
									if (ifcProperty instanceof IfcPropertySingleValue) {
										IfcPropertySingleValue ifcPropertySingleValue = (IfcPropertySingleValue) ifcProperty;
										if (ifcPropertySingleValue.getName().equals("Heat Transfer Coefficient (U)")) {
											if (ifcPropertySingleValue.getNominalValue() instanceof IfcReal) {
												IfcReal ifcReal = (IfcReal) ifcPropertySingleValue.getNominalValue();
												conductivity = ifcReal.getWrappedValue();
											}
										} else if (ifcPropertySingleValue.getName().equals("Roughness")) {
											if (ifcPropertySingleValue.getNominalValue() instanceof IfcInteger) {
												IfcInteger ifcInteger = (IfcInteger) ifcPropertySingleValue.getNominalValue();
												roughness = "";
											}
										} else if (ifcPropertySingleValue.getName().equals("Thermal mass")) {
											if (ifcPropertySingleValue.getNominalValue() instanceof IfcReal) {
												IfcReal ifcReal = (IfcReal) ifcPropertySingleValue.getNominalValue();
												specificHeat = ifcReal.getWrappedValue();
											}
										}
									}
								}
							}					
						}
					}
				}
				
				OsmMaterial osmMaterial = new OsmMaterial(name + "_Material", roughness, thickness, conductivity, 0, specificHeat);
				List<String> osmMaterialList = new ArrayList<String>();
				osmMaterialList.add(osmMaterial.getHandle());
				OsmConstruction osmConstruction = new OsmConstruction(name, "", osmMaterialList);
				osmSurface.setConstructionName(osmConstruction.getHandle());
				windowOrDoorConstructionMap.add(osmConstruction);
				materialList.add(osmMaterial);
				

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

				String constructionName = "";
				if(windowOrDoorMap.containsKey(ifcElement.getOid())) {
					constructionName = windowOrDoorMap.get(ifcElement.getOid());
				}
				osmSubSurface.setConstructionName(constructionName);

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
				
				String constructionName = "";
				if(windowOrDoorMap.containsKey(ifcElement.getOid())) {
					constructionName = windowOrDoorMap.get(ifcElement.getOid());
				}
				osmSubSurface.setConstructionName(constructionName);
				
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





	HashMap<Long, OsmConstruction> constructionMap = new HashMap<Long, OsmConstruction>();
	HashMap<Long, OsmMaterial>     materialMap     = new HashMap<Long, OsmMaterial>();
	public String materialInformation(IfcMaterialSelect ifcMaterialSelect) {
		if (ifcMaterialSelect instanceof IfcMaterialLayerSet) {
			IfcMaterialLayerSet ifcMaterialLayerSet = (IfcMaterialLayerSet) ifcMaterialSelect;
			long coid = ifcMaterialLayerSet.getOid();
			if(constructionMap.containsKey(coid)) {
				return constructionMap.get(coid).getHandle();
			}

			String layerSetName = ifcMaterialLayerSet.getLayerSetName().replace(',', '_');
			List<IfcMaterialLayer> ifcMaterialLayers = ifcMaterialLayerSet.getMaterialLayers();
			List<String> osmMaterialHandle = new ArrayList<String>();

			for (int i = 0; i < ifcMaterialLayers.size(); i++) {
				IfcMaterialLayer ifcMaterialLayer = ifcMaterialLayers.get(i);
				long moid = ifcMaterialLayer.getOid();
				if (materialMap.containsKey(moid)) {
					OsmMaterial osmMaterial = materialMap.get(moid);
					osmMaterialHandle.add(osmMaterial.getHandle());
				} else {
					double layerThickness = ifcMaterialLayer.getLayerThickness() * unit;
					IfcMaterial ifcMaterial = ifcMaterialLayer.getMaterial();
					String materialName = ifcMaterial.getName().replace(',', '_');
					OsmMaterial osmMaterial = new OsmMaterial(materialName + "_" + layerSetName, "MediumRough", layerThickness, 123, 123, 123);
					osmMaterialHandle.add(osmMaterial.getHandle());
					materialMap.put(moid, osmMaterial);
				}
			}
			OsmConstruction osmConstruction = new OsmConstruction(layerSetName, "", osmMaterialHandle);
			constructionMap.put(coid, osmConstruction);
			return osmConstruction.getHandle();
		} else if (ifcMaterialSelect instanceof IfcMaterialLayerSetUsage) {
			IfcMaterialLayerSetUsage ifcMaterialLayerSetUsage = (IfcMaterialLayerSetUsage) ifcMaterialSelect;
			IfcMaterialLayerSet ifcMaterialLayerSet =  ifcMaterialLayerSetUsage.getForLayerSet();
			return materialInformation(ifcMaterialLayerSet);
		} else {
			return "False";
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
					coordinateSys3DTrans(osmPoint, ifcSpace.getObjectPlacement());
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
				coordinateSys3DTrans(osmPoint, ifcSpace.getObjectPlacement());
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
						coordinateSys3DTrans(osmPoint, ifcSpace.getObjectPlacement());
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
	private void coordinateSys3DTrans(OsmPoint relOsmPoint, IfcObjectPlacement ifcObjectPlacement) {
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
	 * Miscellaneous Functions
	 * */

	/**
	 * For internal wall, we link the duplicated walls to each other by
	 * outsideboundaryConditionObject
	 */
	private void addLinkageInformation() {
		for (IfcElement ifcElement:internalWallMap.keySet()) {
			LinkedList<OsmSurface> surfaceList = internalWallMap.get(ifcElement);
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
				if(closestSurfaceIndex>=0) {//found a matched surface

					OsmSurface secondSurface = surfaceList.remove(closestSurfaceIndex);
					firstSurface.setOutsideBoundaryCondition("Surface");
					firstSurface.setOutsideBoundaryConditionObject(secondSurface.getSurfaceName());
					firstSurface.setSunExposure("NoSun");
					firstSurface.setWindExposure("NoWind");
					secondSurface.setOutsideBoundaryCondition("Surface");
					secondSurface.setOutsideBoundaryConditionObject(firstSurface.getSurfaceName());
					secondSurface.setSunExposure("NoSun");
					secondSurface.setWindExposure("NoWind");

					if (ifcElement instanceof IfcSlab){ //slab
						//set type, set exposure, set outside boundary
						OsmPoint secondCenter = computeSurfaceCenter(secondSurface);
						if (firstCenter.getZ() > secondCenter.getZ())
						{
							firstSurface.setTypeName("Floor");
							secondSurface.setTypeName("RoofCeiling");

						} else {
							firstSurface.setTypeName("RoofCeiling");
							secondSurface.setTypeName("Floor");
						}
					}
				} else {
					if (ifcElement instanceof IfcWall)
					{
						firstSurface.setOutsideBoundaryCondition("Outdoors");
						firstSurface.setSunExposure("SunExposed");
						firstSurface.setWindExposure("WindExposed");
					}
					else{// IfcSlab, only slab with a floor type will be added to the surfaceMap.
						firstSurface.setTypeName("Floor");
						firstSurface.setOutsideBoundaryCondition("Ground");
						firstSurface.setSunExposure("NoSun");
						firstSurface.setWindExposure("NoWind");
					}
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

	private void getPointsOfIfcPolyline(IfcPolyline ifcPolyline, List<OsmPoint> osmPointList) {
		List<IfcCartesianPoint> ifcCartesianPointList = ifcPolyline.getPoints();
		for (IfcCartesianPoint ifcCartesianPoint : ifcCartesianPointList) {
			List<Double> point = ifcCartesianPoint.getCoordinates();

			if (ifcCartesianPoint.isSetDim()) {
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
	 * Remove the duplicated points from the list. use OsmPoint.equals to judge
	 * @param point
	 */

	private void deleteDuplicatePoints(List<OsmPoint> points) {
		HashSet<OsmPoint> set = new HashSet<OsmPoint>();

		for (int i = 0; i < points.size(); i++) {
			if (set.contains(points.get(i))) {
				points.remove(i);
			} else {
				set.add(points.get(i));
			}
		}
	}



	private double calUnitConvScale(IfcModelInterface model) {
		double scale = 1.0;
		List<IfcUnitAssignment> ifcUnitAssignmentList =  model.getAll(IfcUnitAssignment.class);
		if (!ifcUnitAssignmentList.isEmpty()) {
			for (IfcUnitAssignment ifcUnitAssignment : ifcUnitAssignmentList) {
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
		}
		return scale;
	}
	
	HashMap<String, Double> unitMap = new HashMap<String, Double>();
	private void setDefaultUnit(IfcModelInterface model) {
		List<IfcUnitAssignment> ifcUnitAssignmentList = model.getAll(IfcUnitAssignment.class);
		if (!ifcUnitAssignmentList.isEmpty()) {
			for (IfcUnitAssignment ifcUnitAssignment : ifcUnitAssignmentList) {
				List<IfcUnit> ifcUnitList = ifcUnitAssignment.getUnits();
				for (IfcUnit ifcUnit : ifcUnitList) {			
					if (ifcUnit instanceof IfcNamedUnit) {
						IfcNamedUnit ifcNamedUnit = (IfcNamedUnit) ifcUnit;
						unitMap.put(ifcNamedUnit.getUnitType().getLiteral(), convertUnit(ifcNamedUnit));
					} else if (ifcUnit instanceof IfcDerivedUnit) {
						IfcDerivedUnit ifcDerivedUnit = (IfcDerivedUnit) ifcUnit;
						if (!ifcDerivedUnit.getUnitType().getLiteral().equals("USERDEFINED")) {
							unitMap.put(ifcDerivedUnit.getUnitType().getLiteral(), convertUnit(ifcDerivedUnit));
						}
					} else if (ifcUnit instanceof IfcSIUnit) {
						IfcSIUnit ifcSIUnit = (IfcSIUnit) ifcUnit;
						unitMap.put(ifcSIUnit.getUnitType().getLiteral(), 1.0);
					}
				}
			}
		}
	}
	
	
	private double getDefaultUnit(String unitName) {
		return unitMap.getOrDefault(unitName, 1.0);
	}
	
	private double convertUnit(IfcUnit ifcUnit) {
		double ratio = 1.0;
		if (ifcUnit instanceof IfcConversionBasedUnit) {
			ratio = getUnitRatio((IfcNamedUnit) ifcUnit);
		} else if (ifcUnit instanceof IfcDerivedUnit) {
			IfcDerivedUnit ifcDerivedUnit = (IfcDerivedUnit) ifcUnit;
			List<IfcDerivedUnitElement> ifcDerivedUnitElements = ifcDerivedUnit.getElements();
			for (IfcDerivedUnitElement ifcDerivedUnitElement : ifcDerivedUnitElements) {
				IfcNamedUnit ifcNamedUnit = ifcDerivedUnitElement.getUnit();
				int exponent = ifcDerivedUnitElement.getExponent();
			    ratio *= Math.pow(getUnitRatio(ifcNamedUnit), exponent);
			}
		}
		return ratio;
	}
	
	private double getUnitRatio(IfcNamedUnit ifcNamedUnit) {
		double ratio = 1.0;
		if (ifcNamedUnit instanceof IfcConversionBasedUnit) {
			IfcConversionBasedUnit ifcConversionBasedUnit = (IfcConversionBasedUnit) ifcNamedUnit;
			IfcMeasureWithUnit ifcMeasureWithUnit = ifcConversionBasedUnit.getConversionFactor();
			IfcValue ifcValue = ifcMeasureWithUnit.getValueComponent();
			if (ifcValue instanceof IfcRatioMeasure) {
				ratio = ((IfcRatioMeasure) ifcValue).getWrappedValue();
			}
		}
		return ratio;		
	}
	

	private void transformUnits(double scale) {
		for (OsmPoint osmPoint : allOsmPoints) {
			osmPoint.setX(osmPoint.getX() * scale);
			osmPoint.setY(osmPoint.getY() * scale);
			osmPoint.setZ(osmPoint.getZ() * scale);
		}
	}

	private boolean nearlyEqual(double a, double b, double epsilon) {
        final double absA = Math.abs(a);
        final double absB = Math.abs(b);
        final double diff = Math.abs(a - b);

        if (a == b) {
            return true;
        } else if (a == 0 || b == 0 || diff < Double.MIN_NORMAL) {
            return diff < epsilon * Double.MIN_NORMAL;
        } else {
            return diff < epsilon * Math.min(absA, absB);
        }
    }

    private boolean nearlyEqual(double a, double b) {
        return nearlyEqual(a, b, 1e-6);
    }
}
