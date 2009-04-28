package org.jasig.portal.portlets;

import java.io.Serializable;

public class BooleanAttribute  implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean value;
    
    public BooleanAttribute() {
    }
    
    public BooleanAttribute(boolean value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public boolean getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(boolean value) {
        this.value = value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}