package org.jasig.portal.events.aggr.portletexec;

import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.PortletActionExecutionEvent;
import org.jasig.portal.events.PortletEventExecutionEvent;
import org.jasig.portal.events.PortletExecutionEvent;
import org.jasig.portal.events.PortletRenderExecutionEvent;
import org.jasig.portal.events.PortletResourceExecutionEvent;
import org.jasig.portal.events.aggr.BaseAggregationKey;
import org.jasig.portal.events.aggr.portlets.AggregatedPortletMapping;

/**
 * Primary Key for a {@link PortletExecutionAggregation}
 * 
 * @author Eric Dalquist
 */
public interface PortletExecutionAggregationKey extends BaseAggregationKey {
    /**
     * @return The name of the tab
     */
    AggregatedPortletMapping getPortletMapping();
    
    /**
     * @return The type of portlet execution 
     */
    ExecutionType getExecutionType();
    
    /**
     * The type of execution being tracked
     */
    public enum ExecutionType {
        ALL(PortletExecutionEvent.class),
        ACTION(PortletActionExecutionEvent.class),
        EVENT(PortletEventExecutionEvent.class),
        RENDER(PortletRenderExecutionEvent.class),
        RESOURCE(PortletResourceExecutionEvent.class);
        
        private final Class<? extends PortalEvent> supportedType;
        
        private ExecutionType(Class<? extends PortalEvent> supportedType) {
            this.supportedType = supportedType;
        }

        public final boolean supports(Class<? extends PortalEvent> type) {
            return supportedType.isAssignableFrom(type);
        }
    }
}
