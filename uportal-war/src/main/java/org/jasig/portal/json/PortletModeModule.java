package org.jasig.portal.json;

import java.io.IOException;

import javax.portlet.PortletMode;

import org.apache.commons.lang.StringUtils;
import org.jasig.portal.portlet.PortletUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class PortletModeModule extends SimpleModule {
    private static final long serialVersionUID = 1L;

    public PortletModeModule() {
        super("PortletModeModule");
        
        this.addSerializer(PortletMode.class, ToStringSerializer.instance);
        this.addDeserializer(PortletMode.class, new PortletModeDeserializer());
    }
    
    private static class PortletModeDeserializer extends StdDeserializer<PortletMode> {
        private static final long serialVersionUID = 1L;

        public PortletModeDeserializer() {
            super(PortletMode.class);
        }

        @Override
        public PortletMode deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
                JsonProcessingException {
            
            final String text = StringUtils.trimToNull(jp.getText());
            if (text == null) {
                return null;
            }
            return PortletUtils.getPortletMode(text);
        }
    }
}