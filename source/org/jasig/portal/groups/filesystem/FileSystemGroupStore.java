/* Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.groups.filesystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.oro.io.GlobFilenameFilter;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.EntityTypes;
import org.jasig.portal.groups.EntityGroupImpl;
import org.jasig.portal.groups.EntityImpl;
import org.jasig.portal.groups.GroupServiceConfiguration;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IEntityGroupStore;
import org.jasig.portal.groups.IEntitySearcher;
import org.jasig.portal.groups.IEntityStore;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.services.GroupService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is an <code>IEntityGroupStore</code> that uses the native file
 * system for its back end.  It also implements <code>IEntityStore</code> and
 * a no-op <code>IEntitySearcher</code>.  You can substitute a functional entity
 * searcher by adding it to the group service element for this component in the
 * configuration document, <code>compositeGroupServices.xml</code>.
 * <p>
 * A groups file system looks like this:
 * <p><code>
 * <hr width="100%">
 *  --&nbsp;groups root<br>
 * <blockquote>&nbsp;--&nbsp;org.jasig.portal.ChannelDefinition<br>
 *     <blockquote>&nbsp;--&nbsp;channel definition file<br>
 *                 &nbsp;--&nbsp;channel definition file<br>
 *        ...<br>
 *     </blockquote>
 * &nbsp;--&nbsp;org.jasig.portal.security.IPerson<br>
 *     <blockquote>&nbsp;--&nbsp;person directory<br>
 *         <blockquote>&nbsp;--&nbsp;person file <br>
 *                     &nbsp;--&nbsp;person file <br>
 *                     ...<br>
 *         </blockquote>
 *        &nbsp;--&nbsp;person directory <br>
 *     </blockquote>
 *      etc.<br>
 * </blockquote>
 * <hr width="100%">
 * </code><p>
 * The groups root is a file system directory declared in the group service
 * configuration document, where it is an attribute of the filesystem group
 * service element.  This directory has sub-directories, each named for the
 * underlying entity type that groups in that sub-directory contain.  If a
 * service only contains groups of IPersons, the groups root would have 1
 * sub-directory named org.jasig.portal.security.IPerson.
 * <p>
 * A directory named for a type may contain both sub-directories and files.
 * The sub-directories represent groups that can contain other groups.  The
 * files represent groups that can contain entity as well as group members.
 * The files contain keys, one to a line, and look like this:
 * <p><code>
 * <hr width="100%">
 * #&nbsp;this is a comment<br>
 * #&nbsp;another comment<br>
 * <br>
 * key1 Key One<br>
 * key2<br>
 * group:org$jasig$portal$security$IPerson/someDirectory/someFile<br>
 * key3<br>
 * &nbsp;# comment <br>
 * <hr width="100%">
 *</code><p>
 * Blank lines and lines that start with the <code>COMMENT</code> String (here
 * <code>#</code>) are ignored.  The first token on a non-ignored line is
 * assumed to be a group member key.  If the key starts with the
 * <code>GROUP_PREFIX</code> (here <code>:group</code>), it is treated as a
 * local group key.  Otherwise, it is assumed to be an entity key.  The rest of
 * the tokens on the line are ignored.
 * <p>
 * The file above contains 3 entity keys, <code>key1</code>, <code>key2</code>,
 * and <code>key3</code>, and 1 group key,
 * <code>org$jasig$portal$security$IPerson/someDirectory/someFile</code>.  It
 * represents a group with 3 entity members and 1 group member.  The local key
 * of a group is its file path starting at the type name, with the
 * <code>FileSystemGroupStore.SUBSTITUTE_PERIOD</code> character substituted
 * for the real period character.
 * <p>
 * The store is not implemented as a singleton, so you can have multiple
 * concurrent instances pointing to different groups root directories.
 * <p>
 *
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class FileSystemGroupStore implements IEntityGroupStore, IEntityStore,
IEntitySearcher
{
    private static final Log log = LogFactory.getLog(FileSystemGroupStore.class);
    // File system constants for unix/windows compatibility:
    protected static char FORWARD_SLASH = '/';
    protected static char BACK_SLASH = '\\';

    // Group file constants:
    protected static String COMMENT = "#";
    protected static String GROUP_PREFIX = "group:";

    // The period is legal in filesystem names but could conflict with
    // the node separator in the group key.
    protected static char PERIOD = '.';
    protected static char SUBSTITUTE_PERIOD = '$';
    protected boolean useSubstitutePeriod = false;

    private static String DEBUG_CLASS_NAME = "FileSystemGroupStore";

    // Path to groups root directory.
    private String groupsRootPath;

    // Either back slash or forward slash.
    protected char goodSeparator;
    protected char badSeparator;

    // Cache of retrieved groups.
    private Map cache;

    private Class defaultEntityType;

    // Value holder adds last modified timestamp.
    private class GroupHolder {
        private long lastModified = 0;
        private IEntityGroup group;
        protected GroupHolder (IEntityGroup g, long lm) {
            this.group = g;
            this.lastModified = lm;
        }
        protected IEntityGroup getGroup () {
            return group;
        }
        protected long getLastModified() {
            return  lastModified;
        }
    }

/**
 * FileSystemGroupStore constructor.
 */
public FileSystemGroupStore() {
    super();
    initialize();
}
/**
 * @return GroupHolder
 */
protected GroupHolder cacheGet(String key)
{
    return (GroupHolder) getCache().get(key);
}
/**
 *
 */
protected void cachePut(String key, Object val)
{
    getCache().put(key, val);
}
/**
 *
 */
protected String conformSeparatorChars(String s)
{
    return s.replace(getBadSeparator(), getGoodSeparator());
}
/**
 * Delete this <code>IEntityGroup</code> from the data store.  We assume that
 * groups will be deleted via the file system, not the group service.
 * @param group org.jasig.portal.groups.IEntityGroup
 */
public void delete(org.jasig.portal.groups.IEntityGroup group) throws GroupsException
{
    throw new UnsupportedOperationException("FileSystemGroupStore.delete() not supported");
}
/**
 * Returns an instance of the <code>IEntityGroup</code> from the data store.
 * @return org.jasig.portal.groups.IEntityGroup
 * @param file java.io.File
 */
private IEntityGroup find(File file) throws GroupsException
{
    return find(getKeyFromFile(file));
}
/**
 * Returns an instance of the <code>IEntityGroup</code> from the data store.
 * @return org.jasig.portal.groups.IEntityGroup
 * @param key java.lang.String
 */
public IEntityGroup find(String key) throws GroupsException
{
    log.debug(
        DEBUG_CLASS_NAME + ".find(): group key: " + key);

    String path = getFilePathFromKey(key);
    File f = new File(path);
    if ( ! f.exists() )
        { return null; }

    GroupHolder groupHolder = cacheGet(key);

    if ( groupHolder == null || (groupHolder.getLastModified() != f.lastModified()) )
    {
        log.debug(
          DEBUG_CLASS_NAME + ".find(): retrieving group from file system for " + path);

        IEntityGroup group = newInstance(f);
        groupHolder = new GroupHolder(group, f.lastModified()) ;
        cachePut(key, groupHolder);
    }
    return groupHolder.getGroup();
}
/**
 * Returns an <code>Iterator</code> over the <code>Collection</code> of
 * <code>IEntityGroups</code> that the <code>IEntity</code> belongs to.
 * @return java.util.Iterator
 * @param ent org.jasig.portal.groups.IEntityGroup
 */
protected Iterator findContainingGroups(IEntity ent) throws GroupsException
{

    log.debug(
        DEBUG_CLASS_NAME + ".findContainingGroups(): for " + ent);

    List groups = new ArrayList();
    File root = getFileRoot(ent.getType());
    if ( root != null )
    {
        File[] files = getAllFilesBelow(root);

       try
        {
            for (int i=0; i<files.length; i++)
            {
                Collection ids = getEntityIdsFromFile(files[i]);
                if ( ids.contains(ent.getKey()) )
                    { groups.add(find(files[i])); }
            }
        }
        catch (Exception ex)
            { throw new GroupsException("Problem reading group files: " + ex.getMessage()); }
    }

    return groups.iterator();
}
/**
 * Returns an <code>Iterator</code> over the <code>Collection</code> of
 * <code>IEntityGroups</code> that the <code>IGroupMember</code> belongs to.
 * @return java.util.Iterator
 * @param group org.jasig.portal.groups.IEntityGroup
 */
protected Iterator findContainingGroups(IEntityGroup group) throws GroupsException
{

    log.debug(
        DEBUG_CLASS_NAME + ".findContainingGroups(): for " + group);

    List groups = new ArrayList();
    {
        String typeName = group.getLeafType().getName();
        File parent = getFile(group).getParentFile();
        if ( ! parent.getName().equals(typeName) )
            { groups.add(find(parent)); }

        File root = getFileRoot(group.getLeafType());
        File[] files = getAllFilesBelow(root);
        try
        {
            for (int i=0; i<files.length; i++)
            {
                Collection ids = getGroupIdsFromFile(files[i]);
                if ( ids.contains(group.getLocalKey()) )
                    { groups.add(find(files[i])); }
            }
        }
        catch (Exception ex)
            { throw new GroupsException("Problem reading group files: " + ex.getMessage()); }
    }
    return groups.iterator();
}
/**
 * Returns an <code>Iterator</code> over the <code>Collection</code> of
 * <code>IEntityGroups</code> that the <code>IGroupMember</code> belongs to.
 * @return java.util.Iterator
 * @param gm org.jasig.portal.groups.IEntityGroup
 */
public Iterator findContainingGroups(IGroupMember gm) throws GroupsException
{
    if ( gm.isGroup() )
    {
        IEntityGroup group = (IEntityGroup) gm;
        return findContainingGroups(group);
    }
    else
    {
        IEntity ent = (IEntity) gm;
        return findContainingGroups(ent);
    }
}
/**
 * Returns an <code>Iterator</code> over the <code>Collection</code> of
 * <code>IEntities</code> that are members of this <code>IEntityGroup</code>.
 * @return java.util.Iterator
 * @param group org.jasig.portal.groups.IEntityGroup
 */
public java.util.Iterator findEntitiesForGroup(IEntityGroup group) throws GroupsException
{
    log.debug(
        DEBUG_CLASS_NAME + ".findEntitiesForGroup(): retrieving entities for group " + group);

    Collection entities = null;
    File f = getFile(group);
    if ( f.isDirectory() )
        { entities = Collections.EMPTY_LIST; }
    else
        { entities = getEntitiesFromFile(f); }

    return entities.iterator();
}
/**
 * Returns an instance of the <code>ILockableEntityGroup</code> from the data store.
 * @return org.jasig.portal.groups.IEntityGroup
 * @param key java.lang.String
 */
public org.jasig.portal.groups.ILockableEntityGroup findLockable(String key)
throws GroupsException
{
    throw new UnsupportedOperationException(DEBUG_CLASS_NAME + ".findLockable() not supported");
}
/**
 * Returns a <code>String[]</code> containing the keys of  <code>IEntityGroups</code>
 * that are members of this <code>IEntityGroup</code>.  In a composite group
 * system, a group may contain a member group from a different service.  This is
 * called a foreign membership, and is only possible in an internally-managed
 * service.  A group store in such a service can return the key of a foreign member
 * group, but not the group itself, which can only be returned by its local store.
 *
 * @return String[]
 * @param group org.jasig.portal.groups.IEntityGroup
 */
public java.lang.String[] findMemberGroupKeys(IEntityGroup group) throws GroupsException
{
    String[] keys;
    File f = getFile(group);
    if ( f.isDirectory() )
    {
        File[] files = f.listFiles();
        keys = new String[files.length];
        for (int i=0; i<files.length; i++)
            { keys[i] = getKeyFromFile(files[i]); }
    }
    else
    {
        try
        {
            Collection groupKeys = getGroupIdsFromFile(f);
            keys = (String[])groupKeys.toArray(new String[groupKeys.size()]);
        }
        catch (Exception ex)
            { throw new GroupsException(DEBUG_CLASS_NAME + ".findMemberGroupKeys(): " +
                 "problem finding group members: " + ex.getMessage()); }
    }
    return keys;
}
/**
 * Returns an <code>Iterator</code> over the <code>Collection</code> of
 * <code>IEntityGroups</code> that are members of this <code>IEntityGroup</code>.
 * @return java.util.Iterator
 * @param group org.jasig.portal.groups.IEntityGroup
 */
public java.util.Iterator findMemberGroups(IEntityGroup group) throws GroupsException
{
    String[] keys = findMemberGroupKeys(group);  // No foreign groups here.
    List groups = new ArrayList(keys.length);
    for (int i=0; i<keys.length; i++)
        { groups.add(find(keys[i])); }
    return groups.iterator();
}
/**
 * Recursive search of directories underneath dir for files that match filter.
 * @return java.util.Set
 */
public Set getAllDirectoriesBelow(File dir)
{
    Set allDirectories = new HashSet();
    if ( dir.isDirectory() )
        { primGetAllDirectoriesBelow(dir, allDirectories); }
    return allDirectories;
}
/**
 * Recursive search of directories underneath dir for files that match filter.
 */
public File[] getAllFilesBelow(File dir)
{
    Set allFiles = new HashSet();
    if ( dir.isDirectory() )
        { primGetAllFilesBelow(dir, allFiles); }
    return (File[]) allFiles.toArray(new File[allFiles.size()]);
}
/**
 * Returns the filesystem separator character NOT in use.
 * @return char
 */
protected char getBadSeparator() {
    return badSeparator;
}
/**
 * @return java.util.Map
 */
protected java.util.Map getCache()
{
    return cache;
}
/**
 * Returns a Class representing the default entity type.
 * @return Class
 */
protected Class getDefaultEntityType() {
    return defaultEntityType;
}
/**
 * @param idFile java.io.File - a file of ids.
 * @return entities Collection.
 */
protected Collection getEntitiesFromFile(File idFile) throws GroupsException
{
    log.debug(
        DEBUG_CLASS_NAME + "getEntitiesFromFile(): for " + idFile.getPath());

    Collection ids = null;
    Class type = getEntityType(idFile);
    if ( EntityTypes.getEntityTypeID(type) == null )
        { throw new GroupsException("Invalid entity type: " + type); }
    try
        { ids = getEntityIdsFromFile(idFile); }
    catch (Exception ex)
        { throw new GroupsException("Problem retrieving keys from file: " + ex.getMessage()); }

    Collection entities = new ArrayList(ids.size());

    for ( Iterator itr=ids.iterator(); itr.hasNext(); )
    {
        String key = (String) itr.next();
        entities.add(GroupService.getEntity(key, type));
    }

    log.debug(
        DEBUG_CLASS_NAME + "getEntitiesFromFile(): Retrieved " + entities.size() + " entities");

    return entities;
}
/**
 * @param idFile java.io.File - a file of ids.
 * @return String[] ids.
 */
protected Collection getEntityIdsFromFile(File idFile) throws IOException, FileNotFoundException
{
    log.debug(
        DEBUG_CLASS_NAME + "getEntityIdsFromFile(): Reading " + idFile.getPath());

    Collection ids = getIdsFromFile(idFile, false);

    log.debug(
        DEBUG_CLASS_NAME + "getEntityIdsFromFile(): Retrieved " + ids.size() + " IDs");

    return ids;
}
/**
 * @param f File
 * @return java.lang.Class
 * The Class is the first node of the full path name.
 */
protected Class getEntityType(File f)
{
    Class cl = null;
    String path = f.getPath();
    String afterRootPath = null;
    Class type = null;
    if ( path.startsWith(getGroupsRootPath()) )
    {
        afterRootPath = path.substring(getGroupsRootPath().length());
        int end = afterRootPath.indexOf(File.separatorChar);
        String typeName = afterRootPath.substring(0,end);

        try
            { type = Class.forName(typeName); }
        catch (ClassNotFoundException cnfe) {}
    }
    return type;
}
/**
 * @param group IEntityGroup.
 * @return File
 */
protected File getFile(IEntityGroup group)
{
    String key = getFilePathFromKey(group.getLocalKey());
    return new File(key);
}
/**
 *
 */
protected String getFilePathFromKey(String key)
{
    log.debug(
        DEBUG_CLASS_NAME + ".getFilePathFromKey(): for key: " + key);
        
    String groupKey = useSubstitutePeriod 
      ? key.replace(SUBSTITUTE_PERIOD, PERIOD) 
      : key;

    String fullKey = getGroupsRootPath() + groupKey;

     log.debug(
        DEBUG_CLASS_NAME + ".getFilePathFromKey(): full key: " + fullKey);

    return conformSeparatorChars(fullKey);
}
/**
 * Returns a File that is the root for groups of the given type.
 */
protected File getFileRoot(Class type)
{
    String path = getGroupsRootPath() + type.getName();
    File f = new File(path);
    return ( f.exists() ) ? f : null;
}
/**
 * Returns the filesystem separator character in use.
 * @return char
 */
protected char getGoodSeparator() {
    return goodSeparator;
}
/**
 * @param idFile java.io.File - a file of ids.
 * @return String[] ids.
 */
protected Collection getGroupIdsFromFile(File idFile) throws IOException, FileNotFoundException
{
    log.debug(
        DEBUG_CLASS_NAME + "getGroupIdsFromFile(): Reading " + idFile.getPath());

    Collection ids = getIdsFromFile(idFile, true);

    log.debug(
        DEBUG_CLASS_NAME + "getGroupIdsFromFile(): Retrieved " + ids.size() + " IDs");

    return ids;
}
/**
 * @return java.lang.String
 */
public java.lang.String getGroupsRootPath() {
    return groupsRootPath;
}
/**
 * @param idFile java.io.File - a file of ids.
 * @return String[] ids.
 */
protected Collection getIdsFromFile(File idFile, boolean groupIds)
throws IOException, FileNotFoundException
{
    Collection ids = new HashSet();
    BufferedReader br = new BufferedReader(new FileReader(idFile));
    String line, tok;

    line = br.readLine();
    while(line != null)
    {
        line = line.trim();
        if ( ! line.startsWith(COMMENT) && (line.length() > 0) )
        {
            StringTokenizer st = new StringTokenizer(line);
            tok = st.nextToken();
            if ( tok != null )
            {
                if ( tok.startsWith(GROUP_PREFIX) )
                {
                    if ( groupIds )
                        { ids.add(tok.substring(GROUP_PREFIX.length())); }
                }
                else
                {
                    if ( ! groupIds )
                        { ids.add(tok); }
                }
            }
        }
        line = br.readLine();
    }
    br.close();

    return ids;
}
/**
 *
 */
protected String getKeyFromFile(File f)
{
    String key = null;
    if ( f.getPath().startsWith(getGroupsRootPath()) )
    {
        key = f.getPath().substring(getGroupsRootPath().length());
        
        if ( useSubstitutePeriod ) 
            {  key = key.replace(PERIOD, SUBSTITUTE_PERIOD); }
    }
    return key;
}
/**
 *
 */
protected void initialize()
{
    cache = Collections.synchronizedMap(new HashMap());

    goodSeparator = File.separatorChar;
    badSeparator = ( goodSeparator == FORWARD_SLASH ) ? BACK_SLASH : FORWARD_SLASH;

    defaultEntityType = org.jasig.portal.security.IPerson.class;
    
    try
    {
        String sep = GroupServiceConfiguration.getConfiguration().getNodeSeparator();
        String period = String.valueOf(PERIOD);
        useSubstitutePeriod = sep.equals(period);
    }
    catch (Exception ex) {}

}
/**
 * @return org.jasig.portal.groups.IEntityGroup
 */
private IEntityGroup newInstance (File f) throws GroupsException
{
    String key = getKeyFromFile(f);
    String name = f.getName();
    Class cl = getEntityType(f);
    return newInstance(key, cl, name);
}
/**
 * @return org.jasig.portal.groups.IEntityGroup
 * We assume that new groups will be created updated via the file system,
 * not the group service.
 */
public IEntityGroup newInstance(Class entityType) throws GroupsException
{
    throw new UnsupportedOperationException(DEBUG_CLASS_NAME +
      ".newInstance(Class cl) not supported");
}
public IEntity newInstance(String key) throws GroupsException
{
    return newInstance(key, getDefaultEntityType());
}
public IEntity newInstance(String key, Class type) throws GroupsException
{
    if ( org.jasig.portal.EntityTypes.getEntityTypeID(type) == null )
        { throw new GroupsException("Invalid group type: " + type); }
    return new EntityImpl(key, type);
}
/**
 * @return org.jasig.portal.groups.IEntityGroup
 */
private IEntityGroup newInstance (String newKey, Class newType, String newName)
throws GroupsException
{
    EntityGroupImpl egi = new EntityGroupImpl(newKey, newType);
    egi.primSetName(newName);
    return egi;
}
/**
 * Returns all directories under dir.
 */
private void primGetAllDirectoriesBelow(File dir, Set allDirectories)
{
    File[] files = dir.listFiles();
    for(int i=0; i<files.length; i++)
    {
        if ( files[i].isDirectory() )
        {
            primGetAllDirectoriesBelow(files[i], allDirectories);
            allDirectories.add(files[i]);
        }
    }
}
/**
 * Returns all files (not directories) underneath dir.
 */
private void primGetAllFilesBelow(File dir, Set allFiles)
{
    File[] files = dir.listFiles();
    for(int i=0; i<files.length; i++)
    {
        if ( files[i].isDirectory() )
            { primGetAllFilesBelow(files[i], allFiles); }
        else
            { allFiles.add(files[i]); }
    }
}
/**
 * Find EntityIdentifiers for entities whose name matches the query string
 * according to the specified method and is of the specified type
 */
public EntityIdentifier[] searchForEntities(String query, int method, Class type)
throws GroupsException
{
    return new EntityIdentifier[0];
}
/**
 * Returns an EntityIdentifier[] of groups of the given leaf type whose names
 * match the query string according to the search method.
 *
 * @param query String the string used to match group names.
 * @param searchMethod see org.jasig.portal.groups.IGroupConstants.
 * @param leafType the leaf type of the groups we are searching for.
 * @return EntityIdentifier[]
 */
public EntityIdentifier[] searchForGroups(String query, int searchMethod, Class leafType)
throws GroupsException
{
    List ids = new ArrayList();
    File baseDir = getFileRoot(leafType);

    log.debug(
        DEBUG_CLASS_NAME + "searchForGroups(): " + query + " method: " +
        searchMethod + " type: " + leafType);

    if ( baseDir != null )
    {
        String nameFilter = null;

        switch (searchMethod)
        {
          case IS:
            nameFilter = query;
            break;
          case STARTS_WITH:
            nameFilter = query+"*";
            break;
          case ENDS_WITH:
           nameFilter = "*"+query;
           break;
          case CONTAINS:
            nameFilter = "*"+query+"*";
            break;
          default:
            throw new GroupsException(DEBUG_CLASS_NAME +
                ".searchForGroups(): Unknown search method: " + searchMethod);
        }

        FilenameFilter filter = new GlobFilenameFilter(nameFilter);
        Set allDirs = getAllDirectoriesBelow(baseDir);
        allDirs.add(baseDir);

        Set allFiles = new HashSet();
        for (Iterator itr = allDirs.iterator(); itr.hasNext(); )
        {
            File[] files = ((File)itr.next()).listFiles(filter);
            for ( int filesIdx=0; filesIdx<files.length; filesIdx++ )
            {
                String key = getKeyFromFile(files[filesIdx]);
                EntityIdentifier ei = new EntityIdentifier(key, EntityTypes.GROUP_ENTITY_TYPE);
                ids.add(ei);
            }
        }
    }

    log.debug(DEBUG_CLASS_NAME +
      ".searchForGroups(): found " + ids.size() + " files.");

    return (EntityIdentifier[]) ids.toArray(new EntityIdentifier[ids.size()]);
}
/**
 * @param newCache java.util.Map
 */
protected void setCache(java.util.Map newCache) {
    cache = newCache;
}
/**
 * @param newGroupsRootPath java.lang.String
 */
protected void setGroupsRootPath(java.lang.String newGroupsRootPath) {
    groupsRootPath = conformSeparatorChars(newGroupsRootPath) + getGoodSeparator();
}
/**
 * Adds or updates the <code>IEntityGroup</code> AND ITS MEMBERSHIPS to the
 * data store, as appropriate.  We assume that groups will be updated via the
 * file system, not the group service.
 * @param group org.jasig.portal.groups.IEntityGroup
 */
public void update(org.jasig.portal.groups.IEntityGroup group) throws GroupsException
{
    throw new UnsupportedOperationException(DEBUG_CLASS_NAME + ".update() not supported");
}
/**
 * Commits the group memberships of the <code>IEntityGroup</code> to
 * the data store.  We assume that groups will be updated via the
 * file system, not the group service.
 * @param group org.jasig.portal.groups.IEntityGroup
 */
public void updateMembers(org.jasig.portal.groups.IEntityGroup group)
throws GroupsException
{
    throw new UnsupportedOperationException(DEBUG_CLASS_NAME + ".updateMembers() not supported");
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
    boolean contains = false;
    File f = getFile(group);
    if ( f.exists() )
    {
        contains = ( f.isDirectory() )
          ? directoryContains(f, member)
          : fileContains(f, member);
    }
    return contains;
}
/**
 * Answers if <code>file</code> contains <code>member</code>.  
 * @return boolean
 * @param directory java.io.File
 * @param group org.jasig.portal.groups.IEntityGroup
 */
private boolean fileContains(File file, IGroupMember member)
throws GroupsException
{
    Collection ids=null;
    try 
    {
        ids = ( member.isEntity() )
          ? getEntityIdsFromFile(file)
          : getGroupIdsFromFile(file);
    }
    catch (Exception ex)
    {
        throw new GroupsException("Error retrieving ids from file: " 
          + ex.getMessage());
    }
    return ids.contains(member.getKey());
}

/**
 * Answers if <code>directory</code> contains <code>member</code>.  A 
 * directory can only contain (other) groups.  
 * @return boolean
 * @param directory java.io.File
 * @param group org.jasig.portal.groups.IEntityGroup
 */
private boolean directoryContains(File directory, IGroupMember member)
{
    boolean found=false;
    if ( member.isGroup() )
    {
        File memberFile = getFile((IEntityGroup)member);
        File[] files = directory.listFiles();
        for (int i=0; i<files.length & ! found; i++)
            { found = files[i].equals(memberFile); }
    }
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
