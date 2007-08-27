/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ALChannelDescription composed of [");
        sb.append(super.toString());
        sb.append("] and [");
        sb.append(this.alproperties);
        sb.append("]");
        return sb.toString();
    }
}
