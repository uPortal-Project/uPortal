package org.jasig.portal.layout.dlm.remoting;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.ChannelRegistryManager;
import org.jasig.portal.PortalException;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.w3c.dom.Document;

/**
 * Serializes the Channel listing for the current session user.
 * 
 * @author jennifer.bourey@yale.edu
 * @version $Revision$ $Date$
 */
public class RetrieveChannelListServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Retrieve the user's UserInstance object
        try {
            final WebApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
            final IUserInstanceManager userInstanceManager = (IUserInstanceManager) applicationContext.getBean("userInstanceManager", IUserInstanceManager.class);
            
            IUserInstance userInstance = userInstanceManager.getUserInstance(request);
			Document registry = ChannelRegistryManager.getChannelRegistry(userInstance.getPerson());
			response.setContentType("text/xml; charset=UTF-8");          
            response.getWriter().print("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			response.getWriter().print(org.jasig.portal.utils.XML.serializeNode(registry));
		} catch (PortalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
