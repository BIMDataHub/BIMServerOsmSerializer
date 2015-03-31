package org.bimserver.osm.serializer;

import java.util.Set;

import org.bimserver.emf.Schema;
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginException;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.serializers.AbstractSerializerPlugin;
import org.bimserver.plugins.serializers.EmfSerializer;


public class OsmSerializerPlugin extends AbstractSerializerPlugin {
	private boolean initialized = false;

	@Override
	public EmfSerializer createSerializer(PluginConfiguration pluginConfiguration) {
		return new OsmSerializer();
	}

	@Override
	public String getDescription() {
		return "OsmSerializer";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}
	
	@Override
	public boolean needsGeometry() {
		return false;
	}

	@Override
	public void init(PluginManager pluginManager) throws PluginException {
		//pluginManager.requireSchemaDefinition(null); ?
		initialized = true;
	}

	@Override
	public String getDefaultName() {
		return "Osm";
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public ObjectDefinition getSettingsDefinition() {
		return super.getSettingsDefinition();
	}

	@Override
	public String getDefaultExtension() {
		return "osm";
	}

	@Override
	public String getDefaultContentType() {
		return "application/osm";
	}

	@Override
	public Set<Schema> getSupportedSchemas() {
		return Schema.IFC2X3TC1.toSet();
	}
}