package org.jasig.portal.events.tincan.om;

public class LrsActor {
    private final String mbox;
    private final String name;
    private final String objectType = "Agent";

    public LrsActor(String mbox, String name) {
        this.mbox = mbox;
        this.name = name;
    }

    public String getMbox() {
        return mbox;
    }

    public String getName() {
        return name;
    }

    public String getObjectType() {
        return objectType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mbox == null) ? 0 : mbox.hashCode());
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
        LrsActor other = (LrsActor) obj;
        if (mbox == null) {
            if (other.mbox != null)
                return false;
        } else if (!mbox.equals(other.mbox))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LrsActor [mbox=" + mbox + ", name=" + name + ", objectType=" + objectType + "]";
    }
}
