/*
 * Created on Aug 25, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.jasig.portal.channels.jsp;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ICacheable;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.properties.PropertiesManager;

/**
 * Implementors of this interface can be used in the Jsp Channel Type to 
 * a create a channel whose content is served up from JSPs in a model II 
 * controller architecture. The controller can act on each incoming request 
 * via the processRuntimeData method and alter its internal model accordingly.
 * There is one controller intance per channel per user so it can safely use
 * instance variables. Any business objects that need to be passed to one of
 * its JSPs via the request object should be placed in the Map returned from
 * that method.
 * 
 * @author Mark Boyd
 */
public interface IController extends ICacheable
{
    public static String JSP_DEPLOY_PATH = PropertiesManager.getProperty(
            Deployer.JSP_DEPLOY_DIR_PROP,"/WEB-INF/classes");
    
    /**
     * Allows the plugged-in controller for the jsp channel to have access to 
     * publish-time parameters and other information about the user. Included 
     * in the set are parameters whose keys end in ".jsp". Additionally, there
     * will be one parameter whose key is "controllerClass". The are used by
     * the jsp channel itself. For more information see the indicated method.
     * Beyond these two restrictions on keys any other parameters can be 
     * specified during publishing that are needed by the controller to perform
     * its work. In addition to the static data the HttpSession is also passed
     * in and can be used to set both session and application scope values to
     * be used in its JSPs.
     * 
     * @see org.jasig.portal.channels.jsp.IController#getJspToRender()
     */
    public void setStaticData(ChannelStaticData csd);

    /** Allows the plugged-in controller for the jsp channel to know about 
     * channel events.
     * 
     * @see org.jasig.portal.IChannel#receiveEvent(org.jasig.portal.PortalEvent)
     */
    public void receiveEvent(PortalEvent ev);

    /**
     * Allows the plugged-in controller for the jsp channel to have access to 
     * request-time parameters passed back to the channel instance and to take
     * action internally. Any objects that should be passed to the jsp to be
     * delegated to should be placed in the returned Map object and they will
     * be added to the request.setAttribute() method using the same keys and
     * values. If no objects are to be passed to the jsp via the request object
     * then this method can return null. If a Map is returned two parameters 
     * will be passed added in to be passed to the 
     * jsp by the containing Jsp Channel type and will override values having 
     * the same key already located within the Map passed back from the 
     * controller. These are "baseActionUrl" and "baseMediaUrl".
     */
    public Map processRuntimeData(ChannelRuntimeData drd, HttpSession s);

    /**
     * Returns a Map of jsp pages that are exposed by the controller.  This
     * map is a name/value pair, where the name is the actual jsp channel name
     * and the value is the request path.
     *
     * Process flow of the channel framework dictates that the map should be
     * available to the controlling channel during the setStaticData method
     * call.
     *
     * An example of the values that would be typically be placed in the map by
     * the controller is:
     * 
     * <pre>
     * jspmap.put("show.UserInfo.jsp","jsps/user.jsp"); 
     * </pre>
     *
     **/
    public Map getJspMap();
    
    /**
     * Returns the id of the jsp that should be delegated to for this request. 
     * 
     * The
     * set of ids that can be returned and the jsps that each id maps to is defined
     * in the getJspMap method. Their value
     * indicates the location of the specific jsp page to be used. The location 
     * returned follows the pattern used by 
     * java.lang.Class.getResource(). If the value begins with a "/" it is left 
     * unchanged; otherwise, the package name of the
     * controller class is prepended to the value after converting "." to "/". In
     * either case the location is expected to be relative to the 
     * "WEB-INF/classes" directory for the webapp and the JSP is then delegated 
     * to using a request dispatcher. If the controller, its JSPs, and any 
     * other resource are  is deployed as a CAR the Channel class extracts
     * and class files and JSPs into WEB-INF/classes in package relative 
     * locations so that the web server can compile the JSPs and so that the
     * JSPs can access the classes. All other resources remain within the CAR
     * and are accesses appropriately by the Channel.     * 
     *
     * An example of the values that would be typically be placed in the map by
     * the controller is:
     * 
     * <pre>
     * map.put("show.UserInfo.jsp","jsps/user.jsp"); 
     * </pre>
     *
     * For the above example if the controller were in the com.sct.myChannel 
     * package and this method returned "show.UserInfo.jsp" then the fully 
     * qualified path specified to acquire the dispatcher would be:
     * 
     * "/WEB-INF/classes/com/sct/myChannel/jsp/user.jsp"
     *  
     * This method should never return a value of null. If the last content 
     * generated by the channel should be used the ICacheable implementations
     * should indicate such behavior and prevent this method from being 
     * called. This method will only be called when new rendering is required
     * as dictated by reponses to the ICacheable implementation methods.
     */
    public String getJspToRender();    
}
