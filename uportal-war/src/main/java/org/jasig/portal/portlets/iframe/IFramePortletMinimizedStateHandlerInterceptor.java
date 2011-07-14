package org.jasig.portal.portlets.iframe;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.springframework.web.portlet.handler.HandlerInterceptorAdapter;

/**
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class IFramePortletMinimizedStateHandlerInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandleRender(RenderRequest request, RenderResponse response, Object handler) throws Exception {
        if (WindowState.MINIMIZED.equals(request.getWindowState())) {

            String url = request.getPreferences().getValue("src", null);
            if (url == null) {
                url = request.getPreferences().getValue("url", null);
            }
            response.setProperty("externalPortletLink", url);

            return false;
        }
        
        return true;
    }
    
}
