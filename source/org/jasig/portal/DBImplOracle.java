/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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
 *
 */


package  org.jasig.portal;
import  java.sql.*;
import  org.w3c.dom.Element;
import  org.apache.xerces.dom.DocumentImpl;

/**
 * Oracle optimized SQL implementation of the 2.x relational database model
 * @author George Lindholm
 * @version $Revision$
 */

public class DBImplOracle extends DBImpl implements IDBImpl {

  protected Element createChannelNode(Statement stmt, DocumentImpl doc, int chanId, String idTag) throws java.sql.SQLException
  {
    Element channel = null;
    String sQuery = "SELECT UC.*, CHAN_PARM_NM, CHAN_PARM_VAL,CHAN_H_D_IND,CHAN_PARM_OVRD,CHAN_PARM_DESC FROM UP_CHANNEL UC, UP_CHAN_PARAM UCP WHERE UC.CHAN_ID=" + chanId +
      " AND UC.CHAN_ID = UCP.CHAN_ID(+)";
    Logger.log (Logger.DEBUG, sQuery);

    ResultSet rs = stmt.executeQuery (sQuery);
    try {
      if (rs.next()) {
        channel = doc.createElement("channel");
        Element system = doc.createElement("system");
        createChannelNodeHeaders(doc, chanId, idTag, rs, channel, system);

        do {
          createChannelNodeParameters(doc, rs, channel, system);
        } while (rs.next());
        rs.close();
        channel.appendChild(system);
      }
    } finally {
      rs.close();
   }
    return channel;
  }

  protected void createLayout(Connection con, DocumentImpl doc, Statement stmt, Element root, int userId, int profileId, int layoutId, int structId) throws java.sql.SQLException
  {
    if (structId == 0) { // End of line
      return;
    }

    int nextStructId;
    int chldStructId;
    int chanId;
    Element system = null;
    Element parameter = null;
    Element structure;

    String sQuery = "SELECT ULS.*,STRUCT_H_D_IND,STRUCT_PARM_NM,STRUCT_PARM_VAL FROM UP_LAYOUT_STRUCT ULS, UP_STRUCT_PARAM USP WHERE ULS.USER_ID=" + userId +
      " AND ULS.LAYOUT_ID = " + layoutId + " AND ULS.STRUCT_ID=" + structId + " AND ULS.STRUCT_ID = USP.STRUCT_ID(+)";
    Logger.log (Logger.DEBUG, sQuery);
    ResultSet rs = stmt.executeQuery (sQuery);
    try {
      rs.next();
      nextStructId = rs.getInt("NEXT_STRUCT_ID");
      chldStructId = rs.getInt("CHLD_STRUCT_ID");
      chanId = rs.getInt("CHAN_ID");

      structure = createLayoutStructure(rs, chanId, userId, stmt, doc);

      system = (Element) structure.getElementsByTagName("system").item(0);
      if (chanId != 0) { // Channel
        parameter = (Element) structure.getElementsByTagName("parameter").item(0);
      }

      do {
        createLayoutStructureParameter(chanId, rs, structure, parameter, system);
      } while (rs.next());
    } finally {
      rs.close();
    }
    root.appendChild(structure);

    if (chanId == 0) {  // Folder
      createLayout(con, doc, stmt, structure, userId, profileId, layoutId, chldStructId);
    }

    createLayout(con, doc, stmt, root, userId, profileId, layoutId, nextStructId);
  }

}
