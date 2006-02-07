/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.UserLayoutStoreFactory;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** 
 * Looks for, applies against the ilf, and updates accordingly within the plf
 * the set of parameter edits made against channels incorporated from fragments.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.6
 */
public class ParameterEditManager
{
    public static final String RCS_ID = "@(#) $Header$";
    private static final Log LOG = LogFactory.getLog(ParameterEditManager.class);

    private static RDBMDistributedLayoutStore dls = null;

    /**
     * Hands back the single instance of RDBMDistributedLayoutStore. There is
     * already a method for aquiring a single instance of the configured layout
     * store so we delegate over there so that all references refer to the same
     * instance. This method is solely for convenience so that we don't have to
     * keep calling UserLayoutStoreFactory and casting the resulting class.
     */
    private static RDBMDistributedLayoutStore getDLS()
    {
        if ( dls == null )
        {
            IUserLayoutStore uls = null;
            uls = UserLayoutStoreFactory.getUserLayoutStoreImpl();
            dls = (RDBMDistributedLayoutStore) uls;
        }
        return dls;
    }

    /**
       Get the parm edit set if any from the plf and process each edit command
       removing any that fail from the set so that the set is self cleaning.
     * @throws Exception
    */
    static void applyAndUpdateParmEditSet( Document plf,
                                           Document ilf,
                     IntegrationResult result ) 
    {

        Element pSet = null;
        try
        {
            pSet = getParmEditSet( plf, null, false );
        }
        catch( Exception e )
        {
            LOG.error("Exception occurred while getting user's DLM " +
                    "paramter-edit-set.", e);
        }

        if ( pSet == null )
            return;

        NodeList edits = pSet.getChildNodes();

        for( int i=edits.getLength()-1; i>=0; i-- )
        {
            if ( applyEdit( (Element) edits.item(i), ilf ) == false )
            {
                pSet.removeChild( edits.item(i) );
                result.changedPLF = true;
            }
            else
            {
                result.changedILF = true;
            }
        }

        if ( pSet.getChildNodes().getLength() == 0 )
        {
            plf.getDocumentElement().removeChild( pSet );
            result.changedPLF = true;
        }
    }

    /**
     * Attempt to apply a single channel parameter edit command and return true
     * if it succeeds or false otherwise. If the edit is disallowed or the
     * target element no longer exists in the document the edit command fails
     * and returns false.
     * @throws Exception
     */
    private static boolean applyEdit( Element edit, Document ilf ) 
    {
        String nodeID = edit.getAttribute( Constants.ATT_TARGET );

        Element channel = ilf.getElementById( nodeID );

        if ( channel == null )
            return false;

        // now get the name of the parameter to be edited and find that element
        String parmName = edit.getAttribute( Constants.ATT_NAME );
        String parmValue = edit.getAttribute( Constants.ATT_USER_VALUE );
        NodeList ilfParms = channel.getChildNodes();
        Element targetParm = null;
        
        for(int i=0; i<ilfParms.getLength(); i++)
        {
            Element ilfParm = (Element) ilfParms.item(i); 
            if (ilfParm.getAttribute(Constants.ATT_NAME).equals(parmName))
            {
                targetParm = ilfParm;
                break;
            }
        }
        if (targetParm == null) // parameter not found so we are free to set
        {
            Element parameter = ilf.createElement("parameter");
            parameter.setAttribute("name", parmName);
            parameter.setAttribute("value", parmValue);
            parameter.setAttribute("override", "yes");
            channel.appendChild(parameter);
            return true;
        }
        /* TODO Add support for fragments to set dlm:editAllowed attribute for
         * channel parameters. (2005.11.04 mboyd)
         * 
         * In the commented code below, the check for editAllowed will never be 
         * seen on a parameter element in the 
         * current database schema approach used by DLM. This is because 
         * parameters are second class citizens of the layout structure. They
         * are not found in the up_layout_struct table but only in the 
         * up_layout_param table. DLM functionality like dlm:editAllowed,
         * dlm:moveAllowed, dlm:deleteAllowed, and dlm:addChildAllowed were 
         * implemented without schema changes by adding these as parameters to
         * structural elements and upon loading any parameter that begins with
         * 'dlm:' is placed as an attribute on the containing structural 
         * element. So any channel parameter entry with dlm:editAllowed has that
         * value placed as an attribute on the containing channel not on the 
         * parameter that was meant to have it.
         * 
         * The only solution would be to add special dlm:parm children below
         * channels that would get the editAllowed value and then when creating
         * the DOM don't create those as child elements but use them to set the
         * attribute on the corresponding parameter by having the name of the
         * dlm:parm element be the name of the parameter to which it is to be 
         * related.
         * 
         * The result of this lack of functionality is that fragments can't 
         * mark any channel parameters as dlm:editAllowed='false' thereby
         * further restricting which channel parameters can be edited beyond 
         * what the channel definition specifies during publishing.  
         */
        //Attr editAllowed = targetParm.getAttributeNode( Constants.ATT_EDIT_ALLOWED );
        //if ( editAllowed != null && editAllowed.getNodeValue().equals("false"))
        //    return false;
        
        // target parm found. See if channel definition will still allow changes.
        
        Attr override = targetParm.getAttributeNode( Constants.ATT_OVERRIDE );
        if ( override != null 
                && !override.getNodeValue().equals(Constants.CAN_OVERRIDE))
            return false;

        // now see if the change is still needed
        if (targetParm.getAttribute(Constants.ATT_VALUE).equals(parmValue))
            return false; // user's edit same as fragment or chan def
        
        targetParm.setAttribute("value", parmValue);
        return true;
    }

    /**
     * Get the parameter edits set if any stored in the root of the document or
     * create it if passed-in create flag is true.
     */
    private static Element getParmEditSet( Document plf,
                                         IPerson person,
                                         boolean create )
        throws PortalException
    {
        Node root = plf.getDocumentElement();
        Node child = root.getFirstChild();

        while( child != null )
        {
            if ( child.getNodeName().equals( Constants.ELM_PARM_SET ) )
            return (Element) child;
            child = child.getNextSibling();
        }

        if ( create == false )
            return null;

        String ID = null;

        try
        {
            ID = getDLS().getNextStructDirectiveId( person );
        }
        catch (Exception e)
        {
            throw new PortalException( "Exception encountered while " +
                                       "generating new parameter edit set node " +
                                       "Id for userId=" + person.getID(), e );
        }
        Element parmSet = plf.createElement( Constants.ELM_PARM_SET );
        parmSet.setAttribute( Constants.ATT_TYPE,
                     Constants.ELM_PARM_SET );
        parmSet.setAttribute( Constants.ATT_ID, ID );
        parmSet.setIdAttribute(Constants.ATT_ID, true);
        root.appendChild( parmSet );
        return parmSet;
    }

    /**
     * Create and append a parameter edit directive to parameter edits set for
     * applying a user specified value to a named parameter of the incorporated
     * channel represented by the passed-in target id. If one already exists
     * for that node and that name then the value of the existing edit is 
     * changed to the passed-in value.
     */
    public static synchronized void addParmEditDirective( Element compViewChannelNode,
                                             String targetId,
                                             String name,
                                             String value,
                                             IPerson person )
        throws PortalException
    {
        Document plf = (Document) person.getAttribute( Constants.PLF );
        Element parmSet = getParmEditSet( plf, person, true );
        NodeList edits = parmSet.getChildNodes();
        Element existingEdit = null;
        
        for(int i=0; i<edits.getLength(); i++)
        {
            Element edit = (Element) edits.item(i); 
            if (edit.getAttribute(Constants.ATT_TARGET).equals(targetId) &&
                    edit.getAttribute(Constants.ATT_NAME).equals(name))
            {
                existingEdit = edit;
                break;
            }
        }
        if (existingEdit == null) // existing one not found, create a new one
        {
            addParmEditDirective(targetId, name, value, person, plf, parmSet);
            return;
        }
        existingEdit.setAttribute(Constants.ATT_USER_VALUE, value);
    }
    /**
       This method does the actual work of adding a newly created parameter
       edit and adding it to the parameter edits set.
    */
    private static void addParmEditDirective( String targetID,
                                            String name, 
                                            String value,
                                            IPerson person,
                                            Document plf,
                                            Element parmSet )
        throws PortalException
    {
        
        String ID = null;

        try
        {
            ID = getDLS().getNextStructDirectiveId( person );
        }
        catch (Exception e)
        {
            throw new PortalException( "Exception encountered while " +
                                       "generating new parameter edit node " +
                                       "Id for userId=" + person.getID(), e );
        }
        Element parm = plf.createElement( Constants.ELM_PARM_EDIT );
        parm.setAttribute( Constants.ATT_TYPE, Constants.ELM_PARM_EDIT );
        parm.setAttribute( Constants.ATT_ID, ID );
        parm.setIdAttribute(Constants.ATT_ID, true);
        parm.setAttributeNS( Constants.NS_URI,
                Constants.ATT_TARGET, targetID );
        parm.setAttribute( Constants.ATT_NAME, name );
        parm.setAttribute( Constants.ATT_USER_VALUE, value );
        parmSet.appendChild( parm );
    }
    /**
     * Remove a parameter edit directive from the parameter edits set for
     * applying user specified values to a named parameter of an incorporated
     * channel represented by the passed-in target id. If one doesn't exists
     * for that node and that name then this call returns without any effects.
     */
    public static void removeParmEditDirective( String targetId,
                                             String name,
                                             IPerson person )
        throws PortalException
    {
        Document plf = (Document) person.getAttribute( Constants.PLF );
        Element parmSet = getParmEditSet( plf, person, false );
        
        if (parmSet == null)
            return; // no set so no edit to remove
        
        NodeList edits = parmSet.getChildNodes();
        Element existingEdit = null;
        
        for(int i=0; i<edits.getLength(); i++)
        {
            Element edit = (Element) edits.item(i); 
            if (edit.getAttribute(Constants.ATT_TARGET).equals(targetId) &&
                    edit.getAttribute(Constants.ATT_NAME).equals(name))
            {
                parmSet.removeChild(edit);
                break;
            }
        }
        if (parmSet.getChildNodes().getLength() == 0) // no more edits, remove
        {
            Node parent = parmSet.getParentNode();
            parent.removeChild(parmSet);
        }
    }
}
