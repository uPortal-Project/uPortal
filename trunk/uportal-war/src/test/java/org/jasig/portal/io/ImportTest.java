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

package org.jasig.portal.io;

import java.util.Map;
import java.util.HashMap;

import junit.framework.TestCase;
import org.danann.cernunnos.Attributes;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.runtime.ScriptRunner;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

public class ImportTest extends TestCase {
    
    // Instance Members.
    private ApplicationContext ctx;
    private final ScriptRunner runner = new ScriptRunner();
    private Map<String,Task> importTasks;
    private final SAXReader sax = new SAXReader(); 
       
    @Override
    public void setUp() throws Exception {
        
        // We're going to use this app context in many ways...
        ctx = new ClassPathXmlApplicationContext("org/jasig/portal/io/applicationContext.xml");
        
        // The tasks...
        this.importTasks = (Map<String,Task>) ctx.getBean("importTasks");        
        
    }
    
    public void testImportEntityTypes() throws Exception {
        
        final MockEntityTypesSingleton mock = (MockEntityTypesSingleton) ctx.getBean("mockEntityTypesSingleton");
        
        Resource[] entityTypes = ctx.getResources("classpath:/org/jasig/portal/io/resources/**/*.entity-type");
        for (Resource et : entityTypes) {

            // Parse the Element and retrieve the associated Task...
            final Node n = sax.read(et.getInputStream()).getRootElement();
            final String importScript = n.valueOf("@script");
            final Task k = importTasks.get(importScript);
            
            // Figure out what the type/description should be...
            final Class<?> type = Class.forName(n.valueOf("name"));
            final String desc = n.valueOf("desc-name");

            if (k == null) {
                fail("There is no importTask defined for this entity:" 
                        + "\n\tscriptLocation=" + importScript 
                        + "\n\tentityDocument=" + et.getURL().toExternalForm());
            } else {
                final Map<String,Object> req = new HashMap<String,Object>();
                req.put(Attributes.NODE, n);
                runner.run(k, req);
                if (!mock.wasLast(type, desc)) {
                    final StringBuilder bldr = new StringBuilder();
                    bldr.append("Test of import file '").append(et.getURL().toExternalForm())
                                .append("' failed;  type='").append(type.getName())
                                .append("' desc='").append(desc).append("'");
                    fail(bldr.toString());
                }
            }

        }

    }
    
    /*
     * Nested Types.
     */
    
    public static final class MockEntityTypesSingleton {
        
        private Class<?> lastType;
        private String lastDescription;
        
        public void addEntityTypeIfNecessary(final Class<?> clazz, final String description) {
            this.lastType = clazz;
            this.lastDescription = description;
        }
        
        public boolean wasLast(final Class<?> clazz, final String description) {
            return clazz.equals(lastType) && description.equals(lastDescription);
        }
        
    }
    
}
