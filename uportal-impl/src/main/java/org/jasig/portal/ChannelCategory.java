/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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