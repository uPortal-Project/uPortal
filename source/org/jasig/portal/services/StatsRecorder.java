/* Copyright 2002, 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.stats.DoNothingStatsRecorder;
import org.jasig.portal.services.stats.IStatsRecorder;
import org.jasig.portal.services.stats.StatsRecorderLayoutEventListener;
import org.jasig.portal.services.stats.StatsRecorderSettings;
import org.jasig.portal.spring.PortalApplicationContextFacade;
import org.springframework.beans.BeansException;

/**
 * 
 * Static cover for the primary instance of IStatsRecorder.
 * 
 * This class makes the primary instance of IStatsRecorder defined as a
 * Spring bean named "statsRecorder" available via static lookup.
 * 
 * Various parts of the portal call
 * the static methods in this service to record events such as 
 * when a user logs in, logs out, and subscribes to a channel.
 * We forward those method calls to the configured instance of IStatsRecorder.
 * 
 * Object instances configured via Spring and therefore ammenable to Dependency
 * Injection can and probably should receive their IStatsRecorded instance via 
 * injection rather than statically accessing this class.
 * 
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$ $Date$
 * 
 * @deprecated IStatsRecorder implementation is replaced with a much more flexible system 
 * based on the Spring ApplicationEventPublisher and Event Listeners. 
 * For more information see:
 * http://www.ja-sig.org/wiki/display/UPC/Proposal+to+Deprecate+IStatsRecorder
 */
public final class StatsRecorder {
  
    /**
     * The name of the Spring-configured IStatsRecorder instance to which we
     * expect to delegate.
     */
    public static final String BACKING_BEAN_NAME = "statsRecorder";
    
    private static final Log log = LogFactory.getLog(StatsRecorder.class);
  
    private static IStatsRecorder STATS_RECORDER;
    
    /*
     * Static block in which we discover our backing IStatsRecorder.
     */
    static {
        
        synchronized (StatsRecorder.class) {
            try {
                // our first preference is to get the stats recorder from the 
                // PortalApplicationContextFacade (which fronts Spring bean configuration)
                STATS_RECORDER = (IStatsRecorder) PortalApplicationContextFacade.getPortalApplicationContext().getBean(BACKING_BEAN_NAME, IStatsRecorder.class);
            
            } catch (BeansException be) {
                // don't let exceptions about misconfiguration of the stats recorder propogate
                // to the uPortal code that called StatsRecorder.  Instead, fall back on 
                // failing to record anything, logging the configuration problem.
                log.error("Unable to retrieve IStatsRecorder instance from Portal Application Context: is there a bean of name [" + BACKING_BEAN_NAME + "] ?", be);
                STATS_RECORDER = new DoNothingStatsRecorder();
            }
        }
        
    }
    
  
    private StatsRecorder() {
        // do nothing
        // we're a static cover, no need to instantiate.
    }
  
  /**
   * Creates an instance of a 
   * <code>StatsRecorderLayoutEventListener</code>.
   * 
   * There is currently no difference between calling this method and using the 
   * StatsRecorderLayoutEventListener constructor directly.
   * 
   * @return a new stats recorder layout event listener instance
   */
  public final static StatsRecorderLayoutEventListener newLayoutEventListener(IPerson person, UserProfile profile) {
    return new StatsRecorderLayoutEventListener(person, profile);
  }  
  
  /**
   * Record the successful login of a user.
   * @param person the person who is logging in
   */
  public static void recordLogin(IPerson person) {
      STATS_RECORDER.recordLogin(person);
  }

  /**
   * Record the logout of a user.
   * @param person the person who is logging out
   */
  public static void recordLogout(IPerson person) {
    STATS_RECORDER.recordLogout(person);
  }
  
  /**
   * Record that a new session is created for a user.
   * @param person the person whose session is being created
   */
  public static void recordSessionCreated(IPerson person) {
      STATS_RECORDER.recordSessionCreated(person);
  }
  
  /**
   * Record that a user's session is destroyed
   * (when the user logs out or his/her session
   * simply times out)
   * @param person the person whose session is ending
   */
  public static void recordSessionDestroyed(IPerson person) {
      STATS_RECORDER.recordSessionDestroyed(person);
  }
  
  /**
   * Record that a new channel is being published
   * @param person the person publishing the channel
   * @param channelDef the channel being published
   */
  public static void recordChannelDefinitionPublished(IPerson person, ChannelDefinition channelDef) {
      STATS_RECORDER.recordChannelDefinitionPublished(person, channelDef);
  } 
  
  /**
   * Record that an existing channel is being modified
   * @param person the person modifying the channel
   * @param channelDef the channel being modified
   */
  public static void recordChannelDefinitionModified(IPerson person, ChannelDefinition channelDef) {
      STATS_RECORDER.recordChannelDefinitionModified(person, channelDef);
  }  
  
  /**
   * Record that a channel is being removed
   * @param person the person removing the channel
   * @param channelDef the channel being modified
   */
  public static void recordChannelDefinitionRemoved(IPerson person, ChannelDefinition channelDef) {
      STATS_RECORDER.recordChannelDefinitionRemoved(person, channelDef);
  }  
  
  /**
   * Record that a channel is being added to a user layout
   * @param person the person adding the channel
   * @param profile the profile of the layout to which the channel is being added
   * @param channelDesc the channel being subscribed to
   */
  public static void recordChannelAddedToLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
      STATS_RECORDER.recordChannelAddedToLayout(person, profile, channelDesc);
  }    
  
  /**
   * Record that a channel is being updated in a user layout
   * @param person the person updating the channel
   * @param profile the profile of the layout in which the channel is being updated
   * @param channelDesc the channel being updated
   */
  public static void recordChannelUpdatedInLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
      STATS_RECORDER.recordChannelUpdatedInLayout(person, profile, channelDesc);
  }  

  /**
   * Record that a channel is being moved in a user layout
   * @param person the person moving the channel
   * @param profile the profile of the layout in which the channel is being moved
   * @param channelDesc the channel being moved
   */
  public static void recordChannelMovedInLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
      STATS_RECORDER.recordChannelMovedInLayout(person, profile, channelDesc);
  }
  
  /**
   * Record that a channel is being removed from a user layout
   * @param person the person removing the channel
   * @param profile the profile of the layout to which the channel is being added
   * @param channelDesc the channel being removed from a user layout
   */
  public static void recordChannelRemovedFromLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
      STATS_RECORDER.recordChannelRemovedFromLayout(person, profile, channelDesc);
  }
  
  /**
   * Record that a folder is being added to a user layout
   * @param person the person adding the folder
   * @param profile the profile of the layout to which the folder is being added
   * @param folderDesc the folder being subscribed to
   */
  public static void recordFolderAddedToLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {
      STATS_RECORDER.recordFolderAddedToLayout(person, profile, folderDesc);
  }    
  
  /**
   * Record that a folder is being updated in a user layout
   * @param person the person updating the folder
   * @param profile the profile of the layout in which the folder is being updated
   * @param folderDesc the folder being updated
   */
  public static void recordFolderUpdatedInLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {
      STATS_RECORDER.recordFolderUpdatedInLayout(person, profile, folderDesc);
  }  

  /**
   * Record that a folder is being moved in a user layout
   * @param person the person moving the folder
   * @param profile the profile of the layout in which the folder is being moved
   * @param folderDesc the folder being moved
   */
  public static void recordFolderMovedInLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {
      STATS_RECORDER.recordFolderMovedInLayout(person, profile, folderDesc);
  }
  
  /**
   * Record that a folder is being removed from a user layout
   * @param person the person removing the folder
   * @param profile the profile of the layout to which the folder is being added
   * @param folderDesc the folder being removed from a user layout
   */
  public static void recordFolderRemovedFromLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {
      STATS_RECORDER.recordFolderRemovedFromLayout(person, profile, folderDesc);
  }  
  
  /**
   * Record that a channel is being instantiated
   * @param person the person for whom the channel is instantiated
   * @param profile the profile of the layout for whom the channel is instantiated
   * @param channelDesc the channel being instantiated
   */
  public static void recordChannelInstantiated(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
      STATS_RECORDER.recordChannelInstantiated(person, profile, channelDesc);
  }  
  
  /**
   * Record that a channel is being rendered
   * @param person the person for whom the channel is rendered
   * @param profile the profile of the layout for whom the channel is rendered
   * @param channelDesc the channel being rendered
   */
  public static void recordChannelRendered(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
      STATS_RECORDER.recordChannelRendered(person, profile, channelDesc);
  }  

  /**
   * Record that a channel is being targeted.  In other words,
   * the user is interacting with the channel via either a 
   * hyperlink or form submission.
   * @param person the person interacting with the channel
   * @param profile the profile of the layout in which the channel resides
   * @param channelDesc the channel being targeted
   */
  public static void recordChannelTargeted(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
      STATS_RECORDER.recordChannelTargeted(person, profile, channelDesc);
  }
  
  
  
  
  /**
   * This method is deprecated.  Stats recorder settings are no longer necessarily
   * global.  This method (continues to) access information about only one 
   * particular way in which StatsRecorder can be configured, that of portal.properties
   * entries specifying booleans about which kinds of statistics should be recorded,
   * fronting the StatsRecorderSettings static singleton.
   * 
   * Instead of using the Static Singleton (anti-)pattern, you can instead wire
   * together and configure your IStatsRecorder as a Spring-managed bean named
   * "statsRecorder" and there apply, in a strongly typed and more flexible way,
   * your desired statistics recording configuration.
   * 
   * Specifically, the ConditionalStatsRecorder wrapper now provides a 
   * JavaBean-properties approach to querying the settings that were previously
   * accessible via this method.
   * 
   * Note that since StatsRecorderSettings is a Static Singleton, this implementation
   * of this method continues to do what the 2.5.0 implementation did.  The change
   * since 2.5.0 is that StatsRecorderSettings is no longer necessarily 
   * controlling of StatsRecorder behavior.
   * 
   * Gets the value of a particular stats recorder from StatsRecorderSettings.
   * Possible settings are available from <code>StatsRecorderSettings</code>.
   * For example: <code>StatsRecorder.get(StatsRecorderSettings.RECORD_LOGIN)</code>
   * @param setting the setting
   * @return the value for the setting
   * @deprecated since uPortal 2.5.1, recorder settings are no longer global
   */
  public static boolean get(int setting) {
      return StatsRecorderSettings.instance().get(setting);
  }  
  
  /**
   * This method is deprecated.  Stats recorder settings are no longer necessarily
   * global.  This method (continues to) provide a very thin layer in front of just
   * one particular way in which StatsRecorder can be configured, that of
   * portal.poperties specifying booleans about which kinds of statistics should be
   * recorded, fronting the StatsRecorderSettings static singleton.
   * 
   * Instead of using the Static Singleton (anti-)patterh, you can instead wire 
   * together and configure your IStatsRecorder as a Spring-managed bean named
   * "statsRecorder" and there apply, in a strongly typed and more flexible way, 
   * your desired statistics recording configuration.
   * 
   * Specifically, the ConditionalStatsRecorder wrapper now provides a 
   * JavaBean-properties approach to configuring the stats recorder even filtering
   * that was previously configurable via this method.
   * 
   * Note that since StatsRecorderSettings is a Static Singleton, this implementation
   * of this method continues to do what the 2.5.0 implementation did.  The change
   * since 2.5.-0 is that StatsRecorderSettings is no longer necessarily controlling
   * of StatsRecorderBehavior.
   * 
   * CALLING THIS METHOD MAY HAVE NO EFFECT ON StatsRecorder BEHAVIOR.
   * This method will only have effect if the IStatsRecorder implementation
   * is actually using StatsRecorderSettings.
   * 
   * Sets the value of a particular stats recorder setting.
   * Possible settings are available from <code>StatsRecorderSettings</code>.
   * For example: <code>StatsRecorder.set(StatsRecorderSettings.RECORD_LOGIN, true)</code>
   * @param setting the setting to change
   * @param newValue the new value for the setting
   * @deprecated since uPortal 2.5.1, recorder settings are no longer necessarily global
   */
  public static void set(int setting, boolean newValue) {
    StatsRecorderSettings.instance().set(setting, newValue);
  }  
  
}
