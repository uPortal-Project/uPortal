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


  public boolean checkRestriction( UserLayoutNode node ) throws PortalException;

}

