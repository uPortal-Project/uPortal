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
 *
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
 */
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
        long time2 = Calendar.getInstance().getTime().getTime();
        log.info( "CPermissionsManagerFactory took  "
                + String.valueOf((time2 - time1)) + " ms to instantiate");
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
                log.error( e);
            }
        }
        if (servantClasses.get(name) != null) {
            try {
                rs = (IServant)((Class)servantClasses.get(name)).newInstance();
            } catch (Exception e) {
                log.error( e);
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



