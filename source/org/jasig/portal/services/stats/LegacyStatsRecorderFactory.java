/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.car.CarResources;
import org.jasig.portal.properties.PropertiesManager;


/**
 * Emulates the uPortal 2.5.0 and earlier implementation of StatsRecorder 
 * discovery and configuration by means of portal.properties-declared 
 * IStatsRecorderFactory.
 * 
 * WARNING: Do NOT declare this factory as the stats recorder factory in
 * portal.properties.  This is the implementation of getting the IStatsRecorder
 * produced by the factory declared in portal.properties.  If you declare this factory
 * there, then you would be instructing this factory to instantiate itself.  This 
 * implementation will detect the case where the property defines this factory
 * and avoid infinite recursion by falling back on the DoNothingStatsRecorder.
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5.1
 */
public final class LegacyStatsRecorderFactory 
    implements IStatsRecorderFactory {
    
    private final Log log = LogFactory.getLog(getClass());
    
    /**
     * The name of the PropertiesManager managed property the value of which will
     * be the name of the class implementing IStatsRecorderFactory that we should
     * instantiate and use to get the desired IStatsRecorder.
     */
    public static final String STATS_RECORDER_FACTORY_CLASS_NAME_PROPERTY = 
        "org.jasig.portal.services.stats.StatsRecorderFactory.implementation";


    public IStatsRecorder getStatsRecorder() {
        
        String statsRecorderFactoryName = null;
        IStatsRecorderFactory statsRecorderFactory = null;
        try {
            // Get a stats recorder from the stats recorder factory. 
            statsRecorderFactoryName = PropertiesManager.getProperty(STATS_RECORDER_FACTORY_CLASS_NAME_PROPERTY);
            
            if (statsRecorderFactoryName.equals(getClass().getName())) {
                // fail using the declared factory and fall back upon the DoNothingStatsRecorder
                // as handled in the catch below.
                throw new IllegalStateException("The portal.properties property " 
                        + STATS_RECORDER_FACTORY_CLASS_NAME_PROPERTY 
                        + " must not declare that the stats recorder factory is " 
                        + getClass().getName() );
            }
            
            statsRecorderFactory = (IStatsRecorderFactory) CarResources.getInstance().getClassLoader().loadClass(statsRecorderFactoryName).newInstance();
        } catch (Exception e) {
            log.error( "Unable to instantiate stats recorder '" + statsRecorderFactoryName  + "'. Continuing with DoNothingStatsRecorder.", e);
            statsRecorderFactory = new DoNothingStatsRecorderFactory();          
        }
        try {
            IStatsRecorder factoryProducedRecorder = statsRecorderFactory.getStatsRecorder();
            
            /* 
             * the legacy behavior was to propogate events in threads for this purpose
             * this is not implemented using ThreadFiringStatsRecorder
             * so, front the IStatsRecorder from the factory with the thread firing
             * wrapper:
             */
        
            // read thread pooling configuration from portal.properties
            String prefix = "org.jasig.portal.services.StatsRecorder.threadPool_";
            int initialThreads = PropertiesManager.getPropertyAsInt(prefix + "initialThreads");
            int maxThreads = PropertiesManager.getPropertyAsInt(prefix + "maxThreads");
            int threadPriority = PropertiesManager.getPropertyAsInt(prefix + "threadPriority");
            
            // instantiate the thread firing wrapper
            ThreadFiringStatsRecorder threadFiringWrapper = 
                new ThreadFiringStatsRecorder(initialThreads, maxThreads, threadPriority);
            
            // chain the wrapper to the underlying IStatsRecorder implementation
            threadFiringWrapper.setTargetStatsRecorder(factoryProducedRecorder);
            
            
            /*
             * The legacy behavior was to only fire those threads if configured, also in
             * portal.properties, to record the particular event.
             * This is now implemented using ConditionalStatsRecorder.
             */
            ConditionalStatsRecorder conditionalWrapper = new ConditionalStatsRecorder();
            
            // the portal.properties configuration is represented by this flags implementation, 
            // which is backed by the static singleton StatsRecorderSettings:
            IStatsRecorderFlags portalPropertiesDefinedFlags = new SettingsBackedStatsRecorderFlagsImpl();
            
            conditionalWrapper.setFlags(portalPropertiesDefinedFlags);
            
            // chain the wrapper to the thread firing instance
            
            conditionalWrapper.setTargetStatsRecorder(threadFiringWrapper);
            
            // we're done wiring up an IStatsRecorder implementing the legacy
            // behavior
            
            return conditionalWrapper;
            
            } catch (Exception e) {
            log.error("Error instantiating StatsRecorder", e);
            
            // fall back on do nothing implementation
            return new DoNothingStatsRecorder();
        }
    }
    
}

