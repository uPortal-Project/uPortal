package org.apereo.portal.portlet.registry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletEntityId;

public class PortletEntityDataSerializer extends StdSerializer<PortletEntityData> {

    private static final long serialVersionUID = 1L;

    protected PortletEntityDataSerializer() {
        this(null);
    }

    protected PortletEntityDataSerializer(Class<PortletEntityData> t) {
        super(t);
    }

    private Map<String, Object> serializePortletEntityId(IPortletEntityId portletEntityId) {
        Map<String, Object> map = new HashMap<>();
        if (portletEntityId == null) {
            return map;
        }
        PortletEntityIdImpl portletEntityIdImpl = (PortletEntityIdImpl) portletEntityId;
        String layoutNodeId = portletEntityIdImpl.getLayoutNodeId();
        int userId = portletEntityIdImpl.getUserId();
        IPortletDefinitionId portletDefinitionId = portletEntityIdImpl.getPortletDefinitionId();

        map.put("portletDefinitionId", portletDefinitionId.getLongId());
        map.put("layoutNodeId", layoutNodeId);
        map.put("userId", userId);
        return map;
    }

    @Override
    public void serialize(
            PortletEntityData portletEntityData,
            JsonGenerator generator,
            SerializerProvider provider)
            throws IOException {
        IPortletEntityId portletEntityId = portletEntityData.getPortletEntityId();
        Map<String, Object> portletEntityIdMap = serializePortletEntityId(portletEntityId);
        IPortletDefinitionId portletDefinitionId = portletEntityData.getPortletDefinitionId();
        generator.writeStartObject();
        generator.writePOJOField("portletEntityId", portletEntityIdMap);
        generator.writeNumberField("portletDefinitionId", portletDefinitionId.getLongId());
        generator.writeStringField("layoutNodeId", portletEntityData.getLayoutNodeId());
        generator.writeNumberField("userId", portletEntityData.getUserId());
        generator.writeEndObject();
    }
}
