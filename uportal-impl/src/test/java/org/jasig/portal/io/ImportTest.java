/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
