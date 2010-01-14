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

package  org.jasig.portal.channels.permissionsmanager;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;

import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IChannel;
import org.jasig.portal.IPermissible;
import org.jasig.portal.IServant;
import org.jasig.portal.PortalException;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * CPermissionsManagerServantFactory
 *
 * calling getPermissionsServant will return an instance of the default
 * CPermissionsManagerServant implementation
 *
 * @author Alex Vigdor
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class CPermissionsManagerServantFactory {
    private static final Log log = LogFactory.getLog(CPermissionsManagerServantFactory.class);
    private static CPermissionsManagerServantFactory _instance;
    private HashMap servantClasses = new HashMap();

    /** Creates new CPermissionsManagerServantFactory */
    protected CPermissionsManagerServantFactory () {
    }

    /**
     * put your documentation comment here
     * @return
     */
    static IServant getPermissionsServant () {
        return  getPermissionsServant("CPermissionsManagerServant");
    }

    /**
     * put your documentation comment here
     * @param name
     * @return
     */
    protected static IServant getPermissionsServant (String name) {
        return  instance().getServant(name);
    }

    /**
     * put your documentation comment here
     * @param owner
     * @param principals
     * @param activities
     * @param targets
     * @param staticData
     * @return the permissions servant
     * @exception org.jasig.portal.PortalException
     */
    public static IServant getPermissionsServant (IPermissible owner, ChannelStaticData staticData, IAuthorizationPrincipal[] principals,
            String[] activities, String[] targets) throws org.jasig.portal.PortalException {
        boolean isOK = true;
        long time1 = Calendar.getInstance().getTime().getTime();
        RDBMPermissibleRegistry.registerPermissible(owner.getClass().getName());

        IServant servant = getPermissionsServant();
        IPermissible[] owners = new IPermissible[1];
        owners[0] = owner;
        if (activities == null) {
            activities = owner.getActivityTokens();
        }
        if (targets == null) {
            targets = owner.getTargetTokens();
        }
        if (staticData != null) {
            try {
                ChannelStaticData slaveSD = (ChannelStaticData)staticData.clone();
                Enumeration srd = slaveSD.keys();
                while (srd.hasMoreElements()) {
                    slaveSD.remove(srd.nextElement());
                }
                HashMap tHash = new HashMap(1);
                tHash.put(owner.getOwnerToken(), targets);
                HashMap aHash = new HashMap(1);
                aHash.put(owner.getOwnerToken(), activities);
                if (principals != null) {
                    slaveSD.put("prmPrincipals", principals);
                }
                slaveSD.put("prmOwners", owners);
                slaveSD.put("prmActivities", aHash);
                slaveSD.put("prmTargets", tHash);
                slaveSD.put("prmView", "Assign By Owner");
                ((IChannel)servant).setStaticData(slaveSD);
            } catch (Exception e) {
                isOK = false;
            }
        }
        else {
            isOK = false;
        }
        if (!isOK) {
            throw  (new PortalException("CPermissionsServantFactory.getPermissionsServant():: unable to properly initialize servant, check that mast staticData is being properly passed to this method"));
        }
        if (log.isInfoEnabled()) {
            long time2 = Calendar.getInstance().getTime().getTime();
            log.info( "CPermissionsManagerFactory took  "
                    + String.valueOf((time2 - time1)) + " ms to instantiate");
        }

        return  servant;
    }

    /**
     * put your documentation comment here
     * @param name
     * @return
     */
    protected IServant getServant (String name) {
        IServant rs = null;
        if (servantClasses.get(name) == null) {
            try {
                Class cserv = Class.forName("org.jasig.portal.channels.permissionsmanager."
                        + name);
                servantClasses.put(name, cserv);
            } catch (Exception e) {
                log.error(e, e);
            }
        }
        if (servantClasses.get(name) != null) {
            try {
                rs = (IServant)((Class)servantClasses.get(name)).newInstance();
            } catch (Exception e) {
                log.error(e, e);
            }
        }
        return  rs;
    }

    /**
     * put your documentation comment here
     * @return
     */
    protected static synchronized CPermissionsManagerServantFactory instance () {
        if (_instance == null) {
            _instance = new CPermissionsManagerServantFactory();
        }
        return  _instance;
    }
}



