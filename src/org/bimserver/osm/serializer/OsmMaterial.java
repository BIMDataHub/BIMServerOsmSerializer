package org.bimserver.osm.serializer;

import java.util.UUID;

public class OsmMaterial {
	private String      handle;
	private String      name;
	private String      roughness;
	private double      thickness;
	private double      conductivity;
	private double      density;
	private double      specificHeat;
	private double      thermalAbsorptance;
	private double      solarAbsorptance;
	private double      visibleAbsorptance;

	public OsmMaterial() {
		this.handle       = UUID.randomUUID().toString();
		this.name         = "";
		this.roughness    = "";
		this.thickness    = 0.01;
		this.conductivity = 0.01;
		this.density      = 0.01;
		this.specificHeat = 0.01;
		this.thermalAbsorptance = 0.9;
		this.solarAbsorptance = 0.7;
		this.visibleAbsorptance = 0.7;
	}

	public OsmMaterial(String name, String roughness, double thickness, double conductivity, double density, double specificHeat) {
		this.handle       = UUID.randomUUID().toString();
		this.name         = name;
		this.roughness    = roughness;
		this.thickness    = Double.compare(thickness, 3.0) < 0 ? thickness : 3.0;
		this.conductivity = conductivity;
		this.density      = density;
		this.specificHeat = specificHeat;
		this.thermalAbsorptance = 0.9;
		this.solarAbsorptance = 0.7;
		this.visibleAbsorptance = 0.7;
	}

	public String getHandle() {
		return handle;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("OS:Material,").append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Handle", "{" + handle + "},")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Name", name + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Roughness", roughness + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Thickness", thickness + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Conductivity", conductivity + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Roughness", roughness + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Density", density + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Specific Heat", specificHeat + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Thermal Absorptance", thermalAbsorptance  + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Solar Absorptance", solarAbsorptance + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Visible Absorptance", visibleAbsorptance + ";")).append(System.getProperty("line.separator"));	
		str.append(System.getProperty("line.separator"));

		return str.toString();
	}
}