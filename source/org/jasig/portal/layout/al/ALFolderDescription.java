/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al;

import java.util.Hashtable;
import java.util.List;
import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.node.FolderDescriptionImpl;
import org.jasig.portal.layout.al.common.node.IFolderDescription;
import org.jasig.portal.layout.al.common.restrictions.IUserLayoutRestriction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



/**
 * An extension of the FolderDescription for the Aggregated Layout implementation
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version 1.0
 */
public class ALFolderDescription extends FolderDescriptionImpl implements IALFolderDescription {

    protected ALNodeProperties alproperties;

    public ALFolderDescription() {
        super();
        alproperties=new ALNodeProperties();
    }

    public ALFolderDescription(IFolderDescription d) {
        super(d);
        if(d instanceof IALFolderDescription) {
            alproperties=new ALNodeProperties((IALFolderDescription)d);
        } else {            
            this.alproperties=new ALNodeProperties();
        }
    }

    public ALFolderDescription(Element xmlNode) throws PortalException {
        super(xmlNode);
        alproperties=new ALNodeProperties();
    }

    /**
     * Set fragment id
     *
     * @param fragmentId a fragment id
     */
    public void setFragmentId ( IFragmentId fragmentId ) {
        this.alproperties.setFragmentId(fragmentId);
    }

    /**
     * Get fragment id
     *
     * @return a fragment id
     */
    public IFragmentId getFragmentId() {
        return this.alproperties.getFragmentId();
    }

    /**
     * Set fragment-local node id
     *
     * @param fragmentNodeId an id of the node within its fragment
     */
    public void setFragmentNodeId ( IFragmentLocalNodeId fragmentNodeId ) {
        this.alproperties.setFragmentNodeId(fragmentNodeId);
    }

    /**
     * Get fragment-local node id
     *
     * @return an id of the node within its fragment
     */
    public IFragmentLocalNodeId getFragmentNodeId() {
        return this.alproperties.getFragmentNodeId();
    }

    /**
     * Sets the group identificator for this node.
     * @param group a <code>String</code> group identificator value
     */
    public void setGroup ( String group ) {
        this.alproperties.setGroup(group);
    }

    /**
     * Gets the priority value for this node.
     */
    public String getGroup() {
        return this.alproperties.getGroup();
    }


    /**
     * Sets the hashtable of restrictions bound to this node
     * @param restrictions a <code>Hashtable</code> of restriction expressions
     */
    public void setRestrictions ( Hashtable restrictions ) {
        this.alproperties.setRestrictions(restrictions);
    }

    /**
     * Gets the hashtable of restrictions bound to this node
     * @return a set of restriction expressions
     */
    public Hashtable getRestrictions () {
        return this.alproperties.getRestrictions();
    }


    /**
     * Adds the restriction for this node.
     * @param restriction a <code>IUserLayoutRestriction</code> a restriction
     */
    public void addRestriction( IUserLayoutRestriction restriction ) {
        this.alproperties.addRestriction(restriction);
    }

    /**
     * Gets a restriction by the type.
     * @param restrictionName a <code>String</code>  name of the restriction
     * @return a IUserLayoutRestriction
     */
    public IUserLayoutRestriction getRestriction( String restrictionName ) {
        return this.alproperties.getRestriction(restrictionName);
    }

    /**
     * Gets a restrictions list by a restriction path.
     * @param restrictionPath a <code>String</code> restriction path
     * @return a IUserLayoutRestriction
     */
    public List getRestrictionsByPath( String restrictionPath ) {
        return this.alproperties.getRestrictionsByPath(restrictionPath);
    }

    public void addRestrictionChildren(Element node, Document root) {
        this.alproperties.addRestrictionChildren(node,root);
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.IALNodeProperties#isFragmentRoot()
     */
    public boolean isFragmentRoot() {
        return this.alproperties.isFragmentRoot();
    }
    /* (non-Javadoc)
     * @see org.jasig.portal.layout.al.IALNodeProperties#setFragmentRoot(boolean)
     */
    public void setFragmentRoot(boolean value) {
        this.alproperties.setFragmentRoot(value);
    }
}
