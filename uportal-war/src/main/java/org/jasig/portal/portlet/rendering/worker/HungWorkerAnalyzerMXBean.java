package org.jasig.portal.portlet.rendering.worker;

import java.util.Map;


/**
 * JMX stats for HungWorkerAnalyzer
 * 
 * @author Eric Dalquist
 */
public interface HungWorkerAnalyzerMXBean {

    /**
     * @return Total number of hung workers
     */
    int getHungPortletCountTotal();
    
    /**
     * @return Hung worker counts by portlet
     */
    Map<String, Integer> getHungPortletCounts();
    
    /**
     * @param percentPermittedErrantByFname The allowed ratio of hung portlets to available workers per fname
     */
    void setPercentPermittedErrantByFname(double percentPermittedErrantByFname);

    /**
     * @return The allowed ratio of hung portlets to available workers per fname 
     */
    double getPercentPermittedErrantByFname();
}