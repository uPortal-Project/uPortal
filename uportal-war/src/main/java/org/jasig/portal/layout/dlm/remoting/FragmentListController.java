/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.layout.dlm.remoting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPathConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.AuthorizationException;
import org.jasig.portal.layout.IUserLayoutStore;
import org.jasig.portal.layout.dlm.ConfigurationLoader;
import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.layout.dlm.FragmentDefinition;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.security.AdminEvaluator;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.jasig.portal.xml.xpath.XPathOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Spring controller that returns a JSON or XML representation of DLM fragments.  For
 * non-admins, this will only display the channels the user is allowed to
 * manage or subscribe to.  Admins have a choice of viewing manageable,
 * subscribable, or all channels by the "type" request parameter.</p>
 * <p>Request parameters:</p>
 * <ul>
 *   <li>xml: if "true", return an XML view of the channels rather than a
 *   JSON view</li>
 *   <li>type: "subscribe", "manage", or "all".  Displays subscribable,
 *   manageable, or all channels (admin only).  Default is subscribable.
 * </ul>
 *
 * @author Drew Wills, drew@unicon.net
 */
@Controller
@RequestMapping("/fragmentList")
public class FragmentListController {
    
    private static final Sort DEFAULT_SORT = Sort.PRECEDENCE;
    private static final String CHANNEL_FNAME_XPATH = "//channel/@fname";
    
    private ConfigurationLoader dlmConfig;
    private IPersonManager personManager;    
    private IUserLayoutStore userLayoutStore;
    private IPortletDefinitionRegistry portletRegistry;
    private XPathOperations xpathOperations;
    private final Log log = LogFactory.getLog(getClass());
    
    @Autowired
    public void setXpathOperations(XPathOperations xpathOperations) {
        this.xpathOperations = xpathOperations;
    }

    @Autowired
    public void setConfigurationLoader(ConfigurationLoader dlmConfig) {
        this.dlmConfig = dlmConfig;
    }

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setUserLayoutStore(IUserLayoutStore userLayoutStore) {
        this.userLayoutStore = userLayoutStore;
    }

    @Autowired
    public void setPortletRegistry(IPortletDefinitionRegistry portletRegistry) {
        this.portletRegistry = portletRegistry;
    }
        
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView listFragments(HttpServletRequest req) throws ServletException {

        // Verify that the user is allowed to use this service
        IPerson user = personManager.getPerson(req);
        if(!AdminEvaluator.isAdmin(user)) {
            throw new AuthorizationException("User " + user.getUserName() + " not an administrator.");
        }

        Map<String,Document> fragmentLayoutMap = null;
        if (userLayoutStore != null) {
            try {
                fragmentLayoutMap = userLayoutStore.getFragmentLayoutCopies();
            } catch (Exception e) {
                String msg = "Failed to access fragment layouts";
                log.error(msg, e);
                throw new ServletException(msg, e);
            }
        }
        
        List<FragmentBean> fragments = new ArrayList<FragmentBean>(); 
        for (FragmentDefinition frag : dlmConfig.getFragments()) {
            
            Document layout = fragmentLayoutMap != null 
                                ? fragmentLayoutMap.get(frag.getOwnerId())
                                : null;

            List<String> portlets = null;
            if (layout != null) {
                portlets = new ArrayList<String>();
                NodeList channelFNames = this.xpathOperations.evaluate(CHANNEL_FNAME_XPATH, layout, XPathConstants.NODESET);
                for (int i=0; i < channelFNames.getLength(); i++) {
                    String fname = channelFNames.item(i).getTextContent();
                    IPortletDefinition pDef = portletRegistry.getPortletDefinitionByFname(fname);
                    portlets.add(pDef.getTitle());
                }
            }
            
            fragments.add(FragmentBean.create(frag, portlets));

        }
        
        // Determine & follow sorting preference...
        Sort sort = DEFAULT_SORT; 
        String sortParam = req.getParameter("sort");
        if (sortParam != null) {
            sort = Sort.valueOf(sortParam);
        }
        Collections.sort(fragments, sort.getComparator());

        return new ModelAndView("jsonView", "fragments", fragments);

    }
    
    /*
     * Nested Types
     */
    
    private enum Sort {
        
        PRECEDENCE {
            public Comparator<FragmentBean> getComparator() {
                return new Comparator<FragmentBean>() {
                    @Override
                    public int compare(FragmentBean frag1, FragmentBean frag2) {
                        // When sorting by precedence, use reverse order to 
                        // match the order in which the portal will sort them 
                        // as tabs.
                        return frag2.getPrecedence().compareTo(frag1.getPrecedence());
                    }
                };
            }
        },
        
        NAME {
            public Comparator<FragmentBean> getComparator() {
                return new Comparator<FragmentBean>() {
                    @Override
                    public int compare(FragmentBean frag1, FragmentBean frag2) {
                        return frag1.getName().compareTo(frag2.getName());
                    }
                };
            }
        };
        
        public abstract Comparator<FragmentBean> getComparator();
        
    }
    
    /**
     * Very simple class representing a DLM fragment.
     */
    public static final class FragmentBean {
        
        // Instance Members.
        private final String name;
        private final String ownerId;
        private final Double precedence;
        private final List<String> audience;
        private final List<String> portlets;
        
        public static FragmentBean create(FragmentDefinition frag, List<String> portlets) {
            
            // Assertions.
            if (frag == null) {
                String msg = "Argument 'frag' cannot be null";
                throw new IllegalArgumentException(msg);
            }
            
            // NB:  'portlets' may be null
            
            return new FragmentBean(frag.getName(), frag.getOwnerId(), 
                            frag.getPrecedence(), frag.getEvaluators(),
                            portlets);
            
        }
        
        public String getName() {
            return name;
        }
        
        public String getOwnerId() {
            return ownerId;
        }
        
        public Double getPrecedence() {
            return precedence;
        }
        
        public List<String> getAudience() {
            return audience;
        }
        
        public List<String> getPortlets() {
            return portlets;
        }
 
        private FragmentBean(String name, String ownerId, Double precedence, List<Evaluator> audience, List<String> portlets) {            

            this.name = name;
            this.ownerId = ownerId;
            this.precedence = precedence;
            
            List<String> list = new ArrayList<String>();
            for (Evaluator ev : audience) {
                list.add(ev.getSummary());
            }
            this.audience = Collections.unmodifiableList(list);
            if (portlets != null) {
                this.portlets = Collections.unmodifiableList(portlets);
            } else {
                this.portlets = Collections.emptyList();
            }

        }
        
    }

}
