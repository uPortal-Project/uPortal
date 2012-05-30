package org.jasig.portal.dao.usertype;

import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.annotate.JsonFilter;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;

/**
 * Used for mapping instances of {@link UnivariateStatistic} to/from JSON
 * 
 * @author Eric Dalquist
 */
public class StatisticsJacksonColumnMapper extends JacksonColumnMapper {
    private static final long serialVersionUID = 1L;
    private FilterProvider filters;
    
    @Override
    protected void customizeObjectMapper(ObjectMapper mapper) {
      //Just operate on fields
        mapper.setVisibility(JsonMethod.FIELD, Visibility.ANY);
        mapper.setVisibility(JsonMethod.GETTER, Visibility.NONE);
        mapper.setVisibility(JsonMethod.IS_GETTER, Visibility.NONE);
        mapper.setVisibility(JsonMethod.SETTER, Visibility.NONE);
        mapper.setVisibility(JsonMethod.CREATOR, Visibility.NONE);
        
        //Ignore the empty storedData field in all of the stat summary objects
        filters = new SimpleFilterProvider().addFilter(StoredDataFilterMixIn.FILTER_NAME, SimpleBeanPropertyFilter.serializeAllExcept("storedData"));
        mapper.getSerializationConfig().addMixInAnnotations(Object.class, StoredDataFilterMixIn.class);
    }
    @JsonFilter(StoredDataFilterMixIn.FILTER_NAME)
    private interface StoredDataFilterMixIn {
        static final String FILTER_NAME = "storedDataFilter";
    }

    @Override
    protected ObjectWriter createObjectWriter(ObjectMapper mapper) {
        return mapper.writer(filters);
    }

}
