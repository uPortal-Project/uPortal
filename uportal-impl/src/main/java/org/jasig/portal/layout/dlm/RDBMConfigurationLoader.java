/**
 * Copyright 2009 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.dlm;

import java.util.List;

import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;

/**
 * Implementation of {@link ConfigurationLoader} that loads 
 * {@link FragmentDefinition} objects from the database using JPA/Hibernate.  
 * You can enable this feature by setting the <code>ConfigurationLoader.impl</code> 
 * property in dlm.xml.
 * 
 * @author awills
 */
public class RDBMConfigurationLoader extends ConfigurationLoader {
    
    // Instance Members.
    private FragmentDefinitionDao fragmentDao = null;

    @Override
    public FragmentDefinition[] getFragments() {
        List<FragmentDefinition> frags = fragmentDao.getAllFragments();
        FragmentDefinition[] rslt = new FragmentDefinition[frags.size()];
        for (int i=0; i < frags.size(); i++) {
            rslt[i] = frags.get(i);
        }
        return rslt;
    }

    @Override
    public synchronized void init(Document doc) {

        // There's nothing in the dlm.xml file we care about in this Java 
        // class, but we can use this opportunity to get a reference to 
        // the ApplicationContext...
        
        if (this.fragmentDao == null) {
            ApplicationContext ctx = PortalApplicationContextLocator.getApplicationContext();
            fragmentDao = (FragmentDefinitionDao) ctx.getBean("fragmentDefinitionDao");
        }
        
    }

}
