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
		this.roughness    = "MediumRough";
		this.thickness    = 0.001;
		this.conductivity = 0.001;
		this.density      = 0.001;
		this.specificHeat = 0.001;
		this.thermalAbsorptance = 0.9;
		this.solarAbsorptance = 0.7;
		this.visibleAbsorptance = 0.7;
	}

	public OsmMaterial(String name, String roughness, double thickness, double conductivity, double density, double specificHeat) {
		this.handle       = UUID.randomUUID().toString();
		this.name         = name;
		this.roughness    = "MediumRough";
		
		if (Double.compare(thickness, 0.0) > 0 && Double.compare(thickness, 3.0) < 0) {
			this.thickness = thickness;
		} else if (Double.compare(thickness, 0.0) <= 0) {
			this.thickness = 0.001;
		} else if (Double.compare(thickness, 3.0) >= 0) {
			this.thickness = 3.0;
		}
		
		this.conductivity = Double.compare(conductivity, 0.0) > 0 ? conductivity : 0.001;
		this.density      = Double.compare(density, 0.0) > 0 ? density : 0.001;
		this.specificHeat = Double.compare(specificHeat, 0.0) > 0 ? specificHeat : 0.001;
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
		str.append(String.format("%-60s!- Density", density + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Specific Heat", specificHeat + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Thermal Absorptance", thermalAbsorptance  + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Solar Absorptance", solarAbsorptance + ",")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Visible Absorptance", visibleAbsorptance + ";")).append(System.getProperty("line.separator"));	
		str.append(System.getProperty("line.separator"));

		return str.toString();
	}
}