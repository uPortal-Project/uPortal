package org.jasig.portal.events.tincan;

import java.util.Locale;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.jasig.portal.events.LoginEvent;
import org.jasig.portal.events.LogoutEvent;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.PortalRenderEvent;
import org.jasig.portal.events.PortletActionExecutionEvent;
import org.jasig.portal.events.PortletExecutionEvent;
import org.jasig.portal.events.PortletRenderExecutionEvent;
import org.jasig.portal.events.PortletResourceExecutionEvent;
import org.jasig.portal.events.aggr.BasePortalEventAggregator;
import org.jasig.portal.events.aggr.SimplePortalEventAggregator;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletLookupDao;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletMapping;
import org.jasig.portal.events.aggr.session.EventSession;
import org.jasig.portal.events.aggr.tabs.AggregatedTabLookupDao;
import org.jasig.portal.events.aggr.tabs.AggregatedTabMapping;
import org.jasig.portal.events.tincan.om.LocalizedString;
import org.jasig.portal.events.tincan.om.LrsActor;
import org.jasig.portal.events.tincan.om.LrsObject;
import org.jasig.portal.events.tincan.om.LrsStatement;
import org.jasig.portal.events.tincan.om.LrsVerb;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class TinCanPortalEventAggregator extends
        BasePortalEventAggregator<PortalEvent> implements
        SimplePortalEventAggregator<PortalEvent> {
    
    private IPersonAttributeDao personAttributeDao;
    private TinCanEventSender tinCanEventSender;
    private AggregatedTabLookupDao aggregatedTabLookupDao;
    private AggregatedPortletLookupDao aggregatedPortletLookupDao;
    private Ehcache lrsActorCache;
    
    private String emailAttributeName = "mail";
    private String displayNameAttributeName = "displayName";
    
    @Autowired
    public void setAggregatedTabLookupDao(AggregatedTabLookupDao aggregatedTabLookupDao) {
        this.aggregatedTabLookupDao = aggregatedTabLookupDao;
    }

    @Autowired
    public void setAggregatedPortletLookupDao(AggregatedPortletLookupDao aggregatedPortletLookupDao) {
        this.aggregatedPortletLookupDao = aggregatedPortletLookupDao;
    }

    @Autowired
    public void setTinCanEventSender(TinCanEventSender tinCanEventSender) {
        this.tinCanEventSender = tinCanEventSender;
    }

    @Autowired
    public void setPersonAttributeDao(IPersonAttributeDao personAttributeDao) {
        this.personAttributeDao = personAttributeDao;
    }
    
    @Autowired
    @Qualifier("org.jasig.portal.events.tincan.LrsActorCache")
    public void setLrsActorCache(Ehcache lrsActorCache) {
        this.lrsActorCache = lrsActorCache;
    }

    @Value("${org.jasig.portal.events.tincan.TinCanPortalEventAggregator.emailAttributeName:mail}")
    public void setEmailAttributeName(String emailAttributeName) {
        this.emailAttributeName = emailAttributeName;
    }

    @Value("${org.jasig.portal.events.tincan.TinCanPortalEventAggregator.displayNameAttributeName:displayName}")
    public void setDisplayNameAttributeName(String displayNameAttributeName) {
        this.displayNameAttributeName = displayNameAttributeName;
    }


    @Override
    public boolean supports(Class<? extends PortalEvent> type) {
        return true;
    }

    @Override
    public void aggregateEvent(PortalEvent e, EventSession eventSession) {
        final LrsStatement lrsStatement = getLrsStatement(e);
        if (lrsStatement == null) {
            return;
        }
        
        tinCanEventSender.sendEvent(lrsStatement);
    }
    
    protected LrsStatement getLrsStatement(PortalEvent e) {
        final LrsVerb lrsVerb = getLrsVerb(e);
        if (lrsVerb == null) {
            return null;
        }
        
        final LrsActor lrsActor = getLrsActor(e);
        
        final LrsObject lrsObject = getLrsObject(e);
        
        return new LrsStatement(lrsActor, lrsVerb, lrsObject);
    }
    
    protected LrsObject getLrsObject(PortalEvent e) {
        final UrnBuilder objectIdBuilder = new UrnBuilder("UTF-8", "tincan", "uportal", "activities");
        
        final Builder<String, LocalizedString> definitionBuilder = ImmutableMap.builder();
        
        if (e instanceof LoginEvent) {
            objectIdBuilder.add("Login");
        }
        else if (e instanceof LogoutEvent) {
            objectIdBuilder.add("Logout");
        }
        else if (e instanceof PortalRenderEvent) {
            final String targetedLayoutNodeId = ((PortalRenderEvent) e).getTargetedLayoutNodeId();
            final AggregatedTabMapping aggregatedTabMapping = aggregatedTabLookupDao.getMappedTabForLayoutId(targetedLayoutNodeId);
            
            objectIdBuilder.add("tab", aggregatedTabMapping.getFragmentName());
            
            definitionBuilder.put("name", new LocalizedString(Locale.US, aggregatedTabMapping.getDisplayString()));
        }
        else if (e instanceof PortletExecutionEvent) {
            final String fname = ((PortletExecutionEvent) e).getFname();
            final AggregatedPortletMapping mappedPortletForFname = this.aggregatedPortletLookupDao.getMappedPortletForFname(fname);
            
            objectIdBuilder.add("portlet", fname);
            
            definitionBuilder.put("name", new LocalizedString(Locale.US, mappedPortletForFname.getName()));
        }
        
        return new LrsObject(objectIdBuilder.getUri(), "Activity", definitionBuilder.build());
    }
    
    protected LrsVerb getLrsVerb(PortalEvent e) {
        if (e instanceof LoginEvent) {
            return LrsVerb.INITIALIZED;
        }
        else if (e instanceof LogoutEvent) {
            return LrsVerb.EXITED;
        }
        else if (e instanceof PortalRenderEvent) {
            return LrsVerb.EXPERIENCED;
        }
        else if (e instanceof PortletActionExecutionEvent) {
            return LrsVerb.INTERACTED;
        }
        else if (e instanceof PortletRenderExecutionEvent) {
            return LrsVerb.EXPERIENCED;
        }
        else if (e instanceof PortletResourceExecutionEvent) {
            return LrsVerb.INTERACTED;
        }
        
        return null;
    }
    
    protected LrsActor getLrsActor(PortalEvent e) {
        final String userName = e.getUserName();
        
        Element element = this.lrsActorCache.get(userName);
        if (element != null) {
            return (LrsActor)element.getObjectValue();
        }

        final String email;
        final String name;
        final IPersonAttributes person = personAttributeDao.getPerson(userName);
        if (person == null) {
            email = userName;
            name = userName + "@example.com";
        }
        else {
            email = getEmail(person);
            name = getName(person);
        }
        
        final LrsActor lrsActor = new LrsActor("mailto:" + email, name);
        this.lrsActorCache.put(new Element(userName, lrsActor));
        return lrsActor;
    }
    
    protected String getEmail(IPersonAttributes person) {
        final Object email = person.getAttributeValue(emailAttributeName);
        if (email != null) {
            return email.toString();
        }
        
        return person.getName() + "@example.com";
    }
    
    protected String getName(IPersonAttributes person) {
        final Object displayName = person.getAttributeValue(displayNameAttributeName);
        if (displayName != null) {
            return displayName.toString();
        }
        
        return person.getName();
    }
}
