package org.jasig.portal.spring.beans.factory;



import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.fasterxml.jackson.databind.ObjectMapper;

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
        mapper.findAndRegisterModules();
        
        return mapper;
    }
}
