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
		this.uFactor = 0.001;
		this.solarHeatGainCoefficient = 0.001;
		this.visibleTransmittance = 0.001;
	}
	
	public String getHandle() {
		return handle;
	}
	
	public OsmWindowMaterialSimpleGlazingSystem(String name, double uFactor, double solarHeatGainCoefficient, double visibleTransmittance) {
		this.handle = UUID.randomUUID().toString();
		this.name = name;
		
		if (Double.compare(uFactor, 0.0) > 0 && Double.compare(uFactor, 7.0) < 0) {
			this.uFactor = uFactor;
		} else if (Double.compare(uFactor, 0.0) <= 0) {
			this.uFactor = 0.001;
		} else if (Double.compare(uFactor, 7.0) >=0) {
			this.uFactor = 6.999;
		}
		
		if (Double.compare(solarHeatGainCoefficient, 0.0) > 0 && Double.compare(solarHeatGainCoefficient, 1.0) < 0) {
			this.solarHeatGainCoefficient = solarHeatGainCoefficient;
		} else if (Double.compare(solarHeatGainCoefficient, 0.0) <= 0) {
			this.solarHeatGainCoefficient = 0.001;
		} else if (Double.compare(solarHeatGainCoefficient, 1.0) >= 0) {
			this.solarHeatGainCoefficient = 0.999;
		}
		
		if (Double.compare(visibleTransmittance, 0.0) > 0 && Double.compare(visibleTransmittance, 1.0) < 0) {
			this.visibleTransmittance = visibleTransmittance;
		} else if (Double.compare(visibleTransmittance, 0.0) <= 0) {
			this.visibleTransmittance = 0.001;
		} else if (Double.compare(visibleTransmittance, 1.0) >= 0) {
			this.visibleTransmittance = 0.999;
		}
		
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
