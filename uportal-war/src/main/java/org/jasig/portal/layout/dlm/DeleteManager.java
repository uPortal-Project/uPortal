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

package org.jasig.portal.layout.dlm;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.spring.locator.UserLayoutStoreLocator;
import org.jasig.portal.xml.XmlUtilitiesImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** 
 * Looks for, applies against the ilf, and updates accordingly the delete
 * set within a plf.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class DeleteManager
{
    public static final String RCS_ID = "@(#) $Header$";
    private static final Logger LOG = LoggerFactory.getLogger(DeleteManager.class);

    private static IUserLayoutStore dls = null;

    /**
     * Hands back the single instance of RDBMDistributedLayoutStore. There is
     * already a method
     * for aquiring a single instance of the configured layout store so we
     * delegate over there so that all references refer to the same instance.
     * This method is solely for convenience so that we don't have to keep
     * calling UserLayoutStoreFactory and casting the resulting class.
     */
    private static IUserLayoutStore getDLS()
    {
        if ( dls == null )
        {
            dls = UserLayoutStoreLocator.getUserLayoutStore();
        }
        return dls;
    }

    /**
       Get the delete set if any from the plf and process each delete command
       removing any that fail from the delete set so that the delete set is
       self cleaning.
    */
    static void applyAndUpdateDeleteSet( Document plf,
                                         Document ilf,
                     IntegrationResult result )
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Attempting to apply and update delete set for plf {} and ilf {} with IntegrationResult {}.",
                    XmlUtilitiesImpl.toString(plf), XmlUtilitiesImpl.toString(ilf), result);
        }

        Element dSet = null;
        try
        {
            dSet = getDeleteSet( plf, null, false );
        }
        catch( Exception e )
        {
            LOG.error("In applyAndUpdateDeleteSet: exception getting DLM delete-set for plf {} so doing nothing.",
                    XmlUtilitiesImpl.toString(plf), e);
        }

        if ( dSet == null ) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("applyAndUpdateDeleteset: DeleteSet for plf {} null so no delete set to apply. " +
                    "Doing nothing.",
                    XmlUtilitiesImpl.toString(plf) );
            }
            return;
        }

        NodeList deletes = dSet.getChildNodes();

        for( int i=deletes.getLength()-1; i>=0; i-- )
        {
            Element deleteElement = (Element) deletes.item(i);
            if ( applyDelete( deleteElement, ilf ) == false )
            {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Failed to apply delete {}, removing it from delete set.",
                        XmlUtilitiesImpl.toString(deleteElement) );
                }
                dSet.removeChild( deletes.item(i) );
                result.changedPLF = true;
            }
            else
            {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Successfully applied delete {}. NOT removing it from delete set.",
                        XmlUtilitiesImpl.toString(deleteElement) );
                }
                result.changedILF = true;
            }
        }

        if ( dSet.getChildNodes().getLength() == 0 )
        {
            LOG.debug("Delete set {} is now empty.  removing the delete set from plf {}.",
                    dSet, plf);
            plf.getDocumentElement().removeChild( dSet );
            result.changedPLF = true;
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("applyAndUpdateDeleteSet(plf {} ilf {} IntegrationResult {}) returning.",
                XmlUtilitiesImpl.toString(plf), XmlUtilitiesImpl.toString(ilf), result);
        }
    }

    /**
       Attempt to apply a single delete command and return true if it succeeds
       or false otherwise. If the delete is disallowed or the target element
       no longer exists in the document the delete command fails and returns
       false.
    */
    private static boolean applyDelete( Element delete, Document ilf )
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace("applyDelete( delete command {} in document {}).",
                    XmlUtilitiesImpl.toString(delete), XmlUtilitiesImpl.toString(ilf) );
        }

        String nodeID = delete.getAttribute( Constants.ATT_NAME );

        Element e = ilf.getElementById( nodeID );

        if ( e == null ) {
            LOG.warn("In applying delete {} could not find target node with ID {} in ilf {} so doing nothing.",
                    XmlUtilitiesImpl.toString(delete), nodeID, XmlUtilitiesImpl.toString(ilf));
            return false;
        }

        String deleteAllowed = e.getAttribute( Constants.ATT_DELETE_ALLOWED );
        if ( deleteAllowed.equals( "false" ) ) {
            LOG.info("In applying delete {} target node {} had attribute {} with value 'false' so doing nothing",
                    XmlUtilitiesImpl.toString(delete), XmlUtilitiesImpl.toString(e), Constants.ATT_DELETE_ALLOWED);
            return false;
        }

        Element p = (Element) e.getParentNode();
        e.setIdAttribute(Constants.ATT_ID, false);
        p.removeChild( e );

        LOG.debug("Removed child node {} per delete {}; undeclared attribute {} on {} as an ID attribute",
                XmlUtilitiesImpl.toString(e), XmlUtilitiesImpl.toString(delete), Constants.ATT_ID, e);
        return true;
    }

    /**
       Get the delete set if any stored in the root of the document or create
       it if passed in create flag is true.
    */
    private static Element getDeleteSet( Document plf,
                                         IPerson person,
                                         boolean create )
        throws PortalException
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace("entering getDeleteSet() from layout document {} with create flag {})",
                XmlUtilitiesImpl.toString(plf), create);
        }

        Node root = plf.getDocumentElement();
        Node child = root.getFirstChild();

        while( child != null )
        {
            if ( child.getNodeName().equals( Constants.ELM_DELETE_SET ) ) {

                if (LOG.isTraceEnabled()) {
                    LOG.trace("Found and returning delete set {}.",
                        XmlUtilitiesImpl.toString(child));
                }
                return (Element) child;
            }
            child = child.getNextSibling();
        }

        if ( create == false ) {
            LOG.trace("getDeleteSet did not find node named {} in plf and create flag is false; returning null",
                    Constants.ELM_DELETE_SET);
            return null;
        }

        String ID = null;

        try
        {
            ID = getDLS().getNextStructDirectiveId( person );
        }
        catch (Exception e)
        {
            throw new PortalException( "Exception encountered while " +
                                       "generating new delete set node " +
                                       "Id for user [" + person + "].", e );
        }
        Element delSet = plf.createElement( Constants.ELM_DELETE_SET );
        delSet.setAttribute( Constants.ATT_TYPE,
                     Constants.ELM_DELETE_SET );
        delSet.setAttribute( Constants.ATT_ID, ID );
        root.appendChild( delSet );
        LOG.debug("Added new empty delete set {} to plf {}.", delSet, plf);
        return delSet;
    }

    /**
       Create and append a delete directive to delete the node identified by
       the passed in element id. If this node contains any incorporated
       elements then they must also have a delete directive added in here to
       prevent incorporated channels originating in another column from
       reappearing in that column because the position set entry that pulled
       them into this column was now removed. (ie: the user moved an inc'd
       channel to this column and then deleted the column means that the inc'd
       channel should be deleted also.) This was designed to add a delete
       directive for each nested element having an ID so as to work for the
       future case of a tree view.
    */
    public static void addDeleteDirective( Element compViewNode,
                                           String elementID,
                                           IPerson person )
        throws PortalException
    {
        LOG.trace("addDeleteDirective to delete element id {} for person {} with compViewNode {}",
                elementID, person, XmlUtilitiesImpl.toString(compViewNode));

        Document plf = (Document) person.getAttribute( Constants.PLF );
        Element delSet = getDeleteSet( plf, person, true );
        addDeleteDirective( compViewNode, elementID, person, plf, delSet );
    }

    /**
       This method does the actual work of adding a delete directive and then
       recursively calling itself for any incorporated children that need to be
       deleted as well.
    */
    private static void addDeleteDirective( Element compViewNode,
                                            String elementID,
                                            IPerson person,
                                            Document plf,
                                            Element delSet )
        throws PortalException
    {
        LOG.trace("addDeleteDirective to delete {} for user {} with plf {} " +
                "amending delete set {} with comp view {}.",
                elementID, person.getID(), XmlUtilitiesImpl.toString(plf),
                XmlUtilitiesImpl.toString(delSet), XmlUtilitiesImpl.toString(compViewNode));

        String ID = null;

        try
        {
            ID = getDLS().getNextStructDirectiveId( person );
        }
        catch (Exception e)
        {
            throw new PortalException( "Exception encountered while " +
                                       "generating new delete node " +
                                       "Id for userId=" + person.getID(), e );
        }
        Element delete = plf.createElement( Constants.ELM_DELETE );
        delete.setAttribute( Constants.ATT_TYPE, Constants.ELM_DELETE );
        delete.setAttribute( Constants.ATT_ID, ID );
        delete.setAttributeNS( Constants.NS_URI,
                       Constants.ATT_NAME, elementID );
        delSet.appendChild( delete );

        LOG.trace("Appended {} to delete set {}",
                XmlUtilitiesImpl.toString(delete), XmlUtilitiesImpl.toString(delSet) );

        // now pass through children and add delete directives for those with
        // IDs indicating that they were incorporated
        Element child = (Element) compViewNode.getFirstChild();

        while( child != null )
        {
            String childID = child.getAttribute( "ID" );
            if ( childID.startsWith( Constants.FRAGMENT_ID_USER_PREFIX ) ) {
                LOG.trace("Because child of deleted node {} has id {} starting with {}, " +
                        "adding delete directive for it.",
                        XmlUtilitiesImpl.toString(child), childID, Constants.FRAGMENT_ID_USER_PREFIX);
                addDeleteDirective(child, childID, person, plf, delSet);
            }
            child = (Element) child.getNextSibling();
        }

        LOG.trace("Finished adding delete directive to delete {} for user {} with plf {}",
                elementID, person, plf);
    }
}
