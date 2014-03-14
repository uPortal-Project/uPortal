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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.spring.locator.UserLayoutStoreLocator;
import org.jasig.portal.xml.XmlUtilitiesImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Applies and updates position specifiers for child nodes in the
 * composite layout.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class PositionManager
{
    public static final String RCS_ID = "@(#) $Header$";
    private static Logger LOG = LoggerFactory.getLogger(PositionManager.class);

    private static IUserLayoutStore dls = null;
    /**
     * Hands back the single instance of RDBMDistributedLayoutStore. There is
     * already a method
     * for aquiring a single instance of the configured layout store so we
     * delegate over there so that all references refer to the same instance.
     * This method is solely for convenience so that we don't have to keep
     * calling UserLayoutStoreFactory and casting the resulting class.
     */
    private static IUserLayoutStore getDLS() {
        if (dls == null) {
            dls = UserLayoutStoreLocator.getUserLayoutStore();
        }
        return dls;
    }
    
    /**
       This method and ones that it delegates to have the responsibility of
       organizing the child nodes of the passed in composite view parent node
       according to the order specified in the passed in position set and
       return via the passed in result set whether the personal layout
       fragment (one portion of which is the position set) or the incoporated
       layouts fragment (one portion of which is the compViewParent) were
       changed.

       This may also include pulling nodes in from other parents under certain
       circumstances. For example, if allowed a user can move nodes that are
       not part of their personal layout fragment or PLF; the UI elements that
       they own. These node do not exist in their layout in the database but
       instead are merged in with their owned elements at log in and other
       times. So to move them during subsequent merges a position set can
       contain a position directive indicating the id of the node to be moved
       into a specific position in the sibling list and that well may refer to
       a node not in the sibling list to begin with. If the node no longer
       exists in the composite view then that position directive can safely be
       discarded.

       Positioning is meant to preserve as much as possible the user's
       specified ordering of user interface elements but always respecting
       movement restrictions placed on those elements that are incorporated by
       their owners. So the following rules apply from most important to least.

       1) nodes with moveAllowed="false" prevent nodes of lower precedence from
       being to their left or higher with left or higher defined as having a
       lower index in the sibling list. (applyLowerPrecRestriction)

       2) nodes with moveAllowed="false" prevent nodes of equal precedence from
       moving from one side of this node to the other from their position as
       found in the compViewParent initially and prevents nodes with the same
       precedence from moving from other parents into this parent prior to the
       restricted node. Prior to implies a lower sibling index.
       (applyHoppingRestriction)

       3) nodes with moveAllowed="false" prevent nodes of equal precedence
       lower in the sibling list from being reparented. (ie: moving to another
       parent) However, they can be deleted. (applyReparentingCheck)

       4) nodes should be ordered as much as possible in the order specified by
       the user but in view of the above conditions. So if a user has moved
       nodes thus specifying some order and the owner of some node in that set
       then locks one of those nodes some of those nodes will have to move
       back to their orinial positions to conform with the rules above but for
       the remaining nodes they should be found in the same relative order
       specified by the user. (getOrder)

       5) nodes not included in the order specified by the user (ie: nodes
       added since the user last ordered them) should maintain their relative
       order as much as possible and be appended to the end of the sibling
       list after all others rules have been applied. (getOrder)

       Each of these rules is applied by a call to a method 5 being first and
       1 being last so that 1 has the highest precedence and last say. Once
       the final ordering is specified then it is applied to the children of
       the compViewParent and returned.
     */
    static void applyPositions( Element compViewParent,
                                Element positionSet,
                                IntegrationResult result )
        throws PortalException
    {

        if (LOG.isTraceEnabled()) {
            LOG.trace("applyPositions applying positionSet {} to parent {}.",
                    XmlUtilitiesImpl.toString(positionSet),
                    XmlUtilitiesImpl.toString(compViewParent) );
        }

        if (positionSet == null) {
            LOG.trace("Cannot apply a null position set, doing nothing.");
            return;
        }

        if (positionSet.getFirstChild() == null) {
            LOG.trace("Position set was empty so nothing to apply, doing nothing.");
            return;
        }

        List<NodeInfo> order = new ArrayList<NodeInfo>();

        applyOrdering        ( order, compViewParent, positionSet );
        applyNoReparenting   ( order, compViewParent, positionSet );
        applyNoHopping       ( order, compViewParent, positionSet );
        applyLowerPrecedence ( order, compViewParent, positionSet );
        evaluateAndApply     ( order, compViewParent, positionSet, result );

        LOG.trace("Completed applying positionSet {} to parent {} with result {}",
                positionSet, compViewParent, result);
    }

    /**
       This method determines if applying all of the positioning rules and
       restrictions ended up making changes to the compViewParent or the
       original position set. If changes are applicable to the CVP then they
       are applied. If the position set changed then the original stored in the
       PLF is updated.
     */
    static void evaluateAndApply( List<NodeInfo> order,
                                  Element compViewParent,
                                  Element positionSet,
                                  IntegrationResult result )
        throws PortalException
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace("evaluateAndApply order {}, positionSet {}, parent {}, result {}",
                    order,
                    XmlUtilitiesImpl.toString(positionSet),
                    XmlUtilitiesImpl.toString(compViewParent),
                    result);
        }

        adjustPositionSet(order, positionSet, result);

        if ( hasAffectOnCVP( order, compViewParent ) )
        {
            applyToNodes( order, compViewParent );
            result.changedILF = true;
        }


        if (LOG.isTraceEnabled()) {
            LOG.trace("Finished evalauteAndApply positionSet {} with order {} on {} with result {}",
                XmlUtilitiesImpl.toString(positionSet),
                order,
                XmlUtilitiesImpl.toString(compViewParent),
                result);
        }
    }

    /**
       This method trims down the position set to the position directives on
       the node info elements still having a position directive. Any directives
       that violated restrictions were removed from the node info objects so
       the position set should be made to match the order of those still
       having one.
     */
    static void adjustPositionSet( List<NodeInfo> order,
                                   Element positionSet,
                                   IntegrationResult result )
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace("adjustPositionSet adjusting set {} given order {}.",
                    XmlUtilitiesImpl.toString(positionSet), order);
        }

        Node nodeToMatch = positionSet.getFirstChild();
        Element nodeToInsertBefore = positionSet.getOwnerDocument()
        .createElement( "INSERT_POINT" );
        positionSet.insertBefore( nodeToInsertBefore, nodeToMatch );

        for ( Iterator iter = order.iterator();
              iter.hasNext(); )
        {
            NodeInfo ni = (NodeInfo) iter.next();

            if ( ni.positionDirective != null )
            {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Considering position directive {} in NodeInfo {}",
                        XmlUtilitiesImpl.toString(ni.positionDirective), ni);
                }

                // found one check it against the current one in the position
                // set to see if it is different. If so then indicate that
                // something (the position set) has changed in the plf
                if ( ni.positionDirective != nodeToMatch ) {
                    result.changedPLF = true;
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Position directive {} did not match {} so result {} marked PLF changed.",
                            XmlUtilitiesImpl.toString(ni.positionDirective),
                            XmlUtilitiesImpl.toString(nodeToMatch),
                            result);
                    }
                }

                // now bump the insertion point forward prior to
                // moving on to the next position node to be evaluated
                if ( nodeToMatch != null ) {
                    nodeToMatch = nodeToMatch.getNextSibling();

                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Node to match is now {}, the next sibling of the previous node to match.",
                            XmlUtilitiesImpl.toString(nodeToMatch) );
                    }
                }

                // now insert it prior to insertion point
                positionSet.insertBefore( ni.positionDirective,
                                          nodeToInsertBefore );
            } else {
                LOG.trace("{} had no position directive so ignoring it.", ni);
            }
        }

        // now for any left over after the insert point remove them.

        while ( nodeToInsertBefore.getNextSibling() != null ) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Removing leftover-after-the-insertion-point node {} from positionSet {}.",
                   XmlUtilitiesImpl.toString(nodeToInsertBefore.getNextSibling()),
                   XmlUtilitiesImpl.toString(positionSet) );
            }
            positionSet.removeChild(nodeToInsertBefore.getNextSibling());
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("Removing the insertion point {} from position set {}",
                XmlUtilitiesImpl.toString(nodeToInsertBefore),
                XmlUtilitiesImpl.toString(positionSet) );
        }
        positionSet.removeChild( nodeToInsertBefore );

        if (LOG.isTraceEnabled()) {
            LOG.trace("completed adjustPositionSet {} given order {} with result {}.",
                XmlUtilitiesImpl.toString(positionSet), order, result);
        }
    }

    /**
       This method compares the children by id in the order list with
       the order in the compViewParent's ui visible children and returns true
       if the ordering differs indicating that the positioning if needed.
     */
    static boolean hasAffectOnCVP( List<NodeInfo> order,
                                   Element compViewParent )
    {

        if ( order.size() == 0 ) {
            LOG.trace("Empty order {} has no effect on {}",
                    order, compViewParent);
            return false;
        }
        
        
        int idx = 0;
        Element child = (Element) compViewParent.getFirstChild();
        NodeInfo ni = (NodeInfo) order.get( idx );

        if ( child == null && ni != null ) { // most likely nodes to be pulled in
            if (LOG.isTraceEnabled()) {
                LOG.trace("Order {} has effect on {} because that node has no child but order is not empty.",
                    order, XmlUtilitiesImpl.toString(compViewParent) );
            }
            return true;
        }
        
        while ( child != null )
        {
            if ( child.getAttribute( "hidden" ).equals( "false" ) &&
                 ( ! child.getAttribute( "chanID" ).equals( "" ) ||
                   child.getAttribute( "type" ).equals( "regular" ) ) )
            {
                if ( ni.id.equals( child.getAttribute( Constants.ATT_ID ) ) )
                {
                    if ( idx >= order.size()-1 ) {// at end of order list
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("Order {} has no effect on {} " +
                                "because all order elements matched corresponding children of the parent.",
                                order, XmlUtilitiesImpl.toString(compViewParent) );
                        }
                        return false;
                    }

                    ni = (NodeInfo) order.get( ++idx );
                }
                else {// if not equal then return true
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Order {} has effect on {} because an order element {} id " +
                            "didn't match the {} attribute " +
                            "of the corresponding child {} of the parent element.",
                            order,
                            XmlUtilitiesImpl.toString(compViewParent),
                            ni,
                            Constants.ATT_ID,
                            XmlUtilitiesImpl.toString(child) );
                    }
                    return true;
                }
            } else {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Ignoring child {} because either it is hidden or " +
                        "it is a folder of a type other than regular.",
                        XmlUtilitiesImpl.toString(child) );
                }
            }
            child = (Element) child.getNextSibling();
        }
        if ( idx < order.size() ) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Order {} affects {}; there are nodes to be pulled in.",
                    order, XmlUtilitiesImpl.toString(compViewParent) );
            }
            return true; // represents nodes to be pulled in
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("Order {} does not affect {}",
                order, XmlUtilitiesImpl.toString(compViewParent) );
        }
        return false;
    }

    /**
       This method applies the ordering specified in the passed in order list
       to the child nodes of the compViewParent. Nodes specified in the list
       but located elsewhere are pulled in.
     */
    static void applyToNodes( List<NodeInfo> order,
                              Element compViewParent )
    {
        LOG.trace("Applying order {} to the children of {}", order, compViewParent);
        // first set up a bogus node to assist with inserting
        Node insertPoint = compViewParent.getOwnerDocument()
        .createElement( "bogus" );
        Node first = compViewParent.getFirstChild();

        if ( first != null )
            compViewParent.insertBefore( insertPoint, first );
        else
            compViewParent.appendChild( insertPoint );

        // now pass through the order list inserting the nodes as you go
        for ( int i = 0; i<order.size(); i++ )
            compViewParent.insertBefore( ( (NodeInfo) order.get( i ) ).node,
                                         insertPoint );

        compViewParent.removeChild( insertPoint );

        LOG.trace("Applied order {} to the children of {}", order, compViewParent);
    }                                 

    /**
       This method is responsible for preventing nodes with lower precedence
       from being located to the left (lower sibling order) of nodes having a
       higher precedence and moveAllowed="false".
     */
    static void applyLowerPrecedence( List<NodeInfo> order,
                                      Element compViewParent,
                                      Element positionSet )
    {
        LOG.trace("Apply lower precendence for order {} and positionSet {} on parent {}",
                order, positionSet, compViewParent);
        for ( int i = 0; i<order.size(); i++ )
        {
            NodeInfo ni = (NodeInfo) order.get( i );
            if ( ni.node.getAttribute( Constants.ATT_MOVE_ALLOWED )
                 .equals( "false" ) )
            {
                for ( int j=0; j<i; j++ )
                {
                    NodeInfo lefty = (NodeInfo) order.get( j );
                    if ( lefty.precedence == null ||
                         lefty.precedence.isLessThan( ni.precedence ) )
                    {
                        order.remove( j );
                        order.add( i, lefty );
                    }
                }
            }
        }

        LOG.trace("Applied lower precendence for order {} and positionSet {} on parent {}",
                order, positionSet, compViewParent);
    }                                 

    /**
       This method is responsible for preventing nodes with identical
       precedence in the same parent from hopping over each other so that a
       layout fragment can lock two tabs that are next to each other and they
       can only be separated by tabs with higher precedence.

       If this situation is detected then the positioning of all nodes
       currently in the compViewParent is left as they are found in the CVP
       with any nodes brought in from
       other parents appended at the end with their relative order preserved.
     */
    static void applyNoHopping( List<NodeInfo> order,
                                Element compViewParent,
                                Element positionSet )
    {
        // no trace log here because would be redundant with the more contextualized logging below.

        if ( isIllegalHoppingSpecified( order ) == true )
        {
            if (LOG.isInfoEnabled()) {
                LOG.info("applyNoHopping: Bad hop specified for order {} and positionSet {} on {} so enforcing.",
                    order, XmlUtilitiesImpl.toString(positionSet), XmlUtilitiesImpl.toString(compViewParent));
            }
            ArrayList cvpNodeInfos = new ArrayList();

            // pull those out of the position list from the CVP
            for ( int i = order.size()-1; i>=0; i-- )
                if ( ((NodeInfo) order.get( i )).indexInCVP != -1 )
                    cvpNodeInfos.add( order.remove( i ) );

            // what is left is coming from other parents. Now push them back in
            // in the order specified in the CVP

            Object[] nodeInfos = cvpNodeInfos.toArray();
            Arrays.sort( nodeInfos, new NodeInfoComparator() );
            List list = Arrays.asList( nodeInfos );
            order.addAll( 0, list );

            if (LOG.isInfoEnabled()) {
                LOG.info("applyNoHopping: After enforcing, bad-hop-free order {} and positionSet {} on {}.",
                    order, XmlUtilitiesImpl.toString(positionSet), XmlUtilitiesImpl.toString(compViewParent) );
            }
        } else {
            if (LOG.isTraceEnabled()) {
                LOG.trace("applyNoHopping: No bad hop specified for order {} and positionSet {} on {} " +
                        "so did nothing.",
                    order, XmlUtilitiesImpl.toString(positionSet), XmlUtilitiesImpl.toString(compViewParent));
            }
        }
    }

    /**
       This method determines if any illegal hopping is being specified.
       To determine if the positioning is specifying an ordering that will
       result in hopping I need to determine for each node n in the list if
       any of the nodes to be positioned to its right currently lie to its
       left in the CVP and have moveAllowed="false" and have the same
       precedence or if any of the nodes to be positioned to its left currently
       lie to its right in the CVP and have moveAllowed="false" and have the
       same precedence.

     */
    static boolean isIllegalHoppingSpecified( List<NodeInfo> order )
    {
        for ( int i=0; i< order.size(); i++ )
        {
            NodeInfo ni = ( NodeInfo ) order.get( i );

            // look for move restricted nodes
            if ( ! ni.node.getAttribute( Constants.ATT_MOVE_ALLOWED )
                 .equals( "false" ) )
                continue;

            // now check nodes in lower position to see if they "hopped" here
            // or if they have similar precedence and came from another parent.

            for ( int j=0; j<i; j++ )
            {
                NodeInfo niSib = ( NodeInfo ) order.get( j );

                // skip lower precedence nodes from this parent. These will get
                // bumped during the lower precedence check
                if ( niSib.precedence == Precedence.getUserPrecedence() )
                    continue;

                if ( niSib.precedence.isEqualTo( ni.precedence ) && 
                     ( niSib.indexInCVP == -1 || // from another parent
                     ni.indexInCVP < niSib.indexInCVP ) )  { // niSib hopping left


                    LOG.info("Order {} specifies illegal hopping because {} hops left of {}" +
                            " but relative precedence does not allow this.",
                            order, niSib, ni);

                    return true;
                }
            }

            // now check upper positioned nodes to see if they "hopped"
            
            for ( int j=i+1; j<order.size(); j++ )
            {
                NodeInfo niSib = ( NodeInfo ) order.get( j );

                // ignore nodes from other parents and user precedence nodes
                if ( niSib.indexInCVP == -1 ||
                     niSib.precedence == Precedence.getUserPrecedence() )
                    continue;
                
                if ( ni.indexInCVP > niSib.indexInCVP && // niSib hopped right
                     niSib.precedence.isEqualTo( ni.precedence ) ) {

                    LOG.info("Order {} specifies illegal hopping because {} hopped right of {}.",
                            order, niSib, ni);

                    return true;
                }
            }
        }

        LOG.trace("Order {} does not specify illegal hopping.",
                order);

        return false;
    }

    /**
       This method scans through the nodes in the ordered list and identifies
       those that are not in the passed in compViewParent. For those it then
       looks in its current parent and checks to see if there are any down-
       stream (higher sibling index) siblings that have moveAllowed="false".
       If any such sibling is found then the node is not allowed to be
       reparented and is removed from the list.
     */
    static void applyNoReparenting( List<NodeInfo> order,
                                    Element compViewParent,
                                    Element positionSet )
    {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Applying no-reparenting rules to order {} and positionSet {} for parent {}.",
                order, XmlUtilitiesImpl.toString(positionSet), XmlUtilitiesImpl.toString(compViewParent));
        }

        int i = 0;
        while ( i<order.size() )
        {
            NodeInfo ni = (NodeInfo) order.get( i );
            if ( ! ni.node.getParentNode().equals( compViewParent ) )
            {
                ni.differentParent = true;
                if ( isNotReparentable( ni ) )
                {

                    LOG.trace("{} should not be reparented. Zapping its position directive and removing from order.",
                            ni);

                    // this node should not be reparented. If it was placed
                    // here by way of a position directive then delete that
                    // directive out of the ni and posSet will be updated later
                    ni.positionDirective = null;

                    // now we need to remove it from the ordering list but
                    // skip incrementing i, deleted ni now filled by next ni
                    order.remove( i );
                    continue; 
                } else {
                    LOG.trace("{} is re-parent-able and so complies with re-parenting rules.", ni);
                }
            } else {
                LOG.trace("Node {} already has {} as parent so no re-parenting for this node to worry about.",
                        ni, compViewParent);
            }
            i++;
        }

        LOG.trace("Applied no-parenting rules resulting in order {} and positionSet {} for parent {}.",
                order, positionSet, compViewParent);
    }

    /**
       Return true if the passed in node or any of its up-stream (higher index
       siblings have moveAllowed="false".
     */
    private static boolean isNotReparentable( NodeInfo ni )
    {
        // no trace logging here because would be redundant with contextualized logging below

        if ( ni.node.getAttribute( Constants.ATT_MOVE_ALLOWED )
             .equals( "false" ) ) {
            LOG.trace("{} isNotReparentable because the node has value 'false' for attribute {}.",
                    ni, Constants.ATT_MOVE_ALLOWED);
            return true;
        }
        
        Precedence nodePrec = ni.precedence;
        Element node = (Element) ni.node.getNextSibling();
        
        while ( node != null )
        {
            if ( node.getAttribute( Constants.ATT_MOVE_ALLOWED )
                 .equals( "false" ) )
            {
                Precedence p = Precedence
                .newInstance( node.getAttribute( Constants.ATT_FRAGMENT ) );
                if ( nodePrec.isEqualTo( p ) ) {
                    LOG.trace("{} isNotReparentable because its precedence {} is equal to that of sibling {}.",
                            ni, p, node);
                    return true;
                }
            }
            node = (Element) node.getNextSibling();
        }

        LOG.trace("{} is re-parent-able.", ni);
        return false;
    }

    /**
       This method assembles in the passed in order object a list of NodeInfo
       objects ordered first by those specified in the position set and whose
       nodes still exist in the composite view and then by any remaining
       children in the compViewParent.
     */
    static void applyOrdering( List<NodeInfo> order,
                               Element compViewParent,
                               Element positionSet )
    {
        LOG.trace("applyOrdering order {} with positionSet {} on parent {}.",
                order, positionSet, compViewParent);

        // first pull out all visible channel or visible folder children and
        // put their id's in a list of available children and record their
        // relative order in the CVP.
        
        final Map<String, NodeInfo> available = new LinkedHashMap<String, NodeInfo>();

        Element child = (Element) compViewParent.getFirstChild();
        Element next = null;
        int indexInCVP = 0;
        
        while( child != null )
        {
            next = (Element) child.getNextSibling();
            
            if ( child.getAttribute( "hidden" ).equals( "false" ) &&
                 ( ! child.getAttribute( "chanID" ).equals( "" ) ||
                   child.getAttribute( "type" ).equals( "regular" ) ) ) {
                final NodeInfo nodeInfo = new NodeInfo( child,
                                             indexInCVP++ );
                
                final NodeInfo prevNode = available.put( nodeInfo.id, nodeInfo );
                if (prevNode != null) {
                    throw new IllegalStateException("Infinite loop detected in layout. Triggered by " + nodeInfo.id + " with already visited node ids: " + available.keySet());
                }
            } else {
                LOG.trace("Ignoring {} because either hidden or a folder of a type other than regular.", child);
            }
            child = next;
        }

        LOG.trace("Computed visible channel and folder children of {}  as {}.",
                compViewParent, available);

        // now fill the order list using id's from the position set if nodes
        // having those ids exist in the composite view. Otherwise discard
        // that position directive. As they are added to the list remove them
        // from the available nodes in the parent.
        
        Document CV = compViewParent.getOwnerDocument();
        Element directive = (Element) positionSet.getFirstChild();
        
        while ( directive != null )
        {
            next = (Element) directive.getNextSibling();

            // id of child to move is in the name attrib on the position nodes
            String id = directive.getAttribute( "name" );
            child = CV.getElementById( id );
            
            if ( child != null )
            {
                // look for the NodeInfo for this node in the available
                // nodes and if found use that one. Otherwise use a new that
                // does not include an index in the CVP parent. In either case
                // indicate the position directive responsible for placing this
                // NodeInfo object in the list.
                
                final String childId = child.getAttribute( Constants.ATT_ID );
                NodeInfo ni = available.remove(childId);
                if (ni == null) {
                    ni = new NodeInfo( child );
                }
                
                ni.positionDirective = directive;
                order.add( ni );

                LOG.trace("Computed order {} for node {}", ni, child);
            } else {
                LOG.debug("Ignoring directive {} regarding nodeID {} because could not find element with that ID.",
                        directive, id);
            }
            directive = next;
        }

        // now append any remaining ids from the available list maintaining
        // the order that they have there.

        order.addAll(available.values());

        LOG.trace("after applyOrdering order {} with positionSet {} on parent {}.",
                order, positionSet, compViewParent);
    }

    /**
       This method updates the positions recorded in a position set to reflect
       the ids of the nodes in the composite view of the layout. Any position
       nodes already in existence are reused to reduce database interaction
       needed to generate a new ID attribute. If any are left over after
       updating those position elements are removed. If no position set existed
       a new one is created for the parent. If no ILF nodes are found in the
       parent node then the position set as a whole is reclaimed.
    */
    public static void updatePositionSet( Element compViewParent,
                                          Element plfParent,
                                          IPerson person )
        throws PortalException
    {
        LOG.debug("Updating Position Set for element {} given plfParent {} for {}",
                compViewParent, plfParent, person);

        if ( compViewParent.getChildNodes().getLength() == 0 )
        {
            // no nodes to position. if set exists reclaim the space.
            Element positions = getPositionSet( plfParent, person, false );
            if ( positions != null ) {
                LOG.debug("No nodes to position.  Reclaiming positionSet {} from PLF {} for user {}",
                        positions, plfParent, person);
                plfParent.removeChild( positions );
            } else {
                LOG.debug("No nodes to position.  No existing positionSet in PLF {} to reclaim for user {}.",
                        plfParent, person);
            }
            return;
        }
        Element posSet = (Element) getPositionSet( plfParent, person, true );
        Element position = (Element) posSet.getFirstChild();
        Element viewNode = (Element) compViewParent.getFirstChild();
        boolean ilfNodesFound = false;
        
        while( viewNode != null )
        {
            String ID = viewNode.getAttribute( Constants.ATT_ID );
            String channelId = viewNode.getAttribute( Constants.ATT_CHANNEL_ID );
            String type = viewNode.getAttribute( Constants.ATT_TYPE );
            String hidden = viewNode.getAttribute( Constants.ATT_HIDDEN );

            if ( ID.startsWith( Constants.FRAGMENT_ID_USER_PREFIX ) )
            ilfNodesFound = true;
            
            if ( ! channelId.equals("") ||    // its a channel node or
             ( type.equals("regular") &&  // a regular, visible folder
               hidden.equals("false") ) )
            {
                if ( position != null ) {
                    position.setAttribute( Constants.ATT_NAME, ID );
                    LOG.trace("{} is a channel or a not-hidden, regular folder.  Targeted {} at it.",
                            viewNode, position);
                } else {
                    position = createAndAppendPosition( ID, posSet, person );
                    LOG.trace("{} is a channel or a not-hidden, regular folder.  Created and targeted {} at it.",
                            viewNode, position);
                }
                position = (Element) position.getNextSibling();
            } else {
                LOG.trace("updatePositionSet: {} is a hidden or non-type-regular folder.  Ignoring it.", viewNode);
            }

            viewNode = (Element) viewNode.getNextSibling();
        }

        if ( ilfNodesFound == false ) { // only plf nodes, no pos set needed
            plfParent.removeChild( posSet );
            LOG.debug("updatePositionSet: No ilf nodes found, so no position set needed.  Removed {} from {}.",
                    posSet, plfParent);
        } else
        {
            LOG.trace("updatePositionSet: ILF nodes were found, so reclaiming any leftover positions.");
            // reclaim any leftover positions
            while( position != null )
            {
                LOG.trace("Reclaiming leftover position {}.", position);
                Element nextPos = (Element) position.getNextSibling();
                posSet.removeChild( position );
                position = nextPos;
            }
        }
    }

    /**
       This method locates the position set element in the child list of the
       passed in plfParent or if not found it will create one automatically
       and return it if the passed in create flag is true.
    */
    private static Element getPositionSet( Element plfParent,
                                           IPerson person,
                                           boolean create )
        throws PortalException
    {
        Node child = plfParent.getFirstChild();

        while( child != null )
        {
            if ( child.getNodeName().equals( Constants.ELM_POSITION_SET ) ) {
                LOG.trace("getPositionSet: got {} from {} for {}.",
                        child, plfParent, person);
                return (Element) child;
            }
            child = child.getNextSibling();
        }
        if ( create == false ) {
            LOG.trace("getPositionSet: No existing position set in plf {} for user {}; create flag is false.",
                    plfParent, person);
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
                                       "generating new position set node " +
                                       "Id for userId=" + person.getID(),e );
        }
        Document plf = plfParent.getOwnerDocument();
        Element positions = plf.createElement( Constants.ELM_POSITION_SET );
        positions.setAttribute( Constants.ATT_TYPE,
                                Constants.ELM_POSITION_SET );
        positions.setAttribute( Constants.ATT_ID, ID );
        plfParent.appendChild( positions );

        LOG.trace("getPositionSet found no existing position set in plf {} so for user {} so added one.",
                plfParent, person);

        return positions;
    }

    /**
       Create, append to the passed in position set, and return a position
       element that references the passed in elementID.
    */
    private static Element createAndAppendPosition( String elementID,
                                                    Element positions,
                                                    IPerson person )
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
                                       "generating new position node " +
                                       "Id for userId=" + person.getID(), e );
        }
        Document plf = positions.getOwnerDocument();
        Element position = plf.createElement( Constants.ELM_POSITION );
        position.setAttribute( Constants.ATT_TYPE, Constants.ELM_POSITION );
        position.setAttribute(Constants.ATT_ID, ID);
        position.setAttributeNS( Constants.NS_URI,
                                 Constants.ATT_NAME, elementID );
        positions.appendChild( position );

        LOG.debug("Added Position Set entry {} referencing {}, resulting in position set {} for {}.",
                position, elementID, positions, person);

        return position;
    }

    static class NodeInfoComparator
        implements Comparator
    {
        public int compare(Object o1,
                           Object o2)
        {
            return ((NodeInfo) o1).indexInCVP - ((NodeInfo) o2).indexInCVP;
        }
    }

}
