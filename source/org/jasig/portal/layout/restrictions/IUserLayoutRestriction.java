/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.restrictions;


import org.jasig.portal.PortalException;
import org.jasig.portal.layout.ILayoutNode;


/**
 * IUserLayoutRestriction is the base interface for UserLayout restrictions.
 * 
 * @author Michael Ivanov
 * @version $Revision$
 */
// create table up_restrictions (restriction_id integer(10) primary key, restriction_name varchar(50) not null );
// insert into up_restrictions (restriction_id,restriction_name) values (1,'priority');
// create table up_layout_restrictions ( user_id integer(10) not null, layout_id integer(10) not null, node_id varchar(20) not null, restriction_id integer(10) not null,
// restriction_value varchar(200) not null, restriction_tree_path varchar(300), constraint up_layout_restrictions_pk primary key (user_id,layout_id,node_id,restriction_id),
// constraint up_layout_restrictions_fk foreign key (restriction_id) references up_restrictions (restriction_id));
// insert into up_layout_restrictions ( user_id, layout_id, node_id, restriction_id, restriction_value) values (2,1,5,1,'2-5,8');
// insert into up_layout_struct_aggr (user_id,layout_id,struct_id,next_struct_id,chld_struct_id,external_id,chan_id,name,type,hidden,
// immutable,unremovable) select user_id,layout_id,struct_id,next_struct_id,chld_struct_id,external_id,chan_id,name,type,hidden,
// immutable,unremovable from up_layout_struct;
// delete from up_layout_struct_aggr where user_id != 2 or layout_id != 1;


public interface IUserLayoutRestriction {
	
  // The local restriction path defined for every restriction by default 	   
  public final static String LOCAL_RESTRICTION_PATH = "local";	
  
  // The parent restriction path, could be setup to a parent node	   
  public final static String PARENT_RESTRICTION_PATH = "parent";	
  
  // The children restriction path, could be setup to all children 
  public final static String CHILDREN_RESTRICTION_PATH = "children";

  
  /**
   * Returns the name of the current restriction
   * @return a <code>String</code> name
   */
  public String getName();
  
  /**
   * Sets the name of the current restriction
   * @param a <code>String</code> name
   */
  public void setName( String name );

   /**
     * Gets the unique key of the current restriction
     * @return a <code>String</code> unique key
     */
  public String getUniqueKey();


  /**
     * Checks the restriction for the given property value
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException
     */
  public boolean checkRestriction(String propertyValue) throws PortalException;


  /**
     * Checks the relative restriction on a given node
     * @param node a <code>ILayoutNode</code> node
     * @return a boolean value
     * @exception PortalException
     */
  public boolean checkRestriction ( ILayoutNode node ) throws PortalException;


  /**
     * Sets the restriction expression
     * @param restrictionExpression a <code>String</code> expression
     */
  public void setRestrictionExpression ( String restrictionExpression );


  /**
     * Gets the restriction expression
     * @return a <code>String</code> expression
     */
  public String getRestrictionExpression();


  /**
   * Sets the restriction path
   * @param restrictionPath a <code>String</code> path
   */
  public void setRestrictionPath ( String restrictionPath );
  
   /**
     * Gets the tree path for the current restriction
     * @return a <code>String</code> tree path
     */
  public String getRestrictionPath();

}

