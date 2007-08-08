/* Copyright 2006 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm.processing;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.StructureStylesheetUserPreferences;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.layout.dlm.Constants;
import org.jasig.portal.layout.dlm.DistributedLayoutManager;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Sample IParameterProcessor that when used watches for a tab with a name of
 * "Sticky Tab" and continuously sets that tab's ID as the activeTab
 * stylesheet parameters to force that tab to stay in focus.
 * 
 * @author Mark Boyd
 */
public class ExampleStickyTabEnforcer implements IParameterProcessor
{
    private DistributedLayoutManager dlMgr = null;
    
    /**
     * Acquires an instance of DistributedLayoutManager to allow this processor
     * to look at all tabs in the user's layout.
     * 
     * @see org.jasig.portal.layout.dlm.processing.IParameterProcessor#setResources(org.jasig.portal.security.IPerson, org.jasig.portal.layout.dlm.DistributedLayoutManager)
     */
    public void setResources(IPerson person, DistributedLayoutManager dlm)
    {
        this.dlMgr = dlm;
    }

    /**
     * Looks for a tab in the user's layout with a title of "Sticky Tab" and if
     * found sets that tab's ID as the value of a structure stylesheet parameter
     * "activeTab" which is looked for and used by DLM's default structure
     * stylesheet.
     * 
     * @see org.jasig.portal.layout.dlm.processing.IParameterProcessor#processParameters(org.jasig.portal.UserPreferences, javax.servlet.http.HttpServletRequest)
     */
    public void processParameters(UserPreferences prefs, HttpServletRequest request)
    {
        NodeList nodes = dlMgr.getUserLayoutDOM().getElementsByTagName("folder");
        for(int i=0; i<nodes.getLength(); i++)
        {
            Element folder = (Element) nodes.item(i);
            String name = folder.getAttribute("name");
            if (name.equals("Sticky Tab"))
            {
                String id = folder.getAttribute(Constants.ATT_ID);
                StructureStylesheetUserPreferences ssup 
                    = prefs.getStructureStylesheetUserPreferences();
                ssup.putParameterValue("activeTab", id);
                ssup.putParameterValue("tabID", id);
            }
        }
    }
}
