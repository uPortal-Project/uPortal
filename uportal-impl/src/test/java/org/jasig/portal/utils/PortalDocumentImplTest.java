/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import java.util.Hashtable;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Testcase for PortalDocumentImpl as deprecated for uPortal 2.5.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class PortalDocumentImplTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }


    /**
     * Demonstrate that the legacy getIdentifiers method continues to function.
     * @throws Exception as one of the test failure modalities
     */
    public void testGetIdentifiers() 
        throws Exception {
        
        Document dom = 
            ResourceLoader.getResourceAsDocument(this.getClass(), "identifiedDoc.xml");
    
        PortalDocumentImpl pdi = new PortalDocumentImpl(dom);
        Hashtable idsToElements = pdi.getIdentifiers();
        
        Element elemOne = (Element) idsToElements.get("id1");
        assertEquals("testValue1", elemOne.getAttribute("testAttribute"));
        
        Element elemTwo = (Element) idsToElements.get("id2");
        assertEquals("testValue2", elemTwo.getAttribute("testAttribute"));
        
        Element deeplyNestedChild = (Element) idsToElements.get("deepChild");
        assertEquals("deepChildValue", deeplyNestedChild.getAttribute("deepChildAttribute"));
        
    }

}
