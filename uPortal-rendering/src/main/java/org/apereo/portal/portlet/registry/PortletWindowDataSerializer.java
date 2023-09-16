package org.apereo.portal.portlet.registry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletEntityId;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortletWindowDataSerializer extends StdSerializer<PortletWindowData> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final long serialVersionUID = 1L;

    protected PortletWindowDataSerializer() {
        this(null);
    }

    protected PortletWindowDataSerializer(Class<PortletWindowData> t) {
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

    private Map<String, Object> serializePortletWindowId(IPortletWindowId windowId) {

        Map<String, Object> map = new HashMap<>();
        if (windowId == null) {
            return map;
        }
        PortletWindowIdImpl windowIdImpl = (PortletWindowIdImpl) windowId;

        Map<String, Object> portletEntityIdMap =
                serializePortletEntityId(windowIdImpl.getPortletEntityId());
        map.put("portletEntityId", portletEntityIdMap);
        map.put("windowInstanceId", windowIdImpl.getWindowInstanceId());
        return map;
    }

    @Override
    public void serialize(
            PortletWindowData portletWindowData,
            JsonGenerator generator,
            SerializerProvider provider)
            throws IOException {
        IPortletEntityId portletEntityId = portletWindowData.getPortletEntityId();
        Map<String, Object> portletEntityIdMap = serializePortletEntityId(portletEntityId);

        IPortletWindowId portletWindowId = portletWindowData.getPortletWindowId();
        Map<String, Object> portletWindowIdMap = serializePortletWindowId(portletWindowId);

        IPortletWindowId delegationParentId = portletWindowData.getDelegationParentId();
        Map<String, Object> delegationParentIdMap = serializePortletWindowId(delegationParentId);

        generator.writeStartObject();
        generator.writePOJOField("portletEntityId", portletEntityIdMap);
        generator.writePOJOField("portletWindowId", portletWindowIdMap);
        generator.writePOJOField("delegationParentId", delegationParentIdMap);
        generator.writePOJOField("renderParameters", portletWindowData.getRenderParameters());
        generator.writePOJOField(
                "publicRenderParameters", portletWindowData.getPublicRenderParameters());
        generator.writeStringField("portletMode", portletWindowData.getPortletMode().toString());
        generator.writeStringField("windowState", portletWindowData.getWindowState().toString());
        generator.writeEndObject();
    }
}
