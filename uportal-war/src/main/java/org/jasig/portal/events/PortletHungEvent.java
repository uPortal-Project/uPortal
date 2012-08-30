package org.jasig.portal.events;

import org.jasig.portal.portlet.rendering.worker.IPortletExecutionWorker;

/**
 * Used to signal that a portlet execution has hung
 * 
 * @author Eric Dalquist
 */
public class PortletHungEvent extends PortalEvent {
    private static final long serialVersionUID = 1L;
    
    private transient final IPortletExecutionWorker<?> worker;
    
    private final String fname;

    @SuppressWarnings("unused")
    private PortletHungEvent() {
        this.worker = null;
        this.fname = null;
    }

    PortletHungEvent(PortalEventBuilder eventBuilder, IPortletExecutionWorker<?> worker) {
        super(eventBuilder);
        
        this.worker = worker;
        this.fname = worker.getPortletFname();
    }
    
    /**
     * @return The portlet worker that is hung. Not available during event aggregation
     */
    public IPortletExecutionWorker<?> getWorker() {
        return worker;
    }

    /**
     * @return The functional name of the hung portlet
     */
    public String getFname() {
        return fname;
    }
    
    @Override
    public String toString() {
        return super.toString() + 
                ", fname=" + this.fname + "]";
    }
}
