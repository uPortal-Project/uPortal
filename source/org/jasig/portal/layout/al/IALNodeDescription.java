/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.al;

import java.util.Collection;
import org.jasig.portal.layout.al.common.restrictions.ILayoutRestriction;
import org.jasig.portal.layout.al.common.restrictions.RestrictionPath;
import org.jasig.portal.layout.al.common.restrictions.RestrictionType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * An interface describing methods common to the Aggregated Layout node descriptions
 *
 * @author Michael Ivanov: mvi at immagic.com
 * @version 1.0
 */
public interface IALNodeDescription {

    /**
     * Set fragment id
     *
     * @param fragmentId a fragment id
     */
    public void setFragmentId ( IFragmentId fragmentId );

    /**
     * Get fragment id
     *
     * @return an id of the fragment to which the node belongs
     */
    public IFragmentId getFragmentId();

    /**
     * Set fragment-local node id
     *
     * @param an id of the node within the fragment
     */
    public void setFragmentNodeId ( IFragmentLocalNodeId fragmentNodeId );

    /**
     * Get fragment-local node id
     *
     * @return an id of the fragment within the fragment
     */
    public IFragmentLocalNodeId getFragmentNodeId();

    /**
     * Determine if a given node is a root of the fragment to
     * which it belongs
     * @return <code>true</code> if the node is a root of its fragment
     */
    public boolean isFragmentRoot();
    
    /**
     * Specify if a given node is a root of the fragment to which
     * it belongs.
     * @param value <code>true</code> if the node is a root of its fragment
     */
    public void setFragmentRoot(boolean value);
    
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
     * @param restrictions a <code>Collection</code> of restriction expressions
     */
    public void setRestrictions ( Collection restrictions );

    /**
     * Gets the hashtable of restrictions bound to this node
     * @return a set of restriction expressions
     */
    public Collection getRestrictions ();


    /**
     * Adds the restriction for this node.
     * @param restriction a <code>IUserLayoutRestriction</code> a restriction
     */
    public void addRestriction( ILayoutRestriction restriction );

    /**
     * Gets a restriction by the name and the restriction path.
     * @param restrictionType a <code>RestrictionType</code> restriction type
     * @param restrictionPath a <code>RestrictionPath</code> restriction path
     * @return a IUserLayoutRestriction
     */
    public ILayoutRestriction getRestriction( RestrictionType restrictionType, RestrictionPath restrictionPath );
    
    /**
     * Gets a local restriction by the given name.
     * @param restrictionType a <code>RestrictionType</code> restriction type
     * @return a IUserLayoutRestriction
     */
    public ILayoutRestriction getLocalRestriction( RestrictionType restrictionType );

    /**
     * Gets a restrictions list by a restriction path.
     * @param restrictionPath a <code>RestrictionPath</code> restriction path
     * @return a IUserLayoutRestriction
     */
    public Collection getRestrictionsByPath( RestrictionPath restrictionPath );

    public void addRestrictionChildren(Element node, Document root);

}
