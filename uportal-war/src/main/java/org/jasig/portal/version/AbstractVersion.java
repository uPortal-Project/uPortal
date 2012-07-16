package org.jasig.portal.version;

import java.io.Serializable;

import org.jasig.portal.version.om.Version;

/**
 * Base class for versions that implements a "correct" equals hashCode, equals and toString
 * 
 * @author Eric Dalquist
 */
public abstract class AbstractVersion implements Version, Serializable {
	private int hashCode = 0;

    @Override
    public final int hashCode() {
    	int result = hashCode;
    	if (result == 0) {
	        final int prime = 31;
	        result = 1;
	        result = prime * result + getMajor();
	        result = prime * result + getMinor();
	        result = prime * result + getPatch();
	        hashCode = result;
    	}
    	return result;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || obj.hashCode() != this.hashCode())
            return false;
        if (!(obj instanceof Version))
            return false;
        Version other = (Version) obj;
        if (getMajor() != other.getMajor())
            return false;
        if (getMinor() != other.getMinor())
            return false;
        if (getPatch() != other.getPatch())
            return false;
        return true;
    }
    
    @Override
    public final String toString() {
        return getMajor() + "." + getMinor() + "." + getPatch();
    }
}
