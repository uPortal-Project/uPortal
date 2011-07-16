/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.io.xml;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

/**
 * Wraps common logic for configuring a {@link Jaxb2Marshaller}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractJaxbDataHandler<T> 
        implements IDataImporter<T>, IDataExporter<T>, IDataDeleter<T>, InitializingBean {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
    private String schemaLocation;
    
    public final void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }

    public final void setContextPaths(String... contextPaths) {
        this.jaxb2Marshaller.setContextPaths(contextPaths);
    }

    public final void setContextPath(String contextPath) {
        this.jaxb2Marshaller.setContextPath(contextPath);
    }

    public final void setSchema(Resource schemaResource) {
        this.jaxb2Marshaller.setSchema(schemaResource);
    }

    public final void setSchemas(Resource[] schemaResources) {
        this.jaxb2Marshaller.setSchemas(schemaResources);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final Map<String, Object> properties = new LinkedHashMap<String, Object>();
        properties.put(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8");
        properties.put(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
        if (this.schemaLocation != null) {
            properties.put(javax.xml.bind.Marshaller.JAXB_SCHEMA_LOCATION, this.schemaLocation);
        }
        
        this.jaxb2Marshaller.setMarshallerProperties(properties);
        
        this.jaxb2Marshaller.afterPropertiesSet();
    }

    @Override
    public final Unmarshaller getUnmarshaller() {
        return this.jaxb2Marshaller;
    }

    @Override
    public final Marshaller getMarshaller() {
        return this.jaxb2Marshaller;
    }
}
