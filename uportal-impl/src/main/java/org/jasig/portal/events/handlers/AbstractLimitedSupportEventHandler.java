package org.jasig.portal.events.handlers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.events.EventHandler;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.security.IPerson;

/**
 * Abstract implemenation that allows for one EventHandler to handle many events
 * configurably without being defined multiple times.
 * <br/>
 * The {@link #supports(PortalEvent)} method checks three criteria for a {@link PortalEvent} to determine if it is
 * supported or not.
 * <li>
 *   <ul>If the user is a guest or not</li>
 *   <ul>If the userName matches a specified set of names</li>
 *   <ul>If the PortalEvents's type matches a set of PortalEvent classes</li>
 * </li>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.6
 */
public abstract class AbstractLimitedSupportEventHandler implements EventHandler {
    /** Protected logging instance. */
	protected Log logger = LogFactory.getLog(this.getClass());
	
	/** The list of supported classes. */
	private boolean supportGuest = true;
	private Set<String> supportedUserNames;
    private Set<Class<? extends PortalEvent>> supportedEvents;
	private boolean explicitMatching = false;
    private boolean requireAll = true;
    

	public final boolean supports(final PortalEvent event) {
	    //Guest support check
	    final IPerson person = event.getPerson();
	    if (this.supportGuest || !person.isGuest()) {
            if (!this.requireAll) {
                return true;
            }
	    }
	    else {
	        if (this.requireAll) {
	            return false;
	        }
	    }
        
        //userName check
        final String userName = person.getUserName();
        if (this.supportedUserNames == null || this.supportedUserNames.contains(userName)) {
            if (!this.requireAll) {
                return true;
            }
        }
        else {
            if (this.requireAll) {
                return false;
            }
        }
        
	    //If no supported events configured return true
	    if (this.supportedEvents == null) {
	        return true;
	    }
	    
        //If explicit matching just do two contains checks
	    final Class<? extends PortalEvent> eventType = event.getClass();
        if (this.explicitMatching) {
            return this.supportedEvents.contains(eventType);
        }
        
        //Check inheritance for includes match if no explicitly matching
        for (final Class<? extends PortalEvent> includedType : this.supportedEvents) {
            if (includedType.isAssignableFrom(eventType)) {
                return true;
            }
        }
        
        return false;
	}


    /**
     * @return the supportedEvents
     */
    public Collection<Class<? extends PortalEvent>> getSupportedEvents() {
        return supportedEvents;
    }
    /**
     * If no <code>supportedEvents</code> {@link Collection} is configured all {@link PortalEvent} sub-classes are
     * supported otherwise matching is done. If <code>explicitMatching</code> is true Class equality matching is used,
     * if it is false {@link Class#isAssignableFrom(Class)} is called on each supported event type passing the tested
     * event as the argument. The property defaults to null (all event types)
     * 
     * @param supportedEvents the supportedEvents to set
     */
    public void setSupportedEvents(Collection<Class<? extends PortalEvent>> supportedEvents) {
        if (supportedEvents == null) {
            this.supportedEvents = null;
        }
        else {
            this.supportedEvents = new HashSet<Class<? extends PortalEvent>>(supportedEvents);
        }
    }

    /**
     * @return the explicitMatching
     */
    public boolean isExplicitMatching() {
        return explicitMatching;
    }
    /**
     * @param explicitMatching the explicitMatching to set
     */
    public void setExplicitMatching(boolean explicitMatching) {
        this.explicitMatching = explicitMatching;
    }


    /**
     * @return the supportedUserNames
     */
    public Collection<String> getSupportedUserNames() {
        return supportedUserNames;
    }
    /**
     * If no <code>supportedUserNames</code> {@link Collection} is configured all user-names are supported otherwise
     * exact String equality matching is done to determine supported userNames. The property defaults to null (all user
     * names)
     * 
     * @param supportedUserNames the supportedUserNames to set
     */
    public void setSupportedUserNames(Collection<String> supportedUserNames) {
        if (supportedUserNames == null) {
            this.supportedUserNames = null;
        }
        else {
            this.supportedUserNames = new HashSet<String>(supportedUserNames);
        }
    }

    /**
     * @return the supportGuest
     */
    public boolean isSupportGuest() {
        return supportGuest;
    }
    /**
     * If the <code>supportGuest</code> property is true {@link PortalEvent}s where {@link IPerson#isGuest()} is true or
     * false will be supported. If the <code>supportGuest</code> property is false only {@link PortalEvent}s where
     * {@link IPerson#isGuest()} is false will be supported. The property defaults to true.
     * 
     * @param supportGuest the supportGuest to set
     */
    public void setSupportGuest(boolean supportGuest) {
        this.supportGuest = supportGuest;
    }

    /**
     * @return the requireAll
     */
    public boolean isRequireAll() {
        return requireAll;
    }
    /**
     * The <code>requireAll</code> can be used to require either any one criteria match for support or all three
     * criteria.
     * 
     * @param requireAll the requireAll to set
     */
    public void setRequireAll(boolean requireAll) {
        this.requireAll = requireAll;
    }
}
