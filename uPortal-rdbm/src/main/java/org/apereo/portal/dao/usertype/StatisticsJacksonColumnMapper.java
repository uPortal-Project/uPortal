/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.dao.usertype;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.commons.math3.stat.descriptive.UnivariateStatistic;

/**
 * Used for mapping instances of {@link UnivariateStatistic} to/from JSON
 *
 */
public class StatisticsJacksonColumnMapper extends JacksonColumnMapper {
    private static final long serialVersionUID = 1L;
    private FilterProvider filters;

    @Override
    protected void customizeObjectMapper(ObjectMapper mapper) {
        //Just operate on fields
        mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.SETTER, Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.CREATOR, Visibility.NONE);

        //Ignore the empty storedData field in all of the stat summary objects
        filters =
                new SimpleFilterProvider()
                        .addFilter(
                                StoredDataFilterMixIn.FILTER_NAME,
                                SimpleBeanPropertyFilter.serializeAllExcept("storedData"));
        mapper.addMixInAnnotations(Object.class, StoredDataFilterMixIn.class);
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
