/* Copyright 2001-2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups.local.searchers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.local.ITypedEntitySearcher;

/**
 * Searches the portal DB for channels.  Used by EntitySearcherImpl
 *
 * @author Alex Vigdor
 * @version $Revision$
 */


public class RDBMChannelDefSearcher implements ITypedEntitySearcher {
    private static final Log log = LogFactory.getLog(RDBMChannelDefSearcher.class);
  private static final String is_search="select CHAN_ID from UP_CHANNEL where (CHAN_NAME=? or CHAN_TITLE=?)";
  private static final String partial_search="select CHAN_ID from UP_CHANNEL where (CHAN_NAME like ? or CHAN_TITLE like ?)";
  private Class chanDef;

  public RDBMChannelDefSearcher() {
      chanDef = org.jasig.portal.ChannelDefinition.class;
  }
  public EntityIdentifier[] searchForEntities(String query, int method) throws GroupsException {
    //System.out.println("searching for channel");
    EntityIdentifier[] r = new EntityIdentifier[0];
    ArrayList ar = new ArrayList();
    Connection conn = null;
    PreparedStatement ps = null;

        try {
            conn = RDBMServices.getConnection();
            switch(method){
              case IS:
                ps = conn.prepareStatement(RDBMChannelDefSearcher.is_search);
                break;
              case STARTS_WITH:
                query = query+"%";
                ps = conn.prepareStatement(RDBMChannelDefSearcher.partial_search);
                break;
              case ENDS_WITH:
                query = "%"+query;
                ps = conn.prepareStatement(RDBMChannelDefSearcher.partial_search);
                break;
              case CONTAINS:
                query = "%"+query+"%";
                ps = conn.prepareStatement(RDBMChannelDefSearcher.partial_search);
                break;
              default:
                throw new GroupsException("Unknown search type");
            }
            ps.clearParameters();
            ps.setString(1,query);
            ps.setString(2,query);
            ResultSet rs = ps.executeQuery();
            //System.out.println(ps.toString());
            while (rs.next()){
              //System.out.println("result");
              ar.add(new EntityIdentifier(rs.getString(1),chanDef));
            }
            ps.close();
        } catch (Exception e) {
            log.error("RDBMChannelDefSearcher.searchForEntities(): " + ps, e);
        } finally {
            RDBMServices.releaseConnection(conn);
        }
      return (EntityIdentifier[]) ar.toArray(r);
  }
  public Class getType() {
    return chanDef;
  }
}