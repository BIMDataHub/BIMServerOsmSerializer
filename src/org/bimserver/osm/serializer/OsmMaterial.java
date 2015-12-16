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
	//private thermalAbsorptance
	//private solarAbsorptance
	//private visibleAbsorptance;

	public OsmMaterial() {
		this.handle       = UUID.randomUUID().toString();
		this.name         = "";
		this.roughness    = "";
		this.thickness    = 0.0;
		this.conductivity = 0.0;
		this.density      = 0.0;
		this.specificHeat = 0.0;
	}

	public OsmMaterial(String name, String roughness, double thickness, double conductivity, double density, double specificHeat) {
		this.handle       = UUID.randomUUID().toString();
		this.name         = name;
		this.roughness    = roughness;
		this.thickness    = thickness;
		this.conductivity = conductivity;
		this.density      = density;
		this.specificHeat = specificHeat;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRoughness(String roughness) {
		this.roughness = roughness;
	}

	public void setThickness(double thickness) {
		this.thickness = thickness;
	}

	public void setConductivity(double conductivity) {
		this.conductivity = conductivity;
	}

	public void setDensity(double density) {
		this.density = density;
	}

	public void setSpecificHeat(double specificHeat) {
		this.specificHeat = specificHeat;
	}

	public String getHandle() {
		return handle;
	}

	public String getName() {
		return name;
	}

	public String getRoughness() {
		return roughness;
	}

	public double getThickness() {
		return thickness;
	}

	public double getConductivity() {
		return conductivity;
	}

	public double getDensity() {
		return density;
	}

	public double getSpecificHeat() {
		return specificHeat;
	}

	public String toString() {
		StringBuilder output = new StringBuilder();

		output.append("OS:Material,\n");
		output.append("{" + handle + "}" + ",                              !- Handle\n");
		output.append(name + ",                                            !- Name\n");
		output.append(roughness + ",                                       !- Roughness\n");
		output.append(thickness + ",                                 	   !- Thickness\n");
		output.append(conductivity + ",                                    !- Conductivity\n");
		output.append(density + ",                                         !- Density\n");
		output.append(specificHeat + ",                                    !- Specific Heat\n");
		output.append(",                                                   !- Thermal Absorptance\n");
		output.append(",                                                   !- Solar Absorptance\n");
		output.append(";                                                   !- Visible Absorptance\n");
		output.append("\n");

		return output.toString();
	}
}