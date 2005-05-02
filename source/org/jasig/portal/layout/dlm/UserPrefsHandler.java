/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import org.jasig.portal.security.IPerson;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** 
 * Applies user changes that are part of the uPortal User Preferences
 * storage (not part of the layout structure) to the user's plf prior to
 * persisting.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class UserPrefsHandler
{
    public static final String RCS_ID = "@(#) $Header$";

    /**
       Records changes made to element attributes that are defined as being
       part of a user's user preferences object and not part of the layout.
       These attributes are specified in the .sdf files for the structure and
       theme stylesheets. The value is not stored in the layout but the loading
       of user prefs joins to a layout struct and hence that struct must exist
       in the layout. This call gets that node into the PLF if not there
       already and prevents it from being removed if no other changes were made
       to it or its children by the user.
    */
    public static void setUserPreference( Element compViewNode,
                                          String attributeName,
                                          IPerson person )
        throws Exception
    {
        Document doc = compViewNode.getOwnerDocument();
        NodeList nodes = doc.getElementsByTagName("layout");

        boolean layoutOwner = false;
        boolean templateUser = false;
        Attr attrib = null;
        Element e = null;
        // Search Elements in nodelist
        for (int i = 0; i < nodes.getLength(); i++)
        {
            e = (Element)nodes.item(i);
            attrib = e.getAttributeNodeNS(Constants.NS_URI, Constants.LCL_FRAGMENT_NAME);
            if (attrib != null)
            {
                layoutOwner = true;
            }
            else
            {
                attrib = e.getAttributeNodeNS(Constants.NS_URI, Constants.LCL_IS_TEMPLATE_USER);
                if (attrib != null)
                {
                    templateUser = true;
                }
            }
        }

        if (!layoutOwner && !templateUser)
        {
            Element plfNode = HandlerUtils.getPLFNode( compViewNode, person,
                                                       true, // create if not found
                                                       false ); 
            EditManager.addPrefsDirective( plfNode, attributeName, person );
        }
    }
}

