/**
 * Copyright (c) 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.groups.local.searchers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.local.ITypedEntitySearcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    try{
      chanDef = Class.forName("org.jasig.portal.ChannelDefinition");
    }
    catch(Exception e){
      log.error(e); 
    }
  }
  public EntityIdentifier[] searchForEntities(String query, int method) throws GroupsException {
    //System.out.println("searching for channel");
    EntityIdentifier[] r = new EntityIdentifier[0];
    ArrayList ar = new ArrayList();
    Connection conn = null;
    RDBMServices.PreparedStatement ps = null;

        try {
            conn = RDBMServices.getConnection();
            switch(method){
              case IS:
                ps = new RDBMServices.PreparedStatement(conn,RDBMChannelDefSearcher.is_search);
                break;
              case STARTS_WITH:
                query = query+"%";
                ps = new RDBMServices.PreparedStatement(conn,RDBMChannelDefSearcher.partial_search);
                break;
              case ENDS_WITH:
                query = "%"+query;
                ps = new RDBMServices.PreparedStatement(conn,RDBMChannelDefSearcher.partial_search);
                break;
              case CONTAINS:
                query = "%"+query+"%";
                ps = new RDBMServices.PreparedStatement(conn,RDBMChannelDefSearcher.partial_search);
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
            log.error("RDBMChannelDefSearcher.searchForEntities(): " + ps);
            log.error( e);
        } finally {
            RDBMServices.releaseConnection(conn);
        }
      return (EntityIdentifier[]) ar.toArray(r);
  }
  public Class getType() {
    return chanDef;
  }
}