package org.bimserver.osm.serializer;

public class OsmPoint {
	private Double	x;
	private Double	y;
	private Double	z;

	public OsmPoint() {
		this.x = 0.0;
		this.y = 0.0;
		this.z = 0.0;
	}

	public OsmPoint(Double x, Double y) {
		this.x = x;
		this.y = y;
		this.z = 0.0;
	}

	public OsmPoint(Double x, Double y, Double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public OsmPoint(OsmPoint p) {
		this.x = p.getX();
		this.y = p.getY();
		this.z = p.getZ();
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

	public Double getZ() {
		return z;
	}

	public void setZ(Double z) {
		this.z = z;
	}

	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}

	public boolean equals(OsmPoint p) {
		return this.x == p.x && this.y == p.y && this.z == p.z ? true : false; 
	}

	public void addWith(OsmPoint p) {
		this.x += p.x;
		this.y += p.y;
		this.z += p.z;
	}
}
