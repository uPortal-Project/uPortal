package org.apereo.portal.portlet.registry;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.Map;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletEntityId;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortletWindowDataDeserializer extends StdDeserializer<PortletWindowData> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected PortletWindowDataDeserializer() {
        this(null);
    }

    protected PortletWindowDataDeserializer(Class<?> vc) {
        super(vc);
    }

    private static final long serialVersionUID = 1L;

    private IPortletWindowId deserializePortletWindowId(JsonNode node) {
        if (node == null) {
            return null;
        }
        IPortletEntityId portletEntityId = deserializePortletEntityId(node.get("portletEntityId"));
        if (portletEntityId == null) {
            return null;
        }
        String windowInstanceId = node.get("windowInstanceId").asText();
        if (StringUtils.isBlank(windowInstanceId)) {
            windowInstanceId = null;
        }
        // TODO this shouldn't be necessary.  Figure out why serializer does this
        if ("null".equals(windowInstanceId)) {
            windowInstanceId = null;
        }
        IPortletWindowId portletWindowId =
                new PortletWindowIdImpl(portletEntityId, windowInstanceId);
        return portletWindowId;
    }

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
    public PortletWindowData deserialize(JsonParser parser, DeserializationContext context)
            throws IOException, JacksonException {
        JsonNode node = parser.getCodec().readTree(parser);
        JsonNode portletEntityIdNode = node.get("portletEntityId");
        JsonNode portletWindowIdNode = node.get("portletWindowId");
        JsonNode delegationParentIdNode = node.get("delegationParentId");
        JsonNode portletModeNode = node.get("portletMode");
        JsonNode windowStateNode = node.get("windowState");

        IPortletWindowId portletWindowId = this.deserializePortletWindowId(portletWindowIdNode);
        IPortletEntityId portletEntityId = this.deserializePortletEntityId(portletEntityIdNode);
        IPortletWindowId delegationParentId =
                this.deserializePortletWindowId(delegationParentIdNode);

        PortletMode portletMode = new PortletMode(portletModeNode.asText());
        WindowState windowState = new WindowState(windowStateNode.asText());

        Map<String, String[]> renderParameters =
                parser.getCodec().treeToValue(node.get("renderParameters"), Map.class);
        Map<String, String[]> publicRenderParameters =
                parser.getCodec().treeToValue(node.get("publicRenderParameters"), Map.class);

        PortletWindowData portletWindowData =
                new PortletWindowData(portletWindowId, portletEntityId, delegationParentId);
        portletWindowData.setPortletMode(portletMode);
        portletWindowData.setWindowState(windowState);
        portletWindowData.setRenderParameters(renderParameters);
        portletWindowData.setPublicRenderParameters(publicRenderParameters);
        return portletWindowData;
    }
}
