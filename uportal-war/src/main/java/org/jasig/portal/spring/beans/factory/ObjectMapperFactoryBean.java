package org.jasig.portal.spring.beans.factory;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * Builds an ObjectMapper configured with annotation support
 * 
 * @author Eric Dalquist
 */
public class ObjectMapperFactoryBean extends AbstractFactoryBean<ObjectMapper> {

    @Override
    public Class<?> getObjectType() {
        return ObjectMapper.class;
    }

    @Override
    protected ObjectMapper createInstance() throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        final AnnotationIntrospector pair = new AnnotationIntrospector.Pair(new JacksonAnnotationIntrospector(), new JaxbAnnotationIntrospector());
        mapper.getDeserializationConfig().withAnnotationIntrospector(pair);
        mapper.getSerializationConfig().withAnnotationIntrospector(pair);
        
        return mapper;
    }

}
