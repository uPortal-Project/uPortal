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


package org.jasig.portal.layout.restrictions;


import org.jasig.portal.PortalException;
import org.jasig.portal.layout.ALNode;


/**
 * <p>Title: The base interface for UserLayout restrictions </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Instructional Media & Magic </p>
 * @author Michael Ivanov
 * @version 1.1
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


  /**
     * Returns the type of the current restriction
     * @return a restriction type respresented in the <code>RestrictionTypes</code> interface
     */
  public int getRestrictionType();

   /**
     * Gets the restriction name
     * @return a <code>String</code> restriction name
     */
  public String getRestrictionName();


  /**
     * Checks the restriction for the given property value
     * @param propertyValue a <code>String</code> property value to be checked
     * @return a boolean value
     * @exception PortalException
     */
  public boolean checkRestriction(String propertyValue) throws PortalException;


  /**
     * Checks the relative restriction on a given node
     * @param node a <code>ALNode</code> node
     * @return a boolean value
     * @exception PortalException
     */
  public boolean checkRestriction ( ALNode node ) throws PortalException;


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
     * Gets the tree path for the current restriction
     * @return a <code>String</code> tree path
     */
  public String getRestrictionPath();

}

