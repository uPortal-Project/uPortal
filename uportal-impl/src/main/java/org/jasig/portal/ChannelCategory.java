/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal;

/**
 * A channel category.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class ChannelCategory {

  String id;
  String name;
  String descr;
  String creatorId;

  /**
   * Constructs a ChannelCategory
   */
  public ChannelCategory(String id) {
    this.id = id;
  }

  // Getter methods
  public String getId() { return id; }
  public String getName() { return name; }
  public String getDescription() { return descr; }
  public String getCreatorId() { return creatorId; }

  // Setter methods
  public void setName(String name) { this.name = name; }
  public void setDescription(String descr) { this.descr = descr; }
  public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

  public String toString() {
    return "ChannelCategory: id=" + id + ", name=" + name + ", description=" + descr + ", creatorId=" + creatorId;
  }
}