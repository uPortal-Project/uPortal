/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;

import java.util.Hashtable;
import java.util.Vector;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.restrictions.IUserLayoutRestriction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



/**
 * An extension of the ChannelDescription for the Aggregated Layout implementation
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version 1.0
 */
public class ALChannelDescription extends UserLayoutChannelDescription implements IALChannelDescription {

    protected ALNodeProperties alproperties=new ALNodeProperties();

    public ALChannelDescription() {
        super();
    }

    public ALChannelDescription(Element xmlNode) throws PortalException {
        super(xmlNode);
    }

    public ALChannelDescription(IUserLayoutChannelDescription d) {
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
     * @param fragmentId a <code>String</code> value
     */
    public void setFragmentId ( String fragmentId ) {
        this.alproperties.setFragmentId(fragmentId);
    }

    /**
     * Get fragment id
     *
     * @return a <code>String</code> value
     */
    public String getFragmentId() {
        return this.alproperties.getFragmentId();
    }

    /**
     * Set fragment node id
     *
     * @param fragmentNodeId a <code>String</code> value
     */
    public void setFragmentNodeId ( String fragmentNodeId ) {
        this.alproperties.setFragmentNodeId(fragmentNodeId);
    }

    /**
     * Get fragment node id
     *
     * @return a <code>String</code> value
     */
    public String getFragmentNodeId() {
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
    public Vector getRestrictionsByPath( String restrictionPath ) {
        return this.alproperties.getRestrictionsByPath(restrictionPath);
    }

    public void addRestrictionChildren(Element node, Document root) {
        this.alproperties.addRestrictionChildren(node,root);
    }
}
