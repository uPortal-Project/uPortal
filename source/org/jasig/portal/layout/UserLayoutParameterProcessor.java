package org.jasig.portal.layout;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelManager;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.UserPreferences;
import org.jasig.portal.security.IPerson;

/**
 * This helper class processes HttpServletRequests for parameters relating to
 * user layout actions, propogating appropriate events to the user's layout
 * manager, preferences, and channel manager.
 * 
 * This class results from factoring the processUserLayoutParameters method out
 * of UserInstance in an effort to make UserInstance smaller and more literate.
 */
public class UserLayoutParameterProcessor {

    protected final Log log = LogFactory.getLog(getClass());

    private final IUserLayoutManager userLayoutManager;

    private final UserPreferences userPrefs;

    public UserLayoutParameterProcessor(IUserLayoutManager ulm,
            UserPreferences userPrefs) {
        
        if (ulm == null) {
            throw new IllegalArgumentException("Cannot construct a UserLayoutParameterProcessor with a null user layout manager");
        }
        
        if (userPrefs ==  null) {
            throw new IllegalArgumentException("Cannot construct a UserLayoutParameterProcessor with a null user preferences");
        }
        
        this.userLayoutManager = ulm;
        this.userPrefs = userPrefs;
    }

    /**
     * Process layout action events. 
     * Events are described by the following
     * request params: uP_help_target uP_about_target uP_edit_target
     * uP_remove_target uP_detach_target
     * 
     * @param req
     *            a <code>HttpServletRequest</code> value
     * @param channelManager
     *            a <code>ChannelManager</code> value
     * @exception PortalException
     *                if an error occurs
     */
    public synchronized void processUserLayoutParameters(
            HttpServletRequest req, HttpServletResponse res, ChannelManager channelManager,
            IPerson person) throws PortalException {

        String[] values;
        if ((values = req.getParameterValues("uP_help_target")) != null) {
            for (int i = 0; i < values.length; i++) {
                channelManager.passPortalEvent(req, res, values[i],
                        PortalEvent.HELP_BUTTON);
            }
        }
        if ((values = req.getParameterValues("uP_about_target")) != null) {
            for (int i = 0; i < values.length; i++) {
                channelManager.passPortalEvent(req, res, values[i],
                        PortalEvent.ABOUT_BUTTON);
            }
        }
        if ((values = req.getParameterValues("uP_edit_target")) != null) {
            for (int i = 0; i < values.length; i++) {
                channelManager.passPortalEvent(req, res, values[i],
                        PortalEvent.EDIT_BUTTON);
            }
        }
        if ((values = req.getParameterValues("uP_detach_target")) != null) {
            channelManager
                    .passPortalEvent(req, res, values[0], PortalEvent.DETACH_BUTTON);
        }

        // Propagate minimize/maximize events to the channels
        String[] tcattrs = req.getParameterValues("uP_tcattr");
        if (tcattrs != null) {
            for (int i = 0; i < tcattrs.length; i++) {
                String aName = tcattrs[i];
                if ("minimized".equals(aName)) {
                    String[] aNode = req.getParameterValues(aName
                            + "_channelId");
                    if (aNode != null && aNode.length > 0) {
                        for (int j = 0; j < aNode.length; j++) {
                            String aValue = req.getParameter(aName + "_"
                                    + aNode[j] + "_value");

                            PortalEvent e = null;

                            if ("true".equals(aValue)) {
                                e = PortalEvent.MINIMIZE;
                            } else {
                                e = PortalEvent.MAXIMIZE;
                            }

                            channelManager.passPortalEvent(req, res, aNode[j], e);

                            if (log.isDebugEnabled())
                                log.debug("Sending window state event to '"
                                        + aName + "' of '" + aNode[j]
                                        + "' to '" + aValue + "'.");
                        }
                    }
                }
            }
        }

        userLayoutManager.processLayoutParameters(person, userPrefs, req);

    }

}
