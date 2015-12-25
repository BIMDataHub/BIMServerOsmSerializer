package org.bimserver.osm.serializer;

import java.util.UUID;

public class OsmConstruction {
	private String      handle;
	private String      name;
	private String      surfaceRenderingName;
	private String      layerHandle;

	public OsmConstruction() {
		this.handle               = UUID.randomUUID().toString();
		this.name                 = "";
		this.surfaceRenderingName = "";
		this.layerHandle          = "";
	}

	public OsmConstruction(String name, String surfaceRenderingName, String layerHandle) {
		this.handle               = UUID.randomUUID().toString();
		this.name                 = name;
		this.surfaceRenderingName = surfaceRenderingName;
		this.layerHandle          = layerHandle;
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
		output.append("{" + layerHandle + "}" + ";                   !- Layer 1\n");
		output.append("\n");
		
		return output.toString();
	}
}