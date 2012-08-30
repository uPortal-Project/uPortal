package org.jasig.portal.events;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker;
import org.jasig.portal.url.IPortalRequestInfo;

/**
 * Publishes events related to portlet execution
 * 
 * @author Eric Dalquist
 */
public interface IPortletExecutionEventFactory {
    
    //********** Portlet Hung Events **********//
    
    public void publishPortletHungEvent(HttpServletRequest request, Object source,
            IPortletExecutionWorker<?> worker);
    
    public void publishPortletHungCompleteEvent(Object source,
            IPortletExecutionWorker<?> worker);
    
    //********** Portlet Execution Events **********//
    
    public void publishPortletActionExecutionEvent(HttpServletRequest request, Object source, 
            String fname, long executionTime, Map<String, List<String>> parameters);
    public void publishPortletEventExecutionEvent(HttpServletRequest request, Object source, 
            String fname, long executionTime, Map<String, List<String>> parameters, QName eventName);
    public void publishPortletRenderHeaderExecutionEvent(HttpServletRequest request, Object source, 
            String fname, long executionTime, Map<String, List<String>> parameters, boolean targeted, boolean usedPortalCache);
    public void publishPortletRenderExecutionEvent(HttpServletRequest request, Object source, 
            String fname, long executionTime, Map<String, List<String>> parameters, boolean targeted, boolean usedPortalCache);
    public void publishPortletResourceExecutionEvent(HttpServletRequest request, Object source, 
            String fname, long executionTime, Map<String, List<String>> parameters, String resourceId, boolean usedBrowserCache, boolean usedPortalCache);
    
    //********** Portal Rendering Pipeline Events **********//
    
    public void publishPortalRenderEvent(HttpServletRequest request, Object source, String requestPathInfo, long executionTime,
            IPortalRequestInfo portalRequestInfo);
}