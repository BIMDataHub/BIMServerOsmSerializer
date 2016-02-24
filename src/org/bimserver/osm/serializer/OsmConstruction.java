package org.bimserver.osm.serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OsmConstruction {
	private String            handle;
	private String            name;
	private String            surfaceRenderingName;
	private List<String>      layers;

	public OsmConstruction() {
		this.handle               = UUID.randomUUID().toString();
		this.name                 = "";
		this.surfaceRenderingName = "";
		this.layers               = new ArrayList<String>();
	}

	public OsmConstruction(String name, String surfaceRenderingName, List<String> layers) {
		this.handle               = UUID.randomUUID().toString();
		this.name                 = name;
		this.surfaceRenderingName = surfaceRenderingName;
		this.layers               = layers;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSurfaceRenderingName(String surfaceRenderingName) {
		this.surfaceRenderingName = surfaceRenderingName;
	}

	public String getHandle() {
		return handle;
	}

	public String getName() {
		return name;
	}

	public String getSurfaceRenderingName() {
		return surfaceRenderingName;
	}


	public String toString() {
		StringBuilder output = new StringBuilder();

		output.append("OS:Construction,\n");
		output.append("{" + handle + "}" + ",                              !- Handle\n");
		output.append(name + ",                                            !- Name\n");
		output.append(surfaceRenderingName + ",                            !- Surface Rendering Name\n");
		int size = layers.size();
		
		if (size <= 0) {
			output.append(";                                               !- Layer \n");
		} else {
			for (int i = 1; i <= size; i++) {
				if(i != size) {
					output.append("{" + layers.get(i - 1) + "}" + ",           !- Layer " + i + "\n");
				} else {
					output.append("{" + layers.get(i - 1) + "}" + ";           !- Layer " + i + "\n");
				}		
			}
		}
		output.append("\n");
		return output.toString();
	}
}
