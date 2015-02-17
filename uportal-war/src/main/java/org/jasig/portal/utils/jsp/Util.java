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
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jasig.portal.spring.beans.factory.ObjectMapperFactoryBean;
import org.springframework.web.util.UriUtils;


/**
 * JSP Static utility functions
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class Util {
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
     * URL encode a path segment.  This is just a thin wrapper around UriUtils.encodePathSegment.  It is intended
     * for the case where you are building URLs in JS.  c:url + escapeBody doesn't correctly escape
     * the contents (especially "</script>"), and fn:escapeXml incorrectly encodes the URL (it escapes chars
     * like '<' as &lt; instead of %3C).  It should help avoid XSS attacks when building RESTful
     * URLS in js.  Example:
     *
     * Given:  ${userId} -> "<script>alert('test')</script>
     *
     * ...
     * <script>
     *     $.ajax({ url: <c:url value='/users/${up:encodePathSegment(userId)}'/> });
     * </script>
     *
     * Will encode the URL as:
     *
     * /users/%3Cscript%3Ealert('test%')%3C%2Fscript%3E
     *
     * IMPORTANT:
     * Note that this encodes the '/' in </script>  to %2F.  Unfortunately, tomcat
     * still does not interpret %2F correctly unless you relax some security
     * settings (@see tomcat.apache.org/tomcat-7.0-doc/config/systemprops.html#Security,
     * the ALLOW_ENCODED_SLASH property).  So, while this method does a better job
     * at avoiding XSS issues than c:url, it's still not ideal.  Unless the
     * input is whitelisted to avoid invalid input chars, it's still possible to
     * end up with REST URLs that won't work correctly (like the one above), but at
     * least this will protect you from XSS attacks on the front end.
     *
     * @param val the path segment to encode
     * @return the encoded path segment
     */
    public static String encodePathSegment(String val) {
        try {
            return UriUtils.encodePathSegment(val, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // should be unreachable...
            throw new RuntimeException(e);
        }
    }
}
