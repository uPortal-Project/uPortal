/**
 * Copyright © 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
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

package  org.jasig.portal;

import org.jasig.portal.utils.DTDResolver;
import org.jasig.portal.utils.DocumentFactory;
import org.jasig.portal.utils.CounterStoreFactory;
import org.jasig.portal.services.LogService;
import org.jasig.portal.security.IPerson;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Reference implementation of IChannelRegistryStore.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class RDBMChannelRegistryStore implements IChannelRegistryStore {

  /**
   * Get channel types.
   * @return types, the channel types as a Document
   * @throws java.sql.SQLException
   */
  public ChannelType[] getChannelTypes() throws SQLException {
    ChannelType[] channelTypes = null;
    Connection con = RDBMServices.getConnection();

    try {
      Statement stmt = con.createStatement();
      try {
        String query = "SELECT TYPE_ID, TYPE, TYPE_NAME, TYPE_DESCR, TYPE_DEF_URI FROM UP_CHAN_TYPE";
        LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.getChannelTypes(): " + query);
        ResultSet rs = stmt.executeQuery(query);
        try {
          List channelTypesList = new ArrayList();
          while (rs.next()) {
            int channelTypeId = rs.getInt(1);
            String javaClass = rs.getString(2);
            String name = rs.getString(3);
            String descr = rs.getString(4);
            String cpdUri = rs.getString(5);

            ChannelType channelType = new ChannelType(channelTypeId, javaClass, name, descr, cpdUri);
            channelTypesList.add(channelType);
          }
          channelTypes = (ChannelType[])channelTypesList.toArray(new ChannelType[0]);
        } finally {
          rs.close();
        }
      } finally {
        stmt.close();
      }
    } finally {
      RDBMServices.releaseConnection(con);
    }
    return channelTypes;
  }

  /**
   * Registers a new channel type.
   * @param chanType a channel type
   * @throws SQLException
   */
  public void addChannelType (ChannelType chanType) throws SQLException {
    Connection con = null;

    try {
      int nextID = CounterStoreFactory.getCounterStoreImpl().getIncrementIntegerId("UP_CHAN_TYPE");
      String javaClass = chanType.getJavaClass();
      String name = chanType.getName();
      String descr = chanType.getDescription();
      String cpdUri = chanType.getCpdUri();

      con = RDBMServices.getConnection();

      // Set autocommit false for the connection
      RDBMServices.setAutoCommit(con, false);
      Statement stmt = con.createStatement();
      try {
        // Insert channel type.
        String insert = "INSERT INTO UP_CHAN_TYPE VALUES (" +
         "'" + nextID + "', " +
         "'" + javaClass + "', " +
         "'" + name + "', " +
         "'" + descr + "', " +
         "'" + cpdUri + "')";
        LogService.instance().log(LogService.DEBUG, "RDBMChannelRegistryStore.addChannelType(): " + insert);
        int rows = stmt.executeUpdate(insert);

        // Commit the transaction
        RDBMServices.commit(con);
      } catch (SQLException sqle) {
        // Roll back the transaction
        RDBMServices.rollback(con);
        throw sqle;
      } finally {
          stmt.close();
      }
    } catch (Exception e) {
      throw new SQLException(e.getMessage());
    } finally {
      RDBMServices.releaseConnection(con);
    }
  }
}
