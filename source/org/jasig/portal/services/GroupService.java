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

package org.jasig.portal.services;
import org.jasig.portal.groups.*;
import org.jasig.portal.*;
/**
 *  This class maintains a facade to an underlying IEntityGroupStore implementation specified 
 *  in the portal properties file.  It privately maintains a singleton instance, and is accessed 
 *  via static methods, which delegate to instance methods, which call on the specific implementation.
 *
 * @author  Alex Vigdor
 * @version $Revision$
 */
public class GroupService {
    private static GroupService _instance = null;
    private static IEntityGroupStore m_groupstore = null;
    private static String s_groupstorename = PropertiesManager.getProperty("org.jasig.portal.services.GroupService.EntityGroupStoreImpl");
    /** Creates new GroupService */
    private GroupService() {
        if (s_groupstorename == null) {
            LogService.log(LogService.ERROR, new PortalException
            ("EntityGroupStoreImpl not specified or incorrect in portal.properties"));
        }
        else {
            try {
                m_groupstore = (IEntityGroupStore)Class.forName(s_groupstorename).newInstance();
            }
            catch (Exception e) {
                LogService.log(LogService.ERROR, new PortalException
                ("Failed to instantiate " + s_groupstorename));
            }
        }
    }
    
    private static synchronized GroupService instance(){
        if(_instance==null){
            _instance = new GroupService();
        }
        return _instance;
    }
    protected void idelete(IEntityGroup group) throws GroupsException{
        m_groupstore.delete(group);
    }
    
    public static void delete(IEntityGroup group) throws GroupsException{
       instance().idelete(group);
    }
    /**
     * Returns an instance of the <code>IEntityGroup</code> from the data store.
     * @return org.jasig.portal.groups.IEntityGroup
     * @param key java.lang.String
     */
    protected IEntityGroup ifind(String key) throws GroupsException{
        return m_groupstore.find(key);
    }
    public static IEntityGroup find(String key) throws GroupsException{
        return instance().ifind(key);
    }
    /**
     * Returns an <code>Iterator</code> over the <code>Collection</code> of
     * <code>IEntityGroups</code> that the <code>IGroupMember</code> belongs to.
     * @return java.util.Iterator
     * @param gm org.jasig.portal.groups.IEntityGroup
     */
    protected java.util.Iterator ifindContainingGroups(IGroupMember gm) throws GroupsException{
        return m_groupstore.findContainingGroups(gm);
    }
    public static java.util.Iterator findContainingGroups(IGroupMember gm) throws GroupsException{
        return instance().ifindContainingGroups(gm);
    }
    /**
     * Returns an <code>Iterator</code> over the <code>Collection</code> of
     * <code>IEntityGroups</code> that are members of this <code>IEntityGroup</code>.
     * @return java.util.Iterator
     * @param group org.jasig.portal.groups.IEntityGroup
     */
    protected java.util.Iterator ifindMemberGroups(IEntityGroup group) throws GroupsException{
        return m_groupstore.findMemberGroups(group);
    }
    public static java.util.Iterator findMemberGroups(IEntityGroup group) throws GroupsException{
        return instance().ifindMemberGroups(group);
    }
    /**
     * @return org.jasig.portal.groups.IEntityGroup
     */
    protected IEntityGroup inewInstance(Class entityType) throws GroupsException{
        return m_groupstore.newInstance(entityType);
    }
    public static IEntityGroup newInstance(Class entityType) throws GroupsException{
        return instance().inewInstance(entityType);
    }
    /**
     * Adds or updates the <code>IEntityGroup</code> to the data store, as appropriate.
     * @param group org.jasig.portal.groups.IEntityGroup
     */
    protected void iupdate(IEntityGroup group) throws GroupsException{
        m_groupstore.update(group);
    }
    public static void update(IEntityGroup group) throws GroupsException{
        instance().iupdate(group);
    }
    /**
     * Commits the group memberships of the <code>IEntityGroup</code> to
     * the data store.
     * @param group org.jasig.portal.groups.IEntityGroup
     */
    protected void iupdateMembers(IEntityGroup group) throws GroupsException{
        m_groupstore.updateMembers(group);
    }
    public static void updateMembers(IEntityGroup group) throws GroupsException{
        instance().iupdateMembers(group);
    }
    
}
