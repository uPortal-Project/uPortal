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
 */

package org.jasig.portal.channels.groupsmanager;

import java.util.ArrayList;
import java.util.Iterator;

import javax.naming.Name;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.IIndividualGroupService;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class SearchResultsGroupImpl implements IEntityGroup{
  protected ArrayList members = new ArrayList();
  protected String name;
  protected String description;
  protected String creator;
  protected EntityIdentifier ei;
  protected Class leafType;

  public SearchResultsGroupImpl(Class leafType) {
    this.leafType = leafType;
    ei = new EntityIdentifier(null,IEntityGroup.class);
  }

  public void addMember(IGroupMember gm) throws GroupsException{
    members.add(gm);
  }

  public java.util.Iterator getMembers() throws GroupsException {
    return members.iterator();
  }
  public String getCreatorID() {
    return this.creator;
  }
  public String getDescription() {
    return this.description;
  }
  public String getLocalKey() {
    return ei.getKey();
  }
  public String getName() {
    return this.name;
  }
  public Name getServiceName() {
    return null;
  }
  public void removeMember(IGroupMember gm) {
    /**@todo: Implement this org.jasig.portal.groups.IEntityGroup method*/
    throw new java.lang.UnsupportedOperationException("Method removeMember() not yet implemented.");
  }
  public void setCreatorID(String userID) {
    this.creator = userID;
  }
  public void setDescription(String name) {
    this.description = name;
  }
  public void setName(String name) throws GroupsException {
    this.name=name;
  }
  public void setLocalGroupService(IIndividualGroupService groupService) throws GroupsException {
    /**@todo: Implement this org.jasig.portal.groups.IEntityGroup method*/
    throw new java.lang.UnsupportedOperationException("Method setLocalGroupService() not yet implemented.");
  }
  public boolean contains(IGroupMember gm) throws GroupsException {
    Iterator members = getMembers();
    while (members.hasNext()){
      IGroupMember m = (IGroupMember) members.next();
      if (m.isGroup() && m.getKey().equals(gm.getKey())){
        return true;
      }
    }
    return false;
  }
  public boolean deepContains(IGroupMember gm) throws GroupsException {
    /**@todo: Implement this org.jasig.portal.groups.IGroupMember method*/
    throw new java.lang.UnsupportedOperationException("Method deepContains() not yet implemented.");
  }
  public boolean equals(Object o) {
    /**@todo: Implement this org.jasig.portal.groups.IGroupMember method*/
    throw new java.lang.UnsupportedOperationException("Method equals() not yet implemented.");
  }
  public Iterator getAllContainingGroups() throws GroupsException {
    /**@todo: Implement this org.jasig.portal.groups.IGroupMember method*/
    throw new java.lang.UnsupportedOperationException("Method getAllContainingGroups() not yet implemented.");
  }
  public Iterator getAllEntities() throws GroupsException {
    /**@todo: Implement this org.jasig.portal.groups.IGroupMember method*/
    throw new java.lang.UnsupportedOperationException("Method getAllEntities() not yet implemented.");
  }
  public Iterator getAllMembers() throws GroupsException {
    /**@todo: Implement this org.jasig.portal.groups.IGroupMember method*/
    throw new java.lang.UnsupportedOperationException("Method getAllMembers() not yet implemented.");
  }
  public Iterator getContainingGroups() throws GroupsException {
    /**@todo: Implement this org.jasig.portal.groups.IGroupMember method*/
    throw new java.lang.UnsupportedOperationException("Method getContainingGroups() not yet implemented.");
  }
  public Iterator getEntities() throws GroupsException {
    /**@todo: Implement this org.jasig.portal.groups.IGroupMember method*/
    throw new java.lang.UnsupportedOperationException("Method getEntities() not yet implemented.");
  }
  public Class getEntityType() {
    return leafType;
  }
  public String getKey() {
    return ei.getKey();
  }
  public Class getLeafType() {
    return leafType;
  }
  public IEntityGroup getMemberGroupNamed(String name) throws GroupsException {
    /**@todo: Implement this org.jasig.portal.groups.IGroupMember method*/
    throw new java.lang.UnsupportedOperationException("Method getMemberGroupNamed() not yet implemented.");
  }
  public Class getType() {
    return IEntityGroup.class;
  }
  public EntityIdentifier getUnderlyingEntityIdentifier() {
    return ei;
  }
  public boolean isDeepMemberOf(IGroupMember gm) throws GroupsException {
    return false;
  }
  public boolean isMemberOf(IGroupMember gm) throws GroupsException {
    return false;
  }
  public EntityIdentifier getEntityIdentifier() {
    return ei;
  }

  public void update() throws GroupsException{

  }

  public void updateMembers() throws GroupsException{

  }

  public boolean isEditable() throws GroupsException{
    return true;
  }
  public void delete() throws GroupsException {
    /**@todo: Implement this org.jasig.portal.groups.IEntityGroup method*/
    throw new java.lang.UnsupportedOperationException("Method delete() not yet implemented.");
  }

  public boolean hasMembers() throws GroupsException {
    if (members.size()>0){
    return true;
    }
    return false;
  }
  public boolean isEntity() {
    return false;
  }
  public boolean isGroup() {
    return true;
  }
}