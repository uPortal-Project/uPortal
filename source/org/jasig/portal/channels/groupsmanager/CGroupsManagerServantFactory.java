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
import org.jasig.portal.services.*;
import org.jasig.portal.groups.*;
import org.jasig.portal.IServant;
import org.jasig.portal.*;
import java.util.HashMap;
import java.util.*;

/**
 * CGroupsManagerServantFactory
 *
 * calling getPermissionsServant will return an instance of the default
 * CPermissionsManagerServant implementation
 *
 * @author Alex Vigdor
 * @version $Revision$
 */

public class CGroupsManagerServantFactory {
    private static CGroupsManagerServantFactory _instance;
    private HashMap servantClasses = new HashMap();
    /** Creates new CGroupsManagerServantFactory */
    protected CGroupsManagerServantFactory() {
    }

    protected static IServant getGroupsServant(){
        return getGroupsServant("CGroupsManagerServant");
    }

    protected static IServant getGroupsServant(String name){
        return instance().getServant(name);
    }

    public static IServant getGroupsServantforSelection(ChannelStaticData staticData, String message) throws PortalException{
      return getGroupsServantforSelection(staticData,message,null);
    }

    public static IServant getGroupsServantforGroupMemberships(ChannelStaticData staticData, String message, IGroupMember member, boolean allowFinish) throws PortalException{
      IServant servant = null;
      String typeKey = null;

        try{
          typeKey = GroupService.getRootGroup(member.getType()).getKey();
        }
        catch(Exception e){
          ;
        }

      try {
        servant = getGroupsServant();
        ChannelStaticData slaveSD = (ChannelStaticData) staticData.clone();
        zeroStaticData(slaveSD);
        slaveSD.setParameter("grpView","tree");
        slaveSD.setParameter("grpMode","select");
        slaveSD.setParameter("grpBlockEntitySelect","true");
        slaveSD.put("grpPreSelectForMember",member);
        if (typeKey!=null){
           slaveSD.setParameter("grpViewKey",typeKey);
        }
        if(!allowFinish){
           slaveSD.setParameter("grpAllowFinish","false");
        }
        if (message != null){
          slaveSD.setParameter("grpServantMessage",message);
        }
        ((IChannel)servant).setStaticData(slaveSD);
      }
      catch (Exception e){
          throw(new PortalException("CGroupsManagerServantFactory - unable to initialize servant"));
      }
      return servant;
    }

    public static IServant getGroupsServantforSelection(ChannelStaticData staticData, String message, String type) throws PortalException{
        return getGroupsServantforSelection(staticData, message, type, true,true);
    }
    public static IServant getGroupsServantforSelection(ChannelStaticData staticData, String message, String type, boolean allowFinish, boolean allowEntitySelect) throws PortalException{
      long time1 = Calendar.getInstance().getTime().getTime();
      IServant servant;
      String typeKey = null;
      if (type!=null && !type.equals("")){
        try{
          typeKey = GroupService.getDistinguishedGroup(type).getKey();
        }
        catch(Exception e){
          ;
        }
      }
      try {
        servant = getGroupsServant();
        ChannelStaticData slaveSD = (ChannelStaticData) staticData.clone();
        zeroStaticData(slaveSD);
        slaveSD.setParameter("grpView","tree");
        slaveSD.setParameter("grpMode","select");
        if (typeKey!=null){
           slaveSD.setParameter("grpViewKey",typeKey);
        }
        if(!allowFinish){
           slaveSD.setParameter("grpAllowFinish","false");
        }
        if(!allowEntitySelect){
           slaveSD.setParameter("grpBlockEntitySelect","true");
        }
        if (message != null){
          slaveSD.setParameter("grpServantMessage",message);
        }
        ((IChannel)servant).setStaticData(slaveSD);
      }
      catch (Exception e){
          throw(new PortalException("CGroupsManagerServantFactory - unable to initialize servant"));
      }
      long time2 = Calendar.getInstance().getTime().getTime();
                LogService.instance().log(LogService.INFO, "CGroupsManagerFactory took  "
                        + String.valueOf((time2 - time1)) + " ms to instantiate selection servant");
      return servant;
    }

    public static IServant getGroupsServantforAddRemove(ChannelStaticData staticData, String groupKey) throws PortalException{
        long time1 = Calendar.getInstance().getTime().getTime();
      IServant servant;
      try {
        IEntityGroup testgroup = GroupService.findGroup(groupKey);
        testgroup.getClass();
        servant = getGroupsServant();
        ChannelStaticData slaveSD = (ChannelStaticData) staticData.clone();
        zeroStaticData(slaveSD);
        slaveSD.setParameter("grpView","edit");
        slaveSD.setParameter("grpViewKey",groupKey);
        ((IChannel)servant).setStaticData(slaveSD);
      }
      catch (Exception e){
          throw(new PortalException("CGroupsManagerServantFactory - unable to initialize servant"));
      }
        long time2 = Calendar.getInstance().getTime().getTime();
                LogService.instance().log(LogService.INFO, "CGroupsManagerFactory took  "
                        + String.valueOf((time2 - time1)) + " ms to instantiate add/remove servant");
        return servant;
    }

    protected static void zeroStaticData(ChannelStaticData sd){
        Enumeration srd = sd.keys();
        while (srd.hasMoreElements()) {
            sd.remove(srd.nextElement());
        }
    }

    protected IServant getServant(String name){
        IServant rs = null;
        if (servantClasses.get(name)==null){
            try{
                Class cserv = Class.forName("org.jasig.portal.channels.groupsmanager."+name);
                servantClasses.put(name,cserv);
            }
            catch(Exception e){
                LogService.instance().log(LogService.ERROR,e);
            }
        }
        if (servantClasses.get(name)!=null){
            try {
                rs = (IServant) ((Class) servantClasses.get(name)).newInstance();
            }
            catch(Exception e){
                LogService.instance().log(LogService.ERROR,e);
            }
        }
        return rs;
    }

    protected static synchronized CGroupsManagerServantFactory instance(){
        if(_instance==null){
            _instance = new CGroupsManagerServantFactory();
        }
        return _instance;
    }

}
