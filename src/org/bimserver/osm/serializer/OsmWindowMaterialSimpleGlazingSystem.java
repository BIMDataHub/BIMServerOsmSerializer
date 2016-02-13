package org.bimserver.osm.serializer;

import java.util.UUID;

public class OsmWindowMaterialSimpleGlazingSystem {
	private String handle;
	private String name;
	private double uFactor;
	private double solarHeatGainCoefficient;
	private double visibleTransmittance;
	
	public OsmWindowMaterialSimpleGlazingSystem() {
		this.handle = UUID.randomUUID().toString();
		this.name = "";
		this.uFactor = 0.0;
		this.solarHeatGainCoefficient = 0.0;
		this.visibleTransmittance = 0.0;
	}
	
	public String getHandle() {
		return handle;
	}
	
	public OsmWindowMaterialSimpleGlazingSystem(String name, double uFacotr, double solarHeatGainCoefficient, double visibleTransmittance) {
		this.handle = UUID.randomUUID().toString();
		this.name = name;
		this.uFactor = uFacotr;
		this.solarHeatGainCoefficient = solarHeatGainCoefficient;
		this.visibleTransmittance = visibleTransmittance;
	}
	
	public String toString() {
		StringBuilder output = new StringBuilder();

		output.append("OS:WindowMaterial:SimpleGlazingSystem,\n");
		output.append("{" + handle + "}" + ",                              !- Handle\n");
		output.append(name + ",                                            !- Name\n");
		output.append(uFactor + ",                                         !- U-Factor\n");
		output.append(solarHeatGainCoefficient+ ",                         !- Solar Heat Gain Coefficient\n");
		output.append(visibleTransmittance + ";                            !- Visible Transmittance\n");
		
		output.append("\n");

		return output.toString();
	}

}
