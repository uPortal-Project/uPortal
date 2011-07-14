package org.jasig.portal.character.stream;

import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;

import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.character.stream.events.PortletLinkPlaceholderEventImpl;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public class PortletLinkPlaceholderEventSource extends PortletPlaceholderEventSource {
    
    @Override
    protected List<CharacterEvent> getCharacterEvents(IPortletWindowId portletWindowId, MatchResult matchResult) {
        String defaultPortletUrl = matchResult.group(2);
        return Arrays.asList((CharacterEvent) new PortletLinkPlaceholderEventImpl(portletWindowId, defaultPortletUrl));
    }

}
