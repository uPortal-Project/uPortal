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

package org.jasig.portal.url;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class UrlStringBuilderTest {

    @Test
    public void testInvalidEncoding() {
        try {
            new UrlStringBuilder("NOT VALID");
            Assert.fail("Encoding 'NOT VALID' should throw an exception");
        }
        catch (RuntimeException re) {
            //expected
        }
    }

    @Test
    public void testEmptyBuilder() {
        final UrlStringBuilder builder = new UrlStringBuilder("UTF-8");
        final String url = builder.toString();
        Assert.assertEquals("/", url);
    }

    @Test
    public void testEmptyProtocolHostBuilder() {
        final UrlStringBuilder builder = new UrlStringBuilder("UTF-8", "http", "www.example.com");
        final String url = builder.toString();
        Assert.assertEquals("http://www.example.com", url);
    }

    @Test
    public void testEmptyProtocolHostPortBuilder() {
        final UrlStringBuilder builder = new UrlStringBuilder("UTF-8", "http", "www.example.com", 8080);
        final String url = builder.toString();
        Assert.assertEquals("http://www.example.com:8080", url);
    }

    @Test
    public void testParameterEmptyBuilder() {
        final UrlStringBuilder builder = new UrlStringBuilder("UTF-8");
        
        builder.addParameter("p1", "v1", null, "v2");
        
        builder.addParameter("p2");
        builder.addParameter("p2", (List<String>)null);
        builder.setParameter("p2", Arrays.asList("va,?", "v b"));
        
        final String url = builder.toString();
        Assert.assertEquals("/?p1=v1&p1=&p1=v2&p2=va%2C%3F&p2=v+b", url);
    }

    @Test
    public void testParameterProtocolHostPortBuilder() {
        final UrlStringBuilder builder = new UrlStringBuilder("UTF-8", "http", "www.example.com", 8080);
        
        builder.addParameter("p1", "v1", null, "v2");
        
        builder.setParameter("p0");
        
        builder.addParameter("p2", (String)null);
        builder.setParameter("p2", Arrays.asList("va,?", "v b"));
        
        final String url = builder.toString();
        Assert.assertEquals("http://www.example.com:8080?p1=v1&p1=&p1=v2&p0&p2=va%2C%3F&p2=v+b", url);
    }

    @Test
    public void testParametersBuilder() {
        final UrlStringBuilder builder = new UrlStringBuilder("UTF-8");
        
        final Map<String, List<String>> p0 = new LinkedHashMap<String, List<String>>();
        p0.put("notSeen", Arrays.asList("b", "c"));
        
        builder.setParameters(p0);
        builder.addParameters(p0);
        
        final Map<String, List<String>> p1 = new LinkedHashMap<String, List<String>>();
        p1.put("a", Arrays.asList("b", "c"));
        p1.put("b", Arrays.asList(null, "d"));
        p1.put("c", null);
        
        builder.setParameters(p1);
        
        builder.addParameters("uP_", p1);
        
        final String url = builder.toString();
        Assert.assertEquals("/?a=b&a=c&b=&b=d&c&uP_a=b&uP_a=c&uP_b=&uP_b=d&uP_c", url);
    }

    @Test
    public void testPathEmptyBuilder() {
        final UrlStringBuilder builder = new UrlStringBuilder("UTF-8");
        
        builder.addPath("portal");
        builder.addPath("home");
        builder.addPath("normal", "render.uP");
        
        final String url = builder.toString();
        Assert.assertEquals("/portal/home/normal/render.uP", url);
    }

    @Test
    public void testParameterPathEmptyBuilder() {
        final UrlStringBuilder builder = new UrlStringBuilder("UTF-8");
        
        builder.addParameter("p1", "v1", null, "v2");
        
        builder.addParameter("p2", (String)null);
        builder.setParameter("p2", Arrays.asList("va,?", "v b"));
        
        builder.setPath();
        builder.addPath("portal");
        builder.setPath("foo", "bar");
        builder.addPath("home");
        builder.addPath("normal", "render.uP");
        
        final String url = builder.toString();
        Assert.assertEquals("/foo/bar/home/normal/render.uP?p1=v1&p1=&p1=v2&p2=va%2C%3F&p2=v+b", url);
    }

    @Test
    public void testCloneParameterPathEmptyBuilder() {
        final UrlStringBuilder builder = new UrlStringBuilder("UTF-8");
        
        builder.addParameter("p1", "v1", null, "v2");
        
        builder.addParameter("p2", (String)null);
        builder.setParameter("p2", Arrays.asList("va,?", "v b"));
        
        builder.setPath();
        builder.addPath("portal");
        builder.setPath("foo", "bar");
        builder.addPath("home");
        builder.addPath("normal", "render.uP");
        
        
        final UrlStringBuilder builder2 = (UrlStringBuilder)builder.clone();
        Assert.assertEquals(builder, builder2);
        Assert.assertEquals(builder.hashCode(), builder2.hashCode());
        
        builder2.setPath();
        Assert.assertNotSame(builder, builder2);
        Assert.assertNotSame(builder.hashCode(), builder2.hashCode());
        
        final String url = builder.toString();
        Assert.assertEquals("/foo/bar/home/normal/render.uP?p1=v1&p1=&p1=v2&p2=va%2C%3F&p2=v+b", url);
        
        final String url2 = builder2.toString();
        Assert.assertEquals("/?p1=v1&p1=&p1=v2&p2=va%2C%3F&p2=v+b", url2);
    }
}
