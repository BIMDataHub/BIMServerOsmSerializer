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

	public String getHandle() {
		return handle;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();

		str.append("OS:Construction,").append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Handle", "{" + handle + "},")).append(System.getProperty("line.separator"));
		str.append(String.format("%-60s!- Name", name + ",")).append(System.getProperty("line.separator"));
	    str.append(String.format("%-60s!- Surface Rendering Name", surfaceRenderingName + ",")).append(System.getProperty("line.separator"));
	    int size = layers.size();
		if (size <= 0) {
			str.append(String.format("%-60s!- Layer", ";")).append(System.getProperty("line.separator"));
		} else {
			for (int i = 1; i <= size; i++) {
				String delimiter = i == size ? ";" : ",";
				str.append(String.format("%-60s!- Layer %d", "{" + layers.get(i - 1) + "}" + delimiter, i)).append(System.getProperty("line.separator"));	
			}
		}
		str.append(System.getProperty("line.separator"));
		
		return str.toString();
	}
}
