/**
 * Copyright (c) 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.groups.ldap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.EntityGroupImpl;
import org.jasig.portal.groups.EntityImpl;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IEntitySearcher;
import org.jasig.portal.groups.IEntityStore;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.SmartCache;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * LDAPGroupStore.
 * @author Alex Vidgor
 * @version $Revision$
 */
public class LDAPGroupStore implements IEntityGroupStore, IEntityStore, IEntitySearcher {
  protected String url;
  protected String logonid;
  protected String logonpassword;
  protected String keyfield;
  protected String namefield;
  protected String usercontext="";
  protected HashMap groups;
  protected SmartCache contexts;
  protected SmartCache personkeys;
  protected static Class iperson = org.jasig.portal.security.IPerson.class;
  protected static Class group = org.jasig.portal.groups.IEntityGroup.class;
  protected static short ELEMENT_NODE = Node.ELEMENT_NODE;

  public LDAPGroupStore() {
    Document config = null;
    try{
      config = ResourceLoader.getResourceAsDocument(this.getClass(),"/properties/groups/LDAPGroupStoreConfig.xml");
    }
    catch(Exception rme){
      throw new RuntimeException("LDAPGroupStore: Unable to find configuration configuration document");
    }
    init(config);
  }

  public LDAPGroupStore(Document config){
    init(config);
  }

  protected void init(Document config){
    this.groups = new HashMap();
    this.contexts = new SmartCache(120);
    config.normalize();
    int refreshminutes = 120;
    Element root = config.getDocumentElement();
    NodeList nl = root.getElementsByTagName("config");
    if (nl.getLength() == 1){
      Element conf = (Element) nl.item(0);
      Node cc = conf.getFirstChild();
      //NodeList cl= conf.getF.getChildNodes();
      //for(int i=0; i<cl.getLength(); i++){
      while (cc!=null){
        if(cc.getNodeType()==ELEMENT_NODE){
          Element c = (Element) cc;
          c.normalize();
          Node t = c.getFirstChild();
          if(t!=null && t.getNodeType()==Node.TEXT_NODE){
            String name = c.getNodeName();
            String text = ((Text) t).getData();
            //System.out.println(name+" = "+text);
            if (name.equals("url")){
              url = text;
            }
            else if (name.equals("logonid")){
              logonid = text;
            }
            else if (name.equals("logonpassword")){
              logonpassword = text;
            }
            else if (name.equals("keyfield")){
              keyfield = text;
            }
            else if (name.equals("namefield")){
              namefield = text;
            }
            else if (name.equals("usercontext")){
              usercontext = text;
            }
            else if (name.equals("refresh-minutes")){
              try{
                 refreshminutes = Integer.parseInt(text);
              }
              catch(Exception e){}
            }
          }
        }
        cc = cc.getNextSibling();
      }
    }
    else{
      throw new RuntimeException("LDAPGroupStore: config file must contain one config element");
    }

    this.personkeys = new SmartCache(refreshminutes*60);

    NodeList gl = root.getChildNodes();
    for (int j=0; j<gl.getLength(); j++){
      if(gl.item(j).getNodeType() == ELEMENT_NODE){
        Element g = (Element) gl.item(j);
        if (g.getNodeName().equals("group")){
          GroupShadow shadow = processXmlGroupRecursive(g);
          groups.put(shadow.key,shadow);
        }
      }
    }

  }

  protected String[] getPersonKeys(String groupKey){
    String[] r= (String[]) personkeys.get(groupKey);
    if(r==null){
      GroupShadow shadow = (GroupShadow) groups.get(groupKey);
      if (shadow.entities!=null){
        r = shadow.entities.getPersonKeys();
      }
      else {
        r = new String[0];
      }
      personkeys.put(groupKey,r);
    }
    return r;
  }

  protected GroupShadow processXmlGroupRecursive(Element groupElem){
    GroupShadow shadow = new GroupShadow();
    shadow.key = groupElem.getAttribute("key");
    shadow.name = groupElem.getAttribute("name");
    //System.out.println("Loading configuration for group "+shadow.name);
    ArrayList subgroups = new ArrayList();
    NodeList nl = groupElem.getChildNodes();
    for(int i = 0; i<nl.getLength(); i++){
      if (nl.item(i).getNodeType()==ELEMENT_NODE){
        Element e = (Element) nl.item(i);
        if(e.getNodeName().equals("group")){
          GroupShadow sub = processXmlGroupRecursive(e);
          subgroups.add(sub);
          groups.put(sub.key,sub);
        }
        else if(e.getNodeName().equals("entity-set")){
          shadow.entities = new EntitySet(e);
        }
        else if(e.getNodeName().equals("description")){
          e.normalize();
          Text t= (Text) e.getFirstChild();
          if (t!=null){
            shadow.description = t.getData();
          }
        }
      }
    }
    shadow.subgroups = (GroupShadow[]) subgroups.toArray(new GroupShadow[0]);
    return shadow;
  }

  protected class GroupShadow{
    protected String key;
    protected String name;
    protected String description;
    protected GroupShadow[] subgroups;
    protected EntitySet entities;
  }

  protected class EntitySet{
    public static final int FILTER=1;
    public static final int UNION=2;
    public static final int DIFFERENCE=3;
    public static final int INTERSECTION=4;
    public static final int SUBTRACT=5;
    public static final int ATTRIBUTES=6;

    protected int type;
    protected String filter;
    protected Attributes attributes;
    protected EntitySet[] subsets;

    protected EntitySet(Element entityset){
      entityset.normalize();
      Node n = entityset.getFirstChild();
      while (n.getNodeType()!=Node.ELEMENT_NODE){
        n = n.getNextSibling();
      }
      Element e = (Element) n;
      String type = e.getNodeName();
      boolean collectSubsets = false;
      if (type.equals("filter")){
        this.type = FILTER;
        filter = e.getAttribute("string");
      }
      else if (type.equals("attributes")){
        this.type = ATTRIBUTES;
        attributes = new BasicAttributes();
        NodeList atts = e.getChildNodes();
        for (int i=0; i< atts.getLength(); i++){
          if (atts.item(i).getNodeType() == ELEMENT_NODE){
            Element a = (Element) atts.item(i);
            attributes.put(a.getAttribute("name"),a.getAttribute("value"));
          }
        }
      }
      else if (type.equals("union")){
        this.type = UNION;
        collectSubsets = true;
      }
      else if (type.equals("intersection")){
        this.type = INTERSECTION;
        collectSubsets = true;
      }
      else if (type.equals("difference")){
        this.type = DIFFERENCE;
        collectSubsets = true;
      }
      else if (type.equals("subtract")){
        this.type = SUBTRACT;
        collectSubsets = true;
      }

      if(collectSubsets){
        ArrayList subs = new ArrayList();
        NodeList nl = e.getChildNodes();
        for (int i=0; i < nl.getLength(); i++){
          if (nl.item(i).getNodeType() == Node.ELEMENT_NODE){
            EntitySet subset = new EntitySet((Element)nl.item(i));
            subs.add(subset);
          }
        }
        subsets = (EntitySet[]) subs.toArray(new EntitySet[0]);
      }
    }

    protected String[] getPersonKeys(){
      ArrayList keys = new ArrayList();
      //System.out.println("Loading keys!!");
      String[] subkeys;
      switch (type){
        case FILTER:
          //System.out.println("Performing ldap query!!");
          DirContext context = getConnection();
          NamingEnumeration userlist = null;
          SearchControls sc = new SearchControls();
          sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
          sc.setReturningAttributes(new String[] {keyfield});
          try {
            userlist = context.search(usercontext,filter,sc);
          } catch (NamingException nex) {
            LogService.log(LogService.ERROR,"LDAPGroupStore: Unable to perform filter "+filter);
            LogService.log(LogService.ERROR,nex);
          }
          processLdapResults(userlist,keys);
          break;
        case ATTRIBUTES:
          //System.out.println("Performing ldap attribute query!!");
          DirContext context2 = getConnection();
          NamingEnumeration userlist2 = null;
          try {
            userlist2 = context2.search(usercontext,attributes,new String[] {keyfield});
          } catch (NamingException nex) {
            LogService.log(LogService.ERROR,"LDAPGroupStore: Unable to perform attribute search");
            LogService.log(LogService.ERROR,nex);
          }
          processLdapResults(userlist2,keys);
          break;
        case UNION:
          for(int i=0; i<subsets.length; i++){
            subkeys = subsets[i].getPersonKeys();
            for(int j=0;j<subkeys.length;j++){
              String key =  subkeys[j];
              if(!keys.contains(key)){
                keys.add(key);
              }
            }
          }
          break;
        case INTERSECTION:
          if (subsets.length > 0){
            // load initial keys from first entity set
            String[] interkeys = subsets[0].getPersonKeys();
            // now set non-recurring keys to null
            for(int m=1;m<subsets.length;m++){
              subkeys = subsets[m].getPersonKeys();
              for (int n=0; n < interkeys.length; n++){
                if (interkeys[n] !=null){
                  boolean remove=true;
                  for (int o=0; o<subkeys.length; o++){
                    if (subkeys[o].equals(interkeys[n])){
                      // found a match, so far the intersection for this key is valid
                      remove=false;
                      break;
                    }
                  }
                  if (remove){
                    interkeys[n] = null;
                  }
                }
              }
            }
            for (int p=0; p< interkeys.length; p++){
              if (interkeys[p] != null){
                keys.add(interkeys[p]);
              }
            }
          }
          break;
        case DIFFERENCE:
          if (subsets.length > 0){
            ArrayList discardKeys = new ArrayList();
            subkeys = subsets[0].getPersonKeys();
            // load initial keys from first entity set
            for(int q=0; q<subkeys.length; q++){
              keys.add(subkeys[q]);
            }
            for (int r=1; r<subsets.length; r++){
              subkeys = subsets[r].getPersonKeys();
              for (int s=0; s<subkeys.length; s++){
                String ky = subkeys[s];
                if (keys.contains(ky)){
                  keys.remove(ky);
                  discardKeys.add(ky);
                }
                else{
                  if (!discardKeys.contains(ky)){
                    keys.add(ky);
                  }
                }
              }
            }

          }
          break;
        case SUBTRACT:
          if (subsets.length>0){
            subkeys = subsets[0].getPersonKeys();
            // load initial keys from first entity set
            for(int t=0; t<subkeys.length; t++){
              keys.add(subkeys[t]);
            }
            for(int u=1; u<subsets.length; u++){
              subkeys = subsets[u].getPersonKeys();
              for(int v=0; v<subkeys.length; v++){
                String kyy = subkeys[v];
                if(keys.contains(kyy)){
                  keys.remove(kyy);
                }
              }
            }
          }
          break;
      }
      return (String[]) keys.toArray(new String[0]);
    }
  }

  protected void processLdapResults(NamingEnumeration results, ArrayList keys){
    //long time1 = System.currentTimeMillis();
    //long casting=0;
    //long getting=0;
    //long setting=0;
    //long looping=0;
    //long loop1=System.currentTimeMillis();
    try{
      while(results.hasMore()){
        //long loop2 = System.currentTimeMillis();
        //long cast1=System.currentTimeMillis();
        //looping=looping+loop2-loop1;
        SearchResult result = (SearchResult) results.next();
        //long cast2 = System.currentTimeMillis();
        //long get1 = System.currentTimeMillis();
        Attributes ldapattribs = result.getAttributes();
        //long get2 = System.currentTimeMillis();
        //long set1 = System.currentTimeMillis();
        Attribute attrib = ldapattribs.get(keyfield);
        if (attrib != null) {
            keys.add(String.valueOf(attrib.get()));
        }
        //long set2 = System.currentTimeMillis();
        //loop1=System.currentTimeMillis();
        //casting=casting+cast2-cast1;
        //setting=setting+set2-set1;
        //getting=getting+get2-get1;
      }
    }
    catch(NamingException nex){
        LogService.log(LogService.ERROR,"LDAPGroupStore: error processing results");
       LogService.log(LogService.ERROR,nex);
    }
    finally{
      try{results.close();}catch(Exception e){}
    }
    //long time5 = System.currentTimeMillis();
    //System.out.println("Result processing took "+(time5-time1)+": "+getting+" for getting, "
    //  +setting+" for setting, "+casting+" for casting, "+looping+" for looping,"
    //  +(time5-loop1)+" for closing");
  }

  protected DirContext getConnection(){
     //JNDI boilerplate to connect to an initial context
    DirContext context = (DirContext) contexts.get("context");
    if (context==null){
      Hashtable jndienv = new Hashtable();
      jndienv.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
      jndienv.put(Context.SECURITY_AUTHENTICATION,"simple");
      if (url.startsWith("ldaps")) { // Handle SSL connections
        String newurl = url.substring(0,4) + url.substring(5);
        jndienv.put(Context.SECURITY_PROTOCOL, "ssl");
        jndienv.put(Context.PROVIDER_URL, newurl);
      }
      else {
        jndienv.put(Context.PROVIDER_URL, url);
      }
      if (logonid!=null)
        jndienv.put(Context.SECURITY_PRINCIPAL,logonid);
      if (logonpassword!=null)
        jndienv.put(Context.SECURITY_CREDENTIALS,logonpassword);
      try {
        context = new InitialDirContext(jndienv);
      } catch (NamingException nex) {
        LogService.log(LogService.ERROR,"LDAPGroupStore: unable to get context");
        LogService.log(LogService.ERROR,nex);
      }
      contexts.put("context",context);
    }
    return context;
  }

  protected IEntityGroup makeGroup(GroupShadow shadow) throws GroupsException{
    IEntityGroup group = null;
    if ( shadow != null )
    {
        group = new EntityGroupImpl(shadow.key,iperson); 
        group.setDescription(shadow.description);
        group.setName(shadow.name);
    }
    return group;
  }

  protected GroupShadow getShadow(IEntityGroup group){
    return (GroupShadow) groups.get(group.getLocalKey());
  }

  public void delete(IEntityGroup group) throws GroupsException {
    throw new java.lang.UnsupportedOperationException("LDAPGroupStore: Method delete() not supported.");
  }
  public IEntityGroup find(String key) throws GroupsException {
    return makeGroup((GroupShadow)this.groups.get(key));
  }
  public Iterator findContainingGroups(IGroupMember gm) throws GroupsException {
     ArrayList al = new ArrayList();
     String key;
     GroupShadow[] shadows = getGroupShadows();
     if (gm.isEntity()){
       key = gm.getKey();
       for (int i=0; i < shadows.length; i++){
        String[] keys = getPersonKeys(shadows[i].key);
        for (int j=0; j< keys.length; j++){
          if (keys[j].equals(key)){
            al.add(makeGroup(shadows[i]));
            break;
          }
        }
       }
     }

     if (gm.isGroup()){
        key = ((IEntityGroup)gm).getLocalKey();
        for (int i=0; i < shadows.length; i++){
          for (int j=0; j< shadows[i].subgroups.length; j++){
            if (shadows[i].subgroups[j].key.equals(key)){
              al.add(makeGroup(shadows[i]));
              break;
            }
          }
        }
     }

     return al.iterator();
  }

  public String[] findMemberGroupKeys(IEntityGroup group) throws GroupsException
  {
    List keys = new ArrayList();
    for ( Iterator itr=findMemberGroups(group); itr.hasNext(); )
    {
        IEntityGroup eg = (IEntityGroup) itr.next();
        keys.add(eg.getKey());
    }
    return (String[]) keys.toArray(new String[keys.size()]);
  }
  public Iterator findMemberGroups(IEntityGroup group) throws GroupsException {
    ArrayList al = new ArrayList();
    GroupShadow shadow = getShadow(group);
    for(int i=0; i < shadow.subgroups.length; i++){
      al.add(makeGroup(shadow.subgroups[i]));
    }
    return al.iterator();
  }
  public IEntityGroup newInstance(Class entityType) throws GroupsException {
    throw new java.lang.UnsupportedOperationException("LDAPGroupStore: Method newInstance() not supported");
  }
  public void update(IEntityGroup group) throws GroupsException {
    throw new java.lang.UnsupportedOperationException("LDAPGroupStore: Method update() not supported");
  }
  public void updateMembers(IEntityGroup group) throws GroupsException {
    throw new java.lang.UnsupportedOperationException("LDAPGroupStore: Method updateMembers() not supported");
  }
  public ILockableEntityGroup findLockable(String key) throws GroupsException {
    throw new java.lang.UnsupportedOperationException("LDAPGroupStore: Method findLockable() not supported");
  }

  protected GroupShadow[] getGroupShadows(){
     return (GroupShadow[]) groups.values().toArray(new GroupShadow[0]);
  }

  public EntityIdentifier[] searchForGroups(String query, int method, Class leaftype) throws GroupsException {
    ArrayList ids = new ArrayList();
    GroupShadow[] g = getGroupShadows();
    int i;
    switch (method){
      case IS:
        for (i=0; i<g.length;i++){
          if(g[i].name.equals(query)){
            ids.add(new EntityIdentifier(g[i].key,group));
          }
        }
        break;
      case STARTS_WITH:
        for (i=0; i<g.length;i++){
          if(g[i].name.startsWith(query)){
            ids.add(new EntityIdentifier(g[i].key,group));
          }
        }
        break;
      case ENDS_WITH:
        for (i=0; i<g.length;i++){
          if(g[i].name.endsWith(query)){
            ids.add(new EntityIdentifier(g[i].key,group));
          }
        }
        break;
      case CONTAINS:
        for (i=0; i<g.length;i++){
          if(g[i].name.indexOf(query) > -1){
            ids.add(new EntityIdentifier(g[i].key,group));
          }
        }
        break;
    }
    return (EntityIdentifier[]) ids.toArray(new EntityIdentifier[0]);
  }
  public Iterator findEntitiesForGroup(IEntityGroup group) throws GroupsException {
    GroupShadow shadow = getShadow(group);
    ArrayList al = new ArrayList();
    String[] keys = getPersonKeys(shadow.key);
    for (int i=0; i < keys.length; i++){
      al.add(new EntityImpl(keys[i],iperson));
    }
    return al.iterator();
  }
  public IEntity newInstance(String key) throws GroupsException {
    return new EntityImpl(key, null);
  }
  public IEntity newInstance(String key, Class type) throws GroupsException {
    if ( org.jasig.portal.EntityTypes.getEntityTypeID(type) == null )
        { throw new GroupsException("Invalid group type: " + type); }
    return new EntityImpl(key, type);
  }
  public EntityIdentifier[] searchForEntities(String query, int method, Class type)
  throws GroupsException {
    if (type != group && type != iperson)
      return new EntityIdentifier[0];
    ArrayList ids = new ArrayList();
    switch (method){
      case STARTS_WITH:
          query = query+"*";
        break;
      case ENDS_WITH:
          query="*"+query;
        break;
      case CONTAINS:
          query="*"+query+"*";
        break;
    }
    query = namefield+"="+query;
    DirContext context = getConnection();
    NamingEnumeration userlist = null;
    SearchControls sc = new SearchControls();
    sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
    sc.setReturningAttributes(new String[] {keyfield});
    try {
      userlist = context.search(usercontext,query,sc);
    } catch (NamingException nex) {
      LogService.log(LogService.ERROR,"LDAPGroupStore: Unable to perform filter "+query);
      LogService.log(LogService.ERROR,nex);
    }
    ArrayList keys = new ArrayList();
    processLdapResults(userlist,keys);
    String[] k = (String[]) keys.toArray(new String[0]);
    for (int i=0; i<k.length; i++){
      ids.add(new EntityIdentifier(k[i],iperson));
    }
    return (EntityIdentifier[]) ids.toArray(new EntityIdentifier[0]);
  }

/**
 * Answers if <code>group</code> contains <code>member</code>.
 * @return boolean
 * @param group org.jasig.portal.groups.IEntityGroup
 * @param member org.jasig.portal.groups.IGroupMember
 */
public boolean contains(IEntityGroup group, IGroupMember member) 
throws GroupsException 
{
    boolean found = false;
    Iterator itr = ( member.isGroup() )
      ? findMemberGroups(group)
      : findEntitiesForGroup(group);
    while ( itr.hasNext() && ! found )
        { found = member.equals(itr.next()); }
    return found;
}

/**
 * Answers if <code>group</code> contains a member group named 
 * <code>name</code>.
 * @return boolean
 * @param group org.jasig.portal.groups.IEntityGroup
 * @param name java.lang.String
 */
public boolean containsGroupNamed(IEntityGroup group, String name) 
throws GroupsException 
{
    boolean found = false;
    Iterator itr = findMemberGroups(group);
    while ( itr.hasNext() && ! found )
    {
        String otherName = ((IEntityGroup)itr.next()).getName();
        found = otherName != null && otherName.equals(name);
    }
    return found;
}
}