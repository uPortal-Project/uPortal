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

package org.jasig.portal;

import java.util.ArrayList;
import java.util.Date;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Describes a published channel.
 * @author George Lindholm, ITServices, UBC
 * @version $Revision$
 */
public class ChannelDefinition {
  private int channelPublishId = -1;
  private String chanTitle = "";
  private String chanDesc = "";
  private String chanClass = "";
  private int chanTypeId;
  private int chanPupblUsrId;
  private int chanApvlId;
  private Date chanPublDt;
  private Date chanApvlDt;
  private int chanTimeout;
  private boolean chanEditable;
  private boolean chanHasHelp;
  private boolean chanHasAbout;
  private String chanName = "";
  private String chanFName = "";
  private ArrayList parameters;

  /**
   * Describes a published channel's parameter.
   * A channel can have zero or more parameters.
   */
  protected class ChannelParameter {
    String name;
    String value;
    boolean override;

    public ChannelParameter(String name, String value, String override) {
      this(name, value, RDBMServices.dbFlag(override));
    }

    public ChannelParameter(String name, String value, boolean override) {
      this.name = name;
      this.value = value;
      this.override = override;
    }
  }

  public Date getchanApvlDt() { return chanApvlDt;}

  public ChannelDefinition(int channelPublishId, String chanTitle) {
    this.channelPublishId = channelPublishId;
    this.chanTitle = chanTitle;
  }

  public ChannelDefinition(int channelPublishId, String chanTitle, String chanDesc, String chanClass, int chanTypeId, int chanPupblUsrId, int chanApvlId,
    java.sql.Timestamp chanPublDt, java.sql.Timestamp chanApvlDt, int chanTimeout, String chanEditable, String chanHasHelp,
    String chanHasAbout, String chanName, String chanFName) {
      this(channelPublishId, chanTitle, chanDesc, chanClass, chanTypeId, chanPupblUsrId, chanApvlId, chanPublDt,  chanApvlDt, chanTimeout,
            RDBMServices.dbFlag(chanEditable), RDBMServices.dbFlag(chanHasHelp),
            RDBMServices.dbFlag(chanHasAbout),
            chanName, chanFName);
  }

  public ChannelDefinition(int channelPublishId, String chanTitle, String chanDesc, String chanClass, int chanTypeId, int chanPupblUsrId, int chanApvlId,
    java.sql.Timestamp chanPublDt, java.sql.Timestamp chanApvlDt, int chanTimeout, boolean chanEditable, boolean chanHasHelp,
    boolean chanHasAbout, String chanName, String chanFName) {

    this.channelPublishId = channelPublishId;
    this.chanTitle = chanTitle;
    this.chanDesc = chanDesc;
    this.chanClass = chanClass;
    this.chanTypeId = chanTypeId;
    this.chanPupblUsrId = chanPupblUsrId;
    this.chanApvlId = chanApvlId;
    this.chanPublDt = chanPublDt;
    this.chanApvlDt = chanApvlDt;
    this.chanTimeout = chanTimeout;
    this.chanEditable = chanEditable;
    this.chanHasHelp = chanHasHelp;
    this.chanHasAbout = chanHasAbout;
    this.chanName = chanName;
    this.chanFName =chanFName;
  }

  public void addParameter(String name, String value, String override) {
    if (parameters == null) {
      parameters = new ArrayList(5);
    }

    parameters.add(new ChannelParameter(name, value, override));
  }

  /**
   * Minimum attributes a channel must have
   */
  private Element getBase(Document doc, String idTag, String chanClass,
    boolean editable, boolean hasHelp, boolean  hasAbout) {
    Element channel = doc.createElement("channel");
    ((org.apache.xerces.dom.DocumentImpl)doc).putIdentifier(idTag, channel);
    channel.setAttribute("ID", idTag);
    channel.setAttribute("chanID", channelPublishId + "");
    channel.setAttribute("timeout", chanTimeout + "");
    channel.setAttribute("name", chanName);
    channel.setAttribute("title", chanTitle);
    channel.setAttribute("fname", chanFName);
    channel.setAttribute("class", chanClass);
    channel.setAttribute("typeID", chanTypeId + "");
    channel.setAttribute("editable", editable ? "true" : "false");
    channel.setAttribute("hasHelp", hasHelp ? "true" : "false");
    channel.setAttribute("hasAbout", hasAbout ? "true" : "false");
    return channel;
  }

  private final Element nodeParameter(Document doc, String name, int value) {
    return nodeParameter(doc, name, Integer.toString(value));
  }

  private final Element nodeParameter(Document doc, String name, String value) {
    Element parameter = doc.createElement("parameter");
    parameter.setAttribute("name", name);
    parameter.setAttribute("value", value);
    return parameter;
  }

  private final void addParameters(Document doc, Element channel) {
    if (parameters != null) {
      for (int i = 0; i < parameters.size(); i++) {
        ChannelParameter cp = (ChannelParameter) parameters.get(i);

        Element parameter = nodeParameter(doc, cp.name, cp.value);
        if (cp.override) {
          parameter.setAttribute("override", "yes");
        }
        channel.appendChild(parameter);
      }
    }
  }

  /**
   * Display a message where this channel should be
   */
  public Element getDocument(Document doc, String idTag, String statusMsg, int errorId) {
    Element channel = getBase(doc, idTag, "org.jasig.portal.channels.CError", false, false, false);
    addParameters(doc, channel);
    channel.appendChild(nodeParameter(doc, "CErrorMessage", statusMsg));
    channel.appendChild(nodeParameter(doc, "CErrorChanId", idTag));
    channel.appendChild(nodeParameter(doc, "CErrorErrorId", errorId));
    return channel;
  }

  /**
   * return an xml representation of this channel
   */
  public Element getDocument(Document doc, String idTag) {
    Element channel = getBase(doc, idTag, chanClass, chanEditable, chanHasHelp, chanHasAbout);
    channel.setAttribute("description", chanDesc);
    addParameters(doc, channel);
    return channel;
  }

  /**
   * Is it time to reload me from the data store
   */
  public boolean refreshMe() {
    return false;
  }
}

