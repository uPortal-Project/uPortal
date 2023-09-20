package org.apereo.portal.portlet.registry;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletEntityId;

public class PortletEntityDataDeserializer extends StdDeserializer<PortletEntityData> {

    private static final long serialVersionUID = 1L;

    protected PortletEntityDataDeserializer() {
        this(null);
    }

    protected PortletEntityDataDeserializer(Class<PortletEntityData> t) {
        super(t);
    }

    // TODO DRY - same as in PortletWindowData
    private IPortletEntityId deserializePortletEntityId(JsonNode node) {
        if (node == null) {
            return null;
        }
        IPortletDefinitionId portletDefinitionId =
                new InternalPortletDefinitionId(node.get("portletDefinitionId").asLong());
        String layoutNodeId = node.get("layoutNodeId").asText();
        int userId = node.get("userId").asInt();
        IPortletEntityId portletEntityId =
                new PortletEntityIdImpl(portletDefinitionId, layoutNodeId, userId);
        if (StringUtils.isBlank(portletEntityId.getStringId())) {
            return null;
        }
        return portletEntityId;
    }

    @Override
    public PortletEntityData deserialize(JsonParser parser, DeserializationContext context)
            throws IOException, JacksonException {
        JsonNode node = parser.getCodec().readTree(parser);
        JsonNode portletEntityIdNode = node.get("portletEntityId");
        JsonNode portletDefinitionIdNode = node.get("portletDefinitionId");
        JsonNode userIdNode = node.get("userId");
        JsonNode layoutNodeIdNode = node.get("layoutNodeId");

        IPortletEntityId portletEntityId = deserializePortletEntityId(portletEntityIdNode);
        IPortletDefinitionId portletDefinitionId =
                new InternalPortletDefinitionId(portletDefinitionIdNode.asLong());
        int userId = userIdNode.asInt();
        String layoutNodeId = layoutNodeIdNode.asText();
        PortletEntityData portletEntityData =
                new PortletEntityData(portletEntityId, portletDefinitionId, layoutNodeId, userId);
        return portletEntityData;
    }
}
