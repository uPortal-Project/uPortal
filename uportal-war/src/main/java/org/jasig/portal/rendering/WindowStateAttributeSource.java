package org.jasig.portal.rendering;

import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetParameterDescriptor;
import org.jasig.portal.portlet.PortletUtils;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.springframework.beans.factory.annotation.Required;

import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;


/**
 * Custom attribute source that can override the window state.
 *
 * This is intended for the JSON layout theme where it puts all the portlets
 * in minimized mode to reduce render cost.   It previously tried to set the
 * windowState on the PortletWindow object and then used a flag to disable
 * writing the window state to the DB.  Because the PortletWindow is
 * cached in the session, it would still break the layout for
 * each subsequent render in that session though.  New approach is to not
 * modify the PortletWindow and just customize the channel XML tag attributes
 * without ever updating the PortletWindow or persisting the windowState
 * change.
 *
 * See UP-4364
 *
 * @since 4.2
 *
 * @author Josh Helmer, jhelmer.unicon.net
 */
public class WindowStateAttributeSource extends PortletWindowAttributeSource {
    private WindowState windowState;


    public void setWindowState(final WindowState windowState) {
        this.windowState = windowState;
    }


    @Override
    protected WindowState getWindowState(HttpServletRequest request, IPortletWindow window) {
        if (windowState != null) {
            return windowState;
        }

        return super.getWindowState(request, window);
    }
}
