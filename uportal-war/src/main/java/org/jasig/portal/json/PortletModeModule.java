/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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