/* Copyright 2001-2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups.local.searchers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.local.ITypedEntitySearcher;

/**
 * Searches the portal DB for people.  Used by EntitySearcherImpl
 *
 * @author Alex Vigdor
 * @version $Revision$
 */


public class RDBMPersonSearcher  implements ITypedEntitySearcher{
    private static final Log log = LogFactory.getLog(RDBMPersonSearcher.class);
  private static final String user_is_search="select USER_NAME from UP_USER where USER_NAME=?";
  private static final String user_partial_search="select USER_NAME from UP_USER where USER_NAME like ?";
  private static final String person_partial_search="select USER_NAME from UP_PERSON_DIR where (FIRST_NAME like ? or LAST_NAME like ?)";
  private static final String person_is_search = "select USER_NAME from UP_PERSON_DIR where (FIRST_NAME = ? or LAST_NAME = ?)";
  private Class personDef;

  public RDBMPersonSearcher() {
    personDef = org.jasig.portal.security.IPerson.class;
  }
  public EntityIdentifier[] searchForEntities(String query, int method) throws GroupsException {
    //System.out.println("searching for channel");
    EntityIdentifier[] r = new EntityIdentifier[0];
    ArrayList ar = new ArrayList();
    Connection conn = null;
    PreparedStatement ps = null;
    PreparedStatement ups = null;
    PreparedStatement uis = null;
    ResultSet rs = null;
    ResultSet urs = null;
    ResultSet uprs = null;

        try {
            conn = RDBMServices.getConnection();
            uis = conn.prepareStatement(RDBMPersonSearcher.user_is_search);
            switch(method){
              case IS:
                ps = conn.prepareStatement(RDBMPersonSearcher.person_is_search);
                ups = uis;
                break;
              case STARTS_WITH:
                query = query+"%";
                ps = conn.prepareStatement(RDBMPersonSearcher.person_partial_search);
                ups = conn.prepareStatement(RDBMPersonSearcher.user_partial_search);
                break;
              case ENDS_WITH:
                query = "%"+query;
                ps = conn.prepareStatement(RDBMPersonSearcher.person_partial_search);
                ups = conn.prepareStatement(RDBMPersonSearcher.user_partial_search);
                break;
              case CONTAINS:
                query = "%"+query+"%";
                ps = conn.prepareStatement(RDBMPersonSearcher.person_partial_search);
                ups = conn.prepareStatement(RDBMPersonSearcher.user_partial_search);
                break;
              default:
                throw new GroupsException("Unknown search type");
            }
            ps.clearParameters();
            ps.setString(1,query);
            ps.setString(2,query);
            rs = ps.executeQuery();
            //System.out.println(ps.toString());
            while (rs.next()){
              //System.out.println("result");
              uis.clearParameters();
              uis.setString(1,rs.getString(1));
              urs = uis.executeQuery();
              if(urs.next()){
                ar.add(new EntityIdentifier(urs.getString(1),personDef));
              }
            }

            ups.clearParameters();
            ups.setString(1,query);
            uprs = ups.executeQuery();
            while (uprs.next()){
                ar.add(new EntityIdentifier(uprs.getString(1),personDef));
            }
        } catch (SQLException e) {
            throw new GroupsException("RDBMChannelDefSearcher.searchForEntities(): " + ps,e);
        } finally {
            if (rs!=null) RDBMServices.closeResultSet(rs);
            if (urs!=null) RDBMServices.closeResultSet(urs);
            if (uprs!=null) RDBMServices.closeResultSet(uprs);
            if (ps!=null) RDBMServices.closeStatement(ps);
            if (uis!=null) RDBMServices.closeStatement(uis);
            if (ups!=null) RDBMServices.closeStatement(ups);
            if (conn!=null) RDBMServices.releaseConnection(conn);
        }
      return (EntityIdentifier[]) ar.toArray(r);
  }
  public Class getType() {
    return personDef;
  }
}
