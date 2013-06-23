package org.jasig.portal.events.tincan.om;

public class LrsStatement {
    private final LrsActor actor;
    private final LrsVerb verb;
    private final LrsObject object;

    public LrsStatement(LrsActor actor, LrsVerb verb, LrsObject object) {
        this.actor = actor;
        this.verb = verb;
        this.object = object;
    }

    public LrsActor getActor() {
        return actor;
    }

    public LrsVerb getVerb() {
        return verb;
    }

    public LrsObject getObject() {
        return object;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((actor == null) ? 0 : actor.hashCode());
        result = prime * result + ((object == null) ? 0 : object.hashCode());
        result = prime * result + ((verb == null) ? 0 : verb.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LrsStatement other = (LrsStatement) obj;
        if (actor == null) {
            if (other.actor != null)
                return false;
        } else if (!actor.equals(other.actor))
            return false;
        if (object == null) {
            if (other.object != null)
                return false;
        } else if (!object.equals(other.object))
            return false;
        if (verb != other.verb)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LrsStatement [actor=" + actor + ", verb=" + verb + ", object="
                + object + "]";
    }
}
