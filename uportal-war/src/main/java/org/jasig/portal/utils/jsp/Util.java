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

package org.jasig.portal.utils.jsp;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.jasig.portal.spring.beans.factory.ObjectMapperFactoryBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSP Static utility functions
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class Util {
    
    final static Logger logger = LoggerFactory.getLogger(Util.class);
    
    private static final ObjectMapper OBJECT_MAPPER;
    static {
        ObjectMapper mapper;
        try {
            final ObjectMapperFactoryBean omfb = new ObjectMapperFactoryBean();
            omfb.afterPropertiesSet();
            mapper = omfb.getObject();
        }
        catch (Exception e) {
            mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
        }
        
        OBJECT_MAPPER = mapper;
    }
    
    public static boolean contains(Collection<?> coll, Object o) {
        return coll != null && coll.contains(o);
    }

    public static boolean containsKey(Map<?, ?> map, Object o) {
        return map != null && map.containsKey(o);
    }

    public static boolean containsValue(Map<?, ?> map, Object o) {
        return map != null && map.containsValue(o);
    }
    
    public static boolean instanceOf(Object obj, String className) throws ClassNotFoundException {
        final ClassLoader cl = obj.getClass().getClassLoader();
        final Class<?> clazz = Class.forName(className, true, cl);
        final boolean isInstanceOf = obj.getClass().isAssignableFrom(clazz);
        return isInstanceOf;
    }

    public static String json(Object obj) throws JsonGenerationException, JsonMappingException, IOException {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }
    
    /**
     * Tests if a string is a valid URL
     * Will open a connection and make sure that it can connect to the URL
     * it looks for an HTTP status code of 200 on a GET.  This is a valid URL.
     * @param url - A string representing a url
     * @return True if URL is valid according to included definition
     */
    public static boolean isValidUrl(String url){
        HttpURLConnection huc = null;
        boolean isValid = false;
        try {
            URL u = new URL(url);
            huc = ( HttpURLConnection )u.openConnection();
            huc.setRequestMethod("GET");
            huc.connect();
            int response = huc.getResponseCode();
            if(response != HttpURLConnection.HTTP_OK){
                throw new IOException(String.format("URL %s did not return a valid response code while attempting to connect.  Expected: %d. Received: %d", 
                        url, HttpURLConnection.HTTP_OK, response));
            }
            isValid=true;
        } catch (IOException e) {
                logger.warn("A problem happened while trying to verify url: {} Error Message: {}", url, e.getMessage());
        } finally {
            if(huc!=null){
                huc.disconnect();
            };
        }
        return isValid;
    }
}
