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