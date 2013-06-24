package org.jasig.portal.events.tincan;

import org.jasig.portal.events.tincan.om.LrsStatement;

/**
 * API to which {@link LrsStatement}s are sent to to be sent on to the destination
 * system.
 * 
 * @author Eric Dalquist
 */
public interface TinCanEventSender {
    void sendEvent(LrsStatement statement);
}
