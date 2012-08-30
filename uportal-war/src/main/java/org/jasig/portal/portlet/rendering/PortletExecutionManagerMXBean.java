package org.jasig.portal.portlet.rendering;

import java.util.Map;


public interface PortletExecutionManagerMXBean {

    /**
     * @param maxEventIterations The maximum number of iterations to spend dispatching events for a single event request.
     */
    void setMaxEventIterations(int maxEventIterations);
    
    /**
     * @return The maximum number of iterations to spend dispatching events for a single event request.
     */
    int getMaxEventIterations();

    /**
     * @param ignoreTimeouts If true timeouts will be ignored
     */
    void setIgnoreTimeouts(boolean ignoreTimeouts);
    
    /**
     * @return If true timeouts will be ignored
     */
    boolean isIgnoreTimeouts();

    /**
     * @param extendedTimeoutExecutions Number of executions from sever start to extend timeouts for each portlet
     */
    void setExtendedTimeoutExecutions(int extendedTimeoutExecutions);
    
    /**
     * @return Number of executions from sever start to extend timeouts for each portlet
     */
    int getExtendedTimeoutExecutions();
    
    /**
     * @param extendedTimeoutMultiplier Multiplier to use for extended timeouts
     */
    void setExtendedTimeoutMultiplier(long extendedTimeoutMultiplier);
    
    /**
     * @return Multiplier to use for extended timeouts
     */
    long getExtendedTimeoutMultiplier();
    
    /**
     * @return Number of times each portlet has been executed
     */
    Map<String, Integer> getPortletExecutionCounts();
    
}