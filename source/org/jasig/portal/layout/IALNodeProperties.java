/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;

import java.util.Hashtable;
import java.util.Vector;

import org.jasig.portal.layout.restrictions.IUserLayoutRestriction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * An interface describing methods common to the Aggregated Layout node descriptions
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version 1.0
 */
public interface IALNodeProperties {

    /**
     * Set fragment id
     *
     * @param fragmentId a <code>String</code> value
     */
    public void setFragmentId ( String fragmentId );

    /**
     * Get fragment id
     *
     * @return a <code>String</code> value
     */
    public String getFragmentId();

    /**
     * Set fragment node id
     *
     * @param fragmentNodeId a <code>String</code> value
     */
    public void setFragmentNodeId ( String fragmentNodeId );

    /**
     * Get fragment node id
     *
     * @return a <code>String</code> value
     */
    public String getFragmentNodeId();

    /**
     * Sets the group identificator for this node.
     * @param group a <code>String</code> group identificator value
     */
    public void setGroup ( String group );

    /**
     * Gets the priority value for this node.
     */
    public String getGroup();


    /**
     * Sets the hashtable of restrictions bound to this node
     * @param restrictions a <code>Hashtable</code> of restriction expressions
     */
    public void setRestrictions ( Hashtable restrictions );

    /**
     * Gets the hashtable of restrictions bound to this node
     * @return a set of restriction expressions
     */
    public Hashtable getRestrictions ();


    /**
     * Adds the restriction for this node.
     * @param restriction a <code>IUserLayoutRestriction</code> a restriction
     */
    public void addRestriction( IUserLayoutRestriction restriction );

    /**
     * Gets a restriction by the type.
     * @param restrictionName a <code>String</code>  name of the restriction
     * @return a IUserLayoutRestriction
     */
    public IUserLayoutRestriction getRestriction( String restrictionName );

    /**
     * Gets a restrictions list by a restriction path.
     * @param restrictionPath a <code>String</code> restriction path
     * @return a IUserLayoutRestriction
     */
    public Vector getRestrictionsByPath( String restrictionPath );

    public void addRestrictionChildren(Element node, Document root);

}
