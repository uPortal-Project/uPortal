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

package org.jasig.portal.layout.dlm.providers;

import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jasig.portal.fragment.subscribe.IUserFragmentSubscription;
import org.jasig.portal.fragment.subscribe.dao.IUserFragmentSubscriptionDao;
import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.layout.dlm.EvaluatorFactory;
import org.jasig.portal.layout.dlm.FragmentDefinition;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.spring.locator.UserFragmentSubscriptionDaoLocator;

/**
 * SubscribedTabEvaluator is a DLM evaluator that determines if a given fragment
 * is in the specified IPerson's fragment subscription list.  This evaluator
 * also performs a real-time permissions lookup to ensure that the user is
 * still permissioned to be include the fragment. 
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SubscribedTabEvaluator extends Evaluator {
    
    @Column(name = "OWNER_ID")
    private final String ownerId;

    @SuppressWarnings("unused")
    private SubscribedTabEvaluator() {
        this.ownerId = null;
    }
    
    /**
     * Construct a new SubscribedTabEvaluator for the specified fragment owner.
     * 
     * @param ownerId
     */
    public SubscribedTabEvaluator(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public Class<? extends EvaluatorFactory> getFactoryClass() {
        return SubscribedTabEvaluatorFactory.class;
    }

    @Override
    public boolean isApplicable(IPerson person) {
        
        IUserFragmentSubscriptionDao userFragmentInfoDao = UserFragmentSubscriptionDaoLocator.getUserIdentityStore();

        // get the list of current fragment subscriptions for this person
        List<IUserFragmentSubscription> fragments = userFragmentInfoDao
                .getUserFragmentInfo(person);
        
        // iterate through the subscription list to determine if the 
        // specified person is actively subscribed to the fragment associated
        // with this evaluator instance
        for (IUserFragmentSubscription fragment : fragments) {
            if (fragment.isActive() && fragment.getFragmentOwner().equals(ownerId)) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public void toElement(Element parent) {
        // Assertions.
        if (parent == null) {
            String msg = "Argument 'parent' cannot be null.";
            throw new IllegalArgumentException(msg);
        }

        Element rslt = null;
        QName q = new QName("audience", FragmentDefinition.NAMESPACE);
        rslt = DocumentHelper.createElement(q);
        rslt.addAttribute("evaluatorFactory", this.getFactoryClass().getName());
        parent.add(rslt);
    }

    @Override
    public String getSummary() {
        return "(OPT-IN USERS)";
    }

}
