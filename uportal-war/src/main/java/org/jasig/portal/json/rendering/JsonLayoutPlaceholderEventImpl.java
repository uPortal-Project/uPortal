package org.jasig.portal.json.rendering;

import org.jasig.portal.character.stream.events.CharacterEventTypes;

public class JsonLayoutPlaceholderEventImpl implements JsonLayoutPlaceholderEvent {

    @Override
    public CharacterEventTypes getEventType() {
        return CharacterEventTypes.JSON_LAYOUT;
    }

}
