/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.permissionsmanager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

import org.jasig.portal.ChannelCacheKey;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ICacheable;
import org.jasig.portal.IChannel;
import org.jasig.portal.channels.groupsmanager.CGroupsManagerServantFactory;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPermissionManager;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Element;

/**
 * CPermissionsManager allows graphical administration of permissions for all owners
 * that have a representative implementation of IPermissible recorded in the table
 * UPC_PERM_MGR
 *
 * @author Alex Vigdor
 * @version $Revision$
 */
public class CPermissionsManager
        implements IChannel, ICacheable {
    private static final Log log = LogFactory.getLog(CPermissionsManager.class);
    
    protected PermissionsSessionData session;
    protected final String sslLocation = "CPermissionsManager.ssl";

    /**
     * put your documentation comment here
     */
    public CPermissionsManager () {
    }

    /**
     * put your documentation comment here
     * @param rD
     */
    public void setRuntimeData (org.jasig.portal.ChannelRuntimeData rD) {
        session.startRD = Calendar.getInstance().getTime().getTime();
        session.runtimeData = rD;
        log.debug("PermissionsManager - setting runtime data");
        // test if servant exists and has finished
        if (session.servant != null){
          try {
            ((IChannel) session.servant).setRuntimeData(rD);
            if (session.servant.isFinished()) {
              getGroupServantResults(session);
            }
          }
          catch (Exception e){
            log.error(e, e);
          }
        }

        String prmCommand = session.runtimeData.getParameter("prmCommand");
        if (prmCommand != null) {
            IPermissionCommand pc = CommandFactory.get(prmCommand);
            if (pc != null) {
              try{
                pc.execute(session);
              }
              catch(Exception e){
                log.error("Error executing command [" + prmCommand + "]", e);
                session.runtimeData.setParameter("commandResponse","Error executing command "+ prmCommand +": "+e.getMessage());
              }
            }
        }


        if ((!session.gotOwners)) {
            session.view="Select Owners";
        }
        if ((session.gotOwners) && (!session.gotActivities)) {
            PermissionsXML.autoSelectSingleChoice(session,"activity");
            session.view="Select Activities";
        }
        if (session.gotActivities && !session.gotTargets) {
            PermissionsXML.autoSelectSingleChoice(session,"target");
            session.view="Select Targets";
        }
        if (session.gotTargets && (session.principals == null)) {
            log.debug("PermissionsManager - Checking Servant");
            prepServant(session);
        }
        if (session.principals != null){
            if (session.staticData.getParameter("prmView") != null) {
              session.view=session.staticData.getParameter("prmView");
              session.staticData.remove("prmView");
            }
            else if (session.runtimeData.getParameter("prmView") != null) {
              session.view=session.runtimeData.getParameter("prmView");
            }
            else if (!session.view.startsWith("Assign")){
              session.view="Assign By Principal";
            }
        }
    }

    protected void prepServant(PermissionsSessionData session){
      try {
          if (session.servant == null) {

              // get a different version of the servant depnding on whether Owners have been retrieved
              if (!session.gotOwners){
                log.debug("PermissionsManager - creating new basic Servant");
                session.servant =
                  CGroupsManagerServantFactory.getGroupsServantforSelection(
                    session.staticData,"Select principals you would like to assign permissions to",
                    GroupService.EVERYONE);
              }
              else {
                  // build an array of groupmembers for pre-selection
                  log.debug("PermissionsManager - creating new pre-selecting Servant");
                  ArrayList gmembers = new ArrayList();
                  Element[] owners = PermissionsXML.getSelectedOwners(session);
                  for (int j= 0; j < owners.length ; j++){
                    if (log.isDebugEnabled())
                      log.debug("analyzing owner "+owners[j].getAttribute("name"));
                    String ownerKey = owners[j].getAttribute("token");
                    IPermissionManager pm = AuthorizationService.instance().newPermissionManager(ownerKey);
                    String[] acts = null;
                    if (session.gotActivities){
                      acts = PermissionsXML.getSelectedActivities(session,owners[j]);
                    }
                    String[] tgts = null;
                    if (session.gotTargets){
                       tgts = PermissionsXML.getSelectedTargets(session,owners[j]);
                    }

                    if (acts != null){
                      for (int a = 0; a< acts.length ; a++){
                        if (tgts != null){
                           for (int t=0; t< tgts.length; t++){
                              populateMembers(gmembers,pm.getAuthorizedPrincipals(acts[a],tgts[t]));
                           }
                        }
                        else {
                            populateMembers(gmembers,pm.getAuthorizedPrincipals(acts[a], null));
                        }
                      }
                    }
                    else {
                      if (tgts !=null){
                         for (int t=0; t< tgts.length; t++){
                            populateMembers(gmembers,pm.getAuthorizedPrincipals(null,tgts[t]));
                         }
                      }
                      else {
                          populateMembers(gmembers,pm.getAuthorizedPrincipals(null, null));
                      }
                    }

                    // use pre-populated list to get servant
                    session.servant = CGroupsManagerServantFactory.getGroupsServantforSelection(
                      session.staticData,"You may view principals with existing permissions on the items you have selected by clicking \"Done\", or use the select and deselect buttons to add or remove principals for whom you would like to view/assign permissions",
                      GroupService.EVERYONE,true,true,
                      (IGroupMember[])gmembers.toArray(new IGroupMember[0]));
                  }
              }
              ChannelRuntimeData servantRD = (ChannelRuntimeData)session.runtimeData.clone();
              Enumeration srd = servantRD.keys();
              // clear out runtimeData in case of chained Group servant creation
              while (srd.hasMoreElements()) {
                  servantRD.remove(srd.nextElement());
              }
              ((IChannel)session.servant).setRuntimeData(servantRD);
              session.view="Select Principals";
          }
          else {
              log.debug("PermissionsManager - using existing Servant");
          }



      } catch (Exception e) {
          log.error(e, e);
      }
    }

    protected void populateMembers(ArrayList gmembers, IAuthorizationPrincipal[] aps){
      if (log.isDebugEnabled())
          log.debug("PermissionsManager.PopulateMembers(): checking principal set of size"+aps.length);
      for (int a = 0; a< aps.length ; a++){
        try {
          IGroupMember agm = AuthorizationService.instance().getGroupMember(aps[a]);
          if (log.isDebugEnabled())
              log.debug("PermissionsManager.PopulateMembers(): checking whether "+
                      agm.getType()+"."+agm.getKey()+" needs to be added");
          if (!gmembers.contains(agm)){
            gmembers.add(agm);
          }
        }
        catch(Exception e){
          log.error(e, e);
        }
      }
    }

    protected void getGroupServantResults(PermissionsSessionData session){
      try {
        log.debug("PermissionsManager - Getting servant results");
        Object[] results = session.servant.getResults();
        if (results != null && results.length > 0) {
            IAuthorizationPrincipal[] iap = new IAuthorizationPrincipal[results.length];
            for (int i = 0; i< results.length ; i++){
              IGroupMember gm = (IGroupMember) results[i];
              iap[i] = AuthorizationService.instance().newPrincipal(gm);
            }
            session.principals=iap;
            log.debug("PermissionsManager - Getting rid of Servant");
            session.servant=null;
            PermissionsXML.populatePrincipals(session);
        }
        else {
            log.debug("PermissionsManager - Group Servant yielded no results, assuming abort and running Cancel");
            IPermissionCommand cmd = CommandFactory.get("Cancel");
            cmd.execute(session);
            //session.isFinished=true;
            //session.runtimeData.setParameter("commandResponse", "You must select at least once principal to continue");
        }
      }
      catch (Exception e) {
        log.error(e, e);
      }
    }

    /**
     * put your documentation comment here
     * @param portalEvent
     */
    public void receiveEvent (org.jasig.portal.PortalEvent portalEvent) {}

    /**
     * put your documentation comment here
     * @return a new <code>ChannelRuntimeProperties</code>
     */
    public org.jasig.portal.ChannelRuntimeProperties getRuntimeProperties () {
        return  new ChannelRuntimeProperties();
    }

    /**
     * put your documentation comment here
     * @param out
     */
    public void renderXML (org.xml.sax.ContentHandler out) {
        try {
            long time1 = Calendar.getInstance().getTime().getTime();
            if (session.view.equals("Select Principals") &&
                    session.isAuthorized) {
                try {
                    log.debug("PermissionsManager - Calling servant renderXML");
                    //IChannel servant = (IChannel)staticData.get("prmServant");
                    ((IChannel)session.servant).renderXML(out);
                } catch (Exception e) {
                    log.error( "CPermissionsManager: failed to use servant"
                            + e);
                }
            }
            if (!session.view.equals("Select Principals")
                    || !session.isAuthorized) {
                long time2 = Calendar.getInstance().getTime().getTime();
                XSLT xslt = XSLT.getTransformer(this, session.runtimeData.getLocales());
                xslt.setXML(PermissionsXML.getViewDoc(session));
                xslt.setTarget(out);
                xslt.setStylesheetParameter("baseActionURL", session.runtimeData.getBaseActionURL());
                xslt.setStylesheetParameter("prmView", session.view);
                if (session.runtimeData.get("commandResponse") != null) {
                    xslt.setStylesheetParameter("commandResponse", session.runtimeData.getParameter("commandResponse"));
                }
                xslt.setXSL(sslLocation, "CPermissions", session.runtimeData.getBrowserInfo());
                transform(xslt);
                if (log.isDebugEnabled()) {
                    long time3 = Calendar.getInstance().getTime().getTime();
                    log.debug("CPermissionsManager timer: "
                            + String.valueOf((time3 - time1)) + " ms total, xsl took "
                            + String.valueOf((time3 - time2)) + " ms for view " + session.view);
                    log.debug("CPermissionsManager timer: "
                            + String.valueOf((time3 - session.startRD)) + " since start RD");
                }
               
            }
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    /**
     * put your documentation comment here
     * @param xslt
     */
    protected void transform (XSLT xslt) {
        try {
            if (session.isAuthorized) {
                xslt.setStylesheetParameter("isAdminUser", "true");
            }
            xslt.transform();
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    /**
     * put your documentation comment here
     * @param sD
     */
    public void setStaticData (org.jasig.portal.ChannelStaticData sD) {
        this.session = new PermissionsSessionData();
        session.staticData = sD;
        try {
            IEntityGroup admin = GroupService.getDistinguishedGroup(GroupService.PORTAL_ADMINISTRATORS);
            IGroupMember me = AuthorizationService.instance().getGroupMember(session.staticData.getAuthorizationPrincipal());
            if (admin.deepContains(me)) {
                session.isAuthorized = true;
            }
        } catch (Exception e) {
            log.error(e, e);
        }
        session.isFinished=false;

    }


    /**
     * put your documentation comment here
     * @return a <code>ChannelCacheKey</code> used to lookup Channels from the cache
     */
    public ChannelCacheKey generateKey () {
        ChannelCacheKey cck;
        if (session.servant == null) {
            cck = new ChannelCacheKey();
            cck.setKey(session.staticData.getChannelPublishId()+"-"+session.staticData.getChannelSubscribeId() + "-" + String.valueOf(session.staticData.getPerson().getID()));
            cck.setKeyValidity(session.runtimeData.getParameter("prmView"));
            log.debug("CPermissionsManager.generateKey() : set validity to "
                    + session.runtimeData.getParameter("prmView"));
        }
        else {
            cck = ((ICacheable)session.servant).generateKey();
        }
        return  cck;
    }

    /**
     * put your documentation comment here
     * 
     * @param validity
     * @return <code>true</code> if cache is valid; <code>false</code> otherwise
     */
    public boolean isCacheValid (Object validity) {
        boolean valid = false;
        if (session.servant == null) {
            if (validity != null) {
                if (validity.equals(session.runtimeData.getParameter("prmView")) && session.runtimeData.get("commandResponse")
                        == null) {
                    valid = true;
                }
            }
            if (log.isDebugEnabled()) {
                long time3 = Calendar.getInstance().getTime().getTime();
                log.debug("CPermissionsManager.isCacheValid() time since setRD: "
                        + String.valueOf((time3 - session.startRD)) + ", valid=" + valid);
            }

        }
        else {
            valid = ((ICacheable)session.servant).isCacheValid(validity);
        }
        return  valid;
    }
}



