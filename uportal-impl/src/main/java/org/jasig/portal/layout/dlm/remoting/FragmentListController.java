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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.AuthorizationException;
import org.jasig.portal.layout.dlm.ConfigurationLoader;
import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.layout.dlm.FragmentDefinition;
import org.jasig.portal.security.AdminEvaluator;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPersonManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

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
public class FragmentListController extends AbstractController {
    
    private static final Sort DEFAULT_SORT = Sort.PRECEDENCE; 
    
    private ConfigurationLoader dlmConfig;
    private IPersonManager personManager;
    
    public void setConfigurationLoader(ConfigurationLoader dlmConfig) {
        this.dlmConfig = dlmConfig;
    }

    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest req, HttpServletResponse res) {

        // Verify that the user is allowed to use this service
        IPerson user = personManager.getPerson(req);
        if(!AdminEvaluator.isAdmin(user)) {
            throw new AuthorizationException("User " + user.getUserName() + " not an administrator.");
        }

        List<FragmentBean> fragments = new ArrayList<FragmentBean>(); 
        for (FragmentDefinition frag : dlmConfig.getFragments()) {
            fragments.add(FragmentBean.fromFragmentDefinition(frag));
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
        
        public static FragmentBean fromFragmentDefinition(FragmentDefinition frag) {
            
            // Assertions.
            if (frag == null) {
                String msg = "Argument 'frag' cannot be null";
                throw new IllegalArgumentException(msg);
            }
            
            return new FragmentBean(frag.getName(), frag.getOwnerId(), 
                            frag.getPrecedence(), frag.getAudience());
            
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
        
        private FragmentBean(String name, String ownerId, Double precedence, List<Evaluator> audience) {

            this.name = name;
            this.ownerId = ownerId;
            this.precedence = precedence;
            
            List<String> list = new ArrayList<String>();
            for (Evaluator ev : audience) {
                list.add(ev.getSummary());
            }
            this.audience = Collections.unmodifiableList(list);

        }
        
    }

}
