/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.permissionsmanager;

import java.util.HashMap;

import org.jasig.portal.IPermissible;
import org.jasig.portal.IServant;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermission;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.XSLT;


/**
 * CPermissionsManagerServant is an IServant subclass of CPermissionsManager
 * This will allow other channels to delegate to CPermissionsManager at runtime
 *
 * Master channels should instantiate this channel with the following
 * staticData parameter preset:
 *
 * prmOwners = IPermissible[] owners
 *
 * where owners is an array with a single element being an instance of the
 * master's representative IPermissible class.
 *
 * see org.jasig.portal.IPermissible for more information
 *
 * @author Alex Vigdor
 * @version $Revision$
 */
public class CPermissionsManagerServant extends CPermissionsManager
        implements IServant {
    private static final Log log = LogFactory.getLog(CPermissionsManagerServant.class);
    
    private IPermission[] results;

    /** Creates new CPermissionsManagerServant */
    public CPermissionsManagerServant () {
    }

    /**
     * put your documentation comment here
     * @param xslt
     */
    protected void transform (XSLT xslt) {
        if (!isFinished()) {
            try {
                xslt.setStylesheetParameter("isAdminUser", "true");
                xslt.transform();
            } catch (Exception e) {
                log.debug(e, e);
            }
        }
    }

    /**
     * put your documentation comment here
     * 
     * @return <code>true</code> if finished; <code>false</code> otherwise
     */
    public boolean isFinished () {
        return  session.isFinished;
    }

    /**
     * put your documentation comment here
     * Currently returns and empy array
     * @return the results
     */
    public Object[] getResults () {
        return  results;
    }

    public void setStaticData (org.jasig.portal.ChannelStaticData sD) {
        super.setStaticData(sD);
        session.isAuthorized = true;

        // handle pre-selection for servant mode
        if (session.staticData.get("prmOwners") != null) {
            log.debug("PermissionServant.setStaticData(): processing pre-selection");

            // use specified set of owners
            IPermissible[] selOwners = (IPermissible[])session.staticData.get("prmOwners");
            for (int j =0; j < selOwners.length ; j++){
              PermissionsXML.setSelected(session,selOwners[j].getClass().getName(),"owner", selOwners[j].getOwnerToken(),true);

              if (session.staticData.get("prmTargets") != null) {
                  if (((HashMap)session.staticData.get("prmTargets")).get(selOwners[j].getOwnerToken())
                          != null) {
                    String[] selTargets = (String[])((HashMap)session.staticData.get("prmTargets")).get(selOwners[j].getOwnerToken());
                    if (log.isDebugEnabled())
                        log.debug("PermissionServant.setStaticData(): got "+selTargets.length+" pre-selected targets");
                    for (int s=0;s<selTargets.length;s++){
                      PermissionsXML.setSelected(session,selOwners[j].getClass().getName(),"target",selTargets[s],true);
                    }
                    session.gotTargets = true;
                  }
                  else {
                    log.debug("PermissionServant.setStaticData(): error retrieving pre-selected targets");
                  }
              }

              if (session.staticData.get("prmActivities") != null) {
                  if (((HashMap)session.staticData.get("prmActivities")).get(selOwners[j].getOwnerToken())
                          != null) {
                      String[] selActivities = (String[])((HashMap)session.staticData.get("prmActivities")).get(selOwners[j].getOwnerToken());
                      if (log.isDebugEnabled())
                          log.debug("PermissionServant.setStaticData(): got "+selActivities.length+" pre-selected activities");
                      for (int s=0;s<selActivities.length;s++){
                        PermissionsXML.setSelected(session,selOwners[j].getClass().getName(),"activity",selActivities[s],true);
                      }
                      session.gotActivities = true;
                  }
                  else {
                    log.debug("PermissionServant.setStaticData(): error retrieving pre-selected activities");
                  }
              }

            }
            session.gotOwners = true;
        }
        if (session.staticData.get("prmPrincipals")!=null){
          session.principals = (IAuthorizationPrincipal[]) session.staticData.get("prmPrincipals");
          PermissionsXML.populatePrincipals(session);
        }
    }
}



