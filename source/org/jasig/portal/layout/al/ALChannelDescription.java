/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al;

import java.util.Collection;
import org.jasig.portal.PortalException;
import org.jasig.portal.layout.al.common.node.ChannelDescriptionImpl;
import org.jasig.portal.layout.al.common.node.IChannelDescription;
import org.jasig.portal.layout.al.common.restrictions.IUserLayoutRestriction;
import org.jasig.portal.layout.al.common.restrictions.RestrictionPath;
import org.jasig.portal.layout.al.common.restrictions.RestrictionType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



/**
 * An extension of the ChannelDescription for the Aggregated Layout implementation
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version 1.0
 */
public class ALChannelDescription extends ChannelDescriptionImpl implements IALChannelDescription {

    protected ALNodeProperties alproperties=new ALNodeProperties();

    public ALChannelDescription() {
        super();
    }

    public ALChannelDescription(Element xmlNode) throws PortalException {
        super(xmlNode);
    }

    public ALChannelDescription(IChannelDescription d) {
        super(d);
        if(d instanceof IALChannelDescription) {
            this.alproperties=new ALNodeProperties((IALChannelDescription)d);
        } else {
            this.alproperties=new ALNodeProperties();
        }
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
     * @param fragmentNodeId an id of the node within a fragment
     */
    public void setFragmentNodeId ( IFragmentLocalNodeId fragmentNodeId ) {
        this.alproperties.setFragmentNodeId(fragmentNodeId);
    }

    /**
     * Get fragment-local node id
     *
     * @return an id of the node within a fragment
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
     * Sets the collection of restrictions bound to this node
     * @param restrictions a <code>Collection</code> of restriction expressions
     */
    public void setRestrictions ( Collection restrictions ) {
        this.alproperties.setRestrictions(restrictions);
    }

    /**
     * Gets the collection of restrictions bound to this node
     * @return a set of restriction expressions
     */
    public Collection getRestrictions () {
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
     * Gets a restriction by the given type and path.
     * @param restrictionType a <code>RestrictionType</code> restriction type
     * @param restrictionPath a <code>RestrictionPath</code>  restriction path
     * @return a IUserLayoutRestriction
     */
    public IUserLayoutRestriction getRestriction(RestrictionType restrictionType,RestrictionPath restrictionPath) {
            return this.alproperties.getRestriction(restrictionType,restrictionPath);
    }
    /**
     * Gets a restriction by the type.
     * @param restrictionType a <code>RestrictionType</code> restriction type
     * @return a IUserLayoutRestriction
     */
    public IUserLayoutRestriction getLocalRestriction(RestrictionType restrictionType) {
            return this.alproperties.getLocalRestriction(restrictionType);
    }
    /**
     * Gets a restrictions list by a restriction path.
     * @param restrictionPath a <code>RestrictionPath</code> restriction path
     * @return a IUserLayoutRestriction
     */
    public Collection getRestrictionsByPath( RestrictionPath restrictionPath ) {
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
