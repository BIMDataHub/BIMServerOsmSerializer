package org.bimserver.osm.serializer;

import java.util.UUID;

public class OsmConstruction {
	private String      handle;
	private String      name;
	private String      surfaceRenderingName;
	private OsmMaterial layer;

	public OsmConstruction() {
		this.handle               = UUID.randomUUID().toString();
		this.name                 = "";
		this.surfaceRenderingName = "";
		this.layer                = new OsmMaterial();
	}

	public OsmConstruction(String name, String surfaceRenderingName, OsmMaterial layer) {
		this.handle               = UUID.randomUUID().toString();
		this.name                 = name;
		this.surfaceRenderingName = surfaceRenderingName;
		this.layer                = layer;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSurfaceRenderingName(String surfaceRenderingName) {
		this.surfaceRenderingName = surfaceRenderingName;
	}

	public void setLayer(OsmMaterial layer) {
		this.layer = layer;
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

	public OsmMaterial getLayer() {
		return layer;
	}

	public String toString() {
		StringBuilder output = new StringBuilder();

		output.append("OS:Construction,\n");
		output.append("{" + handle + "}" + ",                              !- Handle\n");
		output.append(name + ",                                            !- Name\n");
		output.append(surfaceRenderingName + ",                            !- Surface Rendering Name\n");
		output.append("{" + layer.getHandle() + "}" + ";                   !- Layer 1\n");
		output.append("\n");
		
		return output.toString();
	}
}