package test;

import java.util.Set;

import org.bimserver.emf.Schema;
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.models.store.ParameterDefinition;
import org.bimserver.models.store.PrimitiveDefinition;
import org.bimserver.models.store.PrimitiveEnum;
import org.bimserver.models.store.StoreFactory;
import org.bimserver.models.store.StringType;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginException;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.serializers.AbstractSerializerPlugin;
import org.bimserver.plugins.serializers.EmfSerializer;
import org.bimserver.plugins.serializers.SerializerPlugin;

public class IfcOSMSerializerPlugin implements SerializerPlugin {
	private boolean initialized = false;

	@Override
	public EmfSerializer createSerializer(PluginConfiguration pluginConfiguration) {
		return new IfcOSMSerializer();
	}

	@Override
	public String getDescription() {
		return "IfcOSMSerializer";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}
	
	@Override
	public boolean needsGeometry() {
		return true;
	}

	@Override
	public void init(PluginManager pluginManager) throws PluginException {
		//pluginManager.requireSchemaDefinition(null); ?
		initialized = true;
	}

	@Override
	public String getDefaultName() {
		return "IfcOSM";
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public ObjectDefinition getSettingsDefinition() {
		ObjectDefinition settingsDefinition = super.getSettingsDefinition();

		PrimitiveDefinition stringDefinition = StoreFactory.eINSTANCE.createPrimitiveDefinition();
		stringDefinition.setType(PrimitiveEnum.STRING);

		ParameterDefinition zipExtension = StoreFactory.eINSTANCE.createParameterDefinition();
		zipExtension.setName(ZIP_EXTENSION);
		zipExtension.setDescription("Extension of the downloaded file when using zip compression");
		zipExtension.setType(stringDefinition);
		StringType defaultZipExtensionValue = StoreFactory.eINSTANCE.createStringType();
		defaultZipExtensionValue.setValue("ifczip");
		zipExtension.setDefaultValue(defaultZipExtensionValue);
		settingsDefinition.getParameters().add(zipExtension);

		return settingsDefinition;
	}

	@Override
	public Set<Schema> getSupportedSchemas()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
