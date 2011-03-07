package org.jasig.portal.rest;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.PortletURLProvider.TYPE;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionParameter;
import org.jasig.portal.rest.layout.LayoutPortlet;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletPortalUrl;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

@Controller
public class LayoutRESTController {
    
    protected final Log log = LogFactory.getLog(getClass());
    
    IUserLayoutStore userLayoutStore;
    
    @Autowired(required = true)
    public void setUserLayoutStore(IUserLayoutStore userLayoutStore) {
        this.userLayoutStore = userLayoutStore;
    }
    
    IPersonManager personManager;
    
    @Autowired(required = true)
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }
    
    private IPortalUrlProvider urlProvider;
    
    @Autowired(required = true)
    public void setPortalUrlProvider(IPortalUrlProvider urlProvider) {
        this.urlProvider = urlProvider;
    }
    
    private IUserInstanceManager userInstanceManager;
    
    @Autowired(required = true)
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }
    
    private IPortletDefinitionDao portletDao;
    
    @Autowired(required = true)
    public void setPortletDao(IPortletDefinitionDao portletDao) {
        this.portletDao = portletDao;
    }
    
    @RequestMapping(value="/layoutDoc", method = RequestMethod.GET)
    public ModelAndView getRESTController(HttpServletRequest request, HttpServletResponse response) {
        final IPerson person = personManager.getPerson(request);
        List<LayoutPortlet> portlets = new ArrayList<LayoutPortlet>();
        
        
        try {
            
            final IUserInstance ui = userInstanceManager.getUserInstance(request);

            final IUserPreferencesManager upm = ui.getPreferencesManager();

            final UserProfile profile = upm.getCurrentProfile();
            Document document = userLayoutStore.getUserLayout(person, profile);
            
            NodeList portletNodes = document.getElementsByTagName("channel");
            for (int i = 0; i < portletNodes.getLength(); i++) {
                try {
                    
                    LayoutPortlet portlet = new LayoutPortlet();
                    NamedNodeMap attributes = portletNodes.item(i).getAttributes();
                    portlet.setTitle(attributes.getNamedItem("title").getNodeValue());
                    portlet.setDescription(attributes.getNamedItem("description").getNodeValue());
                    portlet.setNodeId(attributes.getNamedItem("ID").getNodeValue());
                    
                    IPortletDefinition def = portletDao.getPortletDefinitionByFname(attributes.getNamedItem("fname").getNodeValue());
                    IPortletDefinitionParameter iconParam = def.getParameter("iconUrl");
                    if (iconParam != null) {
                        portlet.setIconUrl(iconParam.getValue());                        
                    }
                    
                    // get the maximized URL for this portlet
                    IPortletPortalUrl url = urlProvider.getPortletUrlByNodeId(TYPE.RENDER, request, attributes.getNamedItem("ID").getNodeValue());
                    url.setWindowState(WindowState.MAXIMIZED);
                    portlet.setUrl(url.getUrlString());
                    portlets.add(portlet);

                } catch (Exception e) {
                    log.warn("Exception construction JSON representation of mobile portlet", e);
                }
            }
            
            ModelAndView mv = new ModelAndView();
            mv.addObject("layout", portlets);
            mv.setViewName("json");
            return mv;
        } catch (Exception e) {
            log.error("Error retrieving user layout document", e);
        }
        
        return null;
    }

}
