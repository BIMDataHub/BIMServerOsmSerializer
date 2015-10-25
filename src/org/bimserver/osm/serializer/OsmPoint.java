package org.bimserver.osm.serializer;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class OsmPoint {
	private Double	x;
	private Double	y;
	private Double	z;

	public OsmPoint() {
		x = 0.0;
		y = 0.0;
		z = 0.0;
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
		x = p.getX();
		y = p.getY();
		z = p.getZ();
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

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof OsmPoint))
			return false;
		if (obj == this)
			return true;
		
		OsmPoint rhs = (OsmPoint) obj;
		return new EqualsBuilder().append(x, rhs.x).append(y, rhs.y).append(z, rhs.z).isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17,31).append(x).append(y).append(z).toHashCode();
	}

	public void addWith(OsmPoint p) {
		this.x += p.x;
		this.y += p.y;
		this.z += p.z;
	}
}
