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
		StringBuilder str = new StringBuilder();
		
		str.append("OS:WindowMaterial:SimpleGlazingSystem,").append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Handle", "{" + handle + "},")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Name", name + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- U-Factor", uFactor + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Solar Heat Gain Coefficient", solarHeatGainCoefficient + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Visible Transmittance", visibleTransmittance + ";")).append(System.getProperty("line.separator"));
		str.append(System.getProperty("line.separator"));
		
		return str.toString();
	}
}
