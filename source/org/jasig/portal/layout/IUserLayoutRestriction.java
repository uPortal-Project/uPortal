package org.jasig.portal.layout;


import org.jasig.portal.PortalException;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Instructional Media & Magic </p>
 * @author Michael Ivanov
 * @version 1.0
 */
// create table up_layout_restrictions ( user_id number(10), layout_id number(10), node_id varchar(50), restriction_name varchar(50),
// restriction_value varchar(200), restriction_node_id varchar(50) constraints pk_restrictions primary key (user_id,layout_id,node_id);


public interface IUserLayoutRestriction {


  public boolean checkRestriction( UserLayoutNode node ) throws PortalException;

}

