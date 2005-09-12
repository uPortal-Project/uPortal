/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.stats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.UserProfile;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutFolderDescription;
import org.jasig.portal.security.IPerson;

/**
 * IStatsRecorder implementation which delegates to zero to many child IStatsRecorders.
 * This class allows you to use multiple stats recorders.
 * 
 * We invoke each child IStatsRecorder within a try-catch Throwable in order to
 * guarantee that no particular IStatsRecorder's failure will prevent propogation of
 * IStatsRecorder events to other children. We do not propogate these exceptions
 * to our caller in order to prevent failures in statistics recording from propogating
 * into other modules.  (For instance, a failure to record a login shouldn't 
 * prevent a user from being able to log in at all.)
 * 
 * @version $Revision$ $Date$
 * @since uPortal 2.5.1
 */
public final class ListStatsRecorder 
    implements IStatsRecorder {

    private final Log log = LogFactory.getLog(getClass());
    
    /**
     * Recorders to which we broadcast IStatsRecorder method calls, in order.
     */
    private IStatsRecorder[] children = new IStatsRecorder[0];
    
    /**
     * Get the recorders to which we broadcast IStatsRecorder method calls.
     * @return recorders to which we broadcast IStatsRecorder method calls.
     */
    public IStatsRecorder[] getChildren() {
        return this.children;
    }
    
    /**
     * Set the recorders to which we broadcast IStatsRecorder method calls.
     * @param children to which we will broadcast IStatsRecorder method calls.
     */
    public void setChildren(IStatsRecorder[] children) {
        if (children == null) {
            throw new IllegalArgumentException("Cannot set children to null.");
        }
        this.children=children;
    }
    
    public void recordLogin(IPerson person) {
        // defensive local reference so that if something injects a new array of targets
        // while we're iterating, the length of the array we're iterating over cannot change
        IStatsRecorder[] targets = this.children;
        
        for (int i = 0; i < targets.length; i++) {
            IStatsRecorder target = targets[i];
            if (target == null) {
                log.error("Cannot propogate recordLogin() to a null IStatsRecorder (array element " + i + ")");
            } else {
                try {
                    target.recordLogin(person);
                } catch (Throwable t) {
                    // no matter what went wrong, we log it and then try the remaining
                    // IStatsRecorder event handlers.  No recorder's failure should prevent
                    // our trying the other recorders, and no recorder's failure should
                    // be propogated to our caller.
                    log.error("Error propogating recordLogin() to " + target, t);
                }
            }
        }
        
    }

    public void recordLogout(IPerson person) {
        // defensive local reference so that if something injects a new array of targets
        // while we're iterating, the length of the array we're iterating over cannot change
        IStatsRecorder[] targets = this.children;
        
        for (int i = 0; i < targets.length; i++) {
            IStatsRecorder target = targets[i];
            if (target == null) {
                log.error("Cannot propogate recordLogout() to a null IStatsRecorder (array element " + i + ")");
            } else {
                try {
                    target.recordLogout(person);
                } catch (Throwable t) {
                    // no matter what went wrong, we log it and then try the remaining
                    // IStatsRecorder event handlers.  No recorder's failure should prevent
                    // our trying the other recorders, and no recorder's failure should
                    // be propogated to our caller.
                    log.error("Error propogating recordLogout() to " + target, t);
                }
            }
        }
    }

    public void recordSessionCreated(IPerson person) {
        // defensive local reference so that if something injects a new array of targets
        // while we're iterating, the length of the array we're iterating over cannot change
        IStatsRecorder[] targets = this.children;
        
        for (int i = 0; i < targets.length; i++) {
            IStatsRecorder target = targets[i];
            if (target == null) {
                log.error("Cannot propogate recordSessionCreated() to a null IStatsRecorder (array element " + i + ")");
            } else {
                try {
                    target.recordSessionCreated(person);
                } catch (Throwable t) {
                    // no matter what went wrong, we log it and then try the remaining
                    // IStatsRecorder event handlers.  No recorder's failure should prevent
                    // our trying the other recorders, and no recorder's failure should
                    // be propogated to our caller.
                    log.error("Error propogating recordSessionCreated() to " + target, t);
                }
            }
        }
    }

    public void recordSessionDestroyed(IPerson person) {
        // defensive local reference so that if something injects a new array of targets
        // while we're iterating, the length of the array we're iterating over cannot change
        IStatsRecorder[] targets = this.children;
        
        for (int i = 0; i < targets.length; i++) {
            IStatsRecorder target = targets[i];
            if (target == null) {
                log.error("Cannot propogate recordSessionDestroyed() to a null IStatsRecorder (array element " + i + ")");
            } else {
                try {
                    target.recordSessionDestroyed(person);
                } catch (Throwable t) {
                    // no matter what went wrong, we log it and then try the remaining
                    // IStatsRecorder event handlers.  No recorder's failure should prevent
                    // our trying the other recorders, and no recorder's failure should
                    // be propogated to our caller.
                    log.error("Error propogating recordSessionDestroyed() to " + target, t);
                }
            }
        }
    }

    public void recordChannelDefinitionPublished(IPerson person, ChannelDefinition channelDef) {
        // defensive local reference so that if something injects a new array of targets
        // while we're iterating, the length of the array we're iterating over cannot change
        IStatsRecorder[] targets = this.children;
        
        for (int i = 0; i < targets.length; i++) {
            IStatsRecorder target = targets[i];
            if (target == null) {
                log.error("Cannot propogate recordChannelDefinitionPublished() to a null IStatsRecorder (array element " + i + ")");
            } else {
                try {
                    target.recordChannelDefinitionPublished(person, channelDef);
                } catch (Throwable t) {
                    // no matter what went wrong, we log it and then try the remaining
                    // IStatsRecorder event handlers.  No recorder's failure should prevent
                    // our trying the other recorders, and no recorder's failure should
                    // be propogated to our caller.
                    log.error("Error propogating recordChannelDefinitionPublished() to " + target, t);
                }
            }
        }
    }

    public void recordChannelDefinitionModified(IPerson person, ChannelDefinition channelDef) {
        // defensive local reference so that if something injects a new array of targets
        // while we're iterating, the length of the array we're iterating over cannot change
        IStatsRecorder[] targets = this.children;
        
        for (int i = 0; i < targets.length; i++) {
            IStatsRecorder target = targets[i];
            if (target == null) {
                log.error("Cannot propogate recordChannelDefinitionModified() to a null IStatsRecorder (array element " + i + ")");
            } else {
                try {
                    target.recordChannelDefinitionModified(person, channelDef);
                } catch (Throwable t) {
                    // no matter what went wrong, we log it and then try the remaining
                    // IStatsRecorder event handlers.  No recorder's failure should prevent
                    // our trying the other recorders, and no recorder's failure should
                    // be propogated to our caller.
                    log.error("Error propogating recordChannelDefinitionModified() to " + target, t);
                }
            }
        }
    }

    public void recordChannelDefinitionRemoved(IPerson person, ChannelDefinition channelDef) {
        // defensive local reference so that if something injects a new array of targets
        // while we're iterating, the length of the array we're iterating over cannot change
        IStatsRecorder[] targets = this.children;
        
        for (int i = 0; i < targets.length; i++) {
            IStatsRecorder target = targets[i];
            if (target == null) {
                log.error("Cannot propogate recordSessionDestroyed() to a null IStatsRecorder (array element " + i + ")");
            } else {
                try {
                    target.recordChannelDefinitionRemoved(person, channelDef);
                } catch (Throwable t) {
                    // no matter what went wrong, we log it and then try the remaining
                    // IStatsRecorder event handlers.  No recorder's failure should prevent
                    // our trying the other recorders, and no recorder's failure should
                    // be propogated to our caller.
                    log.error("Error propogating recordSessionDestroyed() to " + target, t);
                }
            }
        }
    }

    public void recordChannelAddedToLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
        // defensive local reference so that if something injects a new array of targets
        // while we're iterating, the length of the array we're iterating over cannot change
        IStatsRecorder[] targets = this.children;
        
        for (int i = 0; i < targets.length; i++) {
            IStatsRecorder target = targets[i];
            if (target == null) {
                log.error("Cannot propogate recordChannelAddedToLayout() to a null IStatsRecorder (array element " + i + ")");
            } else {
                try {
                    target.recordChannelAddedToLayout(person, profile, channelDesc);
                } catch (Throwable t) {
                    // no matter what went wrong, we log it and then try the remaining
                    // IStatsRecorder event handlers.  No recorder's failure should prevent
                    // our trying the other recorders, and no recorder's failure should
                    // be propogated to our caller.
                    log.error("Error propogating recordChannelAddedToLayout() to " + target, t);
                }
            }
        }
    }

    public void recordChannelUpdatedInLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
        // defensive local reference so that if something injects a new array of targets
        // while we're iterating, the length of the array we're iterating over cannot change
        IStatsRecorder[] targets = this.children;
        
        for (int i = 0; i < targets.length; i++) {
            IStatsRecorder target = targets[i];
            if (target == null) {
                log.error("Cannot propogate recordChannelUpdatedInLayout() to a null IStatsRecorder (array element " + i + ")");
            } else {
                try {
                    target.recordChannelUpdatedInLayout(person, profile, channelDesc);
                } catch (Throwable t) {
                    // no matter what went wrong, we log it and then try the remaining
                    // IStatsRecorder event handlers.  No recorder's failure should prevent
                    // our trying the other recorders, and no recorder's failure should
                    // be propogated to our caller.
                    log.error("Error propogating recordChannelUpdatedInLayout() to " + target, t);
                }
            }
        }
    }

    public void recordChannelMovedInLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
        // defensive local reference so that if something injects a new array of targets
        // while we're iterating, the length of the array we're iterating over cannot change
        IStatsRecorder[] targets = this.children;
        
        for (int i = 0; i < targets.length; i++) {
            IStatsRecorder target = targets[i];
            if (target == null) {
                log.error("Cannot propogate recordChannelMovedInLayout() to a null IStatsRecorder (array element " + i + ")");
            } else {
                try {
                    target.recordChannelMovedInLayout(person, profile, channelDesc);
                } catch (Throwable t) {
                    // no matter what went wrong, we log it and then try the remaining
                    // IStatsRecorder event handlers.  No recorder's failure should prevent
                    // our trying the other recorders, and no recorder's failure should
                    // be propogated to our caller.
                    log.error("Error propogating recordChannelMovedInLayout() to " + target, t);
                }
            }
        }
    }

    public void recordChannelRemovedFromLayout(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
        // defensive local reference so that if something injects a new array of targets
        // while we're iterating, the length of the array we're iterating over cannot change
        IStatsRecorder[] targets = this.children;
        
        for (int i = 0; i < targets.length; i++) {
            IStatsRecorder target = targets[i];
            if (target == null) {
                log.error("Cannot propogate recordChannelRemovedFromLayout() to a null IStatsRecorder (array element " + i + ")");
            } else {
                try {
                    target.recordChannelRemovedFromLayout(person, profile, channelDesc);
                } catch (Throwable t) {
                    // no matter what went wrong, we log it and then try the remaining
                    // IStatsRecorder event handlers.  No recorder's failure should prevent
                    // our trying the other recorders, and no recorder's failure should
                    // be propogated to our caller.
                    log.error("Error propogating recordCHannelRemovedFromLayout() to " + target, t);
                }
            }
        }
    }

    public void recordFolderAddedToLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {
        // defensive local reference so that if something injects a new array of targets
        // while we're iterating, the length of the array we're iterating over cannot change
        IStatsRecorder[] targets = this.children;
        
        for (int i = 0; i < targets.length; i++) {
            IStatsRecorder target = targets[i];
            if (target == null) {
                log.error("Cannot propogate recordFolderAddedToLayout() to a null IStatsRecorder (array element " + i + ")");
            } else {
                try {
                    target.recordFolderAddedToLayout(person, profile, folderDesc);
                } catch (Throwable t) {
                    // no matter what went wrong, we log it and then try the remaining
                    // IStatsRecorder event handlers.  No recorder's failure should prevent
                    // our trying the other recorders, and no recorder's failure should
                    // be propogated to our caller.
                    log.error("Error propogating recordFolderAddedToLayout() to " + target, t);
                }
            }
        }
    }

    public void recordFolderUpdatedInLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {
        // defensive local reference so that if something injects a new array of targets
        // while we're iterating, the length of the array we're iterating over cannot change
        IStatsRecorder[] targets = this.children;
        
        for (int i = 0; i < targets.length; i++) {
            IStatsRecorder target = targets[i];
            if (target == null) {
                log.error("Cannot propogate recordFolderUpdatedInLayout() to a null IStatsRecorder (array element " + i + ")");
            } else {
                try {
                    target.recordFolderUpdatedInLayout(person, profile, folderDesc);
                } catch (Throwable t) {
                    // no matter what went wrong, we log it and then try the remaining
                    // IStatsRecorder event handlers.  No recorder's failure should prevent
                    // our trying the other recorders, and no recorder's failure should
                    // be propogated to our caller.
                    log.error("Error propogating recordFolderUpdatedInLayout() to " + target, t);
                }
            }
        }
    }

    public void recordFolderMovedInLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {
        // defensive local reference so that if something injects a new array of targets
        // while we're iterating, the length of the array we're iterating over cannot change
        IStatsRecorder[] targets = this.children;
        
        for (int i = 0; i < targets.length; i++) {
            IStatsRecorder target = targets[i];
            if (target == null) {
                log.error("Cannot propogate recordFolderMovedInLayout() to a null IStatsRecorder (array element " + i + ")");
            } else {
                try {
                    target.recordFolderMovedInLayout(person, profile, folderDesc);
                } catch (Throwable t) {
                    // no matter what went wrong, we log it and then try the remaining
                    // IStatsRecorder event handlers.  No recorder's failure should prevent
                    // our trying the other recorders, and no recorder's failure should
                    // be propogated to our caller.
                    log.error("Error propogating recordFolderMovedInLayout() to " + target, t);
                }
            }
        }
    }

    public void recordFolderRemovedFromLayout(IPerson person, UserProfile profile, IUserLayoutFolderDescription folderDesc) {
        // defensive local reference so that if something injects a new array of targets
        // while we're iterating, the length of the array we're iterating over cannot change
        IStatsRecorder[] targets = this.children;
        
        for (int i = 0; i < targets.length; i++) {
            IStatsRecorder target = targets[i];
            if (target == null) {
                log.error("Cannot propogate recordFolderRemovedFromLayout() to a null IStatsRecorder (array element " + i + ")");
            } else {
                try {
                    target.recordFolderRemovedFromLayout(person, profile, folderDesc);
                } catch (Throwable t) {
                    // no matter what went wrong, we log it and then try the remaining
                    // IStatsRecorder event handlers.  No recorder's failure should prevent
                    // our trying the other recorders, and no recorder's failure should
                    // be propogated to our caller.
                    log.error("Error propogating recordFolderRemovedFromLayout() to " + target, t);
                }
            }
        }
    }

    public void recordChannelInstantiated(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
        // defensive local reference so that if something injects a new array of targets
        // while we're iterating, the length of the array we're iterating over cannot change
        IStatsRecorder[] targets = this.children;
        
        for (int i = 0; i < targets.length; i++) {
            IStatsRecorder target = targets[i];
            if (target == null) {
                log.error("Cannot propogate recordChannelInstantiated() to a null IStatsRecorder (array element " + i + ")");
            } else {
                try {
                    target.recordChannelInstantiated(person, profile, channelDesc);
                } catch (Throwable t) {
                    // no matter what went wrong, we log it and then try the remaining
                    // IStatsRecorder event handlers.  No recorder's failure should prevent
                    // our trying the other recorders, and no recorder's failure should
                    // be propogated to our caller.
                    log.error("Error propogating recordChannelInstantiated() to " + target, t);
                }
            }
        }
    }

    public void recordChannelRendered(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
        // defensive local reference so that if something injects a new array of targets
        // while we're iterating, the length of the array we're iterating over cannot change
        IStatsRecorder[] targets = this.children;
        
        for (int i = 0; i < targets.length; i++) {
            IStatsRecorder target = targets[i];
            if (target == null) {
                log.error("Cannot propogate recordChannelRendered() to a null IStatsRecorder (array element " + i + ")");
            } else {
                try {
                    target.recordChannelRendered(person, profile, channelDesc);
                } catch (Throwable t) {
                    // no matter what went wrong, we log it and then try the remaining
                    // IStatsRecorder event handlers.  No recorder's failure should prevent
                    // our trying the other recorders, and no recorder's failure should
                    // be propogated to our caller.
                    log.error("Error propogating recordChannelRendered() to " + target, t);
                }
            }
        }
    }

    public void recordChannelTargeted(IPerson person, UserProfile profile, IUserLayoutChannelDescription channelDesc) {
        // defensive local reference so that if something injects a new array of targets
        // while we're iterating, the length of the array we're iterating over cannot change
        IStatsRecorder[] targets = this.children;
        
        for (int i = 0; i < targets.length; i++) {
            IStatsRecorder target = targets[i];
            if (target == null) {
                log.error("Cannot propogate recordChannelTargeted() to a null IStatsRecorder (array element " + i + ")");
            } else {
                try {
                    target.recordChannelTargeted(person, profile, channelDesc);
                } catch (Throwable t) {
                    // no matter what went wrong, we log it and then try the remaining
                    // IStatsRecorder event handlers.  No recorder's failure should prevent
                    // our trying the other recorders, and no recorder's failure should
                    // be propogated to our caller.
                    log.error("Error propogating recordChannelTargeted() to " + target, t);
                }
            }
        }
    }

}

