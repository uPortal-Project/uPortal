package org.jasig.portal.layout.dlm.providers;

import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
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
public class SubscribedTabEvaluator extends Evaluator {
    
    private final String ownerId;

    private final IUserFragmentSubscriptionDao userFragmentInfoDao;
    
    /**
     * Construct a new SubscribedTabEvaluator for the specified fragment owner.
     * 
     * @param ownerId
     */
    public SubscribedTabEvaluator(String ownerId) {
        this.userFragmentInfoDao = UserFragmentSubscriptionDaoLocator.getUserIdentityStore();
        this.ownerId = ownerId;
    }

    @Override
    public Class<? extends EvaluatorFactory> getFactoryClass() {
        return SubscribedTabEvaluatorFactory.class;
    }

    @Override
    public boolean isApplicable(IPerson person) {
        
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

}
